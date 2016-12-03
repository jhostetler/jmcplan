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
 * @author jhostetler
 *
 */
public abstract class HeuristicBandit<T> extends FiniteBandit<T>
{
	public HeuristicBandit()
	{
		super();
	}
	
	public HeuristicBandit( final ArrayList<T> arms, final StochasticEvaluator<T> eval )
	{
		super( arms, eval );
	}
	
	/**
	 * The heuristic value of the arm. The arm that currently has the largest
	 * heuristic value will be selected next. If heuristic( i ) returns
	 * Double.POSITIVE_INFINITY, arm 'i' will be selected immediately without
	 * evaluating the heuristic for any j > i.
	 * @param i
	 * @return
	 */
	protected abstract double heuristic( final int i );
	
	@Override
	protected final int selectArm( final RandomGenerator rng )
	{
		int istar = 0;
		double hstar = Double.NEGATIVE_INFINITY;
		for( int i = 0; i < Narms(); ++i ) {
			final double h = heuristic( i );
			if( h > hstar ) {
				hstar = h;
				istar = i;
			}
			if( h == Double.POSITIVE_INFINITY ) {
				// Since POSITIVE_INFINITY > POSITIVE_INFINITY is false, no
				// other arm can replace istar in the future.
				break;
			}
		}
		return istar;
	}
}
