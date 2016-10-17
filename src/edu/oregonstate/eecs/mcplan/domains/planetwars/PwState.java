/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.planetwars;

import java.util.ArrayList;
import java.util.Iterator;

import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.UnmodifiableUndirectedGraph;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.util.Generator;
import gnu.trove.list.TDoubleList;

/**
 * @author jhostetler
 *
 */
public class PwState implements State
{
	@SuppressWarnings( "serial" )
	public static class RouteEdge extends DefaultWeightedEdge
	{
		public final int route_id;
		
		public RouteEdge( final int route_id )
		{
			this.route_id = route_id;
		}
	}
	
	public static int Neutral = -1;
	public static int NumPlayers = 2;
	
	public final PwGame game;
	
	public final PwPlanet[] planets;
	public final PwRoute[] routes;
	
//	private final PwRoute[][] route_map;
	public final UnmodifiableUndirectedGraph<Integer, RouteEdge> route_graph;
	public final FloydWarshallShortestPaths<Integer, RouteEdge> shortest_paths;
	
	public final int width;
	public final int height;
	
	public int t = 0;
	
	public PwState( final PwGame game, final PwPlanet[] planets,
					final PwRoute[] routes, final int width, final int height )
	{
		this.game = game;
		this.planets = planets;
		this.routes = routes;
		this.width = width;
		this.height = height;
		
//		route_map = new PwRoute[planets.length][planets.length];
//		for( final PwRoute r : routes ) {
//			assert( route_map[r.a.id][r.b.id] == null );
//			assert( route_map[r.b.id][r.a.id] == null );
//			route_map[r.a.id][r.b.id] = route_map[r.b.id][r.a.id] = r;
//		}
		
		final SimpleWeightedGraph<Integer, RouteEdge> g
			= new SimpleWeightedGraph<Integer, RouteEdge>( RouteEdge.class );
		for( final PwPlanet p : planets ) {
			g.addVertex( p.id );
		}
		for( int i = 0; i < routes.length; ++i ) {
			final PwRoute r = routes[i];
			final RouteEdge e = new RouteEdge( i );
			g.addEdge( r.a.id, r.b.id, e );
			g.setEdgeWeight( e, r.length );
		}
		shortest_paths = new FloydWarshallShortestPaths<Integer, RouteEdge>( g );
		route_graph = new UnmodifiableUndirectedGraph<Integer, RouteEdge>( g );
	}
	
	public PwState( final PwState that )
	{
		this.game = that.game;
		this.planets = new PwPlanet[that.planets.length];
		for( int i = 0; i < planets.length; ++i ) {
			planets[i] = new PwPlanet( that.planets[i] );
		}
		this.routes = new PwRoute[that.routes.length];
		for( int i = 0; i < routes.length; ++i ) {
			routes[i] = new PwRoute( that.routes[i] );
		}
//		this.route_map = that.route_map;
		this.route_graph = that.route_graph;
		this.shortest_paths = that.shortest_paths;
		this.width = that.width;
		this.height = that.height;
		this.t = that.t;
	}
	
	public Generator<PwPlanet> neighbors( final PwPlanet p )
	{
		return new Generator<PwPlanet>() {
			final Iterable<RouteEdge> edges = route_graph.edgesOf( p.id );
			final Iterator<RouteEdge> itr = edges.iterator();
			
			@Override
			public boolean hasNext()
			{
				return itr.hasNext();
			}

			@Override
			public PwPlanet next()
			{
				final RouteEdge e = itr.next();
				final PwRoute r = routes[e.route_id];
				if( r.a.equals( p ) ) {
					return r.b;
				}
				else {
					return r.a;
				}
			}
		};
	}
	
	/**
	 * Returns the Route from p <-> q, or 'null' if p and q are not connected.
	 * @param p
	 * @param q
	 * @return
	 */
	public PwRoute route( final PwPlanet p, final PwPlanet q )
	{
		final RouteEdge e = route_graph.getEdge( p.id, q.id ); //[p.id][q.id];
		if( e == null ) {
			return null;
		}
		else {
			return routes[e.route_id];
		}
	}
	
	public ArrayList<PwRoute> routes( final PwPlanet planet )
	{
		final ArrayList<PwRoute> list = new ArrayList<PwRoute>();
		for( final PwRoute route : routes ) {
			if( route.a == planet || route.b == planet ) {
				list.add( route );
			}
		}
		return list;
	}
	
	public boolean connected( final PwPlanet a, final PwPlanet b )
	{
		for( final PwRoute route : routes ) {
			if( (route.a == a && route.b == b) || (route.a == b && route.b == a) ) {
				return true;
			}
		}
		return false;
	}
	
//	public double[] featureVector()
//	{
//		final TDoubleList fv = new TDoubleArrayList();
//
//		// Planet features
//		for( final PwPlanet p : planets ) {
//			addPlanetFeatures( p, fv );
//		}
//
//		// Ship features
//		final int ship_offset = fv.size();
//		final int Nplayer_ships = (game.max_eta + 1) * game.Nunits();
//		fv.addAll( new double[planets.length * PwPlayer.Ncompetitors * Nplayer_ships] );
//		final int planet_numbers = PwPlayer.Ncompetitors * Nplayer_ships;
//		// Format:
//		// for each planet -> for each player -> for each eta -> for each type
//		for( final PwShip ship : ships ) {
//			final int i = ship_offset // Offset to ship features
//						+ ship.dest.id * planet_numbers // Offset to dest planet
//						+ ship.owner.ordinal() * Nplayer_ships // Offset to player
//						+ ship.arrival_time * game.Nunits(); // Offset to eta
//			for( int t = 0; t < game.Nunits(); ++t ) {
//				fv.set( i + t, fv.get( i + t ) + ship.population[t] );
//			}
//		}
//
//		return fv.toArray();
//	}
	
	private void addPlanetFeatures( final PwPlanet p, final TDoubleList fv )
	{
		for( final PwPlayer player : PwPlayer.values() ) {
			if( p.owner() == player ) {
				fv.add( 1.0 );
			}
			else {
				fv.add( 0.0 );
			}
		}
		
		for( final PwPlayer player : PwPlayer.competitors ) {
			for( final PwUnit type : game.units() ) {
				fv.add( p.population( player, type ) );
			}
		}
		for( final PwUnit type : game.units() ) {
			fv.add( p.storedProduction( type ) );
		}
	}

	public int supply( final PwPlayer player )
	{
		int s = 0;
		for( final PwPlanet planet : planets ) {
			s += planet.supply( player );
		}
		for( final PwRoute route : routes ) {
			s += route.supply( player );
		}
		return s;
	}
	
	/**
	 * Returns:
	 * 	- PwPlayer.Min if Min has won
	 *  - PwPlayer.Max if Max has won
	 *  - PwPlayer.Neutral if the game was a draw
	 *  - null if the game is ongoing
	 * @return
	 */
	public PwPlayer winner()
	{
		final int pn = supply( PwPlayer.Min );
		final int pm = supply( PwPlayer.Max );
		if( pn == 0 && pm > 0 ) {
			return PwPlayer.Max;
		}
		else if( pm == 0 && pn > 0 ) {
			return PwPlayer.Min;
		}
		else if( pn == 0 && pm == 0 ) {
			return PwPlayer.Neutral;
		}
		else {
			return null;
		}
	}

	@Override
	public boolean isTerminal()
	{
		return t >= game.T || winner() != null;
	}

	@Override
	public void close()
	{ }
}
