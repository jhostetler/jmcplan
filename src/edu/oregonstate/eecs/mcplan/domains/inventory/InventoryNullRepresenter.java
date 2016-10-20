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
package edu.oregonstate.eecs.mcplan.domains.inventory;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.ArrayFactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public class InventoryNullRepresenter implements FactoredRepresenter<InventoryState, FactoredRepresentation<InventoryState>>
{
	private final ArrayList<Attribute> attributes;
	
	public InventoryNullRepresenter( final InventoryProblem problem )
	{
		attributes = new ArrayList<Attribute>( 3*problem.Nproducts );
		for( int i = 0; i < problem.Nproducts; ++i ) {
			attributes.add( new Attribute( "i" + i ) );
		}
		for( int i = 0; i < problem.Nproducts; ++i ) {
			attributes.add( new Attribute( "o" + i ) );
		}
		for( int i = 0; i < problem.Nproducts; ++i ) {
			attributes.add( new Attribute( "d" + i ) );
		}
	}
	
	private InventoryNullRepresenter( final ArrayList<Attribute> attributes )
	{
		this.attributes = attributes;
	}
	
	@Override
	public FactoredRepresenter<InventoryState, FactoredRepresentation<InventoryState>> create()
	{
		return new InventoryNullRepresenter( attributes );
	}

	@Override
	public FactoredRepresentation<InventoryState> encode( final InventoryState s )
	{
		final float[] phi = new float[3*s.problem.Nproducts];
		int idx = 0;
		for( int i = 0; i < s.problem.Nproducts; ++i ) {
			phi[idx++] = s.inventory[i];
		}
		for( int i = 0; i < s.problem.Nproducts; ++i ) {
			phi[idx++] = s.orders[i];
		}
		for( int i = 0; i < s.problem.Nproducts; ++i ) {
			phi[idx++] = s.demand[i];
		}
		return new ArrayFactoredRepresentation<InventoryState>( phi );
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes;
	}
}
