/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.LoggerManager;
import edu.oregonstate.eecs.mcplan.sim.ActionNode;
import edu.oregonstate.eecs.mcplan.sim.StateNode;
import edu.oregonstate.eecs.mcplan.sim.TransitionSimulator;

/**
 * @author jhostetler
 *
 */
public class CosmicTransitionSimulator extends TransitionSimulator<CosmicState, CosmicAction>
{
	private final ch.qos.logback.classic.Logger Log = LoggerManager.getLogger( "log.domain" );
	
	private final CosmicParameters params;
	
	public CosmicTransitionSimulator( final CosmicParameters params )
	{
		this.params = params;
	}
	
	@Override
	public StateNode<CosmicState, CosmicAction> initialState( final RandomGenerator rng, final CosmicState s )
	{
		final StateNode<CosmicState, CosmicAction> sn0 = new StateNode<CosmicState, CosmicAction>( s, reward( s ) );
		fireInitialStateSample( sn0 );
		return sn0;
	}
	
	@Override
	public ActionNode<CosmicState, CosmicAction> sampleTransition(
			final RandomGenerator rng, final CosmicState s, final CosmicAction a )
	{
		final double ar = reward( s, a );
		final CosmicState sprime = params.cosmic.take_action( s, a, params.delta_t );
		final double sr = reward( sprime );
//		System.out.println( "\tr = " + r );
		final ActionNode<CosmicState, CosmicAction> tr = new ActionNode<>( a, ar );
		tr.addSuccessor( new StateNode<CosmicState, CosmicAction>( sprime, sr ) );
		Log.info( "a: {}", a );
		Log.info( "ar: {}", ar );
		Log.info( "sprime: {}", sprime );
		Log.info( "sr: {}", sr );
		
		fireTransitionSample( tr );
		
		return tr;
	}
	
	private double reward( final CosmicState s )
	{
		// Reward for supplying loads
		double r = 0;
		for( final Shunt sh : s.shunts() ) {
			final double cur_p = sh.current_P();
			// If Voltage != 1pu, then supplied power may be larger than sh.P().
			// We take the min here so that the agent is not rewarded for driving
			// the voltage away from 1 in order to supply more power.
			//
			// We used to consider P < 0 an error, but Poland has negative
			// power shunts. We don't want them to count towards rewards.
			r += Math.min( Math.max( cur_p, 0 ), Math.max( sh.P(), 0 ) ); // * sh.value();
		}
		return r;
	}

	private double reward( final CosmicState s, final CosmicAction a )
	{
		// TODO: Action cost
		return 0;
	}

}
