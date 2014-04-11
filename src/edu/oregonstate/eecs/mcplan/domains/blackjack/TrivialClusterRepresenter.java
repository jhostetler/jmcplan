/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import edu.oregonstate.eecs.mcplan.IndexedStateSpace;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.abstraction.ClusterAbstraction;

/**
 * @author jhostetler
 *
 */
public class TrivialClusterRepresenter implements Representer<BlackjackState, ClusterAbstraction<BlackjackState>>
{
	private final BlackjackParameters params_;
	private final IndexedStateSpace<BlackjackMdpState> ss_;
	
	public TrivialClusterRepresenter( final BlackjackParameters params, final IndexedStateSpace<BlackjackMdpState> ss )
	{
		params_ = params;
		ss_ = ss;
	}
	
	@Override
	public Representer<BlackjackState, ClusterAbstraction<BlackjackState>> create()
	{
		return new TrivialClusterRepresenter( params_, ss_ );
	}

	@Override
	public ClusterAbstraction<BlackjackState> encode( final BlackjackState s )
	{
		final int[] dv = params_.handValue( s.dealerHand() );
		final int[] pv = params_.handValue( s.hand( 0 ) );
		final BlackjackMdpState bj = new BlackjackMdpState( dv[0], dv[1],
															pv[0], pv[1], s.passed( 0 ) );
		return new ClusterAbstraction<BlackjackState>( ss_.id( bj ) );
	}

}
