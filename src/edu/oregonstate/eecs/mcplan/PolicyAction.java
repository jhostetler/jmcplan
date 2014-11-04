/**
 * 
 */
package edu.oregonstate.eecs.mcplan;


/**
 * @author jhostetler
 *
 */
public class PolicyAction<S, A extends UndoableAction<S>>
	implements UndoableAction<S>, VirtualConstructor<PolicyAction<S, A>>
{
	private final Policy<S, A> pi_;
	private final long t_;
	private A a_ = null;
	private boolean done_ = false;
	
	/**
	 * FIXME: Providing 't' in the constructor only works if we always execute
	 * the PolicyAction in the same state that we created it.
	 * @param pi
	 * @param t
	 */
	public PolicyAction( final Policy<S, A> pi, final long t )
	{
		pi_ = pi;
		t_ = t;
	}
	
	@Override
	public void doAction( final S s )
	{
		assert( !done_ );
		pi_.setState( s, t_ );
		a_ = pi_.getAction();
		a_.doAction( s );
		done_ = true;
	}

	@Override
	public boolean isDone()
	{
		return done_;
	}

	@Override
	public PolicyAction<S, A> create()
	{
		return new PolicyAction<S, A>( pi_, t_ );
	}

	@Override
	public void undoAction( final S s )
	{
		assert( done_ );
		a_.undoAction( s );
		done_ = false;
	}
	
	@Override
	public int hashCode()
	{
		return pi_.hashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof PolicyAction<?, ?>) ) {
			return false;
		}
		@SuppressWarnings( "unchecked" )
		final PolicyAction<S, A> that = (PolicyAction<S, A>) obj;
		return pi_.equals( that.pi_ );
	}
	
	@Override
	public String toString()
	{
		return "PolicyAction[" + pi_.toString() + "]";
	}
}
