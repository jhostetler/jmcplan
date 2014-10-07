/**
 * 
 */
package edu.oregonstate.eecs.mcplan.ml;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.ArithmeticUtils;
import org.apache.commons.math3.util.FastMath;

import edu.oregonstate.eecs.mcplan.util.Csv;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * The contingency table of two partitions. The statistics in the table can
 * be used to compute various measures of clustering quality.
 * 
 * We use the notation and function names of:
 * @article{vinh2010information,
 *	title={Information Theoretic Measures for Clusterings Comparison: Variants, Properties, Normalization and Correction for Chance},
 *	author={Vinh, Nguyen Xuan and Epps, Julien and Bailey, James},
 *	journal={Journal of Machine Learning Research},
 *	volume={11},
 *	pages={2837--2854},
 *	year={2010}
 * }
 * 
 * @author jhostetler
 */
public class ClusterContingencyTable
{
	public final int R;
	public final int C;
	public final int[][] n;
	public final int N;
	public final int[] a;
	public final int[] b;
	
	public ClusterContingencyTable( final ArrayList<Set<RealVector>> U,
									final ArrayList<Set<RealVector>> V )
	{
		R = U.size();
		C = V.size();
		
		int N = 0;
		a = new int[R];
		b = new int[C];
		n = new int[R][C];
		for( int i = 0; i < R; ++i ) {
			final Set<RealVector> u = U.get( i );
			for( int j = 0; j < C; ++j ) {
				final Set<RealVector> v = V.get( j );
				for( final RealVector uu : u ) {
					if( v.contains( uu ) ) {
						a[i] += 1;
						b[j] += 1;
						n[i][j] += 1;
						N += 1;
					}
				}
			}
		}
		this.N = N;
	}
	
	private ClusterContingencyTable( final int R, final int C, final int[][] n,
									 final int N, final int[] a, final int[] b )
	{
		this.R = R;
		this.C = C;
		this.n = n;
		this.N = N;
		this.a = a;
		this.b = b;
	}
	
	public double entropyU()
	{
		double h = 0.0;
		for( int i = 0; i < R; ++i ) {
			final double p = a[i] / ((double) N);
			if( p == 0.0 ) {
				continue;
			}
			h += -p*FastMath.log( 2, p );
		}
		return h;
	}
	
	public double entropyV()
	{
		double h = 0.0;
		for( int j = 0; j < C; ++j ) {
			final double p = b[j] / ((double) N);
			if( p == 0.0 ) {
				continue;
			}
			h += -p*FastMath.log( 2, p );
		}
		return h;
	}
	
	public double mutualInformation()
	{
		final double Nd = N;
		double mi = 0.0;
		for( int i = 0; i < R; ++i ) {
			for( int j = 0; j < C; ++j ) {
				final double p = n[i][j] / Nd;
				if( p == 0 ) {
					continue;
				}
				final double q = p / ((a[i]*b[j])/(Nd*Nd));
				mi += p * FastMath.log( 2, q );
			}
		}
		return mi;
	}
	
	public double normalizedMutualInformation_max()
	{
		return mutualInformation() / Math.max( entropyU(), entropyV() );
	}
	
	public double expectedMutualInformation()
	{
		double emi = 0.0;
		for( int i = 0; i < R; ++i ) {
			for( int j = 0; j < C; ++j ) {
				// We take max( _, 1 ) instead of max( _, 0 ) as in the paper
				// because when nij is 0, the product is 0, but log( 0 )
				// causes NaN.
				final int start = Math.max( a[i] + b[j] - N, 1 );
				final int end = Math.min( a[i], b[j] );
				for( int nij = start; nij <= end; ++nij ) {
					final double p = nij / ((double) N);
					final double L = FastMath.log( 2, N*nij / ((double) a[i]*b[j]) );
					final double logNum = ArithmeticUtils.factorialLog( a[i] )
										  + ArithmeticUtils.factorialLog( b[j] )
										  + ArithmeticUtils.factorialLog( N - a[i] )
										  + ArithmeticUtils.factorialLog( N - b[j] );
					final double logDenom = ArithmeticUtils.factorialLog( N )
											+ ArithmeticUtils.factorialLog( nij )
											+ ArithmeticUtils.factorialLog( a[i] - nij )
											+ ArithmeticUtils.factorialLog( b[j] - nij )
											+ ArithmeticUtils.factorialLog( N - a[i] - b[j] + nij );
					final double all = p * L * FastMath.exp( logNum - logDenom );
					emi += all;
				}
			}
		}
		return emi;
	}
	
	public double adjustedMutualInformation_max()
	{
		if( C == 1 && R == 1 ) {
			return 1.0;
		}
		final double emi = expectedMutualInformation();
		assert( !Double.isNaN( emi ) );
		final double HU = entropyU();
		final double HV = entropyV();
		if( HU < 1e-100 && HV < 1e-100 ) {
			return 1.0;
		}
		final double num = mutualInformation() - emi;
		final double denom = Math.max( HU, HV ) - emi;
//		if( Math.abs( denom ) < 1e-100 && Math.abs( num ) < 1e-100 ) {
//			return 0.0;
//		}
		return num / denom;
	}
	
	public ClusterContingencyTable plus( final ClusterContingencyTable that )
	{
		assert( R == that.R );
		assert( C == that.C );
		
		final int[][] n = Fn.copy( this.n );
		for( int r = 0; r < R; ++r ) {
			for( int c = 0; c < C; ++c ) {
				n[r][c] += that.n[r][c];
			}
		}
		
		final int[] a = Fn.copy( this.a );
		for( int r = 0; r < R; ++r ) {
			a[r] += that.a[r];
		}
		
		final int[] b = Fn.copy( this.b );
		for( int c = 0; c < C; ++c ) {
			b[c] += that.b[c];
		}
		
		return new ClusterContingencyTable( R, C, n, N + that.N, a, b );
	}
	
	public void writeCsv( final PrintStream out )
	{
		final Csv.Writer csv = new Csv.Writer( out );
		for( int r = 0; r < R; ++r ) {
			for( int c = 0; c < C; ++c ) {
				csv.cell( n[r][c] );
			}
			csv.newline();
		}
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		final int width = 5;
		final String fmt = "%" + width + "d";
		
		for( int i = 0; i < width; ++i ) {
			sb.append( " " );
		}
		sb.append( "|" );
		for( int j = 0; j < C; ++j ) {
			sb.append( " " ).append( String.format( fmt, j ) );
		}
		sb.append( "| ai" );
		sb.append( "\n" );
		
		for( int j = 0; j < (width + 1)*(C + 2); ++j ) {
			sb.append( "-" );
		}
		sb.append( "\n" );
		
		for( int i = 0; i < R; ++i ) {
			sb.append( String.format( fmt,i ) ).append( "|" );
			final int max = Fn.argmax( n[i] );
			for( int j = 0; j < C; ++j ) {
				if( j == max + 1 ) {
					sb.append( "*" );
				}
				else {
					sb.append( " " );
				}
				sb.append( String.format( fmt, n[i][j] ) );
			}
			sb.append( "| ").append( String.format( fmt, a[i] ) );
			sb.append( "\n" );
		}
		
		for( int j = 0; j < (width + 1)*(C + 2); ++j ) {
			sb.append( "-" );
		}
		sb.append( "\n" );
		
		sb.append( " bj |" );
		for( int j = 0; j < C; ++j ) {
			sb.append( " " ).append( String.format( fmt, b[j] ) );
		}
		sb.append( "| " ).append( N );
//		sb.append( "\n" );
		
		return sb.toString();
	}
}
