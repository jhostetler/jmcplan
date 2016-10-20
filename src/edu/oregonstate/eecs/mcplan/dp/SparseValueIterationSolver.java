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
import java.util.HashMap;
import java.util.Map;

import edu.oregonstate.eecs.mcplan.MarkovDecisionProblem;
import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public class SparseValueIterationSolver<S, A extends VirtualConstructor<A>> implements Runnable
{
	private final MarkovDecisionProblem<S, A> m_;
	private final HashMap<S, Double> v_ = new HashMap<S, Double>();
	
	private final double gamma_;
	private final double convergence_threshold_;
	private double delta_ = 0.0;
	
	public SparseValueIterationSolver( final MarkovDecisionProblem<S, A> m )
	{
		this( m, 1.0 );
	}
	
	public SparseValueIterationSolver( final MarkovDecisionProblem<S, A> m, final double gamma )
	{
		this( m, gamma, 1e-4 );
	}
	
	public SparseValueIterationSolver( final MarkovDecisionProblem<S, A> m, final double gamma,
									   final double convergence_threshold )
	{
		assert( m.S().isFinite() );
		m_ = m;
		gamma_ = gamma;
		convergence_threshold_ = convergence_threshold;
		
		final Generator<S> g = m.S().generator();
		while( g.hasNext() ) {
			final S s = g.next();
			v_.put( s, 0.0 );
		}
	}
	
	public ValueFunction<S> Vstar()
	{
		final HashMap<S, Double> this_v = v_;
		return new ValueFunction<S>() {
			private final HashMap<S, Double> v_ = new HashMap<S, Double>( this_v );
			@Override
			public double v( final S s )
			{
				return v_.get( s );
			}
		};
	}
	
	public double Q( final S s, final A a )
	{
		double q = m_.R( s, a );
		final Pair<ArrayList<S>, ArrayList<Double>> succ = m_.sparseP( s, a );
		for( int i = 0; i < succ.first.size(); ++i ) {
			q += succ.second.get( i ) * v_.get( succ.first.get( i ) );
		}
		return q;
	}
	
	public Policy<S, A> pistar()
	{
		final Map<S, A> actions = new HashMap<S, A>();
		final Generator<S> g = m_.S().generator();
		while( g.hasNext() ) {
			final S s = g.next();
			double max_q = -Double.MAX_VALUE;
			A max_a = null;
			for( final A a : m_.A().getActionSet( s ) ) {
				final double q = Q( s, a );
				if( q > max_q ) {
					max_q = q;
					max_a = a;
				}
			}
			if( max_a != null ) {
				actions.put( s, max_a );
			}
		}
		return new LookupPolicy<S, A>( actions );
	}

	@Override
	public void run()
	{
		final int count = 0;
		while( true ) {
//			System.out.println( "Iteration " + (count++) );
			iterate();
//			System.out.println( "\t" + v_.toString() );
			if( converged() ) {
				break;
			}
		}
	}
	
	private boolean converged()
	{
		return delta_ < convergence_threshold_;
	}

	private void iterate()
	{
		delta_ = 0.0;
		final Generator<S> gs = m_.S().generator();
		while( gs.hasNext() ) {
			final S s = gs.next();
			assert( s != null );
			final double r = m_.R( s );
			double qstar = -Double.MAX_VALUE;
			for( final A a : m_.A().getActionSet( s ) ) {
//				System.out.println( s.toString() );
				final Pair<ArrayList<S>, ArrayList<Double>> sparse_p = m_.sparseP( s, a );
				double q = m_.R( s, a );
//				System.out.println( sparse_p.first.size() );
				for( int i = 0; i < sparse_p.first.size(); ++i ) {
					final S sprime = sparse_p.first.get( i );
//					System.out.println( sprime.toString() );
					final double p = sparse_p.second.get( i );
					final Double vs = v_.get( sprime );
					if( vs == null ) {
						System.out.println( sprime.toString() );
//						vs = 0.0;
//						v_.put( sprime, vs );
					}
					q += p * gamma_ * vs;
				}
				if( q > qstar ) {
					qstar = q;
				}
			}
			
			final double v = r + (qstar > -Double.MAX_VALUE ? qstar : 0);
			
			final double old_q = v_.put( s, v );
			final double diff = v - old_q;
			delta_ += (diff*diff);
		}
	}
}
