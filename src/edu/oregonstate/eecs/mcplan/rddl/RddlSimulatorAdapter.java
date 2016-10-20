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
package edu.oregonstate.eecs.mcplan.rddl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import rddl.EvalException;
import rddl.RDDL.LCONST;
import rddl.RDDL.LVAR;
import rddl.RDDL.PVAR_INST_DEF;
import rddl.State;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.sim.ResetSimulator;

/**
 * @author jhostetler
 *
 */
public class RddlSimulatorAdapter implements ResetSimulator<RDDLState, RDDLAction>
{
	private final Random rand;
	private final RddlSpec spec;
	private final RDDLState s0;
	private RDDLState state = null;
	
	private final long t = 0L;
	
	// Keep track of reward
	private double accum_reward = 0.0d;
	private double cur_discount = 1.0d;
	private final ArrayList<Double> rewards;
	
	public RddlSimulatorAdapter( final Random rand, final RddlSpec spec, final RDDLState state )
	{
		this.rand = rand;
		this.spec = spec;
		this.s0 = state;
		this.state = new RDDLState( s0 );
		this.rewards = new ArrayList<Double>(spec.rddl_instance._nHorizon);
	}
	
	@Override
	public RDDLState state()
	{ return state; }
	
	@Override
	public void reset()
	{
		state = new RDDLState( s0 );
	}

	@Override
	public void takeAction( final JointAction<RDDLAction> a )
	{
//		System.out.println( "takeAction( " + a + " )" );
		
		// [jhostetler] This is basically copied from rddl.sim.Simulator
		
		try {
		
			// Display state/observations that the agent sees
//			sim._v.display(state, (int) t);
	
			// Get action from policy
			// (if POMDP and first state, no observations available yet so a null is passed)
			final State state_info = ((state._alObservNames.size() > 0) && t == 0) ? null : state;
			final ArrayList<PVAR_INST_DEF> action_list = a.get( 0 ).pvar_list; //.a(); //p.getActions(state_info);
			
			// Check state-action constraints
			state.checkStateActionConstraints(action_list);
			
			// Compute next state (and all intermediate / observation variables)
			state.computeNextState(action_list, rand);
			
			// Calculate reward / objective and store
			final double reward = ((Number)state._reward.sample(
				new HashMap<LVAR,LCONST>(), state, rand)).doubleValue();
			rewards.add(reward);
			accum_reward += cur_discount * reward;
			cur_discount *= spec.rddl_instance._dDiscount;
			
			// Done with this iteration, advance to next round
			state.advanceNextState(false /* do not clear observations */);
			
		}
		catch( final EvalException ex ) {
			throw new RuntimeException( ex );
		}
	}

	@Override
	public long depth()
	{ return t; }

	@Override
	public long t()
	{ return t; }

	@Override
	public int nagents()
	{ return 1; }

	@Override
	public int[] turn()
	{ return new int[] { 0 }; }

	@Override
	public double[] reward()
	{
		if( rewards.isEmpty() ) {
			return new double[] { 0 };
		}
		else {
			return new double[] { rewards.get( rewards.size() - 1 ) };
		}
	}

	@Override
	public boolean isTerminalState()
	{
		return state.isTerminal();
	}

	@Override
	public long horizon()
	{
		return spec.rddl_instance._nHorizon - t;
	}

	@Override
	public String detailString()
	{
		return "RddlSimulatorAdapter";
	}
}
