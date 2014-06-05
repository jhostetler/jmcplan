/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public abstract class StateNode<S, A extends VirtualConstructor<A>>
	extends GameTreeNode<S, A>
{
	public final S token;
	public final int nagents;
	public final int[] turn;
	
	protected final double[] vhat_;
	
	public StateNode( final S token, final int nagents, final int[] turn )
	{
		//assert( nagents == 2 ); // TODO:
		this.token = token;
		this.nagents = nagents;
		this.turn = turn;
		vhat_ = new double[nagents];
	}
	
	public abstract int n();
	
//	public abstract double[] v();
	
	public abstract double[] r();

	public abstract double r( final int i );
	
	public abstract double[] rvar();
	
	public abstract double rvar( final int i );
	
	@Override
	public abstract Generator<? extends ActionNode<S, A>> successors();
	
	public abstract ActionNode<S, A> getActionNode( final JointAction<A> a );
	
//	public ActionNode<S, A> bestAction( final RandomGenerator rng )
//	{
//		// Find all actions with maximal Q-value, and choose one at
//		// random. Randomness is necessary because in the late game, if
//		// the rollout policy always wins, all actions look the same, but
//		// the UCT policy might always pick one that never actually wins
//		// the game due to fixed action generator ordering.
//		double max_q = -Double.MAX_VALUE;
//		final ArrayList<ActionNode<S, A>> action_pool = new ArrayList<ActionNode<S, A>>();
//		for( final ActionNode<S, A> an : Fn.in( successors() ) ) {
////			log.info( "Action {}: n = {}, q = {}, 95% = {}",
////					  e.getKey(), e.getValue().n(), e.getValue().q(), 2 * Math.sqrt( e.getValue().qvar() ) );
//			final double q = an.q( turn );
//			if( q > max_q ) {
//				max_q = q;
//				action_pool.clear();
//			}
//			if( q == max_q ) {
//				action_pool.add( an );
//			}
//		}
////		log.info( "Action pool: {}", action_pool );
//		final int random_action = rng.nextInt( action_pool.size() );
//		System.out.println( "--> Selected " + action_pool.get( random_action ) );
//		return action_pool.get( random_action );
//	}

	@Override
	public void accept( final GameTreeVisitor<S, A> visitor )
	{
		visitor.visit( this );
	}
}