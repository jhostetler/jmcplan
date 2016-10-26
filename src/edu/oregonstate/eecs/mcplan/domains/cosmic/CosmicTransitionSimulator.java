/* LICENSE
Copyright (c) 2013-2016, Jesse Hostetler (jessehostetler@gmail.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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
	
	private final String context;
	private final CosmicParameters params;
	
	public CosmicTransitionSimulator( final String context, final CosmicParameters params )
	{
		this.context = context;
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
		final CosmicState sprime;
		switch( params.getCosmicVersion() ) {
		case take_action:
			sprime = params.cosmic.take_action( s, a, params.delta_t );
			break;
		case take_action_iter:
			sprime = params.cosmic.take_action_iter( s, a, params.delta_t );
			break;
		case take_action2:
			sprime = params.cosmic.take_action2( context, s, a, params.delta_t );
			break;
		default:
			throw new AssertionError();
		}
		
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
