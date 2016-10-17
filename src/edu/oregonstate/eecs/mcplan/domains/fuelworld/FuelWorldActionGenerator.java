/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.fuelworld;

import java.util.ArrayList;
import java.util.Iterator;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import gnu.trove.iterator.TIntIterator;

/**
 * @author jhostetler
 *
 */
public class FuelWorldActionGenerator extends ActionGenerator<FuelWorldState, FuelWorldAction>
{
	// FIXME: This is public to make it easier to implement FuelWorldActionSpace.
	// Long run fix is to combine the best of both interfaces.
	public final ArrayList<FuelWorldAction> actions_ = new ArrayList<FuelWorldAction>();
	private Iterator<FuelWorldAction> itr_ = null;
	
	@Override
	public FuelWorldActionGenerator create()
	{
		return new FuelWorldActionGenerator();
	}

	@Override
	public void setState( final FuelWorldState s, final long t )
	{
		actions_.clear();
		
		final TIntIterator itr = s.adjacency.get( s.location ).iterator();
		while( itr.hasNext() ) {
			final int dest = itr.next();
			actions_.add( new MoveAction( s.location, dest ) );
		}
		
		if( s.fuel_depots.contains( s.location ) ) {
			actions_.add( new RefuelAction() );
		}
		
		itr_ = actions_.iterator();
	}

	@Override
	public int size()
	{
		return actions_.size();
	}

	@Override
	public boolean hasNext()
	{
		return itr_.hasNext();
	}

	@Override
	public FuelWorldAction next()
	{
		return itr_.next();
	}
}
