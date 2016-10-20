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
 * @author jhostetler
 *
 */
public final class Branch extends CosmicFacade
{
	public Branch( final int id, final CosmicParameters params, final MWStructArray ps )
	{
		super( "branch", id, params.br_col_names, ps );
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "Branch[" ).append( id() )
		  .append( "; status: " ).append( status() )
		  .append( "; Pf: " ).append( Pf() )
		  .append( "; Qf: " ).append( Qf() )
		  .append( "; Pt: " ).append( Pt() )
		  .append( "; Qt: " ).append( Qt() )
		  .append( "]" );
		return sb.toString();
	}
	
	public int from()
	{
		return getInt( "from" );
	}
	
	public int to()
	{
		return getInt( "to" );
	}
	
	public int status()
	{
		return getInt( "status" );
	}
	
	public double Pf()
	{
		return getDouble( "Pf" );
	}
	
	public double Qf()
	{
		return getDouble( "Qf" );
	}
	
	public double Pt()
	{
		return getDouble( "Pt" );
	}
	
	public double Qt()
	{
		return getDouble( "Qt" );
	}
}
