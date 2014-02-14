/**
 * 
 */
package edu.oregonstate.eecs.mcplan.dp;

import java.util.Arrays;

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
			m_.A().setState( s );
			final Generator<A> ga = m_.A().generator();
			while( ga.hasNext() ) {
				final A a = ga.next();
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
