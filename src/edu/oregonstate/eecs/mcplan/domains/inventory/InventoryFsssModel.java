/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.inventory;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssModel;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class InventoryFsssModel extends FsssModel<InventoryState, InventoryAction>
{
	public final InventoryProblem problem;
	
	private final double Vmin;
	private final double Vmax;
	
	private final InventoryNullRepresenter base_repr;
	
	public InventoryFsssModel( final InventoryProblem problem )
	{
		this.problem = problem;
		base_repr = new InventoryNullRepresenter( problem );
		
		final double gamma = discount();
		assert( gamma < 1.0 );
		
		// +1 for per-order cost
		Vmin = (1.0 / (1.0 - gamma)) * -(problem.max_inventory*problem.warehouse_cost + 1);
		double r = 0;
		for( int i = 0; i < problem.Nproducts; ++i ) {
			r += problem.price[i] * problem.max_demand;
		}
		Vmax = (1.0 / (1.0 - gamma)) * r;
	}
	
	@Override
	public double Vmin()
	{
		return Vmin;
	}

	@Override
	public double Vmax()
	{
		return Vmax;
	}

	@Override
	public double discount()
	{
		return 0.9;
	}

	@Override
	public Iterable<InventoryAction> actions( final InventoryState s )
	{
		final InventoryActionGenerator actions = new InventoryActionGenerator();
		actions.setState( s, 0L );
		return Fn.takeAll( actions );
	}

	@Override
	public InventoryState sampleTransition( final InventoryState s, final InventoryAction a )
	{
		final InventoryState sprime = s.copy();
		final InventorySimulator sim = new InventorySimulator( sprime );
		sim.takeAction( new JointAction<InventoryAction>( a.create() ) );
		return sprime;
	}

	@Override
	public double reward( final InventoryState s, final InventoryAction a )
	{
		return s.r + a.reward();
	}

	@Override
	public FactoredRepresenter<InventoryState, ? extends FactoredRepresentation<InventoryState>> base_repr()
	{
		return base_repr;
	}

}
