/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.taxi;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public abstract class TaxiAction implements UndoableAction<TaxiState>, VirtualConstructor<TaxiAction>
{
	public abstract void doAction( final TaxiState s, final RandomGenerator rng );
	
	@Override
	public final void doAction( final TaxiState s )
	{
		throw new UnsupportedOperationException( "Use: doAction( TaxiState, RandomGenerator )" );
	}
}
