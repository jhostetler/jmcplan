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
package edu.oregonstate.eecs.mcplan.domains.tamarisk;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * The dynamics implemented here are based on:
 * 
 * Muneepeerakul et al. (2007). A neutral metapopulation model of biodiversity
 * in river networks. Journal of Theoretical Biology.
 * 
 * That paper was the inspiration for the Tamarisk domain of:
 * 
 * http://2013.rl-competition.org/domains/invasive-species
 * 
 * Our implementation follows the paper more closely than the RL competition
 * implementation, specifically with regard to event ordering and use of
 * the Beta distribution to compute the number of colonized cells.
 * 
 * @author jhostetler
 *
 */
public class TamariskSimulator implements UndoSimulator<TamariskState, TamariskAction>
{
	private static class Cell
	{
		public final int reach;
		public final int habitat;
		public final Species species;
		
		public Cell( final int reach, final int habitat, final Species species )
		{
			this.reach = reach;
			this.habitat = habitat;
			this.species = species;
		}
	}
	
	private static class DeathEvent extends TamariskAction
	{
		private ArrayList<Cell> deaths_ = null;
		
		@Override
		public void undoAction( final TamariskState s )
		{
			assert( deaths_ != null );
			for( final Cell c : deaths_ ) {
				s.habitats[c.reach][c.habitat] = c.species;
			}
			deaths_ = null;
		}

		@Override
		public void doAction( final RandomGenerator rng, final TamariskState s )
		{
			assert( deaths_ == null );
			deaths_ = new ArrayList<Cell>();
			for( int i = 0; i < s.params.Nreaches; ++i ) {
				for( int j = 0; j < s.params.Nhabitats; ++j ) {
					final Species species = s.habitats[i][j];
					if( species != Species.None ) {
						final double r = s.rng.nextDouble();
						if( r < s.params.death_rate[species.ordinal()] ) {
							deaths_.add( new Cell( i, j, species ) );
							s.habitats[i][j] = Species.None;
						}
					}
				}
			}
		}

		@Override
		public boolean isDone()
		{ return deaths_ != null; }

		@Override
		public DeathEvent create()
		{ return new DeathEvent(); }

		@Override
		public double cost()
		{ return 0.0; }

		@Override
		public boolean equals( final Object obj )
		{
			return obj != null && obj instanceof DeathEvent;
		}

		@Override
		public int hashCode()
		{ return 3; }

		@Override
		public String toString()
		{ return "DeathEvent"; }
	}
	
	private static class BirthEvent extends TamariskAction
	{
		private final int[][] G_;
		private final double[][] K_;
		
		private ArrayList<Cell> births_ = null;
		
		public BirthEvent( final int[][] G, final double[][] K )
		{
			G_ = G;
			K_ = K;
		}
		
		@Override
		public void undoAction( final TamariskState s )
		{
			assert( births_ != null );
			for( final Cell c : births_ ) {
				assert( s.habitats[c.reach][c.habitat] != Species.None );
				s.habitats[c.reach][c.habitat] = Species.None;
			}
			births_ = null;
		}

