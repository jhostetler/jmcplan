/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import org.apache.commons.math3.linear.ArrayRealVector;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.abstraction.ClusterAbstraction;
import edu.oregonstate.eecs.mcplan.ml.VoronoiClassifier;

/**
 * @author jhostetler
 *
 */
public class ClusterRepresenter implements Representer<BlackjackState, ClusterAbstraction<BlackjackState>>
{
	private final VoronoiClassifier classifier_;
	private final Representer<BlackjackState, ? extends FactoredRepresentation<BlackjackState>> repr_;
	private int neg_idx_ = -1;
	
	public ClusterRepresenter( final VoronoiClassifier classifier,
							   final Representer<BlackjackState, ? extends FactoredRepresentation<BlackjackState>> repr )
	{
		classifier_ = classifier;
		repr_ = repr;
	}
	
	@Override
	public ClusterRepresenter create()
	{
		return new ClusterRepresenter( classifier_, repr_.create() );
	}

	@Override
	public ClusterAbstraction<BlackjackState> encode( final BlackjackState s )
	{
		// FIXME: This is a horrible hack! See comments in UctSearch.visit()
		if( s.passed( 0 ) ) {
			return new ClusterAbstraction<BlackjackState>( neg_idx_-- );
		}
		final int label = classifier_.classify( new ArrayRealVector( repr_.encode( s ).phi() ) );
		return new ClusterAbstraction<BlackjackState>( label );
	}

}
