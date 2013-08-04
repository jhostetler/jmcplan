/**
 * 
 */
package edu.oregonstate.eecs.mcplan;


/**
 * @author jhostetler
 *
 */
public class RepeatPolicy<S, A extends UndoableAction<S>> implements AnytimePolicy<S>
{
	private final UndoableAction<S> a_;
	
	public static <S, A extends UndoableAction<S>> RepeatPolicy<S, A> create( final A a )
	{
		return new RepeatPolicy<S, A>( a );
	}
	
	/**
	 * 
	 */
	public RepeatPolicy( final A a )
	{
		a_ = a;
	}

	@Override
	public void setState( final S s, final long t )
	{ }

	@Override
	public UndoableAction<S> getAction()
	{
		return a_.create();
	}

	@Override
	public void actionResult( final UndoableAction<S> a, final S sprime, final double r )
	{ }

	@Override
	public String getName()
	{
		return "RepeatPolicy[" + a_.toString() + "]";
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
	public UndoableAction<S> getAction( final long control )
	{
		return getAction();
	}
	
	@Override
	public String toString()
	{
		return getName();
	}

}
