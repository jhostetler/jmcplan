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

package edu.oregonstate.eecs.mcplan.ml;

import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.util.Fn;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

public abstract class ConstrainedKMeans implements Runnable
{
	public final int k;
	public final int d;
	public final int N;
	
	public final ArrayList<RealVector> X_;
	
	protected final TIntObjectMap<Pair<int[], double[]>> M_;
	protected final TIntObjectMap<Pair<int[], double[]>> C_;
	
	protected final RealVector[] mu_;
	protected final int[] assignments_;
	
	protected final int[] idx_;
	protected final RandomGenerator rng_;
	
	protected final double convergence_tolerance_ = 0.001;
	private final int max_iter_ = 100;
	
	private double J_ = Double.MAX_VALUE;
	
	public ConstrainedKMeans( final int k, final int d, final ArrayList<RealVector> X,
							  final TIntObjectMap<Pair<int[], double[]>> M,
							  final TIntObjectMap<Pair<int[], double[]>> C,
							  final RandomGenerator rng )
	{
		this.k = k;
		this.d = d;
		N = X.size();
		X_ = X;
		M_ = M;
		C_ = C;
		mu_ = new RealVector[k];
		assignments_ = new int[N];
		idx_ = Fn.range( 0, N );
		rng_ = rng;
	}
	
	@Override
	public final void run()
	{
//		System.out.println( "KMeans: initializing" );
		initializeDistanceFunction();
		initialize();
		int iter_count = 0;
		while( iter_count++ < max_iter_ ) {
//			System.out.println( "KMeans: Iter " + iter_count );
//			System.out.println( "estep()" );
			estep();
//			System.out.println( "mstep()" );
			final boolean mchanged = mstep();
//			System.out.println( "updateDistanceFunction()" );
			final boolean dchanged = updateDistanceFunction();
			if( !mchanged && !dchanged ) {
				break;
			}
		}
	}
	
	public final RealVector[] mu()
	{
		return mu_;
	}
	
	public final int[] assignments()
	{
		return assignments_;
	}
	
	public abstract double distance( final RealVector x1, final RealVector x2 );
	public abstract double distanceMax();
	
	protected abstract void initializeDistanceFunction();
	protected abstract boolean updateDistanceFunction();
	
	private double J( final int i, final int h )
	{
		final RealVector xi = X_.get( i );
		final RealVector muh = mu_[h];
		
		double r = distance( xi, muh );
		
		final Pair<int[], double[]> m = M_.get( i );
		if( m != null ) {
			for( int im = 0; im < m.first.length; ++im ) {
				final int j = m.first[im];
				if( assignments_[j] != h ) {
					r += m.second[im] * distance( xi, X_.get( j ) );
				}
			}
		}
		
		final Pair<int[], double[]> c = C_.get( i );
		if( c != null ) {
			for( int ic = 0; ic < c.first.length; ++ic ) {
				final int j = c.first[ic];
				if( assignments_[j] == h ) {
					r += c.second[ic] * (distanceMax() - distance( xi, X_.get( j ) ));
				}
			}
		}
		
		return r;
	}
	
	private int assign( final int i )
	{
		int hopt = 0;
		double Jopt = Double.MAX_VALUE;
		for( int h = 0; h < k; ++h ) {
			final double ji = J( i, h );
			if( ji <= Jopt ) {
				Jopt = ji;
				hopt = h;
			}
		}
		return hopt;
	}
	
	private void initialize()
	{
		// TODO: Use the better initialization from the paper
		final TIntSet choices = new TIntHashSet( k );
		for( int i = 0; i < k; ++i ) {
			int j = 0;
			do {
				j = rng_.nextInt( N );
			} while( choices.contains( j ) );
			choices.add( j );
			mu_[i] = X_.get( j ).copy();
		}
	}

	private void estep()
	{
		// Uses the 'IMC' technique discussed in Basu et al.
		boolean done = false;
		while( !done ) {
			done = true;
			Fn.shuffle( rng_, idx_ );
			for( int i = 0; i < N; ++i ) {
				final int j = idx_[i];
				final int c = assign( j );
				if( c != assignments_[j] ) {
					done = false;
				}
				assignments_[j] = c;
			}
		}
	}

