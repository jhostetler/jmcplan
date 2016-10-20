/* LICENSE
Copyright (c) 2013-2016, Jesse Hostetler (jessehostetler@gmail.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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
