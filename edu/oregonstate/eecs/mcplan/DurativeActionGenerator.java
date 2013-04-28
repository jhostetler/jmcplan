/**
 * 
 */
package edu.oregonstate.eecs.mcplan;



/**
 * Adapts an ActionGenerator to generate DurativeUndoableActions.
 */
public class DurativeActionGenerator<S, A extends UndoableAction<S, A>>
	implements ActionGenerator<S, DurativeUndoableAction<S, A>>
{
	private final ActionGenerator<S, A> base_;
	private final int epoch_;
	
	/**
	 * 
	 */
	public DurativeActionGenerator( final ActionGenerator<S, A> base, final int epoch )
	{
		base_ = base;
		epoch_ = epoch;
	}

	@Override
	public boolean hasNext()
	{
		return base_.hasNext();
	}

	@Override
	public DurativeUndoableAction<S, A> next()
	{
		return new DurativeUndoableAction<S, A>( new RepeatPolicy<S, A>( base_.next() ), epoch_ );
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setState( final S s, final long t, final int turn )
	{
		base_.setState( s, t, turn );
	}

	@Override
	public ActionGenerator<S, DurativeUndoableAction<S, A>> create()
	{
		return new DurativeActionGenerator<S, A>( base_.create(), epoch_ );
	}

	@Override
	public int size()
	{
		return base_.size();
	}

}
