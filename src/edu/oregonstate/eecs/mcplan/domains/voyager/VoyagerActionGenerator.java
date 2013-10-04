/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.UndoableAction;

/**
 * @author jhostetler
 *
 */
public class VoyagerActionGenerator extends ActionGenerator<VoyagerState, UndoableAction<VoyagerState>>
{
	private Player player_ = null;
	private List<UndoableAction<VoyagerState>> actions_ = new ArrayList<UndoableAction<VoyagerState>>();
	private ListIterator<UndoableAction<VoyagerState>> itr_ = null;
	
	@Override
	public boolean hasNext()
	{
		return itr_.hasNext();
	}

	@Override
	public UndoableAction<VoyagerState> next()
	{
		return itr_.next();
	}

	@Override
	public VoyagerActionGenerator create()
	{
		return new VoyagerActionGenerator();
	}
	
	private void addPlanetActions( final VoyagerState state, final Planet p )
	{
		for( int w = 0; w <= p.population( EntityType.Worker ); ++w ) {
			for( int s = 0; s <= p.population( EntityType.Soldier ); ++s ) {
				if( w + s > 0 ) {
					for( final Planet dest : state.planets ) {
						if( !dest.equals( p ) ) {
							actions_.add( new LaunchAction( p, dest, new int[] { w, s } ) );
						}
					}
				}
			}
		}
		if( p.nextProduced() == EntityType.Worker ) {
			actions_.add( new SetProductionAction( p, EntityType.Soldier ) );
		}
		else {
			actions_.add( new SetProductionAction( p, EntityType.Worker ) );
		}
	}

	@Override
	public void setState( final VoyagerState s, final long t, final int turn )
	{
		player_ = Player.values()[turn];
		actions_ = new ArrayList<UndoableAction<VoyagerState>>();
		for( final Planet p : s.planets ) {
			if( p.owner() == player_ ) {
				addPlanetActions( s, p );
			}
		}
		actions_.add( new NothingAction() );
		itr_ = actions_.listIterator();
	}

	@Override
	public int size()
	{
		return actions_.size();
	}
}
