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
package edu.oregonstate.eecs.mcplan.domains.planetwars;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.math3.random.RandomGenerator;

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
	public void doAction( final RandomGenerator rng, final PwState s )
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
