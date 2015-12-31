/**
 * 
 */
package edu.oregonstate.eecs.mcplan;



/**
 * An AnytimePolicy is a Policy that can be improved incrementally.
 * <p>
 * The getAction() function should have minimal overhead (ie. returning a
 * member variable or performing a single argmax operation).
 */
public abstract class AnytimePolicy<S, A> extends Policy<S, A>
{
	/**
	 * @return False iff no further improvement is possible.
	 */
	public abstract boolean improvePolicy();
}
