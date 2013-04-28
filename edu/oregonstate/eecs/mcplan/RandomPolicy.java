package edu.oregonstate.eecs.mcplan;

import org.apache.commons.math3.random.MersenneTwister;


public class RandomPolicy<S, A> implements AnytimePolicy<S, A>
{
	private final int turn_;
	private final ActionGenerator<S, A> action_gen_;
	private final MersenneTwister rng_;
	
	public RandomPolicy( final int turn, final int seed, final ActionGenerator<S, A> action_gen )
	{
		turn_ = turn;
		action_gen_ = action_gen;
		rng_ = new MersenneTwister( seed );
	}
	
	
	@Override
	public void setState( final S s, final long t )
	{
		action_gen_.setState( s, t, turn_ );
	}

	@Override
	public A getAction()
	{
		int i = rng_.nextInt( action_gen_.size() );
		while( i-- > 0 ) {
			action_gen_.next();
		}
		final A a = action_gen_.next();
//		System.out.println( "Random action: " + a ); // TODO: Debugging
		return a;
	}

	@Override
	public void actionResult( final A a, final S sprime, final double r )
	{ }

	@Override
	public String getName()
	{
		return "RandomPolicy";
	}

	@Override
	public long minControl()
	{
		return 0;
	}

	@Override
	public long maxControl()
	{
		return 0;
	}

	@Override
	public A getAction( final long control )
	{
		return getAction();
	}
	
	@Override
	public String toString()
	{
		return getName();
	}

}
