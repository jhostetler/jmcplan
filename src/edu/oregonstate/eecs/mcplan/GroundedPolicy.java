package edu.oregonstate.eecs.mcplan;

public class GroundedPolicy<S, A> implements AnytimePolicy<S>
{
	public static <S, A> GroundedPolicy<S, A> create( final AnytimePolicy<S> lifted )
	{
		return new GroundedPolicy<S, A>( lifted );
	}
	
	private final AnytimePolicy<S> lifted_;
	private AnytimePolicy<S> choice_ = null;
	
	public GroundedPolicy( final AnytimePolicy<S> lifted )
	{
		lifted_ = lifted;
	}
	
	@Override
	public void setState( final S s, final long t )
	{
		lifted_.setState( s, t );
	}

	@Override
	public UndoableAction<S> getAction()
	{
		choice_ = lifted_.getAction();
		return choice_.getAction();
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		choice_.actionResult( sprime, r );
	}

	@Override
	public String getName()
	{
		return "GroundedPolicy[" + lifted_.getName() + "]";
	}

	@Override
	public long minControl()
	{
		return lifted_.minControl();
	}

	@Override
	public long maxControl()
	{
		return lifted_.maxControl();
	}

	@Override
	public A getAction( final long control )
	{
		// FIXME: We're giving it 2x the control here.
		choice_ = lifted_.getAction( control );
		return choice_.getAction( control );
	}

}
