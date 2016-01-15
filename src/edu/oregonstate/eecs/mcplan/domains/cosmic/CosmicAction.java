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
	
	/**
	 * Apply changes to the state due to this action that are not modeled by
	 * the Cosmic system.
	 * @param sprime The CosmicState obtained by applying take_action
	 */
	public abstract void applyNonCosmicChanges( CosmicState sprime );
	
	@Override
	public abstract int hashCode();
	
	@Override
	public abstract boolean equals( final Object obj );
	
	@Override
	public abstract String toString();
}
