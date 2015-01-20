/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.inventory;

import java.util.ArrayList;
import java.util.Iterator;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.PreferredNumbers;

/**
 * @author jhostetler
 *
 */
public class InventoryActionGenerator extends ActionGenerator<InventoryState, InventoryAction>
{
	private final ArrayList<InventoryAction> actions = new ArrayList<InventoryAction>();
	private Iterator<InventoryAction> itr = null;
	
	@Override
	public ActionGenerator<InventoryState, InventoryAction> create()
	{
		return new InventoryActionGenerator();
	}

	@Override
	public void setState( final InventoryState s, final long t )
	{
		actions.clear();
		final Fn.IntSlice g = PreferredNumbers.Series_1_2_5();
		while( g.hasNext() ) {
			final int n = g.next();
			if( n > s.problem.max_order ) {
				break;
			}
			for( int i = 0; i < s.problem.Nproducts; ++i ) {
				actions.add( new InventoryOrderAction( i, n ) );
			}
		}
		itr = actions.iterator();
	}

	@Override
	public int size()
	{
		return actions.size();
	}

	@Override
	public boolean hasNext()
	{
		return itr.hasNext();
	}

	@Override
	public InventoryAction next()
	{
		return itr.next();
	}
}
