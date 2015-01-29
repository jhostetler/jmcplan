/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.inventory;


/**
 * @author jhostetler
 *
 */
public class InventoryProblem
{
	public final int Nproducts;
	public final int[] price;
	public final int max_inventory;
	public final double warehouse_cost;
	public final int min_order;
	public final int max_order;
	public final double delivery_probability;
	public final int max_demand;
	
	public InventoryProblem( final int Nproducts, final int[] price,
						     final int max_inventory, final double warehouse_cost,
						     final int min_order, final int max_order,
						     final double delivery_probability, final int max_demand )
	{
		this.Nproducts = Nproducts;
		this.price = price;
		this.max_inventory = max_inventory;
		this.warehouse_cost = warehouse_cost;
		this.min_order = min_order;
		this.max_order = max_order;
		this.delivery_probability = delivery_probability;
		this.max_demand = max_demand;
	}
	
	public static InventoryProblem TwoProducts()
	{
		final int Nproducts = 2;
		final int max_inventory = 10;
		final double warehouse_cost = 1;
		final int min_order = 2;
		final int max_order = 5;
		final double delivery_probability = 0.3;
		final int max_demand = 7;
		final int[] price = new int[] { 3, 5 };
		
		final InventoryProblem problem = new InventoryProblem(
			Nproducts, price, max_inventory, warehouse_cost, min_order, max_order, delivery_probability, max_demand );
		return problem;
	}
}
