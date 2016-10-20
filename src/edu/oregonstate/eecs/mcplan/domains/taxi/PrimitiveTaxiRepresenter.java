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

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public class PrimitiveTaxiRepresenter implements FactoredRepresenter<TaxiState, PrimitiveTaxiRepresentation>
{
	private final ArrayList<Attribute> attributes_;

	public PrimitiveTaxiRepresenter( final TaxiState s )
	{
		this( s.Nother_taxis, s.locations.size() );
	}
	
	public PrimitiveTaxiRepresenter( final int Nother_taxis, final int Nlocations )
	{
		attributes_ = new ArrayList<Attribute>();
		attributes_.add( new Attribute( "taxi_x" ) );
		attributes_.add( new Attribute( "taxi_y" ) );
		for( int i = 0; i < Nother_taxis; ++i ) {
			attributes_.add( new Attribute( "other" + i + "_x" ) );
			attributes_.add( new Attribute( "other" + i + "_y" ) );
		}
		attributes_.add( new Attribute( "passenger_taxi" ) );
		for( int i = 0; i < Nlocations; ++i ) {
			attributes_.add( new Attribute( "passenger_" + i ) );
		}
		for( int i = 0; i < Nlocations; ++i ) {
			attributes_.add( new Attribute( "dest_" + i ) );
		}
	}
	
	private PrimitiveTaxiRepresenter( final PrimitiveTaxiRepresenter that )
	{
		this.attributes_ = that.attributes_;
	}
	
	@Override
	public FactoredRepresenter<TaxiState, PrimitiveTaxiRepresentation> create()
	{
		return new PrimitiveTaxiRepresenter( this );
	}

	@Override
	public PrimitiveTaxiRepresentation encode( final TaxiState s )
	{
		return new PrimitiveTaxiRepresentation( s );
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes_;
	}

}
