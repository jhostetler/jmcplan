/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.planetwars;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author jhostetler
 *
 */
public class PwSetProducingAction extends PwEvent
{
	private final PwPlanet planet;
	private final PwUnit unit;
	
	private PwUnit old_unit = null;
	private boolean done = false;
	
	public PwSetProducingAction( final PwPlanet planet, final PwUnit unit )
	{
		this.planet = planet;
		this.unit = unit;
	}

	@Override
	public void undoAction( final PwState s )
	{
		assert( done );
		planet.setProduction( old_unit );
		done = false;
	}
	
	@Override
	public void doAction( final PwState s )
	{
		assert( !done );
		old_unit = planet.nextProduced();
		planet.setProduction( unit );
		done = true;
	}

	@Override
	public boolean isDone()
	{
		return done;
	}

	@Override
	public PwSetProducingAction create()
	{
		return new PwSetProducingAction( planet, unit );
	}
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder( 5, 11 )
			.append( planet ).append( unit ).toHashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof PwSetProducingAction) ) {
			return false;
		}
		final PwSetProducingAction that = (PwSetProducingAction) obj;
		return planet.equals( that.planet ) && unit == that.unit;
	}
	
	@Override
	public String toString()
	{
		return "PwSetProducingAction[" + planet + "; " + unit + "]";
	}
}
