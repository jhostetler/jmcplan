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
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import com.mathworks.toolbox.javabuilder.MWStructArray;

/**
 * This is a wrapper class to give names to the "bus" fields.
 * <p>
 * Refer to 'psconstants.m' for further documentation.
 * <p>
 * It holds the underlying Matlab array via WeakReference, so it will not keep
 * it alive. Do not store references to Shunt instances.
 * 
 * TODO: Strictly speaking we wouldn't leak Matlab memory anyway since it all
 * gets disposed when the corresponding CosmicState is disposed. So the
 * WeakReference is potentially unnecessary overhead.
 */
public final class Bus extends CosmicFacade
{
	public Bus( final int id, final CosmicParameters params, final MWStructArray ps )
	{
//		super( "bus", id, params.bu_col_names, ps );
		super( "bus", params.bus_id_to_matlab_index.get( id ), params.bu_col_names, ps );
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "Bus[" ).append( "id: " ).append( id() )
		  .append( "; zone: " ).append( zone() )
		  .append( "; Vmag: " ).append( Vmag() )
		  .append( "]" );
		return sb.toString();
	}
	
	public double Vang()
	{
		return getDouble( "Vang" );
	}
	
	public double Vmag()
	{
		return getDouble( "Vmag" );
	}
	
	public int zone()
	{
		return getInt( "zone" );
	}
}
