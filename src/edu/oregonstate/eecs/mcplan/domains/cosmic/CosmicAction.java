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
public class CosmicAction implements VirtualConstructor<CosmicAction>
{
	@Override
	public CosmicAction create()
	{
		return new CosmicAction();
	}
	
	public MWNumericArray toMatlab()
	{
		// FIXME: Add non-default actions
		return new MWNumericArray();
	}
}
