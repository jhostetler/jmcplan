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

import java.util.Map;

import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.mathworks.toolbox.javabuilder.MWStructArray;

/**
 * @author jhostetler
 *
 */
public class CosmicFacade
{
	private final MWStructArray ps;
	private final String field;
	private final int mi;
	private final Map<String, Integer> columns;
//	protected final MWNumericArray m;
	
	/**
	 * Does *not* own 'ps'.
	 * @param field
	 * @param mi Matlab index (row in 'ps' structure)
	 * @param columns
	 * @param ps Not owned
	 */
	protected CosmicFacade( final String field, final int mi, final Map<String, Integer> columns, final MWStructArray ps )
	{
		this.ps = ps;
		this.field = field;
		this.mi = mi;
		this.columns = columns;
//		assert( id == getInt( "id" ) );
	}
	
	protected double getDouble( final String name )
	{
		MWNumericArray m = null;
		try {
			m = (MWNumericArray) ps.getField( field, 1 );
			return m.getDouble( index( name ) );
		}
		finally {
			m.dispose();
		}
	}
	
	protected int getInt( final String name )
	{
		MWNumericArray m = null;
		try {
			m = (MWNumericArray) ps.getField( field, 1 );
			return m.getInt( index( name ) );
		}
		finally {
			m.dispose();
		}
	}
	
//	protected double getDouble( final String name )
//	{
////		return m.get().getDouble( index( name ) );
////		return m.getDouble( index( name ) );
//		return (double) o[columns.get( name )];
//	}

//	protected int getInt( final String name )
//	{
////		return m.get().getInt( index( name ) );
////		return m.getInt( index( name ) );
//		return (int) o[columns.get( name )];
//	}
	
	protected final int[] index( final String name )
	{
		return new int[] { mi, columns.get( name ) };
	}
	
	public int id()
	{
		return getInt( "id" );
	}
}
