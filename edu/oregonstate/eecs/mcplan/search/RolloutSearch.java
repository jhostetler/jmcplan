/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.agents.galcon.ActionGenerator;
import edu.oregonstate.eecs.mcplan.agents.galcon.Policy;
import edu.oregonstate.eecs.mcplan.agents.galcon.UndoSimulator;
import edu.oregonstate.eecs.mcplan.agents.galcon.UndoableAction;
import edu.oregonstate.eecs.mcplan.experiments.MeanVarianceAccumulator;

/**
 * @author jhostetler
 *
 */
public class RolloutSearch<S, A extends UndoableAction<S, A>> implements GameTreeSearch<S, A>
{
	private final UndoSimulator<S, A> sim_;
	private final ActionGenerator<S, A> action_gen_;
	private final double c_;
	private final long max_time_;
	private final Policy<S, A> rollout_policy_;
	private final MctsVisitor<S, A> visitor_;
	
	private PrincipalVariation<S, A> pv_ = null;
	private boolean complete_ = false;
	
	public final MeanVarianceAccumulator[] q_;
	public final int[] na_;
	public int ns_ = 0;
	
	/**
	 * Note: action_gen must produce actions in a consistent order!
	 * @param sim
	 * @param action_gen
	 * @param max_time
	 */
	public RolloutSearch( final UndoSimulator<S, A> sim, final ActionGenerator<S, A> action_gen,
						  final double c, final long max_time, final Policy<S, A> rollout_policy,
						  final MctsVisitor<S, A> visitor )
	{
		sim_ = sim;
		action_gen_ = action_gen;
		c_ = c;
		max_time_ = max_time;
		rollout_policy_ = rollout_policy;
		visitor_ = visitor;
		
		action_gen_.setState( sim_.state(), sim_.depth() );
		q_ = new MeanVarianceAccumulator[action_gen_.size()];
		for( int i = 0; i < q_.length; ++i ) {
			q_[i] = new MeanVarianceAccumulator();
		}
		na_ = new int[q_.length];
	}
	
	private Pair<A, Integer> chooseAction()
	{
		A best_action = null;
		int best_idx = 0;
		double best_score = -Double.MAX_VALUE;
		action_gen_.setState( sim_.state(), sim_.depth() );
		int idx = 0;
		while( action_gen_.hasNext() ) {
			final A a = action_gen_.next();
			if( na_[idx] == 0 ) {
				return Pair.makePair( a, idx );
			}
			final double exploit = q_[idx].mean();
			final double explore = c_ * Math.sqrt( Math.log( ns_ ) / na_[idx] );
			final double score = exploit + explore;
			if( score > best_score ) {
				best_score = score;
				best_action = a;
				best_idx = idx;
			}
			++idx;
		}
		assert( best_action != null );
		return Pair.makePair( best_action, best_idx );
	}
	
	private double rollout()
	{
		int depth = 0;
		while( !visitor_.isTerminal( sim_.state() ) ) {
			rollout_policy_.setState( sim_.state(), sim_.depth() );
			final A a = rollout_policy_.getAction();
			sim_.takeAction( a );
			rollout_policy_.actionResult( a, sim_.state(), sim_.getReward() );
			visitor_.rolloutAction( a, sim_.state() );
			++depth;
		}
		final double value = visitor_.terminal( sim_.state() );
		while( depth-- > 0 ) {
			sim_.untakeLastAction();
		}
		return value;
	}
	
	@Override
	public void run()
	{
		assert( !isComplete() );
		final long tstart = System.currentTimeMillis();
		final long tend = tstart + max_time_;
		long t = tstart;
		while( t >= tstart && t < tend ) {
			visitor_.startEpisode( sim_.state() );
			final Pair<A, Integer> a = chooseAction();
			sim_.takeAction( a.first );
			visitor_.treeAction( a.first, sim_.state() );
			visitor_.treeDepthLimit( sim_.state() );
			final double q = rollout();
			sim_.untakeLastAction();
			q_[a.second].add( q );
			na_[a.second] += 1;
			ns_ += 1;
			t = System.currentTimeMillis();
		}
		
		action_gen_.setState( sim_.state(), sim_.depth() );
		A best_action = null;
		int best_count = 0;
		int idx = 0;
		double score = 0.0;
		while( action_gen_.hasNext() ) {
			final A a = action_gen_.next();
			if( best_action == null || na_[idx] > best_count ) {
				best_action = a;
				best_count = na_[idx];
				score = q_[idx].mean();
			}
			++idx;
		}
		
		assert( best_action != null );
		pv_ = new PrincipalVariation<S, A>( 1 );
		pv_.score = score;
		pv_.actions.set( 0, best_action );
		complete_ = true;
	}

	@Override
	public double score()
	{
		return pv_.score;
	}

	@Override
	public PrincipalVariation<S, A> principalVariation()
	{
		return pv_;
	}

	@Override
	public boolean isComplete()
	{
		return complete_;
	}
}
