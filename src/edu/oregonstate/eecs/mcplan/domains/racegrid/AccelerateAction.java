/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racegrid;


/**
 * @author jhostetler
 *
 */
public class AccelerateAction extends RacegridAction
{
	public final int ddx;
	public final int ddy;
	
	private final boolean old_crashed_ = false;
	private boolean done_ = false;
	
	public AccelerateAction( final int ddx, final int ddy )
	{
		this.ddx = ddx;
		this.ddy = ddy;
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof AccelerateAction) ) {
			return false;
		}
		final AccelerateAction that = (AccelerateAction) obj;
		return ddx == that.ddx && ddy == that.ddy;
	}
	
	@Override
	public int hashCode()
	{
		return 13 + 17 * (ddx + 19 * ddy);
	}
	
	@Override
	public AccelerateAction create()
	{
		return new AccelerateAction( ddx, ddy );
	}

	@Override
	public void doAction( final RacegridState s )
	{
		assert( !done_ );
		s.ddx = ddx;
		s.ddy = ddy;
		s.crashed = false;
		done_ = true;
	}

	@Override
	public boolean isDone()
	{
		return done_;
	}

	@Override
	public void undoAction( final RacegridState s )
	{
		assert( done_ );
		s.ddx = 0;
		s.ddy = 0;
		s.crashed = old_crashed_;
	}
	
	@Override
	public String toString()
	{
		return "AccelerateAction(" + ddx + "; " + ddy + ")";
	}
}
