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
package edu.oregonstate.eecs.mcplan.domains.ipc.tamarisk;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.ArrayFactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * Represents counts of Tamarisk and Native for each reach.
 */
public final class IpcTamariskReachRepresenter
	implements FactoredRepresenter<IpcTamariskState, FactoredRepresentation<IpcTamariskState>>
{
	private final IpcTamariskParameters params;
	private final ArrayList<Attribute> attributes = new ArrayList<Attribute>();

	public IpcTamariskReachRepresenter( final IpcTamariskParameters params )
	{
		this.params = params;
		for( int i = 0; i < params.Nreaches; ++i ) {
			final String r = "r" + i;
			attributes.add( new Attribute( r + "t" ) );
			attributes.add( new Attribute( r + "n" ) );
		}
	}
	
	@Override
	public FactoredRepresenter<IpcTamariskState, FactoredRepresentation<IpcTamariskState>> create()
	{
		return new IpcTamariskReachRepresenter( params );
	}

	@Override
	public FactoredRepresentation<IpcTamariskState> encode( final IpcTamariskState s )
	{
		final float[] x = new float[attributes.size()];
		int idx = 0;
		for( int i = 0; i < params.Nreaches; ++i ) {
			final byte[] r = s.reaches[i];
			int t = 0;
			int n = 0;
			for( int j = 0; j < r.length; ++j ) {
				if( (r[j] & IpcTamariskState.Tamarisk) != 0 ) {
					t += 1;
				}
				if( (r[j] & IpcTamariskState.Native) != 0 ) {
					n += 1;
				}
			}
			x[idx++] = t;
			x[idx++] = n;
		}
		assert( idx == x.length );
		return new ArrayFactoredRepresentation<IpcTamariskState>( x );
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes;
	}
}
