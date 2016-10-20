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
package edu.oregonstate.eecs.mcplan.domains.sailing;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.ArrayFactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public class SailingGroundRepresenter implements FactoredRepresenter<SailingState, FactoredRepresentation<SailingState>>
{
	private static ArrayList<Attribute> attributes;
	static {
		attributes = new ArrayList<Attribute>();
		attributes.add( new Attribute( "x" ) );
		attributes.add( new Attribute( "y" ) );
		attributes.add( new Attribute( "w" ) );
		attributes.add( new Attribute( "v" ) );
	}
	
	@Override
	public FactoredRepresenter<SailingState, FactoredRepresentation<SailingState>> create()
	{
		return new SailingGroundRepresenter();
	}

	@Override
	public FactoredRepresentation<SailingState> encode( final SailingState s )
	{
		final float[] x = new float[4];
		x[0] = s.x;
		x[1] = s.y;
		x[2] = s.w;
		x[3] = s.v;
		return new ArrayFactoredRepresentation<SailingState>( x );
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes;
	}
}
