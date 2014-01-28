/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import java.util.ArrayList;

/**
 * @author jhostetler
 *
 */
public abstract class MarkovDecisionProblem<S, A>
{
	protected final StateSpace<S> ss_;
	protected final ActionSpace<S, A> as_;
	
	public MarkovDecisionProblem( final StateSpace<S> ss, final ActionSpace<S, A> as )
	{
		ss_ = ss;
		as_ = as;
	}
	
	public final StateSpace<S> S()
	{
		return ss_;
	}
	
	public final ActionSpace<S, A> A()
	{
		return as_;
	}
	
	public abstract Pair<ArrayList<S>, ArrayList<Double>> sparseP( final S s, final A a );
	
	public abstract double[] P( final S s, final A a );
	public abstract double P( final S s, final A a, final S sprime );
	
	public abstract double R( final S s, final A a );
}
