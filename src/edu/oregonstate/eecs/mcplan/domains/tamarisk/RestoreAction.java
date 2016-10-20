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

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author jhostetler
 *
 */
public class RestoreAction extends TamariskAction
{
	public final int reach;
	private Species[] old_species_ = null;
	private double cost_ = 0.0;
	
	public RestoreAction( final int reach )
	{
		this.reach = reach;
	}
	
	@Override
	public double cost()
	{
		return cost_;
	}
	
	@Override
	public void undoAction( final TamariskState s )
	{
		assert( old_species_ != null );
		for( int i = 0; i < s.params.Nhabitats; ++i ) {
			s.habitats[reach][i] = old_species_[i];
		}
		old_species_ = null;
		cost_ = 0.0;
	}

	@Override
	public void doAction( final RandomGenerator rng, final TamariskState s )
	{
		assert( old_species_ == null );
		old_species_ = Arrays.copyOf( s.habitats[reach], s.params.Nhabitats );
		cost_ = s.params.restore_cost;
		for( int i = 0; i < s.params.Nhabitats; ++i ) {
			if( old_species_[i] == Species.None ) {
				cost_ += s.params.restore_cost_per_empty;
				final double r = s.rng.nextDouble();
				if( r < s.params.restore_rate ) {
					s.habitats[reach][i] = Species.Native;
				}
			}
		}
	}

	@Override
	public boolean isDone()
	{
		return old_species_ != null;
	}

	@Override
	public RestoreAction create()
	{
		return new RestoreAction( reach );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof RestoreAction) ) {
			return false;
		}
		final RestoreAction that = (RestoreAction) obj;
		return reach == that.reach;
	}

	@Override
	public int hashCode()
	{
		return 19 + 37 * reach;
	}

	@Override
	public String toString()
	{
		return "RestoreAction[" + reach + "]";
	}

}
