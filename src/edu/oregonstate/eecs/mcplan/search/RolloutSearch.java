/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.util.List;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.Tokenizable;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.CircularListIterator;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * @author jhostetler
 *
 */
public class RolloutSearch<S extends Tokenizable<T>, T, A extends VirtualConstructor<A>> implements GameTreeSearch<S, T, A>
{
	private final UndoSimulator<S, A> sim_;
	private final ActionGenerator<S, ? extends A> action_gen_;
	private final double c_;
	private final long max_time_;
	private final List<Policy<S, A>> rollout_policies_;
	private final MctsNegamaxVisitor<S, A> visitor_;
	
	private PrincipalVariation<T, A> pv_ = null;
	private boolean complete_ = false;
	
	public final MeanVarianceAccumulator[] q_;
	public final int[] na_;
	public int ns_ = 0;
	
	private long tstart_ = 0L;
	private long tend_ = 0L;
	private long t_ = 0L;
	
	/**
	 * Note: action_gen must produce actions in a consistent order!
	 * @param sim
	 * @param action_gen
	 * @param max_time
	 */
	public RolloutSearch( final UndoSimulator<S, A> sim, final ActionGenerator<S, ? extends A> action_gen,
						  final double c, final long max_time, final List<Policy<S, A>> rollout_policies,
						  final MctsNegamaxVisitor<S, A> visitor )
	{
		sim_ = sim;
		action_gen_ = action_gen;
		c_ = c;
		max_time_ = max_time;
		rollout_policies_ = rollout_policies;
		visitor_ = visitor;
		assert( sim.nagents() == rollout_policies_.size() );
		
		action_gen_.setState( sim_.state(), sim_.depth(), sim_.turn() );
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
		action_gen_.setState( sim_.state(), sim_.depth(), sim_.turn() );
		int idx = 0;
		while( action_gen_.hasNext() ) {
			final A a = action_gen_.next();
			if( na_[idx] == 0 ) {
				return Pair.makePair( a, idx );
			}
			final double exploit = q_[idx].mean();
			assert( ns_ > 0 );
			assert( na_[idx] > 0 );
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
		final CircularListIterator<Policy<S, A>> pitr =
			new CircularListIterator<Policy<S, A>>( rollout_policies_, sim_.turn() );
		while( t_ >= tstart_ && t_ < tend_
			   && !sim_.isTerminalState( ) && !visitor_.isTerminal( sim_.state() ) ) {
			final Policy<S, A> pi = pitr.next();
			pi.setState( sim_.state(), sim_.depth() );
			final A a = pi.getAction();
			sim_.takeAction( a );
			pi.actionResult( sim_.state(), sim_.reward() );
			visitor_.defaultAction( a, sim_.state() );
			++depth;
			t_ = System.currentTimeMillis();
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
		tstart_ = System.currentTimeMillis();
		tend_ = tstart_ + max_time_;
		t_ = tstart_;
		while( t_ >= tstart_ && t_ < tend_ ) {
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
			t_ = System.currentTimeMillis();
		}
		
		System.out.println( "[RolloutSearch] done with rollouts" );
		action_gen_.setState( sim_.state(), sim_.depth(), sim_.turn() );
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
		pv_ = new PrincipalVariation<T, A>( 1 );
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
	public PrincipalVariation<T, A> principalVariation()
	{
		return pv_;
	}

	@Override
	public boolean isComplete()
	{
		return complete_;
	}
}
