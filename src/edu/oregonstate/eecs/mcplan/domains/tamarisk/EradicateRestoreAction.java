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
package edu.oregonstate.eecs.mcplan.domains.tamarisk;

import org.apache.commons.math3.random.RandomGenerator;


/**
 * @author jhostetler
 *
 */
public class EradicateRestoreAction extends TamariskAction
{
	public final int reach;
	private final EradicateAction eradicate_;
	private final RestoreAction restore_;
	
	public EradicateRestoreAction( final int reach )
	{
		this.reach = reach;
		eradicate_ = new EradicateAction( reach );
		restore_ = new RestoreAction( reach );
	}
	
	@Override
	public double cost()
	{
		return eradicate_.cost() + restore_.cost();
	}
	
	@Override
	public void undoAction( final TamariskState s )
	{
		restore_.undoAction( s );
		eradicate_.undoAction( s );
	}

	@Override
	public void doAction( final RandomGenerator rng, final TamariskState s )
	{
		eradicate_.doAction( s );
		restore_.doAction( s );
	}

	@Override
	public boolean isDone()
	{
		return eradicate_.isDone() && restore_.isDone();
	}

	@Override
	public EradicateRestoreAction create()
	{
		return new EradicateRestoreAction( reach );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof EradicateRestoreAction) ) {
			return false;
		}
		final EradicateRestoreAction that = (EradicateRestoreAction) obj;
		return eradicate_.equals( that.eradicate_ ) && restore_.equals( that.restore_ );
	}

	@Override
	public int hashCode()
	{
		return 61 + 67 * (eradicate_.hashCode() + 71 * (restore_.hashCode()));
	}

	@Override
	public String toString()
	{
		return "EradicateRestoreAction[" + reach + "]";
	}
}
