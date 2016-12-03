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
 * The Upper Confidence Bound bandit rule. A popular choice for minimizing
 * cumulative regret.
 * 
 * @article{auer2002finite,
 *   title={Finite-time analysis of the multiarmed bandit problem},
 *   author={Auer, Peter and Cesa-Bianchi, Nicolo and Fischer, Paul},
 *   journal={Machine learning},
 *   volume={47},
 *   number={2-3},
 *   pages={235--256},
 *   year={2002},
 * }
 */
public class UcbBandit<T> extends HeuristicBandit<T>
{
	private final double c;
	
	public UcbBandit( final double c )
	{
		this.c = c;
	}
	
	public UcbBandit( final ArrayList<T> arms, final StochasticEvaluator<T> eval, final double c )
	{
		super( arms, eval );
		this.c = c;
	}
	
	@Override
	public UcbBandit<T> create( final ArrayList<T> arms, final StochasticEvaluator<T> eval )
	{
		return new UcbBandit<>( arms, eval, c );
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
			return mean( i ) + Math.sqrt( (c * Math.log( n() )) / ni );
		}
	}
}