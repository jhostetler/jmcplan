/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racetrack;

import java.util.ArrayList;
import java.util.Iterator;

import edu.oregonstate.eecs.mcplan.ActionGenerator;

/**
 * @author jhostetler
 *
 */
public class RacetrackActionGenerator extends ActionGenerator<RacetrackState, RacetrackAction>
{
	private RacetrackState s_ = null;
	private long t_ = 0L;
	
	private final ArrayList<RacetrackAction> actions_
		= new ArrayList<RacetrackAction>();
	private Iterator<RacetrackAction> itr_ = null;
	
	@Override
	public ActionGenerator<RacetrackState, RacetrackAction> create()
	{
		return new RacetrackActionGenerator();
	}

	@Override
	public void setState( final RacetrackState s, final long t, final int[] turn )
	{
		s_ = s;
		t_ = t;
		
		actions_.clear();
		for( int i = 0; i < 8; ++i ) {
			actions_.add( new AccelerateAction( s.adhesion_limit, s.car_theta + (i*Math.PI / 4) ) );
		}
		itr_ = actions_.iterator();
	}

	@Override
	public int size()
	{
		return actions_.size();
	}

	@Override
	public boolean hasNext()
	{
		return itr_.hasNext();
	}

	@Override
	public RacetrackAction next()
	{
		return itr_.next();
	}
	
}
