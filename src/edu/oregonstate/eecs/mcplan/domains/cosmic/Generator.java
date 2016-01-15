/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import java.lang.ref.WeakReference;

import com.mathworks.toolbox.javabuilder.MWNumericArray;

/**
 * @author jhostetler
 *
 */
public final class Generator
{
	private final int id;
	private final CosmicParameters params;
	private final WeakReference<MWNumericArray> mgen;
	
	public Generator( final int id, final CosmicParameters params, final MWNumericArray mgen )
	{
		this.id = id;
		this.params = params;
		this.mgen = new WeakReference<>( mgen );
		
		assert( id == mgen.getInt( new int[] { id, params.sh_col_names.get( "id" ) } ) );
	}
	
	@Override
	public String toString()
	{
		return "Gen[" + id + "]";
	}
}
