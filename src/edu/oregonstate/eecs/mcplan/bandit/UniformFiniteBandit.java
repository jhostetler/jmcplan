/**
 * 
 */
package edu.oregonstate.eecs.mcplan.bandit;

import java.util.ArrayList;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * @author jhostetler
 *
 */
public class UniformFiniteBandit<T> extends FiniteBandit<T>
{
	private final ArrayList<MeanVarianceAccumulator> r;
	private double rstar = -Double.MAX_VALUE;
	private T tstar = null;
	
	public UniformFiniteBandit()
	{
		super();
		r = null;
	}
	
	public UniformFiniteBandit( final ArrayList<T> arms, final StochasticEvaluator<T> eval )
	{
		super( arms, eval );
		
		r = new ArrayList<MeanVarianceAccumulator>();
		for( int i = 0; i < arms.size(); ++i ) {
			r.add( null );
		}
	}
	
	@Override
	public UniformFiniteBandit<T> create( final ArrayList<T> arms, final StochasticEvaluator<T> eval )
	{
		return new UniformFiniteBandit<>( arms, eval );
	}

	@Override
	public void sampleArm( final RandomGenerator rng )
	{
		final int i = rng.nextInt( arms.size() );
		MeanVarianceAccumulator ri = r.get( i );
		if( ri == null ) {
			ri = new MeanVarianceAccumulator();
			r.set( i, ri );
		}
		final T t = arms.get( i );
		final double rsample = eval.evaluate( rng, t );
		System.out.println( "UniformFiniteBandit: arm " + t + " => " + rsample );
		ri.add( rsample );
		if( ri.mean() > rstar ) {
			rstar = ri.mean();
			tstar = t;
		}
	}

	@Override
	public T bestArm()
	{
		return tstar;
	}
}
