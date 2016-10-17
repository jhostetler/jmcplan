/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import edu.oregonstate.eecs.mcplan.search.DepthRecorder;

/**
 * @author jhostetler
 *
 */
public class VariableDurationActionGenerator<S, A extends UndoableAction<S>>
	extends ActionGenerator<S, DurativeAction<S, A>>
{
	private final ActionGenerator<S, AnytimePolicy<S, A>> base_;
	private final int[] epochs_;
	private final DepthRecorder depth_;
	
	/**
	 * @param base Primitive action generator.
	 * @param epochs List of epochs.
	 * @param depth An object that knows what depth the search is at.
	 */
	public VariableDurationActionGenerator( final ActionGenerator<S, AnytimePolicy<S, A>> base, final int[] epochs,
											final DepthRecorder depth )
	{
		base_ = base.create();
		epochs_ = epochs;
		depth_ = depth;
	}

	@Override
	public boolean hasNext()
	{
		return base_.hasNext();
	}

	@Override
	public DurativeAction<S, A> next()
	{
		final int d = depth_.getDepth();
		return new DurativeAction<S, A>( base_.next(), epochs_[d] );
	}

	@Override
	public void setState( final S s, final long t )
	{
		base_.setState( s, t );
	}

	@Override
	public ActionGenerator<S, DurativeAction<S, A>> create()
	{
		return new VariableDurationActionGenerator<S, A>( base_.create(), epochs_, depth_ );
	}

	@Override
	public int size()
	{
		return base_.size();
	}

}
