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
		super( "bus", params.bus_matlab_index.get( id ), params.bu_col_names, ps );
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
	
	public double Vmag()
	{
		return getDouble( "Vmag" );
	}
	
	public int zone()
	{
		return getInt( "zone" );
	}
}
