/**
 * 
 */
package edu.oregonstate.eecs.mcplan.bandit;

import java.util.ArrayList;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author jhostetler
 *
 */
public abstract class FiniteBandit<T>
{
	protected final ArrayList<T> arms;
	protected final StochasticEvaluator<T> eval;
	
	public FiniteBandit()
	{
		arms = null;
		eval = null;
	}
	
	public FiniteBandit( final ArrayList<T> arms, final StochasticEvaluator<T> eval )
	{
		this.arms = arms;
		this.eval = eval;
	}
	
	public final int Narms()
	{
		return arms.size();
	}
	
	public final T arm( final int i )
	{
		return arms.get( i );
	}
	
	public abstract FiniteBandit<T> create( final ArrayList<T> arms, final StochasticEvaluator<T> eval );
	
	public abstract void sampleArm( final RandomGenerator rng );
	
	public abstract T bestArm();
}
