/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racetrack;


/**
 * @author jhostetler
 *
 */
public class AccelerateAction extends RacetrackAction
{
	private boolean done_ = false;
	
	public final double ddx;
	public final double ddy;
	
	public static AccelerateAction fromPolar( final double theta, final double r )
	{
		return new AccelerateAction( r*Math.cos( theta ), r*Math.sin( theta ) );
	}
	
	public AccelerateAction( final double ddx, final double ddy )
	{
		this.ddx = ddx;
		this.ddy = ddy;
	}
	
	@Override
	public void undoAction( final RacetrackState s )
	{
		assert( done_ );
		
		s.car_ddx = 0;
		s.car_ddy = 0;
		
		done_ = false;
	}

	@Override
	public void doAction( final RacetrackState s )
	{
		assert( !done_ );

		s.car_ddx = ddx;
		s.car_ddy = ddy;
		
		done_ = true;
	}

	@Override
	public boolean isDone()
	{
		return done_;
	}

	@Override
	public AccelerateAction create()
	{
		return new AccelerateAction( ddx, ddy );
	}
	
	@Override
	public int hashCode()
	{
		final long bits = (41 + 37 * Double.doubleToLongBits( ddx )) * 43 + Double.doubleToLongBits( ddy );
		return (int) (bits ^ (bits >>> 32));
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof AccelerateAction) ) {
			return false;
		}
		final AccelerateAction that = (AccelerateAction) obj;
		return ddx == that.ddx && ddy == that.ddy;
	}
	
	@Override
	public String toString()
	{
		return "Accelerate[" + ddx + ", " + ddy + "]";
	}

}
