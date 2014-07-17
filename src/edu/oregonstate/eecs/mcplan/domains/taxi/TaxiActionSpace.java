/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.taxi;

import edu.oregonstate.eecs.mcplan.ActionSpace;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public class TaxiActionSpace extends ActionSpace<TaxiState, TaxiAction>
{
	private final TaxiActionGenerator action_gen_ = new TaxiActionGenerator();
	
	@Override
	public void setState( final TaxiState s )
	{
		action_gen_.setState( s, 0L, new int[] { 0 } );
	}

	@Override
	public int cardinality()
	{
		return action_gen_.size();
	}

	@Override
	public boolean isFinite()
	{
		return true;
	}

	@Override
	public boolean isCountable()
	{
		return true;
	}

	@Override
	public Generator<TaxiAction> generator()
	{
		return action_gen_.create();
	}
	
	@Override
	public int index( final TaxiAction a )
	{
		return TaxiActionGenerator.actions.indexOf( a );
	}
}
