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
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public class SparseValueIterationSolver<S, A> implements Runnable
{
	private final MarkovDecisionProblem<S, A> m_;
	private final HashMap<S, Double> v_ = new HashMap<S, Double>();
	
	private final double gamma_;
	private final double convergence_threshold_ = 0.0001;
	private double delta_ = 0.0;
	
	public SparseValueIterationSolver( final MarkovDecisionProblem<S, A> m )
	{
		this( m, 1.0 );
	}
	
	public SparseValueIterationSolver( final MarkovDecisionProblem<S, A> m, final double gamma )
	{
		assert( m.S().isFinite() );
		m_ = m;
		gamma_ = gamma;
		
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
			m_.A().setState( s );
			final Generator<A> ga = m_.A().generator();
			double max_q = -Double.MAX_VALUE;
			A max_a = null;
			while( ga.hasNext() ) {
				final A a = ga.next();
				final double q = Q( s, a );
				if( q > max_q ) {
					max_q = q;
					max_a = a;
				}
			}
			actions.put( s, max_a );
		}
		return new LookupPolicy<S, A>( actions );
	}

	@Override
	public void run()
	{
		int count = 0;
		while( true ) {
			System.out.println( "Iteration " + (count++) );
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
			double qstar = -Double.MAX_VALUE;
			m_.A().setState( s );
			final Generator<A> ga = m_.A().generator();
			while( ga.hasNext() ) {
				final A a = ga.next();
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
			
			final double old_q = v_.put( s, qstar );
			final double diff = qstar - old_q;
			delta_ += (diff*diff);
		}
	}
}
