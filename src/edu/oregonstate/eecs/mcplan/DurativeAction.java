/**
 * 
 */
package edu.oregonstate.eecs.mcplan;


/**
 * @author jhostetler
 *
 */
public class DurativeAction<S, A> extends Option<S, A>
{
	public final int T;
	private long t = 0;
	
	private final String str_;
	
	public DurativeAction( final Policy<S, A> pi, final int T )
	{
		super( pi );
		this.T = T;
		assert( T > 0 );
		str_ = "DurativeAction(" + T + ")[" + pi.getName() + "]";
	}

	@Override
	public void start( final S s, final long t )
	{
		this.t = t;
//		System.out.println( "Option " + pi.getName() + " started" );
	}

	@Override
	public double terminate( final S s, final long t )
	{
//		System.out.println( "Terminate check " + (t - this.t) );
		return ((t - this.t) >= T ? 1.0 : 0.0);
	}

	@Override
	public Option<S, A> create()
	{
		return new DurativeAction<S, A>( pi, T );
	}

	@Override
	public void setState( final S s, final long t )
	{
		pi.setState( s, t );
	}

	@Override
	public A getAction()
	{
//		t += 1;
		return pi.getAction();
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		// TODO: Accumulate option reward?
		pi.actionResult( sprime, r );
	}

	@Override
	public String getName()
	{
		return str_;
	}
	
	@Override
	public String toString()
	{
		return getName();
	}
	
	@Override
	public int hashCode()
	{
		final int k = 109;
		int h = 101;
		h = h*k + pi.hashCode();
//		h = h*k + T;
		return h;
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof DurativeAction<?, ?>) ) {
			return false;
		}
		@SuppressWarnings( "unchecked" )
		final DurativeAction<S, A> that = (DurativeAction<S, A>) obj;
//		return T == that.t && pi.equals( that.pi );
		return pi.equals( that.pi );
	}
}
