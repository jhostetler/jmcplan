package edu.oregonstate.eecs.mcplan.domains.voyager.policies;

import java.util.ArrayList;
import java.util.List;

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

/**
 * Launches attacks at the nearest enemy planet. Intended for use when we
 * have an overwhelming advantage and just want to end the game.
 */
public class KillPolicy extends AnytimePolicy<VoyagerState, UndoableAction<VoyagerState>>
{
	private final Player self_;
	private final int garrison_;
	
	private VoyagerState s_ = null;
	private long t_ = 0;
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder( 43, 61 ).append( self_ ).append( garrison_ ).toHashCode();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
//		System.out.println( "KillPolicy.equals()" );
		if( obj == null || !(obj instanceof KillPolicy) ) {
			return false;
		}
		final KillPolicy that = (KillPolicy) obj;
//		System.out.println( "\t" + toString() + ".equals( " + that.toString() + " )" );
		return self_ == that.self_
			   && garrison_ == that.garrison_;
	}
	
	public KillPolicy( final Player self, final int garrison )
	{
		self_ = self;
		garrison_ = garrison;
	}
	
	@Override
	public void setState( final VoyagerState s, final long t )
	{
		s_ = s;
		t_ = t;
	}
	
	private double targetWeight( final Planet t, final List<Planet> friendly_planets )
	{
		double w = 0.0;
		for( final Planet f : friendly_planets ) {
			w += Voyager.distance( f, t ) * Math.max( 0, f.population( EntityType.Soldier ) - garrison_ );
		}
		return 1.0 / w;
	}

	@Override
	public UndoableAction<VoyagerState> getAction()
	{
		final ArrayList<Planet> friendly = Voyager.playerPlanets( s_, self_ );
		final ArrayList<Planet> enemy = Voyager.playerPlanets( s_, self_.enemy() );
		if( enemy.isEmpty() ) {
			return new NothingAction();
		}
		Planet target = null;
		double weight = 0;
		for( final Planet t : enemy ) {
			final double tw = targetWeight( t, friendly );
			if( tw > weight ) {
				weight = tw;
				target = t;
			}
		}
		if( target == null ) {
			return new NothingAction();
		}
		while( friendly.size() > 0 ) {
			Planet src = null;
			double dist = Double.MAX_VALUE;
			for( final Planet s : friendly ) {
				final double sd = Voyager.distance( s, target );
				if( sd < dist ) {
					dist = sd;
					src = s;
				}
			}
			if( src != null ) {
				final int spare_soldiers = src.population( EntityType.Soldier ) - garrison_;
				if( spare_soldiers > 0 ) {
					final int[] pop = new int[EntityType.values().length];
					pop[EntityType.Soldier.ordinal()] = spare_soldiers;
					return new LaunchAction( src, target, pop );
				}
				else {
					friendly.remove( src );
				}
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
		return "KillPolicy" + self_.id;
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
