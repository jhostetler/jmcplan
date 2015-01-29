/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

/**
 * @author jhostetler
 *
 */
public class FsssParameters
{
	public final int width;
	public final int depth;
	public final int max_samples;
	
	public FsssParameters( final int width, final int depth, final int max_samples )
	{
		this.width = width;
		this.depth = depth;
		this.max_samples = max_samples;
	}
}
