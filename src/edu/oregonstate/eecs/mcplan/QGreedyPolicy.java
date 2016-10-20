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
package edu.oregonstate.eecs.mcplan;

import gnu.trove.list.TDoubleList;

import java.util.ArrayList;

/**
 * @author jhostetler
 *
 */
public class QGreedyPolicy<S, A> extends Policy<S, A>
{
	private final QFunction<S, A> qfunction_;
	
	private S s_ = null;
	
	public QGreedyPolicy( final QFunction<S, A> qfunction )
	{
		qfunction_ = qfunction;
	}
	
	@Override
	public void setState( final S s, final long t )
	{
		s_ = s;
	}
	
	protected void onQFunctionCalculate( final S s, final Pair<ArrayList<A>, TDoubleList> q )
	{
		
	}

	@Override
	public A getAction()
	{
		qfunction_.calculate( s_ );
		final Pair<ArrayList<A>, TDoubleList> q = qfunction_.get();
		onQFunctionCalculate( s_, q );
		int istar = -1;
		double qstar = -Double.MAX_VALUE;
		for( int i = 0; i < q.first.size(); ++i ) {
			final double qi = q.second.get( i );
			if( qi > qstar ) {
				qstar = qi;
				istar = i;
			}
		}
		final A astar = q.first.get( istar );
		return astar;
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{ }

	@Override
	public String getName()
	{
		return "QGreedyPolicy";
	}

	@Override
	public int hashCode()
	{
		return System.identityHashCode( this );
	}

	@Override
	public boolean equals( final Object that )
	{
		return this == that;
	}

}
