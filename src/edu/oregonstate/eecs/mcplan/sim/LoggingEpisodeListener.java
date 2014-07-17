/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class LoggingEpisodeListener<S, A extends VirtualConstructor<A>> implements EpisodeListener<S, A>
{

	@Override
	public <P extends Policy<S, JointAction<A>>> void startState( final S s,
			final double[] r, final P pi )
	{
		System.out.println( "startState( " + s + ", " + Arrays.toString( r ) + ", " + pi + " )" );
	}

	@Override
	public void preGetAction()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postGetAction( final JointAction<A> a )
	{
		System.out.println( "postGetAction( " + a + " )" );
	}

	@Override
	public void onActionsTaken( final S sprime, final double[] r )
	{
		System.out.println( "onActionsTaken( " + sprime + ", " + Arrays.toString( r ) + " )" );
	}

	@Override
	public void endState( final S s )
	{
		System.out.println( "endState( " + s + " )" );
	}
}
