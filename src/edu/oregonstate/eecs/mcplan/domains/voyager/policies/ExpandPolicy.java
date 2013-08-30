package edu.oregonstate.eecs.mcplan.domains.voyager.policies;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.EntityType;
import edu.oregonstate.eecs.mcplan.domains.voyager.LaunchAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.NothingAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.Planet;
import edu.oregonstate.eecs.mcplan.domains.voyager.Player;
import edu.oregonstate.eecs.mcplan.domains.voyager.Voyager;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerState;

public class ExpandPolicy implements AnytimePolicy<VoyagerState, UndoableAction<VoyagerState>>
{
	private final Player self_;
	
	private VoyagerState s_ = null;
	private long t_ = 0;
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder( 41, 59 ).append( self_ ).toHashCode();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof ExpandPolicy) ) {
			return false;
		}
		final ExpandPolicy that = (ExpandPolicy) obj;
		return self_ == that.self_;
	}
	
	public ExpandPolicy( final Player self )
	{
		self_ = self;
	}
	
	@Override
	public void setState( final VoyagerState s, final long t )
	{
		s_ = s;
		t_ = t;
	}

	@Override
	public UndoableAction<VoyagerState> getAction()
	{
		Planet src = null;
		int workers = 1; // Only consider planets with >1 worker
		for( final Planet p : s_.planets ) {
			if( p.owner() == self_ && p.population( EntityType.Worker ) > workers ) {
				src = p;
				workers = p.population( EntityType.Worker );
			}
		}
		if( src != null ) {
			Planet dest = null;
			double d = Double.MAX_VALUE;
			for( final Planet p : s_.planets ) {
				if( p.owner() == Player.Neutral && Voyager.sq_distance( src, p ) < d ) {
					dest = p;
					d = Voyager.sq_distance( src, dest );
				}
			}
			if( dest != null ) {
				final int[] pop = new int[EntityType.values().length];
				// Guaranteed to be at least 1
				pop[EntityType.Worker.ordinal()] = src.population( EntityType.Worker ) / 2;
				return new LaunchAction( src, dest, pop );
			}
		}
		return new NothingAction();
	}

	@Override
	public void actionResult( final VoyagerState sprime, final double[] r )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName()
	{
		return "ExpandPolicy" + self_.id;
	}
	
	@Override
	public String toString()
	{
		return getName();
	}

	@Override
	public long minControl()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long maxControl()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public UndoableAction<VoyagerState> getAction( final long control )
	{
		return getAction();
	}

}
