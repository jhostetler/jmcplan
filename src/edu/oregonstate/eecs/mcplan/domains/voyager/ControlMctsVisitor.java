/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import edu.oregonstate.eecs.mcplan.search.DefaultMctsVisitor;
import edu.oregonstate.eecs.mcplan.util.F;

/**
 * @author jhostetler
 *
 */
public class ControlMctsVisitor extends
		DefaultMctsVisitor<VoyagerState, VoyagerEvent>
{
	/**
	 * 
	 */
	public ControlMctsVisitor()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isTerminal( final VoyagerState s )
	{
		final int[] total_pops = Voyager.playerTotalPops( s );
		return F.any( new F.Pred.EqInt( 0 ), total_pops );
	}
}
