/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

/**
 * Parameters of the FSSS algorithm.
 */
public class FsssParameters
{
	/**
	 * Sampling width (usually denoted C).
	 */
	public final int width;
	
	/**
	 * Maximum tree depth.
	 * <p>
	 * NOTE: A tree consisting of only the root has depth *1*. A tree with
	 * one layer of action nodes has depth *2*, etc. Depth 0 is reserved for
	 * future use.
	 */
	public final int depth;
	
	/**
	 * The search control budget.
	 */
	public final Budget budget;
	
	/**
	 * If true, use AbstractFsssNode.close() to free unneeded state nodes and
	 * conserve memory. Setting use_close = false can be useful for
	 * debugging because it allows FsssTest.validateTree() to check more things.
	 */
	public final boolean use_close;
	
	public FsssParameters( final int width, final int depth, final Budget budget, final boolean use_close )
	{
		this.width = width;
		this.depth = depth;
		this.budget = budget;
		this.use_close = use_close;
	}
}
