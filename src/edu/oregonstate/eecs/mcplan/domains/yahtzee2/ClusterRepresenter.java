/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

import org.apache.commons.math3.linear.ArrayRealVector;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.abstraction.ClusterAbstraction;
import edu.oregonstate.eecs.mcplan.ml.VoronoiClassifier;

/**
 * @author jhostetler
 *
 */
public class ClusterRepresenter<S extends State> implements Representer<S, ClusterAbstraction<S>>
{
	private final VoronoiClassifier classifier_;
	private final Representer<S, ? extends FactoredRepresentation<S>> repr_;
	private final int neg_idx_ = -1;
	
	public ClusterRepresenter( final VoronoiClassifier classifier,
							   final Representer<S, ? extends FactoredRepresentation<S>> repr )
	{
		classifier_ = classifier;
		repr_ = repr;
	}
	
	@Override
	public ClusterRepresenter<S> create()
	{
		return new ClusterRepresenter<S>( classifier_, repr_.create() );
	}

	@Override
	public ClusterAbstraction<S> encode( final S s )
	{
		// FIXME: This is a horrible hack! See comments in UctSearch.visit()
		if( s.isTerminal() ) {
			return new ClusterAbstraction<S>( neg_idx_ );
		}
		final int label = classifier_.classify( new ArrayRealVector( repr_.encode( s ).phi() ) );
		return new ClusterAbstraction<S>( label );
	}

}
