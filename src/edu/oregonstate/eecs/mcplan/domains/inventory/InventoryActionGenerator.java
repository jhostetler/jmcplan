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
	int next = 0;
	
	@Override
	public ActionGenerator<InventoryState, InventoryAction> create()
	{
		return new InventoryActionGenerator();
	}

	@Override
	public void setState( final InventoryState s, final long t )
	{
		actions.clear();
		next = 0;
		
		actions.add( new InventoryNothingAction() );
		
		final Fn.IntSlice g = PreferredNumbers.Series_1_2_5();
		while( next < s.problem.min_order ) {
			next = g.next();
		}
		do {
			if( next > s.problem.max_order ) {
				break;
			}
			for( int i = 0; i < s.problem.Nproducts; ++i ) {
				actions.add( new InventoryOrderAction( i, next ) );
			}
			next = g.next();
		} while( g.hasNext() );
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