	private boolean mstep()
	{
		final double[][] mu_prime = new double[k][d];
		final int[] nh = new int[k];
		for( int i = 0; i < N; ++i ) {
			final int h = assignments_[i];
			nh[h] += 1;
			final RealVector xi = X_.get( i );
//			mu_prime[h] = mu_prime[h].add( xi.subtract( mu_prime[h] ).mapDivideToSelf( i + 1 ) );
			for( int j = 0; j < d; ++j ) {
				// Incremental mean calculation
				mu_prime[h][j] += (xi.getEntry( j ) - mu_prime[h][j]) / nh[h];
			}
		}
		
		double Jprime = 0.0;
		for( int i = 0; i < N; ++i ) {
			final int h = assignments_[i];
			Jprime += J( i, h );
		}
		
		double delta = 0.0;
		for( int h = 0; h < k; ++h ) {
			final RealVector v = new ArrayRealVector( mu_prime[h], false ); // No copy
//			System.out.println( mu_[h] );
//			System.out.println( v );
			final RealVector diff = mu_[h].subtract( v );
			delta += diff.getNorm();
//			delta += mu_[h].getDistance( v );
//			delta += distance( mu_[h], v );
			mu_[h] = v;
		}
//		System.out.println( "\tdelta = " + delta );
		final double deltaJ = J_ - Jprime;
//		System.out.println( "\tdeltaJ = " + deltaJ );
		J_ = Jprime;
		return deltaJ > convergence_tolerance_;
	}
	
	// -----------------------------------------------------------------------
	
	/**
	 * @param args
	 */
	public static void main( final String[] args )
	{
		final RandomGenerator rng = new MersenneTwister( 42 );
		final int K = 2;
		final int d = 2;
		final ArrayList<RealVector> X = new ArrayList<RealVector>();
		final double u = 2.0;
		final double ell = 8.0;
		final double gamma = 10.0;
		
		for( final int s : new int[] { 0, 5, 10 } ) {
			for( int x = -1; x <= 1; ++x ) {
				for( int y = -1; y <= 1; ++y ) {
					X.add( new ArrayRealVector( new double[] { x + s, y } ) );
				}
			}
		}
		
		final TIntObjectMap<Pair<int[], double[]>> M = new TIntObjectHashMap<Pair<int[], double[]>>();
		M.put( 16, Pair.makePair( new int[] { 20 }, new double[] { 1.0 } ) );
		M.put( 0, Pair.makePair( new int[] { 8 }, new double[] { 1.0 } ) );
		
		final TIntObjectMap<Pair<int[], double[]>> C = new TIntObjectHashMap<Pair<int[], double[]>>();
		C.put( 13, Pair.makePair( new int[] { 20 }, new double[] { 1.0 } ) );
		C.put( 10, Pair.makePair( new int[] { 16 }, new double[] { 1.0 } ) );
		
		final ArrayList<double[]> S = new ArrayList<double[]>();
		M.forEachKey( new TIntProcedure() {
			@Override
			public boolean execute( final int i )
			{
				final Pair<int[], double[]> p = M.get( i );
				if( p != null ) {
					for( final int j : p.first ) {
						S.add( new double[] { i, j } );
					}
				}
				return true;
			}
		} );
		
		final ArrayList<double[]> D = new ArrayList<double[]>();
		C.forEachKey( new TIntProcedure() {
			@Override
			public boolean execute( final int i )
			{
				final Pair<int[], double[]> p = C.get( i );
				if( p != null ) {
					for( final int j : p.first ) {
						D.add( new double[] { i, j } );
					}
				}
				return true;
			}
		} );
		
		final ConstrainedKMeans kmeans = new ConstrainedKMeans( K, d, X, M, C, rng ) {
			RealMatrix A_ = MatrixUtils.createRealIdentityMatrix( d );
			double Dmax_ = 1.0;
			
			@Override
			public double distance( final RealVector x1, final RealVector x2 )
			{
				final RealVector diff = x1.subtract( x2 );
				return Math.sqrt( HilbertSpace.inner_prod( diff, A_, diff ) );
			}

			@Override
			public double distanceMax()
			{
				return Dmax_;
			}
			
			@Override
			protected void initializeDistanceFunction()
			{
				double max_distance = 0.0;
				for( int i = 0; i < X.size(); ++i ) {
					for( int j = i + 1; j < X.size(); ++j ) {
						final double d = distance( X.get( i ), X.get( j ) );
						if( d > max_distance ) {
							max_distance = d;
						}
					}
				}
				Dmax_ = max_distance;
			}
			
			@Override
			protected boolean updateDistanceFunction()
			{
				final InformationTheoreticMetricLearner itml = new InformationTheoreticMetricLearner(
					S, D, u, ell, A_, gamma, rng_ );
				itml.run();
				final double delta = A_.subtract( itml.A() ).getFrobeniusNorm();
				A_ = itml.A();
				initializeDistanceFunction();
				return delta > convergence_tolerance_;
			}
		};
		
		kmeans.run();
		for( int i = 0; i < kmeans.mu().length; ++i ) {
			System.out.println( "Mu " + i + ": " + kmeans.mu()[i] );
			for( int j = 0; j < kmeans.assignments().length; ++j ) {
				if( kmeans.assignments()[j] == i ) {
					System.out.println( "\tPoint " + X.get( j ) );
				}
			}
		}
		
	}
}
