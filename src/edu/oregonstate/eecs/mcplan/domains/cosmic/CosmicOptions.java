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
	public static enum SimgridMethod
	{ recursive, iterative }
	
	public static final class Builder
	{
		private boolean verbose = false;
		private boolean write_log = false;
		private int simgrid_max_recursion = 200;
		private SimgridMethod simgrid_method = SimgridMethod.recursive;
		
		public CosmicOptions finish()
		{
			return new CosmicOptions( verbose, write_log, simgrid_max_recursion,
									  simgrid_method );
		}
		
		public Builder verbose( final boolean verbose )
		{ this.verbose = verbose; return this; }
		
		public Builder write_log( final boolean write_log )
		{ this.write_log = write_log; return this; }
		
		public Builder simgrid_max_recursion( final int max )
		{ this.simgrid_max_recursion = max; return this; }
		
		public Builder simgrid_method( final SimgridMethod method )
		{ this.simgrid_method = method; return this; }
	}
	
	// -----------------------------------------------------------------------
	
	public final boolean verbose;
	public final boolean write_log;
	public final int simgrid_max_recursion;
	public final SimgridMethod simgrid_method;
	
	public CosmicOptions( final boolean verbose, final boolean write_log, final int simgrid_max_recursion,
						  final SimgridMethod simgrid_method )
	{
		this.verbose = verbose;
		this.write_log = write_log;
		this.simgrid_max_recursion = simgrid_max_recursion;
		this.simgrid_method = simgrid_method;
	}
	
	public MWStructArray toMatlab()
	{
		final MWStructArray struct = new MWStructArray( 1, 1,
			new String[] { "verbose", "write_log", "simgrid_max_recursion",
						   "simgrid_method" } );
		struct.set( "verbose", 1, verbose );
		struct.set( "write_log", 1, write_log );
		struct.set( "simgrid_max_recursion", 1, simgrid_max_recursion );
		struct.set( "simgrid_method", 1, simgrid_method.toString() );
		return struct;
	}
}
