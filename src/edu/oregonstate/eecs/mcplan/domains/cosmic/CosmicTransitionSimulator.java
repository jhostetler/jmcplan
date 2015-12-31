/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.sim.Transition;
import edu.oregonstate.eecs.mcplan.sim.TransitionSimulator;

/**
 * @author jhostetler
 *
 */
public class CosmicTransitionSimulator extends TransitionSimulator<CosmicState, CosmicAction>
{
	private final CosmicParameters params;
	
	public CosmicTransitionSimulator( final CosmicParameters params )
	{
		this.params = params;
	}
	
	@Override
	public Transition<CosmicState, CosmicAction> sampleTransition(
			final RandomGenerator rng, final CosmicState s, final CosmicAction a )
	{
		final CosmicState sprime = params.cosmic.take_action( s, a, params.delta_t );
		final double r = reward( s ) + reward( s, a );
		System.out.println( "\tr = " + r );
		return new Transition<>( s, a, r, sprime );
	}
	
	private double reward( final CosmicState s )
	{
		// Reward for supplying loads
		double r = 0;
		System.out.println( "=== Shunts ===" );
		System.out.println( s.ps.getField( "shunt", 1 ) );
		for( final Shunt sh : s.shunts() ) {
			final double cur_p = sh.current_P();
			System.out.println( "\tShunt " + sh.id() + ":" );
			System.out.println( sh.toString() );
			assert( cur_p >= 0 );
			// If Voltage != 1pu, then supplied power may be larger than sh.P().
			// We take the min here so that the agent is not rewarded for driving
			// the voltage away from 1 in order to supply more power.
			r += Math.min( cur_p, sh.P() ) * sh.value();
		}
		return r;
	}

	private double reward( final CosmicState s, final CosmicAction a )
	{
		// TODO: Action cost
		return 0;
	}

}
