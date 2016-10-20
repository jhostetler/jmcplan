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
package edu.oregonstate.eecs.mcplan.domains.taxi;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;

/**
 * @author jhostetler
 *
 */
public class PrimitiveTaxiRepresentation extends FactoredRepresentation<TaxiState>
{
	private final float[] phi_;
	
	public PrimitiveTaxiRepresentation( final TaxiState s )
	{
		// Taxi location, other taxi locations, passenger location indicators +
		// destination indicators.
		final int Nfeatures = 2 + 2*s.Nother_taxis + (2*s.locations.size() + 1);
		phi_ = new float[Nfeatures];
		int idx = 0;
		phi_[idx++] = s.taxi[0];
		phi_[idx++] = s.taxi[1];
		for( final int[] other : s.other_taxis ) {
			phi_[idx++] = other[0];
			phi_[idx++] = other[1];
		}
		phi_[idx + s.passenger + 1] = 1.0f;
		idx += s.locations.size() + 1;
		phi_[idx + s.destination] = 1.0f;
		idx += s.locations.size();
		
		assert( idx == Nfeatures );
	}
	
	private PrimitiveTaxiRepresentation( final PrimitiveTaxiRepresentation that )
	{
		this.phi_ = that.phi_;
	}
	
	@Override
	public float[] phi()
	{
		return phi_;
	}

	@Override
	public PrimitiveTaxiRepresentation copy()
	{
		return new PrimitiveTaxiRepresentation( this );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof PrimitiveTaxiRepresentation) ) {
			return false;
		}
		final PrimitiveTaxiRepresentation that = (PrimitiveTaxiRepresentation) obj;
		return Arrays.equals( phi_, that.phi_ );
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode( phi_ );
	}
}