		@Override
		public void doAction( final RandomGenerator rng, final TamariskState s )
		{
			assert( births_ == null );
			// Plants propagate:
			// 1. Calculate dispersed + exogenous propagules at each Reach
			final int[][] arrivals = new int[s.params.Nreaches][Species.N];
			for( int dest = 0; dest < s.params.Nreaches; ++dest ) {
				for( int src = 0; src < s.params.Nreaches; ++src ) {
					for( int si = 0; si < Species.N; ++si ) {
						arrivals[dest][si] += (int) Math.floor( K_[src][dest] * G_[src][si] );
					}
				}
			}
			if( s.params.exongenous_arrivals ) {
				for( int i = 0; i < s.params.Nreaches; ++i ) {
					for( int j = 0; j < Species.N; ++j ) {
						final BinomialDistribution binom = new BinomialDistribution(
							s.rng, s.params.reach_arrival_rate[i][j], s.params.reach_arrival_prob[i][j] );
						arrivals[i][j] += binom.sample();
					}
				}
			}
			// Reduce arrivals to account for germination failure
			for( int si = 0; si < Species.N; ++si ) {
				if( s.params.germination_success[si] < 1.0 ) {
					for( int i = 0; i < s.params.Nreaches; ++i ) {
						final BinomialDistribution binom = new BinomialDistribution(
							s.rng, arrivals[i][si], s.params.germination_success[si] );
						arrivals[i][si] = binom.sample();
					}
				}
			}
			// 2. Colonize cells
			births_ = new ArrayList<Cell>();
			for( int i = 0; i < s.params.Nreaches; ++i ) {
				// 2.a. Calculate number of colonized cells in each Reach using Beta approximation
				// See: Appendix A of Muneepeerakul et al.
				final double a = 0.5;
				int c = 0;
				for( int j = 0; j < s.params.Nhabitats; ++j ) {
					if( s.habitats[i][j] == Species.None ) {
						c += 1;
					}
				}
				final int total_arrivals = Fn.sum( arrivals[i] );
				if( c == 0 || total_arrivals == 0 ) {
					continue;
				}
				
				// When total_arrivals == 1, alpha and beta below will be < 0,
				// leading to an invalid Beta distribution. Since there is only
				// one arrival, it automatically wins, and we assign it to
				// an empty slot at random.
				if( total_arrivals == 1 ) {
					final int si = Fn.argmax( arrivals[i] );
					int ri = s.rng.nextInt( c );
					for( int j = 0; j < s.params.Nhabitats; ++j ) {
						if( s.habitats[i][j] == Species.None ) {
							if( ri-- == 0 ) {
								s.habitats[i][j] = Species.values()[si];
								break;
							}
						}
					}
					continue;
				}
				
				// FIXME: This part doesn't generalize to >2 species
				final double r = (s.params.competition_factor*arrivals[i][Species.Native.ordinal()])
								 / (s.params.competition_factor*arrivals[i][Species.Native.ordinal()]
								 	+ (1.0 - s.params.competition_factor)*arrivals[i][Species.Tamarisk.ordinal()]);
				
				// Special case for 'c == 1' since 'c - 2' would be -1 in the
				// variance calculation, leading to a complex result.
				if( c == 1 ) {
					for( int j = 0; j < s.params.Nhabitats; ++j ) {
						if( s.habitats[i][j] == Species.None ) {
							final double rand = s.rng.nextDouble();
							if( rand < r ) {
								s.habitats[i][j] = Species.Native;
								births_.add( new Cell( i, j, Species.Native ) );
							}
							else {
								s.habitats[i][j] = Species.Tamarisk;
								births_.add( new Cell( i, j, Species.Tamarisk ) );
							}
							break;
						}
					}
					continue;
				}
				
				final double b = Math.min( total_arrivals, c ) + 0.5;
				final double mu = c - Math.pow( c - 1, b ) * Math.pow( c, 1 - b );
				final double sigma_sq = (c - 1)*Math.pow( c - 2, b )*Math.pow( c, 1 - b )
										+ Math.pow( c - 1, b )*Math.pow( c, 1 - b )
										- Math.pow( c - 1, 2*b )*Math.pow( c, 2 - 2*b );
				final double q = ((mu - a)*(b - mu)/sigma_sq - 1);
				final double alpha = q * ((mu - a)/(b - a));
				final double beta = q - alpha;
				if( alpha < 0 || beta < 0 ) {
					System.out.println( "a = " + a );
					System.out.println( "b = " + b );
					System.out.println( "c = " + c );
					System.out.println( "mu = " + mu );
					System.out.println( "sigma_sq = " + sigma_sq );
					System.out.println( "q = " + q );
					System.out.println( "alpha = " + alpha );
					System.out.println( "beta = " + beta );
					throw new IllegalArgumentException( "(alpha, beta) is not a valid Beta distribution" );
				}
//				System.out.println( "alpha = " + alpha );
//				System.out.println( "beta = " + beta );
				final BetaDistribution beta_dist = new BetaDistribution(
					s.rng, alpha, beta, BetaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY );
				final double y = beta_dist.sample()*(b - a) + a;
				// This is the number of occupied previously-empty cells
				int n = (int) Math.round( y );
				
				// 2.b. Assign species based on competition ratio
				for( int j = 0; j < s.params.Nhabitats; ++j ) {
					if( n == 0 ) {
						break;
					}
					if( s.habitats[i][j] == Species.None ) {
						final double rand = s.rng.nextDouble();
						if( rand < r ) {
							s.habitats[i][j] = Species.Native;
							births_.add( new Cell( i, j, Species.Native ) );
						}
						else {
							s.habitats[i][j] = Species.Tamarisk;
							births_.add( new Cell( i, j, Species.Tamarisk ) );
						}
						n -= 1;
					}
				}
			}
		}

		@Override
		public boolean isDone()
		{ return births_ != null; }

		@Override
		public BirthEvent create()
		{ return new BirthEvent( G_, K_ ); }

		@Override
		public double cost()
		{ return 0.0; }

		@Override
		public boolean equals( final Object obj )
		{
			// Note: The equals()/hashCode() stuff is really only applicable
			// for things like UCT where we need to use Actions as keys to
			// retrieve statistics. This is not the case for "actions" of the
			// Event persuasion, so we don't need a complicated equals() that
			// checks for equivalent effects.
			return obj != null && obj instanceof BirthEvent;
		}

		@Override
		public int hashCode()
		{ return 5; }

		@Override
		public String toString()
		{ return "BirthEvent"; }
	}
	
	private final TamariskState s_;
	private double r_ = 0.0;
	
	private final double[][] K_;
	/** Number of events per time step. */
	private final int Nevents_ = 3;
	
	private final Deque<TamariskAction> action_history_ = new ArrayDeque<TamariskAction>();
	
	public TamariskSimulator( final TamariskState s )
	{
		s_ = s;
		K_ = s.params.calculateDispersionKernel( s_.graph );
	}

