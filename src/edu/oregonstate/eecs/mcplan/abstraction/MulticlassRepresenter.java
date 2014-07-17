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
import edu.oregonstate.eecs.mcplan.Representer;

/**
 * @author jhostetler
 *
 */
public class MulticlassRepresenter<S, X extends FactoredRepresentation<S>>
	implements Representer<S, ClusterAbstraction<S>>
{
	private final Classifier classifier_;
	private final Instances headers_;
	private final FactoredRepresenter<S, X> base_repr_;
	
	public final int Nclasses;
	
	public MulticlassRepresenter( final Classifier classifier, final int Nclasses,
								  final FactoredRepresenter<S, X> base_repr )
	{
		classifier_ = classifier;
		base_repr_ = base_repr;
		this.Nclasses = Nclasses;
		final ArrayList<Attribute> labeled = new ArrayList<Attribute>( base_repr_.attributes() );
		labeled.add( WekaUtil.createNominalAttribute( "__label__", Nclasses ) );
		headers_ = new Instances( "dummy", labeled, 0 );
	}
	
	private MulticlassRepresenter( final MulticlassRepresenter<S, X> that )
	{
		this( that.classifier_, that.Nclasses, that.base_repr_.create() );
	}
	
	@Override
	public Representer<S, ClusterAbstraction<S>> create()
	{
		return new MulticlassRepresenter<S, X>( this );
	}

	@Override
	public ClusterAbstraction<S> encode( final S s )
	{
		try {
			final X x = base_repr_.encode( s );
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
