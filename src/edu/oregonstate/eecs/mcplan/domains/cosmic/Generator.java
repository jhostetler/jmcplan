/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import com.mathworks.toolbox.javabuilder.MWStructArray;

/**
 * @author jhostetler
 *
 */
public final class Generator extends CosmicFacade
{
	public Generator( final int id, final CosmicParameters params, final MWStructArray ps )
	{
		super( "gen", id, params.sh_col_names, ps );
	}
	
	@Override
	public String toString()
	{
		return "Gen[" + id() + "]";
	}
}
