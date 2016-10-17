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
	
	public final int id()
	{
		return getInt( "id" );
	}
}
