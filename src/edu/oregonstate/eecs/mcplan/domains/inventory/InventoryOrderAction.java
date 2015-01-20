/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.inventory;


/**
 * @author jhostetler
 *
 */
public class InventoryOrderAction extends InventoryAction
{
	public final int product;
	public final int quantity;
	
	private boolean done = false;
	
	public InventoryOrderAction( final int product, final int quantity )
	{
		this.product = product;
		this.quantity = quantity;
	}
	
	@Override
	public InventoryOrderAction create()
	{
		return new InventoryOrderAction( product, quantity );
	}
	
	@Override
	public double reward()
	{
		// Per-order cost
		return -1;
	}

	@Override
	public void doAction( final InventoryState s )
	{
		assert( !done );
		s.orders[product] += quantity;
		done = true;
	}

	@Override
	public boolean isDone()
	{
		return done;
	}

	@Override
	public void undoAction( final InventoryState s )
	{
		assert( done );
		s.orders[product] -= quantity;
		done = false;
	}
	
	@Override
	public int hashCode()
	{
		return 5 * (7 + product * (11 + quantity));
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof InventoryOrderAction) ) {
			return false;
		}
		final InventoryOrderAction that = (InventoryOrderAction) obj;
		return product == that.product && quantity == that.quantity;
	}
	
	@Override
	public String toString()
	{
		return "OrderAction[" + product + ", " + quantity + "]";
	}
}
