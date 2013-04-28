/**
 * 
 */
package edu.oregonstate.eecs.mcplan.experiments;

/**
 * @author jhostetler
 *
 */
public abstract class Experiment<Params, World> implements Runnable
{
	public abstract void setup( final Environment env, final Params params, final World world );
	public abstract void finish();
	
	public abstract String getFileSystemName();
}
