package edu.oregonstate.eecs.mcplan;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.util.Fn;


public class RandomPolicy<S, A> extends AnytimePolicy<S, A>
{
	public static <S, A>
	RandomPolicy<S, A> create( final RandomGenerator rng, final ActionGenerator<S, A> action_gen )
	{
		return new RandomPolicy<S, A>( rng, action_gen );
	}
	
	private final ActionGenerator<S, ? extends A> action_gen_;
	private final RandomGenerator rng_;
	
	public RandomPolicy( final RandomGenerator rng, final ActionGenerator<S, ? extends A> action_gen )
	{
		action_gen_ = action_gen;
		rng_ = rng;
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
		action_gen_.setState( s, t );
	}

	@Override
	public A getAction()
	{
		final A a = Fn.uniform_choice( rng_, action_gen_ );
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
	public String toString()
	{
		return getName();
	}

	@Override
	public boolean improvePolicy()
	{
		return false;
	}

}
