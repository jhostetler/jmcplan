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
package edu.oregonstate.eecs.mcplan.dp;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.MarkovDecisionProblem;
import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.Policy;

/**
 * @author jhostetler
 *
 */
public class GreedyPolicy<S, A> extends Policy<S, A>
{
	private final MarkovDecisionProblem<S, A> m_;
	private final ValueFunction<S> v_;
	
	private S s_ = null;
	
	public GreedyPolicy( final MarkovDecisionProblem<S, A> m, final ValueFunction<S> v )
	{
		m_ = m;
		v_ = v;
	}
	
	@Override
	public void setState( final S s, final long t )
	{
		s_ = s;
	}

	@Override
	public A getAction()
	{
		double qstar = -Double.MAX_VALUE;
		A astar = null;
		for( final A a : m_.A().getActionSet( s_ ) ) {
//				System.out.println( s.toString() );
			final Pair<ArrayList<S>, ArrayList<Double>> sparse_p = m_.sparseP( s_, a );
			double q = m_.R( s_, a );
			for( int i = 0; i < sparse_p.first.size(); ++i ) {
				final S sprime = sparse_p.first.get( i );
//					System.out.println( sprime.toString() );
				final double p = sparse_p.second.get( i );
				q += p * v_.v( sprime );
			}
			if( q > qstar ) {
				qstar = q;
				astar = a;
			}
		}
		
		return astar;
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName()
	{
		return "GreedyPolicy";
	}

	@Override
	public int hashCode()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean equals( final Object that )
	{
		// TODO Auto-generated method stub
		return false;
	}

}
