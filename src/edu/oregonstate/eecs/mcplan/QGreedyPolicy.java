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
