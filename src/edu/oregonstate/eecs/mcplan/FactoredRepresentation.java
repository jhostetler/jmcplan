/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import java.util.Arrays;


/**
 * A Representation that additionally represents the state as a feature
 * vector in R^N.
 */
public abstract class FactoredRepresentation<T> extends Representation<T>
{
	/**
	 * Returns the feature vector representation.
	 * @return
	 */
	public abstract double[] phi();
	
	@Override
	public String toString()
	{
		return Arrays.toString( phi() );
	}
}
