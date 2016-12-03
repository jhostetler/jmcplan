/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic.policy;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicAction;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicState;
import edu.oregonstate.eecs.mcplan.domains.cosmic.ShedGlobalAction;

/**
 * @author jhostetler
 *
 */
public class ShedGlobalPolicy extends AnytimePolicy<CosmicState, CosmicAction>
{
	public final double p;
	
	private int[] shunts = null;
	
	public ShedGlobalPolicy( final double p )
	{
		this.p = p;
	}
	
	@Override
	public ShedGlobalPolicy copy()
	{
		return new ShedGlobalPolicy( p );
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
		this.shunts = s.liveShunts();
	}
	
	@Override
	public boolean improvePolicy()
	{
		return false;
	}

	@Override
	public final CosmicAction getAction()
	{
		return new ShedGlobalAction( shunts, p );
	}

	@Override
	public final void actionResult( final CosmicState sprime, final double[] r )
	{ }

	@Override
	public String getName()
	{
		return "ShedGlobal";
	}

	@Override
	public int hashCode()
	{
		final HashCodeBuilder hb = new HashCodeBuilder();
		hb.append( getClass() ).append( p );
		return hb.toHashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof ShedGlobalPolicy) ) {
			return false;
		}
		final ShedGlobalPolicy that = (ShedGlobalPolicy) obj;
		return p == that.p;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "SG(" ).append( p ).append( ")" );
		return sb.toString();
	}
}
