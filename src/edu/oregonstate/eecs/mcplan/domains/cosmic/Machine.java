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
 * This is a wrapper class to give names to the "mac" fields.
 * <p>
 * Refer to 'psconstants.m' for further documentation.
 * <p>
 * It holds the underlying Matlab array via WeakReference, so it will not keep
 * it alive. Do not store references to Machine instances.
 */
public final class Machine extends CosmicFacade
{
	public Machine( final int id, final CosmicParameters params, final MWStructArray ps )
	{
		super( "mac", id, params.ma_col_names, ps );
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "Machine[" ).append( "id: " ).append( id() )
		  .append( "; omega: " ).append( omega() )
		  .append( "]" );
		return sb.toString();
	}
	
	@Override
	public int id()
	{
		// The unique identifier of a Machine is the *bus* number associated
		// with the generator associated with the machine. This field is called
		// 'gen' in psconstants.m, but it clearly refers to a bus.
		return getInt( "gen" );
	}
	
	public double omega()
	{
		return getDouble( "omega" );
	}
}
