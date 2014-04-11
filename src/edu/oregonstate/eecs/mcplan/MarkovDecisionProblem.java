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
	
	public abstract StateSpace<S> S();
	
	public abstract ActionSpace<S, A> A();
	
	public abstract Pair<ArrayList<S>, ArrayList<Double>> sparseP( final S s, final A a );
	
	public abstract double[] P( final S s, final A a );
	public abstract double P( final S s, final A a, final S sprime );
	
	public abstract double R( final S s, final A a );
}
