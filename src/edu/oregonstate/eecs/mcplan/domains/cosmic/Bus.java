/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import java.lang.ref.WeakReference;

import com.mathworks.toolbox.javabuilder.MWNumericArray;

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
public class Bus
{
	private final int id;
	private final CosmicParameters params;
	private final WeakReference<MWNumericArray> mbus;
	
	public Bus( final int id, final CosmicParameters params, final MWNumericArray mbus )
	{
		this.id = id;
		this.params = params;
		this.mbus = new WeakReference<>( mbus );
		
		assert( id == mbus.getInt( new int[] { id, params.bu_col_names.get( "id" ) } ) );
	}
	
	public int id()
	{
//		return mbus.get().getInt( new int[] { id, params.bu_col_names.get( "id" ) } );
		return id;
	}
	
	public double Vmag()
	{
		return mbus.get().getInt( new int[] { id, params.bu_col_names.get( "Vmag" ) } );
	}
}
