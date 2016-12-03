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

package edu.oregonstate.eecs.mcplan.bandit;

import java.util.ArrayList;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * Chooses the current best arm with probability \epsilon, and any other arm
 * with probability (1 - \epsilon) / (K - 1).
 * 
 * @inproceedings{tolpin2012mcts,
 *   title={MCTS Based on Simple Regret},
 *   author={Tolpin, David and Shimony, Solomon Eyal},
 *   booktitle={AAAI},
 *   year={2012}
 * }
 */
public class EpsilonGreedyBandit<T> extends FiniteBandit<T>
{
	public static final double greedy = 1.0;
	public static final double uniform = 0.0;
	
	private final double epsilon;
	
	public EpsilonGreedyBandit( final double epsilon )
	{
		super();
		this.epsilon = epsilon;
	}
	
	public EpsilonGreedyBandit( final ArrayList<T> arms, final StochasticEvaluator<T> eval, final double epsilon )
	{
		super( arms, eval );
		this.epsilon = epsilon;
	}
	
	@Override
	public EpsilonGreedyBandit<T> create( final ArrayList<T> arms, final StochasticEvaluator<T> eval )
	{
		return new EpsilonGreedyBandit<>( arms, eval, epsilon );
	}

	@Override
	protected int selectArm( final RandomGenerator rng )
	{
		// Select un-sampled arms first
		if( n() < Narms() ) {
			for( int i = 0; i < Narms(); ++i ) {
				if( n( i ) == 0 ) {
					return i;
				}
			}
		}
		
		// Avoid RNG if result is actually deterministic
		final boolean greedy;
		if( epsilon == 0 ) {
			greedy = false;
		}
		else if( epsilon == 1 ) {
			greedy = true;
		}
		else {
			greedy = rng.nextDouble() < epsilon;
		}
		
		if( greedy ) {
			return bestIndex();
		}
		else {
			// Return random arm that is not best
			final int i = rng.nextInt( arms.size() - 1 );
			return (i < bestIndex() ? i : i + 1);
		}
	}
}
