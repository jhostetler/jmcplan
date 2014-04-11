/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import edu.oregonstate.eecs.mcplan.ActionGenerator;

/**
 * @author jhostetler
 *
 */
public class VoyagerActionGenerator extends ActionGenerator<VoyagerState, VoyagerAction>
{
	private Player player_ = null;
	private List<VoyagerAction> actions_ = new ArrayList<VoyagerAction>();
	private ListIterator<VoyagerAction> itr_ = null;
	
	@Override
	public boolean hasNext()
	{
		return itr_.hasNext();
	}

	@Override
	public VoyagerAction next()
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
		for( int w = 0; w <= p.population( player_, Unit.Worker ); ++w ) {
			for( int s = 0; s <= p.population( player_, Unit.Soldier ); ++s ) {
				if( w + s > 0 ) {
					for( final Planet dest : state.planets ) {
						if( !dest.equals( p ) && state.adjacent( p, dest ) ) {
							actions_.add( new LaunchAction( player_, p, dest, new int[] { w, s } ) );
						}
					}
				}
			}
		}
		for( final Unit u : Unit.values() ) {
			if( p.nextProduced() != u ) {
				actions_.add( new SetProductionAction( p, u ) );
			}
		}
	}

	@Override
	public void setState( final VoyagerState s, final long t, final int[] turn )
	{
		assert( turn.length == 1 );
		player_ = Player.values()[turn[0]];
		actions_ = new ArrayList<VoyagerAction>();
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
