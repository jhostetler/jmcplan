/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.planetwars;

import java.util.ArrayList;

/**
 * @author jhostetler
 *
 */
public class PwMaps
{
	public static PwState Generic9( final PwGame game )
	{
		int idx = 0;
		final int Main_Min = idx++;
		final int Main_Max = idx++;
		final int Natural_2nd_Min = idx++;
		final int Natural_2nd_Max = idx++;
		final int Natural_3rd_Min = idx++;
		final int Natural_3rd_Max = idx++;
		final int Wing_Min = idx++;
		final int Wing_Max = idx++;
		final int Center = idx++;
		
		// Planets
		final PwPlanet[] planets = new PwPlanet[idx];
		planets[Main_Min] = game.createCapitolPlanet( Main_Min, -7, -8, PwPlayer.Min );
		planets[Main_Max] = game.createCapitolPlanet( Main_Max, 7, 8, PwPlayer.Max );
		planets[Natural_2nd_Min] = game.createNeutralPlanet( Natural_2nd_Min, -4, -5 );
		planets[Natural_2nd_Max] = game.createNeutralPlanet( Natural_2nd_Max, 4, 5 );
		planets[Natural_3rd_Min] = game.createNeutralPlanet( Natural_3rd_Min, -9, -2 );
		planets[Natural_3rd_Max] = game.createNeutralPlanet( Natural_3rd_Max, 9, 2 );
		planets[Wing_Min] = game.createNeutralPlanet( Wing_Min, -6, 4 );
		planets[Wing_Max] = game.createNeutralPlanet( Wing_Max, 6, -4 );
		planets[Center] = game.createNeutralPlanet( Center, 0, 0 );
		final int width = 9*2;
		final int height = 8*2;
		
		// Routes
		final ArrayList<PwRoute> routes = new ArrayList<PwRoute>();
		routes.add( new PwRoute( game, planets[Main_Min], planets[Natural_2nd_Min] ) );
		routes.add( new PwRoute( game, planets[Main_Min], planets[Natural_3rd_Min] ) );
		routes.add( new PwRoute( game, planets[Natural_2nd_Min], planets[Natural_3rd_Min] ) );
		routes.add( new PwRoute( game, planets[Main_Max], planets[Natural_2nd_Max] ) );
		routes.add( new PwRoute( game, planets[Main_Max], planets[Natural_3rd_Max] ) );
		routes.add( new PwRoute( game, planets[Natural_2nd_Max], planets[Natural_3rd_Max] ) );
		routes.add( new PwRoute( game, planets[Natural_2nd_Min], planets[Wing_Min] ) );
		routes.add( new PwRoute( game, planets[Natural_2nd_Min], planets[Wing_Max] ) );
		routes.add( new PwRoute( game, planets[Natural_2nd_Min], planets[Center] ) );
		routes.add( new PwRoute( game, planets[Natural_3rd_Min], planets[Wing_Min] ) );
		routes.add( new PwRoute( game, planets[Natural_3rd_Min], planets[Wing_Max] ) );
		routes.add( new PwRoute( game, planets[Natural_3rd_Min], planets[Center] ) );
		routes.add( new PwRoute( game, planets[Natural_2nd_Max], planets[Wing_Min] ) );
		routes.add( new PwRoute( game, planets[Natural_2nd_Max], planets[Wing_Max] ) );
		routes.add( new PwRoute( game, planets[Natural_2nd_Max], planets[Center] ) );
		routes.add( new PwRoute( game, planets[Natural_3rd_Max], planets[Wing_Min] ) );
		routes.add( new PwRoute( game, planets[Natural_3rd_Max], planets[Wing_Max] ) );
		routes.add( new PwRoute( game, planets[Natural_3rd_Max], planets[Center] ) );
		routes.add( new PwRoute( game, planets[Wing_Min], planets[Center] ) );
		routes.add( new PwRoute( game, planets[Wing_Max], planets[Center] ) );
		
		final PwState s = new PwState(
			game, planets, routes.toArray( new PwRoute[routes.size()] ), width, height );
		return s;
	}
}
