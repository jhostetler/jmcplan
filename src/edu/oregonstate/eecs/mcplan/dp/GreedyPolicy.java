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
