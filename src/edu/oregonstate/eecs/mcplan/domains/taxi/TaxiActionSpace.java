/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.taxi;

import edu.oregonstate.eecs.mcplan.ActionSet;
import edu.oregonstate.eecs.mcplan.ActionSpace;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class TaxiActionSpace extends ActionSpace<TaxiState, TaxiAction>
{
	private final TaxiActionGenerator action_gen_ = new TaxiActionGenerator();

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
	public int index( final TaxiAction a )
	{
		return TaxiActionGenerator.actions.indexOf( a );
	}

	@Override
	public ActionSet<TaxiState, TaxiAction> getActionSet( final TaxiState s )
	{
		action_gen_.setState( s, 0L );
		return ActionSet.constant( Fn.in(action_gen_) );
	}
}
