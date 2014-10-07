/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.fuelworld;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.ActionSpace;
import edu.oregonstate.eecs.mcplan.util.Generator;
import gnu.trove.list.TIntList;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * @author jhostetler
 *
 */
public class FuelWorldActionSpace extends ActionSpace<FuelWorldState, FuelWorldAction>
{
	private final FuelWorldActionGenerator action_gen_ = new FuelWorldActionGenerator();
	
	public final FuelWorldState s0;
	private FuelWorldState s_ = null;
	
	private final TObjectIntMap<FuelWorldAction> index_ = new TObjectIntHashMap<FuelWorldAction>();
	
	public FuelWorldActionSpace( final FuelWorldState s0 )
	{
		this.s0 = s0;
		
		int c = 0;
		for( int i = 0; i < s0.adjacency.size(); ++i ) {
			final TIntList succ = s0.adjacency.get( i );
			for( int j = 0; j < succ.size(); ++j ) {
				index_.put( new MoveAction( i, succ.get( j ) ), c++ );
			}
		}
		index_.put( new RefuelAction(), c++ );
	}
	
	@Override
	public void setState( final FuelWorldState s )
	{
		s_ = s;
	}

	@Override
	public int cardinality()
	{
		return index_.size();
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
	public Generator<FuelWorldAction> generator()
	{
		final ActionGenerator<FuelWorldState, FuelWorldAction> g = action_gen_.create();
		g.setState( s_, 0L, new int[] { 0 } );
		return g;
	}
	
	@Override
	public int index( final FuelWorldAction a )
	{
		return index_.get( a );
	}
}