	@Override
	public TamariskState state()
	{
		return s_;
	}

	@Override
	public void takeAction( final JointAction<TamariskAction> action )
	{
		// Action effects
		final TamariskAction ai = action.get( 0 );
		ai.doAction( s_ );
		final double action_cost = ai.cost();
		action_history_.push( ai );
		
		// Calculate propagules before deaths
		final int[][] G = new int[s_.params.Nreaches][Species.N];
		for( int i = 0; i < s_.params.Nreaches; ++i ) {
			for( int j = 0; j < s_.params.Nhabitats; ++j ) {
				final Species species = s_.habitats[i][j];
				if( species != Species.None ) {
					final int si = species.ordinal();
					G[i][si] += s_.params.production_rate[si];
				}
			}
		}
		
		// Plants die
		final DeathEvent death = new DeathEvent();
		death.doAction( s_ );
		action_history_.push( death );
		
		// Plants are born
		final BirthEvent birth_event = new BirthEvent( G, K_ );
		birth_event.doAction( s_ );
		action_history_.push( birth_event );
		
		// Calculate cost
		double state_cost = 0.0;
		for( int i = 0; i < s_.params.Nreaches; ++i ) {
			boolean contains_tamarisk = false;
			for( int j = 0; j < s_.params.Nhabitats; ++j ) {
				if( s_.habitats[i][j] == Species.Tamarisk ) {
					contains_tamarisk = true;
					state_cost += s_.params.cost_per_tree;
				}
				else if( s_.habitats[i][j] == Species.None ) {
					state_cost += s_.params.cost_empty;
				}
			}
			if( contains_tamarisk ) {
				state_cost += s_.params.cost_per_reach;
			}
		}
		final double total_cost = action_cost + state_cost;
		r_ = -total_cost;
		s_.t += 1;
		
		assert( s_.t <= s_.params.T );
		assert( action_history_.size() % Nevents_ == 0 );
	}
	
	@Override
	public void untakeLastAction()
	{
		s_.t -= 1;
		for( int i = 0; i < Nevents_; ++i ) {
			final TamariskAction a = action_history_.pop();
			a.undoAction( s_ );
		}
		
		assert( s_.t >= 0 );
		assert( action_history_.size() % Nevents_ == 0 );
	}

	@Override
	public long depth()
	{
		return action_history_.size();
	}

	@Override
	public long t()
	{
		return action_history_.size() / Nevents_;
	}

	@Override
	public int nagents()
	{
		return 1;
	}

	@Override
	public int[] turn()
	{
		return new int[] { 0 };
	}

	@Override
	public double[] reward()
	{
		return new double[] { r_ };
	}

	@Override
	public boolean isTerminalState()
	{
		return s_.isTerminal();
	}

	@Override
	public long horizon()
	{
		return Long.MAX_VALUE;
	}

	@Override
	public String detailString()
	{
		return "TamariskSimulator";
	}

	
	public static void main( final String[] args )
	{
		for( int total_arrivals = 1; total_arrivals < 10; ++total_arrivals ) {
			for( int c = 2; c < 10; ++c ) {
				System.out.println( "total = " + total_arrivals + ", c = " + c );
				final double a = 0.5;
				final double b = Math.min( total_arrivals, c ) + 0.5;
				final double mu = c - Math.pow( c - 1, b ) * Math.pow( c, 1 - b );
		//				final double mu = c - Math.exp( b*Math.log( c-1 ) + (1-b)*Math.log( c ) );
				final double sigma_sq = (c - 1)*Math.pow( c - 2, b )*Math.pow( c, 1 - b )
										+ Math.pow( c - 1, b )*Math.pow( c, 1 - b )
										- Math.pow( c - 1, 2*b )*Math.pow( c, 2 - 2*b );
		//				final double sigma_sq = Math.exp( Math.log( c - 1 )+ b*Math.log( c - 2 ) + (1 - b)*Math.log( c ) )
		//										+ Math.exp( b*Math.log( c - 1 ) + (1 - b)*Math.log( c ) )
		//										- Math.exp( 2*b*Math.log( c - 1 ) + (2 - 2*b)*Math.log( c ) );
		//				System.out.println( "mu = " + mu );
		//				System.out.println( "sigma_sq = " + sigma_sq );
				final double q = ((mu - a)*(b - mu)/sigma_sq - 1);
		//				final double q = Math.exp( Math.log( mu - a ) + Math.log( b - mu ) - Math.log( sigma_sq ) ) - 1;
				final double alpha = q * ((mu - a)/(b - a));
				final double beta = q - alpha;
				if( alpha < 0 || beta < 0 ) {
					System.out.println( "a = " + a );
					System.out.println( "b = " + b );
					System.out.println( "c = " + c );
					System.out.println( "mu = " + mu );
					System.out.println( "sigma_sq = " + sigma_sq );
					System.out.println( "q = " + q );
					System.out.println( "alpha = " + alpha );
					System.out.println( "beta = " + beta );
				}
			}
		}
	}
}
