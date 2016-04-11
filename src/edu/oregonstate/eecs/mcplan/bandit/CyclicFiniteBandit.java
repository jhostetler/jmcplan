/**
 * 
 */
package edu.oregonstate.eecs.mcplan.bandit;

import java.util.ArrayList;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.LoggerManager;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * Samples each arm sequentially, then repeats. Useful for deterministic
 * problems.
 */
public class CyclicFiniteBandit<T> extends FiniteBandit<T>
{
	private final ch.qos.logback.classic.Logger LogAgent = LoggerManager.getLogger( "log.agent" );
	
	private final ArrayList<MeanVarianceAccumulator> r;
	private double rstar = -Double.MAX_VALUE;
	private T tstar = null;
	
	private int next = 0;
	private boolean all = false;
	
	public CyclicFiniteBandit()
	{
		super();
		r = null;
	}
	
	public CyclicFiniteBandit( final ArrayList<T> arms, final StochasticEvaluator<T> eval )
	{
		super( arms, eval );
		
		r = new ArrayList<MeanVarianceAccumulator>();
		for( int i = 0; i < arms.size(); ++i ) {
			r.add( null );
		}
	}
	
	@Override
	public CyclicFiniteBandit<T> create( final ArrayList<T> arms, final StochasticEvaluator<T> eval )
	{
		return new CyclicFiniteBandit<>( arms, eval );
	}

	@Override
	public void sampleArm( final RandomGenerator rng )
	{
		final int i = next++;
		
		MeanVarianceAccumulator ri = r.get( i );
		if( ri == null ) {
			ri = new MeanVarianceAccumulator();
			r.set( i, ri );
		}
		final T t = arms.get( i );
		LogAgent.debug( "CyclicFiniteBandit: evaluate " + t );
		final double rsample = eval.evaluate( rng, t );
		LogAgent.info( "CyclicFiniteBandit: arm {} => {}", t, rsample );
		ri.add( rsample );
		if( ri.mean() > rstar ) {
			LogAgent.info( "Lead change: {} => {}", tstar, t );
			LogAgent.info( "\tr: {} => {}", rstar, ri.mean() );
			rstar = ri.mean();
			tstar = t;
		}
		
		if( next >= arms.size() ) {
			next = 0;
			all = true;
		}
	}

	@Override
	public T bestArm()
	{
		return tstar;
	}

	@Override
	public boolean convergenceTest( final double epsilon, final double delta )
	{
		return all;
	}
}
