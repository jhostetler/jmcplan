/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import edu.oregonstate.eecs.mcplan.search.DefaultMctsVisitor;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class ControlMctsVisitor<A> extends DefaultMctsVisitor<VoyagerState, A>
{
	@Override
	public double[] terminal( final VoyagerState s, final int turn )
	{
		final double[] result = new double[nagents()];
		final Player winner = Voyager.winner( s );
		if( winner != null ) {
			for( int i = 0; i < result.length; ++i ) {
				if( winner.ordinal() == i ) {
					result[i] = 1.0;
				}
				else {
					result[i] = -1.0;
				}
			}
		}
		return result;
	}

	@Override
	public boolean isTerminal( final VoyagerState s, final int turn )
	{
		final int[] total_pops = Voyager.playerTotalPops( s );
		return Fn.any( Fn.Pred.Eq( 0 ), total_pops );
	}
}
