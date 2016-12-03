/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic.policy;

import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.domains.cosmic.Branch;
import edu.oregonstate.eecs.mcplan.domains.cosmic.Bus;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicAction;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicNothingAction;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicState;
import edu.oregonstate.eecs.mcplan.domains.cosmic.IslandAction;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * @author jhostetler
 *
 */
public class IsolatePolicy extends AnytimePolicy<CosmicState, CosmicAction>
{
	private CosmicAction a = null;
	
	@Override
	public final IsolatePolicy copy()
	{
		return this;
	}
	
	@Override
	public final void reset()
	{ }
	
	@Override
	public final boolean isStationary()
	{
		return true;
	}
	
	@Override
	public final void setState( final CosmicState s, final long t )
	{
		final TIntList zones = new TIntArrayList();
		final TIntSet cutset = new TIntHashSet();
		for( final Branch br : s.branches() ) {
			if( br.status() == 0 ) {
				final Bus to = s.bus( br.to() );
				final Bus from = s.bus( br.from() );
				if( !s.islands.contains( to.zone() ) ) {
					zones.add( to.zone() );
					cutset.addAll( s.params.zoneCutset( to.zone() ) );
				}
				if( !s.islands.contains( from.zone() ) ) {
					zones.add( from.zone() );
					cutset.addAll( s.params.zoneCutset( from.zone() ) );
				}
			}
		}
		if( !zones.isEmpty() ) {
			a = new IslandAction( zones, cutset );
		}
		else {
			a = new CosmicNothingAction();
		}
	}
	
	@Override
	public boolean improvePolicy()
	{
		return false;
	}

	@Override
	public final CosmicAction getAction()
	{
		return a;
	}

	@Override
	public final void actionResult( final CosmicState sprime, final double[] r )
	{ }

	@Override
	public String getName()
	{
		return "Isolate";
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		return obj instanceof IsolatePolicy;
	}

	@Override
	public String toString()
	{
		return "Isolate";
	}
}
