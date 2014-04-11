/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racetrack;

import java.util.ArrayList;
import java.util.Iterator;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.JointAction;

/**
 * @author jhostetler
 *
 */
public class RacetrackActionGenerator extends ActionGenerator<RacetrackState, JointAction<RacetrackAction>>
{
	private RacetrackState s_ = null;
	private long t_ = 0L;
	
	private final ArrayList<JointAction<RacetrackAction>> actions_
		= new ArrayList<JointAction<RacetrackAction>>();
	private Iterator<JointAction<RacetrackAction>> itr_ = null;
	
	@Override
	public ActionGenerator<RacetrackState, JointAction<RacetrackAction>> create()
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
			actions_.add( new JointAction<RacetrackAction>(
				AccelerateAction.fromPolar( ((double) i)*Math.PI / 4, s.adhesion_limit ) ) );
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
	public JointAction<RacetrackAction> next()
	{
		return itr_.next();
	}
	
}
