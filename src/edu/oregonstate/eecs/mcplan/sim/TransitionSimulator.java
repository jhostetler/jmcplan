/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import java.util.ArrayList;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.State;

/**
 * @author jhostetler
 *
 */
public abstract class TransitionSimulator<S extends State, A> extends TrajectorySimulator<S, A>
{
	public abstract Transition<S, A> sampleTransition( final RandomGenerator rng, final S s, final A a );
	
	@Override
	public final Trajectory<S, A> sampleTrajectory( final RandomGenerator rng, final S s,
													final Policy<S, A> pi, final int depth_limit )
	{
		final ArrayList<Transition<S, A>> ts = new ArrayList<>();
		S st = s;
		int t = 0;
		while( !st.isTerminal() ) {
			pi.setState( st, t ); // FIXME: This is a temporary hack for the Cosmic demo.
								  // What is the right way to do nonstationary policies?
			final A a = pi.getAction();
			final Transition<S, A> tr = sampleTransition( rng, st, a );
			ts.add( tr );
			st = tr.sprime;
			
			t += 1;
			if( t == depth_limit ) {
				break;
			}
		}
		return new Trajectory<>( s, ts );
	}
}
