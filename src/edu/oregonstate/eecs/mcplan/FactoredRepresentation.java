/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * A Representation that additionally represents the state as a feature
 * vector in R^N.
 */
public abstract class FactoredRepresentation<S, F extends FactoredRepresenter<S, F>>
	extends Representation<S, F>
{
	/**
	 * Returns the feature vector representation.
	 * @return
	 */
	public abstract double[] phi();
}
