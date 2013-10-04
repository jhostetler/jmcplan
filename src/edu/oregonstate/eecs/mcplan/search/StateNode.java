/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public abstract class StateNode<S, A extends VirtualConstructor<A>>
	extends GameTreeNode<S, A>
{
	private final Map<A, ActionNode<S, A>> a_ = new HashMap<A, ActionNode<S, A>>();
	
	private int n_ = 0;
	public final S token;
	public final int nagents;
	public final int turn;
	
	public StateNode( final S token, final int nagents, final int turn )
	{
		//assert( nagents == 2 ); // TODO:
		this.token = token;
		this.nagents = nagents;
		this.turn = turn;
	}
	
	public int n()
	{ return n_; }
	
	public void visit()
	{ n_ += 1; }
	
	public abstract double[] v();
	
	@Override
	public Generator<ActionNode<S, A>> successors()
	{
		return Generator.fromIterator( a_.values().iterator() );
	}
	
	public ActionNode<S, A> getActionNode( final A a )
	{
		final ActionNode<S, A> an = a_.get( a );
		return an;
	}
	
	public void attachSuccessor( final A a, final ActionNode<S, A> node )
	{
		a_.put( a, node );
	}
	
	public ActionNode<S, A> bestAction( final RandomGenerator rng )
	{
		// Find all actions with maximal Q-value, and choose one at
		// random. Randomness is necessary because in the late game, if
		// the rollout policy always wins, all actions look the same, but
		// the UCT policy might always pick one that never actually wins
		// the game due to fixed action generator ordering.
		double max_q = -Double.MAX_VALUE;
		final ArrayList<ActionNode<S, A>> action_pool = new ArrayList<ActionNode<S, A>>();
		for( final Map.Entry<A, ActionNode<S, A>> e : a_.entrySet() ) {
//			log.info( "Action {}: n = {}, q = {}, 95% = {}",
//					  e.getKey(), e.getValue().n(), e.getValue().q(), 2 * Math.sqrt( e.getValue().qvar() ) );
			final double q = e.getValue().q( turn );
			if( q > max_q ) {
				max_q = q;
				action_pool.clear();
			}
			if( q == max_q ) {
				action_pool.add( e.getValue() );
			}
		}
//		log.info( "Action pool: {}", action_pool );
		final int random_action = rng.nextInt( action_pool.size() );
		System.out.println( "--> Selected " + action_pool.get( random_action ) );
		return action_pool.get( random_action );
	}

	@Override
	public void accept( final GameTreeVisitor<S, A> visitor )
	{
		visitor.visit( this );
	}
}