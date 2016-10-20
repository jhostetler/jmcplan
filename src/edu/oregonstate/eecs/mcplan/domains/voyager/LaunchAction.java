/* LICENSE
Copyright (c) 2013-2016, Jesse Hostetler (jessehostetler@gmail.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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
	
	public final Player player;
	public final Planet src;
	public final Planet dest;
	public final int[] population;
	
	private Player old_owner_ = null;
	private int[] old_carry_damage_ = null;
	private Unit old_production_ = null;
	private int[] old_stored_production_ = null;
	private Spaceship spaceship_ = null;
	private boolean done_ = false;
	private final String repr_;
	
	/**
	 * 
	 */
	public LaunchAction( final Player player, final Planet src, final Planet dest, final int[] population )
	{
		this.player = player;
		this.src = src;
		this.dest = dest;
		this.population = population;
		assert( !src.equals( dest ) );
		assert( Fn.sum( population ) > 0 );
		assert( Fn.all( Fn.Pred.GreaterEq( 0 ), population ) );
		for( int i = 0; i < population.length; ++i ) {
			assert( src.population( player )[i] >= population[i] );
		}
		repr_ = "LaunchAction[y = " + player.id + ", src = " + src + ", dest = " + dest + ", pop = " + Arrays.toString( population ) + "]";
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
			src.incrementPopulation( player, Unit.values()[i], population[i] );
		}
		src.setOwner( old_owner_ );
		for( final Player y : Player.competitors ) {
			src.setCarryDamage( y, old_carry_damage_[y.id] );
		}
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
		old_carry_damage_ = Arrays.copyOf( src.carryDamage(), src.carryDamage().length );
		old_production_ = src.nextProduced();
		old_stored_production_ = Arrays.copyOf( src.storedProduction(), src.storedProduction().length );
		for( int i = 0; i < population.length; ++i ) {
			src.decrementPopulation( player, Unit.values()[i], population[i] );
		}
		assert( src.totalPopulation( player ) >= 0 );
		spaceship_ = new Spaceship.Builder()
			.owner( src.owner() ).src( src ).x( src.x ).y( src.y )
			.dest( dest ).population( population )
			.hash( s.hash )
			.finish( s.spaceship_factory );
		if( src.totalPopulation( player ) == 0 ) {
			// TODO: clearCarryDamage() here?
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
		return new LaunchAction( player, src, dest, population );
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
