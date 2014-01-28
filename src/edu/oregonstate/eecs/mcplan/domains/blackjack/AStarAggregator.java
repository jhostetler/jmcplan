/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import edu.oregonstate.eecs.mcplan.Representer;

/**
 * @author jhostetler
 *
 */
public class AStarAggregator implements Representer<BlackjackState, AStarAbstraction>
{
	// Optimal strategy computed with value iteration
	private final String[][] hard_actions_ =
		{	{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "S", "S", "S", "H", "H", "H", "H", "H"},
			{"S", "S", "S", "S", "S", "H", "H", "H", "H", "H"},
			{"S", "S", "S", "S", "S", "H", "H", "H", "H", "H"},
			{"S", "S", "S", "S", "S", "H", "H", "H", "H", "H"},
			{"S", "S", "S", "S", "S", "H", "H", "H", "H", "H"},
			{"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},
			{"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},
			{"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},
			{"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},
			{"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"}	};
	private final String[][] soft_actions_ =
		{	{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
			{"S", "S", "S", "S", "S", "S", "S", "H", "H", "H"},
			{"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},
			{"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},
			{"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"}	};
	
//	public AStarAggregator( final String[][] hard_actions, final String[][] soft_actions )
//	{
//		hard_actions_ = hard_actions;
//		soft_actions_ = soft_actions;
//	}

	@Override
	public Representer<BlackjackState, AStarAbstraction> create()
	{
		return new AStarAggregator();
	}

	@Override
	public AStarAbstraction encode( final BlackjackState s )
	{
		final int[] pv = Blackjack.handValue( s.hand( 0 ) );
		final int dv = s.dealerUpcard().BlackjackValue();
//		System.out.println( s.hand( 0 ).toString() );
//		System.out.println( "pv: " + pv[0] + ", dv: " + dv );
		final String as;
		if( !s.passed( 0 ) ) {
			if( pv[1] == 0 ) {
				as = hard_actions_[pv[0] - 4][dv - 2];
			}
			else { // pv[1] > 0
				as = soft_actions_[pv[0] - 12][dv - 2];
			}
		}
		else {
			as = "T";
		}
		
		if( "S".equals( as ) ) {
			return new AStarAbstraction( new PassAction( 0 ), dv, pv[0], s.token() );
		}
		else if( "H".equals( as ) ) {
			return new AStarAbstraction( new HitAction( 0 ), dv, pv[0], s.token() );
		}
		else {
			return new AStarAbstraction( null, dv, pv[0], s.token() );
		}
	}
}
