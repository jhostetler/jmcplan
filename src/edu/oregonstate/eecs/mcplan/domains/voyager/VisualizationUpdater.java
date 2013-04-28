/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveListener;

/**
 * @author jhostetler
 *
 */
public class VisualizationUpdater implements
		SimultaneousMoveListener<VoyagerState, VoyagerEvent>
{

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveListener#startState(java.lang.Object, java.util.ArrayList)
	 */
	@Override
	public <P extends Policy<VoyagerState, VoyagerEvent>> void startState(
			final VoyagerState s, final ArrayList<P> policies )
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveListener#preGetAction(int)
	 */
	@Override
	public void preGetAction( final int player )
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveListener#postGetAction(int, java.lang.Object)
	 */
	@Override
	public void postGetAction( final int player, final VoyagerEvent action )
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveListener#onActionsTaken(java.lang.Object)
	 */
	@Override
	public void onActionsTaken( final VoyagerState sprime )
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveListener#endState(java.lang.Object)
	 */
	@Override
	public void endState( final VoyagerState s )
	{
		// TODO Auto-generated method stub

	}

}
