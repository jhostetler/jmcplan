/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public class GameTreeSimulator<S, F extends Representer<S, F>, A extends VirtualConstructor<A>>
	implements UndoSimulator<Representation<S, F>, A>
{
	private final StateNode<S, F, A> root_;
	private final int nagents_;
	private final RandomGenerator rng_;
	
	private final Deque<StateNode<S, F, A>> history_ = new ArrayDeque<StateNode<S, F, A>>();
	
	private StateNode<S, F, A> s_ = null;
	
	public GameTreeSimulator( final StateNode<S, F, A> root, final int nagents, final RandomGenerator rng )
	{
		root_ = root;
		nagents_ = nagents;
		rng_ = rng;
		s_ = root_;
		history_.push( s_ );
	}
	
	@Override
	public Representation<S, F> state()
	{
		return s_.token;
	}

	@Override
	public void takeAction( final A a )
	{
		final ActionNode<S, F, A> an = s_.getActionNode( a );
		int k = rng_.nextInt( an.n() );
		final Generator<StateNode<S, F, A>> succ = an.successors();
		while( succ.hasNext() ) {
			final StateNode<S, F, A> sprime = succ.next();
			for( int i = 0; i < sprime.n(); ++i ) {
				if( k == 0 ) {
					history_.push( s_ );
					s_ = sprime;
					return;
				}
				k -= 1;
			}
		}
		assert( false );
	}

	@Override
	public void untakeLastAction()
	{
		final StateNode<S, F, A> sprev = history_.pop();
		s_ = sprev;
	}

	@Override
	public long depth()
	{
		return history_.size() - 1;
	}

	@Override
	public long t()
	{
		// FIXME: This assumes that every agent moves at every time step.
		return depth() / getNumAgents();
	}

	@Override
	public int getNumAgents()
	{
		return nagents_;
	}

	@Override
	public int getTurn()
	{
		return (int) depth() % getNumAgents();
	}

	@Override
	public double[] getReward()
	{
		// TODO: How to implement this? Seems to require true joint actions,
		// since component actions have no reward associated.
		return new double[getNumAgents()];
	}

	@Override
	public boolean isTerminalState()
	{
		return !s_.successors().hasNext();
	}

	@Override
	public long horizon()
	{
		// The "horizon" in this simulation is the depth of the tree, which
		// cannot be computed efficiently.
		return Long.MAX_VALUE;
	}

	@Override
	public String detailString()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
