/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Adapts a Policy over Options into a Policy over primitive actions, by
 * forwarding calls to Option.pi.
 */
public class OptionPolicy<S, A> implements Policy<S, A>
{
	public final Policy<S, Option<S, A>> mu;
	public Option<S, A> o = null;
	
	private final RandomGenerator rng_;
	private S s_ = null;
	private long t_ = 0L;
	
	public OptionPolicy( final Policy<S, Option<S, A>> mu, final long seed )
	{
		this.mu = mu;
		rng_ = new MersenneTwister( seed );
	}
	
	private boolean terminate( final S s, final long t, final Option<S, A> o )
	{
		final double beta = o.terminate( s, t );
		if( beta == 1.0 ) {
			return true;
		}
		else if( beta == 0.0 ) {
			return false;
		}
		else {
			return rng_.nextDouble() < beta;
		}
	}
	
	@Override
	public void setState( final S s, final long t )
	{
		s_ = s;
		t_ = t;
	}

	@Override
	public A getAction()
	{
		// NOTE: We're assuming here that options can't terminate in their
		// initial state. This is a necessary assumption to implement
		// anytime behavior correctly, since we need to have at most one
		// 'getAction()' computation.
		if( o == null || terminate( s_, t_, o ) ) {
			System.out.println( "! Terminated" );
			mu.setState( s_, t_ );
			o = mu.getAction();
			o.start( s_, t_ );
		}
		System.out.println( getName() );
		o.setState( s_, t_ );
		return o.getAction();
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		o.pi.actionResult( sprime, r );
	}

	@Override
	public String getName()
	{
		return "OptionPolicy[" + o.pi.getName() + "]";
	}
}
