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
