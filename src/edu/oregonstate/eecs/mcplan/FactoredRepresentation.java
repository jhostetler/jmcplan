/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * A Representation that additionally represents the state as a feature
 * vector in R^N.
 */
public abstract class FactoredRepresentation<S> extends Representation<S>
{
	/**
	 * Returns the feature vector representation.
	 * @return
	 */
	public abstract double[] phi();
}
