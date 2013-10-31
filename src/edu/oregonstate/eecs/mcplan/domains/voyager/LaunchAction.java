/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class LaunchAction extends VoyagerAction
{
	public static final Logger log = LoggerFactory.getLogger( LaunchAction.class );
	
	public final Planet src;
	public final Planet dest;
	public final int[] population;
	
	private Player old_owner_ = null;
	private Unit old_production_ = null;
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
		assert( Fn.sum( population ) > 0 );
		assert( Fn.all( Fn.Pred.GreaterEq( 0 ), population ) );
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
			src.incrementPopulation( Unit.values()[i], population[i] );
		}
		src.setOwner( old_owner_ );
		src.setProduction( old_production_ );
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
		old_production_ = src.nextProduced();
		old_stored_production_ = Arrays.copyOf( src.storedProduction(), src.storedProduction().length );
		for( int i = 0; i < population.length; ++i ) {
			src.decrementPopulation( Unit.values()[i], population[i] );
		}
		assert( src.totalPopulation() >= 0 );
		spaceship_ = new Spaceship.Builder()
			.owner( src.owner() ).src( src ).x( src.x ).y( src.y )
			.dest( dest ).population( population )
			.hash( s.hash )
			.finish( s.spaceship_factory );
		if( src.totalPopulation() == 0 ) {
			src.setOwner( Player.Neutral );
			src.setProduction( Unit.defaultProduction() );
			src.setStoredProduction( Fn.repeat( 0, Unit.values().length ) );
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
	public LaunchAction create()
	{
//		throw new AssertionError();
		return new LaunchAction( src, dest, population );
	}
	
	@Override
	public String toString()
	{
		return repr_;
	}
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder( 17, 31 )
			.append( src ).append( dest ).append( population ).toHashCode();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof LaunchAction) ) {
			return false;
		}
		final LaunchAction that = (LaunchAction) obj;
		return src.equals( that.src )
			   && dest.equals( that.dest )
			   && Arrays.equals( population, that.population );
	}

}
