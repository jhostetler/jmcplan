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
				0 /*Player*/, rng.nextInt(),
				SingleAgentJointActionGenerator.create( actions.create() ) );
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
