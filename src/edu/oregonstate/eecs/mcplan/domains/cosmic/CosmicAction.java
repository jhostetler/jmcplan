/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import com.mathworks.toolbox.javabuilder.MWNumericArray;

import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public abstract class CosmicAction implements VirtualConstructor<CosmicAction>
{
	/**
	 * Returned array is owned by caller.
	 * @param params
	 * @param t
	 * @return
	 */
	public abstract MWNumericArray toMatlab( final CosmicParameters params, final double t );
	
	@Override
	public abstract int hashCode();
	
	@Override
	public abstract boolean equals( final Object obj );
	
	@Override
	public abstract String toString();
}
