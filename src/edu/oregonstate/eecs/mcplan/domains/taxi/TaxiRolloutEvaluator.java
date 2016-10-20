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
package edu.oregonstate.eecs.mcplan.domains.taxi;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.RandomPolicy;
import edu.oregonstate.eecs.mcplan.SingleAgentJointActionGenerator;
import edu.oregonstate.eecs.mcplan.search.EvaluationFunction;
import edu.oregonstate.eecs.mcplan.search.RolloutEvaluator;
import edu.oregonstate.eecs.mcplan.sim.Simulator;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class TaxiRolloutEvaluator implements EvaluationFunction<TaxiState, TaxiAction>
{
	private final int rollout_width;
	private final int rollout_depth;
	private final ActionGenerator<TaxiState, TaxiAction> actions;
	private final RandomGenerator rng;
	private final double discount;
	
	private final EvaluationFunction<TaxiState, TaxiAction> eval;
	
	public TaxiRolloutEvaluator( final int width, final int depth,
								 final ActionGenerator<TaxiState, TaxiAction> actions,
								 final RandomGenerator rng, final double discount )
	{
		rollout_width = width;
		rollout_depth = depth;
		this.actions = actions;
		this.rng = rng;
		this.discount = discount;
		
		final Policy<TaxiState, JointAction<TaxiAction>> rollout_policy
			= new RandomPolicy<TaxiState, JointAction<TaxiAction>>(
				rng, SingleAgentJointActionGenerator.create( actions.create() ) );
		final EvaluationFunction<TaxiState, TaxiAction> heuristic = new EvaluationFunction<TaxiState, TaxiAction>() {
			@Override
			public double[] evaluate( final Simulator<TaxiState, TaxiAction> sim )
			{
				final TaxiState s = sim.state();
				double d = 0.0;
				final int[] p;
				if( s.passenger != TaxiState.IN_TAXI ) {
					p = s.locations.get( s.passenger );
					d += Fn.distance_l1( s.taxi, p );
				}
				else {
					p = s.taxi;
				}
				d += Fn.distance_l1( p, s.locations.get( s.destination ) );
				return new double[] { -d };
			}
		};
		eval = RolloutEvaluator.create( rollout_policy, discount,
										rollout_width, rollout_depth, heuristic );
	}
	
	@Override
	public double[] evaluate( final Simulator<TaxiState, TaxiAction> sim )
	{
		return eval.evaluate( sim );
	}

}
