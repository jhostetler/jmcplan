/**
 * 
 */
package edu.oregonstate.eecs.mcplan.experiments;

import java.io.File;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author jhostetler
 *
 */
public final class Environment
{
	public static class Builder
	{
		private File root_directory = null;
		private RandomGenerator rng = null;
		
		public Builder root_directory( final File f ) { root_directory = f; return this; }
		public Builder rng( final RandomGenerator f ) { rng = f; return this; }
		
		public Environment finish()
		{
			return new Environment( root_directory, rng );
		}
	}
	
	// -----------------------------------------------------------------------
	
	public final File root_directory;
	public final RandomGenerator rng;
	
	public Environment( final File root_directory, final RandomGenerator rng )
	{
		this.root_directory = root_directory;
		this.rng = rng;
	}
}
