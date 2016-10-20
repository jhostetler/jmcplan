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
package edu.oregonstate.eecs.mcplan.domains.firegirl;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.ArrayFactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * The feature representation used in the FireGirl Python code, but with
 * the bias feature and 'd2' removed, since we're not doing regression.
 */
public class FireGirlLocalFeatureRepresenter implements FactoredRepresenter<FireGirlState, FactoredRepresentation<FireGirlState>>
{
	private static final ArrayList<Attribute> attributes;
	
	static {
		attributes = new ArrayList<Attribute>();
		attributes.add( new Attribute( "ignite_date" ) );
		attributes.add( new Attribute( "ignite_temp" ) );
		attributes.add( new Attribute( "ignite_wind" ) );
		attributes.add( new Attribute( "timber_val" ) );
		attributes.add( new Attribute( "timber_ave8" ) );
		attributes.add( new Attribute( "timber_ave24" ) );
		attributes.add( new Attribute( "fuel" ) );
		attributes.add( new Attribute( "fuel_ave8" ) );
		attributes.add( new Attribute( "fuel_ave24" ) );
	}
	
	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes;
	}

	@Override
	public FactoredRepresenter<FireGirlState, FactoredRepresentation<FireGirlState>> create()
	{
		return new FireGirlLocalFeatureRepresenter();
	}

	@Override
	public FactoredRepresentation<FireGirlState> encode( final FireGirlState s )
	{
		final int[] loc = s.ignite_loc;
		final int x = loc[0];
		final int y = loc[1];
		
		final float[] phi = new float[attributes.size()];
		int idx = 0;
		phi[idx++] = s.ignite_date;
		phi[idx++] = s.ignite_temp;
		phi[idx++] = s.ignite_wind;
		phi[idx++] = (float) s.getPresentTimberValue( x, y );
		phi[idx++] = (float) s.getValueAverage( x, y, 1 ); //timber_ave8;
		phi[idx++] = (float) s.getValueAverage( x, y, 2 ); //timber_ave24;
		phi[idx++] = s.fuel_load[x][y];
		phi[idx++] = (float) s.getFuelAverage( x, y, 1 ); //fire_ave8;
		phi[idx++] = (float) s.getFuelAverage( x, y, 2 ); //fire_ave24;
		
		return new ArrayFactoredRepresentation<FireGirlState>( phi );
	}
}
