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
package edu.oregonstate.eecs.mcplan.domains.frogger;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public class RelativeFroggerRepresenter implements FactoredRepresenter<FroggerState, FactoredRepresentation<FroggerState>>
{
	private final ArrayList<Attribute> attributes_;
	private final int vision_;

	public RelativeFroggerRepresenter( final FroggerParameters params, final int vision )
	{
		vision_ = vision;
		attributes_ = new ArrayList<Attribute>();
		attributes_.add( new Attribute( "x" ) );
		attributes_.add( new Attribute( "y" ) );
		for( int i = vision; i >= -vision; --i ) {
			for( int j = -vision; j <= vision; ++j ) {
				if( i == 0 && j == 0 ) {
					continue;
				}
				attributes_.add( new Attribute(
					"car_x" + (j >= 0 ? "+" : "") + j + "_y" + (i >= 0 ? "+" : "") + i ) );
			}
		}
	}
	
	private RelativeFroggerRepresenter( final RelativeFroggerRepresenter that )
	{
		attributes_ = that.attributes_;
		vision_ = that.vision_;
	}
	
	@Override
	public RelativeFroggerRepresenter create()
	{
		return new RelativeFroggerRepresenter( this );
	}

	@Override
	public RelativeFroggerRepresentation encode( final FroggerState s )
	{
		return new RelativeFroggerRepresentation( s, vision_ );
	}
	
	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes_;
	}
	
	@Override
	public String toString()
	{
		return "RelativeFroggerRepresenter";
	}
}
