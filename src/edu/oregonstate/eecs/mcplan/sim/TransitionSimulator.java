/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public abstract class TransitionSimulator<S extends State, A> extends TrajectorySimulator<S, A>
{
	public abstract StateNode<S, A> initialState( final RandomGenerator rng, final S s );
	
	public abstract ActionNode<S, A> sampleTransition( final RandomGenerator rng, final S s, final A a );
	
	@Override
	public final void sampleTrajectory( final RandomGenerator rng, final S s,
										final Policy<S, A> pi, final int depth_limit )
	{
		final StateNode<S, A> sn0 = initialState( rng, s );
		StateNode<S, A> sn = sn0;
		int t = 0;
		pi.reset();
		while( !sn.s.isTerminal() ) {
			pi.setState( sn.s, t );
			final A a = pi.getAction();
			final ActionNode<S, A> tr = sampleTransition( rng, sn.s, a );
			sn = Fn.head( tr.successors() );
			
			t += 1;
			if( t == depth_limit ) {
				break;
			}
		}
	}
}
