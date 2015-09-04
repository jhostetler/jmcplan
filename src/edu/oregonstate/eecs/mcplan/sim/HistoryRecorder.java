/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * Records interaction history during an Episode.
 */
public class HistoryRecorder<S, R extends Representer<S, ? extends Representation<S>>, A extends VirtualConstructor<A>>
	implements EpisodeListener<S, A>
{
	public ArrayList<Representation<S>> states = new ArrayList<Representation<S>>();
	public ArrayList<JointAction<A>> actions = new ArrayList<JointAction<A>>();
	public ArrayList<double[]> rewards = new ArrayList<double[]>();
	
	public final R repr;

	/**
	 * @param repr If null, no states will be recorded.
	 */
	public HistoryRecorder( final R repr )
	{
		this.repr = repr;
	}
	
	@Override
	public <P extends Policy<S, JointAction<A>>> void startState( final S s,
			final double[] r, final P pi )
	{
		if( repr != null ) {
			states.add( repr.encode( s ) );
		}
		rewards.add( r );
	}

	@Override
	public void preGetAction()
	{ }

	@Override
	public void postGetAction( final JointAction<A> a )
	{
		actions.add( a.create() );
	}

	@Override
	public void onActionsTaken( final S sprime, final double[] r )
	{
		if( repr != null ) {
			states.add( repr.encode( sprime ) );
		}
		rewards.add( r );
	}

	@Override
	public void endState( final S s )
	{ }
}
