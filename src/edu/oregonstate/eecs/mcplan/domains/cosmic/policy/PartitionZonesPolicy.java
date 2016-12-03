/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic.policy;

import edu.oregonstate.eecs.mcplan.AnytimePolicy;
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
public class PartitionZonesPolicy extends AnytimePolicy<CosmicState, CosmicAction>
{
	private CosmicAction a = null;
	public final double t;
	
	public PartitionZonesPolicy( final double t )
	{
		this.t = t;
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
		for( int zone = 1; zone <= s.params.Nzones; ++zone ) {
			zones.add( zone );
			final TIntSet cut = s.params.zoneCutset( zone );
			cutset.addAll( cut );
		}
		if( !cutset.isEmpty() ) {
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
		return "PartitionZones";
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ Double.valueOf(t).hashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof PartitionZonesPolicy) ) {
			return false;
		}
		final PartitionZonesPolicy that = (PartitionZonesPolicy) obj;
		return t == that.t;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "PartitionZones[" ).append( t ).append( "]" );
		return sb.toString();
	}
}
