/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.oregonstate.eecs.mcplan.util.F;

/**
 * @author jhostetler
 *
 */
public class LaunchAction extends VoyagerEvent
{
	public static final Logger log = LoggerFactory.getLogger( LaunchAction.class );
	
	public final Planet src;
	public final Planet dest;
	public final int[] population;
	
	private Player old_owner_ = null;
	private List<EntityType> old_production_ = null;
	private int[] old_stored_production_ = null;
	private Spaceship spaceship_ = null;
	private boolean done_ = false;
	private final String repr_;
	
	/**
	 * 
	 */
	public LaunchAction( final Planet src, final Planet dest, final int[] population )
	{
		this.src = src;
		this.dest = dest;
		this.population = population;
		assert( !src.equals( dest ) );
		assert( F.sum( population ) > 0 );
		assert( F.all( new F.Pred.GreaterEqInt( 0 ), population ) );
		for( int i = 0; i < population.length; ++i ) {
			assert( src.population()[i] >= population[i] );
		}
		repr_ = "LaunchAction[src = " + src + ", dest = " + dest + ", population = " + Arrays.toString( population ) + "]";
	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.agents.galcon.UndoableAction#undoAction(java.lang.Object)
	 */
	@Override
	public void undoAction( final VoyagerState s )
	{
		assert( done_ );
		log.debug( "undo {}", toString() );
		final Iterator<Spaceship> sitr = s.spaceships.iterator();
		while( sitr.hasNext() ) {
			final Spaceship sp = sitr.next();
			if( sp.id == spaceship_.id ) {
				sitr.remove();
				break;
			}
		}
		for( int i = 0; i < population.length; ++i ) {
			src.incrementPopulation( EntityType.values()[i], population[i] );
		}
		src.setOwner( old_owner_ );
		src.setProductionSchedule( old_production_ );
		src.setStoredProduction( old_stored_production_ );
		done_ = false;
	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.agents.galcon.Action#doAction(java.lang.Object)
	 */
	@Override
	public void doAction( final VoyagerState s )
	{
		assert( !done_ );
		log.debug( "do {}", toString() );
		old_owner_ = src.owner();
		old_production_ = src.productionSchedule();
		old_stored_production_ = Arrays.copyOf( src.storedProduction(), src.storedProduction().length );
		for( int i = 0; i < population.length; ++i ) {
			src.decrementPopulation( EntityType.values()[i], population[i] );
		}
		assert( src.totalPopulation() >= 0 );
		spaceship_ = new Spaceship.Builder()
			.dest( dest ).owner( src.owner() ).population( population )
			.src( src ).x( src.x ).y( src.y ).finish();
		if( src.totalPopulation() == 0 ) {
			src.setOwner( Player.Neutral );
			src.setProductionSchedule( new ArrayList<EntityType>() );
			src.setStoredProduction( new int[EntityType.values().length] );
		}
		s.spaceships.add( spaceship_ );
		done_ = true;
	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.agents.galcon.Action#isDone()
	 */
	@Override
	public boolean isDone()
	{
		return done_;
	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.agents.galcon.Action#create()
	 */
	@Override
	public VoyagerEvent create()
	{
		return new LaunchAction( src, dest, population );
	}
	
	@Override
	public String toString()
	{
		return repr_;
	}

}
