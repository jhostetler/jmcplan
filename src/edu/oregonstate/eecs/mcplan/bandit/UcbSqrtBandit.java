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

/**
 * A modified UCB rule that uses sqrt() instead of log() in the numerator.
 * Better than UCB in terms of simple regret. Empirically also better than
 * alternative simple regret rules.
 * 
 * @inproceedings{tolpin2012mcts,
 *   title={MCTS Based on Simple Regret},
 *   author={Tolpin, David and Shimony, Solomon Eyal},
 *   booktitle={AAAI},
 *   year={2012}
 * }
 */
public class UcbSqrtBandit<T> extends HeuristicBandit<T>
{
	private final double c;
	
	public UcbSqrtBandit( final double c )
	{
		this.c = c;
	}
	
	public UcbSqrtBandit( final ArrayList<T> arms, final StochasticEvaluator<T> eval, final double c )
	{
		super( arms, eval );
		this.c = c;
	}
	
	@Override
	public UcbSqrtBandit<T> create( final ArrayList<T> arms, final StochasticEvaluator<T> eval )
	{
		return new UcbSqrtBandit<>( arms, eval, c );
	}
	
	@Override
	protected double heuristic( final int i )
	{
		final int ni = n( i );
		if( ni == 0 ) {
			// Sample un-sampled arms first
			return Double.POSITIVE_INFINITY;
		}
		else {
			// The only point of divergence from UCB: sqrt() rather than log()
			return mean( i ) + Math.sqrt( (c * Math.sqrt( n() )) / ni );
		}
	}
}
