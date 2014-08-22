/**
 * 
 */
package edu.oregonstate.eecs.mcplan.abstraction;

import java.util.ArrayList;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;

/**
 * @author jhostetler
 *
 */
public class MulticlassRepresenter<S>
	implements Representer<FactoredRepresentation<S>, Representation<S>>
{
	private final Classifier classifier_;
	private final Instances headers_;
	
	public final int Nclasses;
	
	public MulticlassRepresenter( final Classifier classifier, final int Nclasses,
								  final FactoredRepresenter<S, ? extends FactoredRepresentation<S>> base_repr )
	{
		classifier_ = classifier;
		this.Nclasses = Nclasses;
		final ArrayList<Attribute> labeled = new ArrayList<Attribute>( base_repr.attributes() );
		labeled.add( WekaUtil.createNominalAttribute( "__label__", Nclasses ) );
		headers_ = new Instances( "dummy", labeled, 0 );
		headers_.setClassIndex( labeled.size() - 1 );
	}
	
	private MulticlassRepresenter( final MulticlassRepresenter<S> that )
	{
		classifier_ = that.classifier_;
		headers_ = new Instances( that.headers_, 0 );
		this.Nclasses = that.Nclasses;
	}
	
	@Override
	public MulticlassRepresenter<S> create()
	{
		return new MulticlassRepresenter<S>( this );
	}

	@Override
	public Representation<S> encode( final FactoredRepresentation<S> x )
	{
		try {
			final Instance i = WekaUtil.labeledInstanceFromUnlabeledFeatures( headers_, x.phi() );
			headers_.add( i );
			i.setDataset( headers_ );
			final int c = (int) classifier_.classifyInstance( i );
			headers_.remove( 0 );
			return new ClusterAbstraction<S>( c );
		}
		catch( final RuntimeException ex ) {
			throw ex;
		}
		catch( final Exception ex ) {
			throw new RuntimeException( ex );
		}
	}

}
