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
