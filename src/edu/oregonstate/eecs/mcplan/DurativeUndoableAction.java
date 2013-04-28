/**
 * 
 */
package edu.oregonstate.eecs.mcplan;


/**
 * @author jhostetler
 *
 */
public class DurativeUndoableAction<S, A extends UndoableAction<S, A>>
	implements UndoableAction<S, DurativeUndoableAction<S, A>>
{
	public final Policy<S, A> policy_;
	
	public final int T_;
	private boolean done_ = false;
	
	public DurativeUndoableAction( final Policy<S, A> policy, final int T )
	{
		policy_ = policy;
		T_ = T;
	}
	
	@Override
	public DurativeUndoableAction<S, A> create()
	{
		return new DurativeUndoableAction<S, A>( policy_, T_ );
	}
	
	@Override
	public void doAction( final S s )
	{
		assert( !done_ );
		done_ = !done_;
	}

	@Override
	public boolean isDone()
	{
		return done_;
	}

	@Override
	public void undoAction( final S s )
	{
		assert( done_ );
		done_ = !done_;
	}
	
	@Override
	public String toString()
	{
		return "" + T_ + "x" + policy_.toString();
	}
	
}
