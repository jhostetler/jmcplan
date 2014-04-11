/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.util.ArrayList;

/**
 * @author jhostetler
 *
 */
public class Maps
{
	public static VoyagerState Generic9( final VoyagerParameters params )
	{
		final ArrayList<Planet> planets = new ArrayList<Planet>();
		final int capacity = 6;
		final int[] initial_population = new int[Unit.values().length];
		initial_population[Unit.Worker.ordinal()] = 1;
		final Unit initial_production = Unit.Worker;
		final int Nplanets = 9;
		// TODO: Would prefer to calculate max_eta from the planets, but you
		// need the VoyagerHash object to make the planets!
		final int max_eta = (int) Math.ceil( Math.sqrt( 14*14 + 16*16 ) / params.ship_speed );
		final VoyagerHash hash = new VoyagerHash( params, Nplanets, max_eta );
		
		// NOTE: If you change planet positions, you might need to change
		// the 'max_eta' calculation above.
		planets.add( new Planet.Builder().id( PlanetId.Main_Min ).hash( hash ).capacity( capacity )
										 .x( -7 ).y( -8 ).owner( Player.Min ).finish() );
		planets.get( PlanetId.Main_Min ).setPopulation( Player.Min, initial_population ).setProduction( initial_production );
		planets.add( new Planet.Builder().id( PlanetId.Main_Max ).hash( hash ).capacity( capacity )
										 .x( 7 ).y( 8 ).owner( Player.Max ).finish() );
		planets.get( PlanetId.Main_Max ).setPopulation( Player.Max, initial_population ).setProduction( initial_production );
		planets.add( new Planet.Builder().id( PlanetId.Natural_2nd_Min ).hash( hash ).capacity( capacity )
										 .x( -4 ).y( -5 ).owner( Player.Neutral ).finish() );
		planets.add( new Planet.Builder().id( PlanetId.Natural_2nd_Max ).hash( hash ).capacity( capacity )
										 .x( 4 ).y( 5 ).owner( Player.Neutral ).finish() );
		planets.add( new Planet.Builder().id( PlanetId.Natural_3rd_Min ).hash( hash ).capacity( capacity )
										 .x( -9 ).y( -2 ).owner( Player.Neutral ).finish() );
		planets.add( new Planet.Builder().id( PlanetId.Natural_3rd_Max ).hash( hash ).capacity( capacity )
										 .x( 9 ).y( 2 ).owner( Player.Neutral ).finish() );
		planets.add( new Planet.Builder().id( PlanetId.Wing_Min ).hash( hash ).capacity( capacity )
										 .x( -6 ).y( 4 ).owner( Player.Neutral ).finish() );
		planets.add( new Planet.Builder().id( PlanetId.Wing_Max ).hash( hash ).capacity( capacity )
										 .x( 6 ).y( -4 ).owner( Player.Neutral ).finish() );
		planets.add( new Planet.Builder().id( PlanetId.Center ).hash( hash ).capacity( capacity )
										 .x( 0 ).y( 0 ).owner( Player.Neutral ).finish() );
		
		final VoyagerState initState = new VoyagerState(
			planets.toArray( new Planet[] { } ), Player.values(), 18, 16, hash );
		return initState;
	}
}
