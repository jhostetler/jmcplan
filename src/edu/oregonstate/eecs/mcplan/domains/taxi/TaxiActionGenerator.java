/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.taxi;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.ActionGenerator;

/**
 * @author jhostetler
 *
 */
public class TaxiActionGenerator extends ActionGenerator<TaxiState, TaxiAction>
{
	public static final ArrayList<TaxiAction> actions = new ArrayList<TaxiAction>();
	
	static {
		actions.add( new MoveAction( 1, 0 ) );
		actions.add( new MoveAction( 0, 1 ) );
		actions.add( new MoveAction( -1, 0 ) );
		actions.add( new MoveAction( 0, -1 ) );
		actions.add( new PickupAction() );
		actions.add( new PutdownAction() );
	}
	
	private int idx_ = 0;
	
	@Override
	public TaxiActionGenerator create()
	{
		return new TaxiActionGenerator();
	}

	@Override
	public void setState( final TaxiState s, final long t, final int[] turn )
	{
		idx_ = 0;
	}

	@Override
	public int size()
	{
		return actions.size();
	}

	@Override
	public boolean hasNext()
	{
		return idx_ < size();
	}

	@Override
	public TaxiAction next()
	{
		return actions.get( idx_++ ).create();
	}
}
