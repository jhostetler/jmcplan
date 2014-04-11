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

}
