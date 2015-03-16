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
	public final Budget budget;
	
	public FsssParameters( final int width, final int depth, final Budget budget )
	{
		this.width = width;
		this.depth = depth;
		this.budget = budget;
	}
}
