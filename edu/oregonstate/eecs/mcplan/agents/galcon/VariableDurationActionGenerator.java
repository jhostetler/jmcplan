/**
 * 
 */
package edu.oregonstate.eecs.mcplan.agents.galcon;

import edu.oregonstate.eecs.mcplan.search.DepthRecorder;

/**
 * @author jhostetler
 *
 */
public class VariableDurationActionGenerator<S, A extends UndoableAction<S, A>>
	implements ActionGenerator<S, DurativeUndoableAction<S, A>>
{
	private final ActionGenerator<S, A> base_;
	private final int[] epochs_;
	private final DepthRecorder depth_;
	
	/**
	 * @param base Primitive action generator.
	 * @param epochs List of epochs.
	 * @param depth An object that knows what depth the search is at.
	 */
	public VariableDurationActionGenerator( final ActionGenerator<S, A> base, final int[] epochs,
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
	public DurativeUndoableAction<S, A> next()
	{
		final int d = depth_.getDepth();
		return new DurativeUndoableAction<S, A>( new RepeatPolicy<S, A>( base_.next() ), epochs_[d] );
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setState( final S s )
	{
		base_.setState( s );
	}

	@Override
	public ActionGenerator<S, DurativeUndoableAction<S, A>> create()
	{
		return new VariableDurationActionGenerator<S, A>( base_.create(), epochs_, depth_ );
	}

	@Override
	public int size()
	{
		return base_.size();
	}

}
