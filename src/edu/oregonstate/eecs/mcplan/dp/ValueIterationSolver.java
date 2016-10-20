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

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.ActionSet;
import edu.oregonstate.eecs.mcplan.MarkovDecisionProblem;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public class ValueIterationSolver<S, A> implements Runnable
{
	private final MarkovDecisionProblem<S, A> m_;
	private final double[] v_;
	
	private final double gamma_;
	private final double convergence_threshold_ = 0.00001;
	
	public ValueIterationSolver( final MarkovDecisionProblem<S, A> m )
	{
		this( m, 1.0 );
	}
	
	public ValueIterationSolver( final MarkovDecisionProblem<S, A> m, final double gamma )
	{
		assert( m.S().isFinite() );
		m_ = m;
		v_ = new double[m_.S().cardinality()];
		gamma_ = gamma;
	}
	
	public double[] vstar()
	{
		return v_;
	}

	@Override
	public void run()
	{
		int count = 0;
		while( true ) {
			System.out.println( "Iteration " + (count++) );
			final double[] vprime = iterate();
			System.out.println( "\t" + Arrays.toString( vprime ) );
			if( converged( v_, vprime ) ) {
				break;
			}
			for( int i = 0; i < v_.length; ++i ) {
				v_[i] = vprime[i];
			}
		}
	}
	
	private boolean converged( final double[] v, final double[] vprime )
	{
		double d = 0;
		for( int i = 0; i < v.length; ++i ) {
			final double di = v[i] - vprime[i];
			d += di*di;
		}
		return d < convergence_threshold_;
	}

	private double[] iterate()
	{
		final double[] vprime = new double[v_.length];
		int i = 0;
		final Generator<S> gs = m_.S().generator();
		while( gs.hasNext() ) {
			final S s = gs.next();
			double qstar = -Double.MAX_VALUE;
			final ActionSet<S, A> actions = m_.A().getActionSet( s );
//			final Generator<A> ga = m_.A().generator();
//			while( ga.hasNext() ) {
			for( final A a : actions ) {
//				final A a = ga.next();
				final Generator<S> gsprime = m_.S().generator();
				double q = 0.0;
				int j = 0;
				while( gsprime.hasNext() ) {
					final S sprime = gsprime.next();
					q += m_.P( s, a, sprime ) * (m_.R( s, a ) + gamma_ * v_[j]);
					++j;
				}
				if( q > qstar ) {
					qstar = q;
				}
			}
			vprime[i] = qstar;
			++i;
		}
		
		return vprime;
	}
}
