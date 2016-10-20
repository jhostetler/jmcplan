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

import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.mathworks.toolbox.javabuilder.MWStructArray;

/**
 * Matlab utility functions.
 * @author jhostetler
 */
public class M
{
	/**
	 * Call 'getDimensions()' on the MWArray in 'sa.field'.
	 * @param sa
	 * @param field
	 * @param index
	 * @return
	 */
	public static int[] field_getDimensions( final MWStructArray sa, final String field, final int index )
	{
		MWArray m = null;
		try {
			m = sa.getField( field, index );
			return m.getDimensions();
		}
		finally {
			m.dispose();
		}
	}
	
	public static int field_getInt( final MWStructArray sa, final String field, final int index, final int[] getInt_index )
	{
		MWNumericArray m = null;
		try {
			m = (MWNumericArray) sa.getField( field, index );
			return m.getInt( getInt_index );
		}
		finally {
			m.dispose();
		}
	}
	
	public static void field_set( final MWStructArray sa, final String field, final int index,
								  final int[] set_index, final Object set_value )
	{
		MWNumericArray m = null;
		try {
			m = (MWNumericArray) sa.getField( field, index );
			m.set( set_index, set_value );
		}
		finally {
			m.dispose();
		}
	}
}
