/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.planetwars.a;

import java.util.ArrayList;
import java.util.Iterator;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.domains.planetwars.PwEvent;
import edu.oregonstate.eecs.mcplan.domains.planetwars.PwLaunchAction;
import edu.oregonstate.eecs.mcplan.domains.planetwars.PwNothingAction;
import edu.oregonstate.eecs.mcplan.domains.planetwars.PwPlanet;
import edu.oregonstate.eecs.mcplan.domains.planetwars.PwPlayer;
import edu.oregonstate.eecs.mcplan.domains.planetwars.PwRoute;
import edu.oregonstate.eecs.mcplan.domains.planetwars.PwState;
import edu.oregonstate.eecs.mcplan.domains.planetwars.PwUnit;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.PreferredNumbers;

/**
 * @author jhostetler
 *
 */
public class PwA_ActionGenerator extends ActionGenerator<PwState, JointAction<PwEvent>>
{
	private ArrayList<JointAction<PwEvent>> actions = null;
	private Iterator<JointAction<PwEvent>> itr = null;
	
	@Override
	public PwA_ActionGenerator create()
	{
		return new PwA_ActionGenerator();
	}

	@Override
	public void setState( final PwState s, final long t )
	{
		assert( s.game.Nunits() == 1 );
		final PwUnit u = s.game.unit( 0 );
		
		final ArrayList<PwEvent> min_actions = new ArrayList<PwEvent>();
		final ArrayList<PwEvent> max_actions = new ArrayList<PwEvent>();
		
		min_actions.add( new PwNothingAction() );
		max_actions.add( new PwNothingAction() );
		
		for( final PwPlanet planet : s.planets ) {
			final PwPlayer player = planet.owner();
			if( player == PwPlayer.Neutral ) {
				continue;
			}
			final ArrayList<PwEvent> pa = (player == PwPlayer.Min ? min_actions : max_actions);
			final Fn.IntSlice ns = PreferredNumbers.Series_1_2_5();
			while( true ) {
				final int n = ns.next();
				if( n <= planet.population( player, u ) ) {
					for( final PwRoute route : s.routes( planet ) ) {
						pa.add( new PwLaunchAction( player, planet, route, new int[] { n } ) );
					}
				}
				else {
					break;
				}
			}
		}
		
		actions = new ArrayList<JointAction<PwEvent>>( min_actions.size() * max_actions.size() );
		
		for( final PwEvent min_a : min_actions ) {
			for( final PwEvent max_a : max_actions ) {
				actions.add( new JointAction<PwEvent>( min_a.create(), max_a.create() ) );
			}
		}
		
		itr = actions.iterator();
	}

	@Override
	public int size()
	{
		return actions.size();
	}

	@Override
	public boolean hasNext()
	{
		return itr.hasNext();
	}

	@Override
	public JointAction<PwEvent> next()
	{
		return itr.next();
	}
}
