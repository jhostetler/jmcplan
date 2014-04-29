/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.tamarisk;

/**
 * @author jhostetler
 *
 */
public enum Species
{
	Native,
	Tamarisk,
	/**
	 * 'None' must *always* be last, so that sequential indexing over
	 * non-'None' species works.
	 */
	None;
	
	/** The total number of Species, *not* counting 'None'. */
	public static int N = values().length - 1;
}
