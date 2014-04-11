/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * Evaluates a joint policy by simulating it repeatedly.
 * 
 * @author jhostetler
 */
public final class RolloutEvaluator<S extends State, A extends VirtualConstructor<A>>
	implements EvaluationFunction<S, A>
{
	public static <S extends State, A extends VirtualConstructor<A>>
	RolloutEvaluator<S, A> create( final Policy<S, JointAction<A>> policy, final double discount,
								   final int width, final int depth )
   {
		return new RolloutEvaluator<S, A>( policy, discount, width, depth );
   }
	
	public final Policy<S, JointAction<A>> policy;
	public final double discount;
	public final int width;
	public final int depth;
	
	public RolloutEvaluator( final Policy<S, JointAction<A>> policy, final double discount,
							 final int width, final int depth )
	{
		this.policy = policy;
		this.discount = discount;
		this.width = width;
		this.depth = depth;
	}
	
	@Override
	public double[] evaluate( final UndoSimulator<S, A> sim )
	{
		final int nagents = sim.nagents();
		final double[] qbar = Fn.repeat( 0.0, nagents );
		for( int w = 0; w < width; ++w ) {
			int count = 0;
			final double[] q = Fn.repeat( 0.0, nagents );
			double running_discount = 1.0;
			while( true ) {
				running_discount *= discount;
				if( sim.isTerminalState() ) {
					final double[] r = sim.reward();
					Fn.scalar_multiply_inplace( r, running_discount );
					Fn.vplus_inplace( q, r );
					break;
				}
				else if( count == depth ) {
					// TODO: We should have a way of giving e.g. an "optimistic"
					// reward (Vmax) here. Like a 'getDefaultReward( double r )' method.
					final double[] r = sim.reward();
					Fn.scalar_multiply_inplace( r, running_discount );
					Fn.vplus_inplace( q, r );
					break;
				}
				else {
					// TODO: Should rollout_policy be a policy over X's? Current
					// approach (Policy<S, A>) is more flexible since the policy
					// can use a different representation internally.
					final double[] r = sim.reward();
					policy.setState( sim.state(), sim.t() );
					final JointAction<A> a = policy.getAction();
					sim.takeAction( a );
					count += 1;
					final S sprime = sim.state();
					policy.actionResult( sprime, r );
					Fn.scalar_multiply_inplace( r, running_discount );
					Fn.vplus_inplace( q, r );
				}
			}
//			System.out.println( "\tterminated at depth " + count );
			for( int i = 0; i < count; ++i ) {
				sim.untakeLastAction();
			}
			Fn.vplus_inplace( qbar, q );
		}
		
		Fn.scalar_multiply_inplace( qbar, 1.0 / width );
		return qbar;
	}
	
}
