/**
 * 
 */
package edu.oregonstate.eecs.mcplan;



/**
 * Adapts an ActionGenerator to generate DurativeUndoableActions.
 */
public class DurativeActionGenerator<S, A>
	extends ActionGenerator<S, DurativeAction<S, A>>
{
	private final ActionGenerator<S, ? extends Policy<S, A>> base_;
	private final int epoch_;
	
	/**
	 * 
	 */
	public DurativeActionGenerator( final ActionGenerator<S, ? extends Policy<S, A>> base, final int epoch )
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
	public DurativeAction<S, A> next()
	{
		return new DurativeAction<S, A>( base_.next(), epoch_ );
	}

	@Override
	public void setState( final S s, final long t, final int turn )
	{
		base_.setState( s, t, turn );
	}

	@Override
	public ActionGenerator<S, DurativeAction<S, A>> create()
	{
		return new DurativeActionGenerator<S, A>( base_.create(), epoch_ );
	}

	@Override
	public int size()
	{
		return base_.size();
	}

}
