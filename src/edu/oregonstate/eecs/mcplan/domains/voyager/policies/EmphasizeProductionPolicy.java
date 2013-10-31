package edu.oregonstate.eecs.mcplan.domains.voyager.policies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.domains.voyager.NothingAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.Planet;
import edu.oregonstate.eecs.mcplan.domains.voyager.Player;
import edu.oregonstate.eecs.mcplan.domains.voyager.SetProductionAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.Unit;
import edu.oregonstate.eecs.mcplan.domains.voyager.Voyager;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerState;

/**
 * Switches planets' production to the specified type, in order of how much
 * time they've invested on their current project.
 */
public class EmphasizeProductionPolicy extends AnytimePolicy<VoyagerState, VoyagerAction>
{
	private final Player self_;
	private final Unit type_;
	
	private VoyagerState s_ = null;
	private long t_ = 0;
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder( 37, 53 ).append( self_ ).append( type_ ).toHashCode();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
//		System.out.println( "EmphasizeProductionPolicy.equals()" );
		if( obj == null || !(obj instanceof EmphasizeProductionPolicy) ) {
			return false;
		}
		final EmphasizeProductionPolicy that = (EmphasizeProductionPolicy) obj;
//		System.out.println( "\t" + toString() + ".equals( " + that.toString() + " )" );
		return self_ == that.self_
			   && type_ == that.type_;
	}
	
	public EmphasizeProductionPolicy( final Player self, final Unit type )
	{
		self_ = self;
		type_ = type;
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
		final ArrayList<Planet> friendly_planets = Voyager.playerPlanets( s_, self_ );
		
		Collections.sort( friendly_planets, new Comparator<Planet>() {
			@Override public int compare( final Planet a, final Planet b )
			{
				final double d = Voyager.investment( a, a.nextProduced() )
								 - Voyager.investment( b, b.nextProduced() );
				return (int) Math.signum( d );
			}
		} );
		
		for( final Planet p : friendly_planets ) {
			if( p.nextProduced() != type_ ) {
				return new SetProductionAction( p, type_ );
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
		return "EmphasizeProduction" + self_.id + "[" + type_.toString() + "]";
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
	public VoyagerAction getAction( final long control )
	{
		return getAction();
	}

}
