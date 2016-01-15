/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import com.mathworks.toolbox.javabuilder.MWStructArray;

/**
 * @author jhostetler
 *
 */
public final class CosmicOptions
{
	public static final class Builder
	{
		private boolean verbose = false;
		
		public CosmicOptions finish()
		{
			return new CosmicOptions( verbose );
		}
		
		public Builder verbose( final boolean verbose )
		{ this.verbose = verbose; return this; }
	}
	
	// -----------------------------------------------------------------------
	
	public final boolean verbose;
	
	public CosmicOptions( final boolean verbose )
	{
		this.verbose = verbose;
	}
	
	public MWStructArray toMatlab()
	{
		final MWStructArray struct = new MWStructArray( 1, 1, new String[] { "verbose" } );
		struct.set( "verbose", 1, verbose );
		return struct;
	}
}
