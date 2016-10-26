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

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
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
		
		private final Map<String, Integer> random_generators = new HashMap<String, Integer>();
		
		private boolean random_loads = false;
		private double random_load_min = 0.8;
		private double random_load_max = 1.2;
		// The model is P_{t+1} ~ P_t + Gaussian(0, sigma * P_0) where P_0 is
		// the initial value of P
		private double random_load_sigma = 0.01;
		
		public CosmicOptions finish()
		{
			return new CosmicOptions( verbose, write_log,
									  simgrid_max_recursion, simgrid_method,
									  ImmutableMap.copyOf( random_generators ),
									  random_loads, random_load_min, random_load_max, random_load_sigma );
		}
		
		public Builder verbose( final boolean verbose )
		{ this.verbose = verbose; return this; }
		
		public Builder write_log( final boolean write_log )
		{ this.write_log = write_log; return this; }
		
		public Builder simgrid_max_recursion( final int max )
		{ this.simgrid_max_recursion = max; return this; }
		
		public Builder simgrid_method( final SimgridMethod method )
		{ this.simgrid_method = method; return this; }
		
		public Builder random_loads( final boolean b )
		{ this.random_loads = b; return this; }
		
		public Builder random_load_min( final double min )
		{ this.random_load_min = min; return this; }
		
		public Builder random_load_max( final double max )
		{ this.random_load_max = max; return this; }
		
		public Builder random_load_sigma( final double sigma )
		{ this.random_load_sigma = sigma; return this; }
		
		public Builder random_generator( final String name, final int seed )
		{ this.random_generators.put( name, seed ); return this; }
	}
	
	// -----------------------------------------------------------------------
	
	public final boolean verbose;
	public final boolean write_log;
	public final int simgrid_max_recursion;
	public final SimgridMethod simgrid_method;
	
	public final ImmutableMap<String, Integer> random_generators;
	public final boolean random_loads;
	public final double random_load_min;
	public final double random_load_max;
	public final double random_load_sigma;
	
	public CosmicOptions( final boolean verbose, final boolean write_log,
						  final int simgrid_max_recursion, final SimgridMethod simgrid_method,
						  final ImmutableMap<String, Integer> random_generators,
						  final boolean random_loads, final double random_load_min,
						  final double random_load_max, final double random_load_sigma )
	{
		this.verbose = verbose;
		this.write_log = write_log;
		this.simgrid_max_recursion = simgrid_max_recursion;
		this.simgrid_method = simgrid_method;
		
		this.random_generators = random_generators;
		this.random_loads = random_loads;
		this.random_load_min = random_load_min;
		this.random_load_max = random_load_max;
		this.random_load_sigma = random_load_sigma;
		
		assert( this.random_load_max >= 0 );
	}
	
	public MWStructArray toMatlab()
	{
		final MWStructArray struct = new MWStructArray( 1, 1,
			new String[] { "verbose", "write_log", "simgrid_max_recursion",
						   "simgrid_method", "random" } );
		struct.set( "verbose", 1, verbose );
		struct.set( "write_log", 1, write_log );
		struct.set( "simgrid_max_recursion", 1, simgrid_max_recursion );
		struct.set( "simgrid_method", 1, simgrid_method.toString() );
		// Simulator stochasticity
		{
			final MWStructArray random = new MWStructArray( 1, 1,
				new String[] { "gen", "loads", "load_min", "load_max", "load_sigma" } );
			{
				final MWStructArray gen = new MWStructArray( 1, 1,
					random_generators.keySet().toArray( new String[] { } ) );
				for( final Map.Entry<String, Integer> e : random_generators.entrySet() ) {
					final MWStructArray gen_i = new MWStructArray( 1, 1, new String[] { "seed", "state" } );
					gen_i.set( "seed", 1, e.getValue() );
		//			gen.set( "state", 1, MWArray.EMPTY_ARRAY );
					gen.set( e.getKey(), 1, gen_i );
				}
				random.set( "gen", 1, gen );
			}
			random.set( "loads", 1, random_loads );
			random.set( "load_min", 1, random_load_min );
			random.set( "load_max", 1, random_load_max );
			random.set( "load_sigma", 1, random_load_sigma );
			struct.set( "random", 1, random );
		}
		return struct;
	}
}
