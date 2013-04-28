/**
 * 
 */
package edu.oregonstate.eecs.mcplan;


/**
 * @author jhostetler
 *
 */
public class RepeatPolicy<S, A extends UndoableAction<S, A>> implements AnytimePolicy<S, A>
{
	private final UndoableAction<S, A> a_;
	
	public static <S, A extends UndoableAction<S, A>> RepeatPolicy<S, A> create( final A a )
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
	public void setState( final S s, long t )
	{ }

	@Override
	public A getAction()
	{
		return a_.create();
	}

	@Override
	public void actionResult( final A a, final S sprime, final double r )
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
	public A getAction( final long control )
	{
		return getAction();
	}
	
	@Override
	public String toString()
	{
		return getName();
	}

}
