package edu.oregonstate.eecs.mcplan;

public class PolicyActionGenerator<S, A extends UndoableAction<S>> implements ActionGenerator<S, UndoableAction<S>>
{
	public static <S, A extends UndoableAction<S>>
	PolicyActionGenerator<S, A> create( final ActionGenerator<S, ? extends Policy<S, A>> base )
	{
		return new PolicyActionGenerator<S, A>( base );
	}
	
	private final ActionGenerator<S, ? extends Policy<S, A>> base_;
	
	private S s_ = null;
	private long t_ = 0L;
	
	public PolicyActionGenerator( final ActionGenerator<S, ? extends Policy<S, A>> base )
	{
		base_ = base;
	}
	
	@Override
	public boolean hasNext()
	{
		return base_.hasNext();
	}

	@Override
	public PolicyAction<S, A> next()
	{
		final Policy<S, A> next = base_.next();
		return new PolicyAction<S, A>( next, t_ );
	}

	@Override
	public PolicyActionGenerator<S, A> create()
	{
		return new PolicyActionGenerator<S, A>( base_.create() );
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setState( final S s, final long t, final int turn )
	{
		s_ = s;
		t_ = t;
		base_.setState( s, t, turn );
	}

	@Override
	public int size()
	{
		return base_.size();
	}

}
