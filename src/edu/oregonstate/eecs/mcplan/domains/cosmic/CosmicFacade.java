/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import java.util.Map;

import com.mathworks.toolbox.javabuilder.MWNumericArray;

/**
 * @author jhostetler
 *
 */
public class CosmicFacade
{
	private final int id;
	private final Map<String, Integer> columns;
//	protected final WeakReference<MWNumericArray> m;
	protected final MWNumericArray m;
	
	protected CosmicFacade( final int id, final Map<String, Integer> columns, final MWNumericArray m )
	{
		this.id = id;
		this.columns = columns;
//		this.m = new WeakReference<>( m );
		this.m = m;
		
		assert( id == m.getInt( new int[] { id, columns.get( "id" ) } ) );
	}
	
	protected double getDouble( final String name )
	{
//		return m.get().getDouble( index( name ) );
		return m.getDouble( index( name ) );
	}
	
	protected int getInt( final String name )
	{
//		return m.get().getInt( index( name ) );
		return m.getInt( index( name ) );
	}
	
	protected final int[] index( final String name )
	{
		return new int[] { id, columns.get( name ) };
	}
	
	public final int id()
	{
		return id;
	}
}
