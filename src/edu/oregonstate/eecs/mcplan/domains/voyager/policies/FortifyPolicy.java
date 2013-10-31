/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager.policies;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.domains.voyager.LaunchAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.NothingAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.Planet;
import edu.oregonstate.eecs.mcplan.domains.voyager.Player;
import edu.oregonstate.eecs.mcplan.domains.voyager.SetProductionAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.Unit;
import edu.oregonstate.eecs.mcplan.domains.voyager.Voyager;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerState;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class FortifyPolicy extends Policy<VoyagerState, VoyagerAction>
{
	private final Player player_;
	private final int[] planets_;
	private final int batch_;
	private final int garrison_;
	
	private VoyagerState s_ = null;
	private long t_ = 0L;
	
	public FortifyPolicy( final Player player, final int[] planets, final int batch, final int garrison )
	{
		player_ = player;
		planets_ = planets;
		batch_ = batch;
		garrison_ = garrison;
	}
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder( 859, 863 )
			.append( player_ ).append( planets_ ).append( batch_ ).append( garrison_ ).toHashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof FortifyPolicy) ) {
			return false;
		}
		final FortifyPolicy that = (FortifyPolicy) obj;
		return player_ == that.player_ && Arrays.equals( planets_, that.planets_ )
			   && batch_ == that.batch_ && garrison_ == that.garrison_;
	}
	
	@Override
	public void setState( final VoyagerState s, final long t )
	{
		s_ = s;
		t_ = t;
	}

	@Override
	public VoyagerAction getAction()
	{
		final ArrayList<Planet> friendly = Voyager.playerPlanets( s_, player_ );
		final ArrayList<Planet> srcs = new ArrayList<Planet>();
		final ArrayList<Planet> dests = new ArrayList<Planet>();
		for( final Planet p : friendly ) {
			if( !Fn.contains( planets_, p.id ) ) {
				srcs.add( p );
			}
		}
		for( final Planet p : s_.planets ) {
			if( Fn.contains( planets_, p.id ) ) {
				dests.add( p );
			}
		}
		for( final Planet src : srcs ) {
			final int src_soldiers = src.population( Unit.Soldier );
			if( src_soldiers >= garrison_ + batch_ ) {
				int min_soldiers = Integer.MAX_VALUE;
				Planet dest = null;
				for( final Planet d : dests ) {
					final int dest_soldiers	= Voyager.effectivePopulation( s_, d, player_ )[Unit.Soldier.ordinal()];
					if( dest_soldiers < min_soldiers ) {
						min_soldiers = dest_soldiers;
						dest = d;
					}
				}
				if( dest != null ) {
					return new LaunchAction( src, dest, new int[] { 0, src_soldiers - garrison_ } );
				}
			}
		}
		// Nobody has spare soldiers; ensure soldiers are being produced
		int producing_soldiers = 0;
		for( final Planet q : friendly ) {
			if( q.nextProduced() == Unit.Soldier ) {
				producing_soldiers += 1;
			}
		}
		if( producing_soldiers == 0 ) {
			for( final Planet q : friendly ) {
				if( q.nextProduced() != Unit.Soldier
					&& q.population( Unit.Worker ) > 0 ) {
					return new SetProductionAction( q, Unit.Soldier );
				}
			}
		}
		else {
			for( final Planet p : friendly ) {
				if( p.nextProduced() != Unit.Soldier
					&& p.population( Unit.Worker ) >= p.capacity ) {
					return new SetProductionAction( p, Unit.Soldier );
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
		return "FortifyPolicy";
	}

}
