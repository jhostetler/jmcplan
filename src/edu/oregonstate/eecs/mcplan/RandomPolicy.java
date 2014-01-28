package edu.oregonstate.eecs.mcplan;

import org.apache.commons.math3.random.MersenneTwister;


public class RandomPolicy<S, A> extends AnytimePolicy<S, A>
{
	public static <S, A>
	RandomPolicy<S, A> create( final int turn, final long seed, final ActionGenerator<S, A> action_gen )
	{
		return new RandomPolicy<S, A>( turn, seed, action_gen );
	}
	
	private final int turn_;
	private final ActionGenerator<S, ? extends A> action_gen_;
	private final MersenneTwister rng_;
	
	public RandomPolicy( final int turn, final long seed, final ActionGenerator<S, ? extends A> action_gen )
	{
		turn_ = turn;
		action_gen_ = action_gen;
		rng_ = new MersenneTwister( seed );
	}
	
	@Override
	public int hashCode()
	{
		return 71 * action_gen_.hashCode();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof RandomPolicy<?, ?>) ) {
			return false;
		}
		@SuppressWarnings( "unchecked" )
		final RandomPolicy<S, A> that = (RandomPolicy<S, A>) obj;
		return action_gen_.equals( that.action_gen_ );
	}
	
	@Override
	public void setState( final S s, final long t )
	{
		action_gen_.setState( s, t, new int[] { turn_ } );
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
	public void actionResult( final S sprime, final double[] r )
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
