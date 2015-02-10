/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racegrid;

import java.util.ArrayList;

import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import edu.oregonstate.eecs.mcplan.search.EvaluationFunction;
import edu.oregonstate.eecs.mcplan.sim.Simulator;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class ShortestPathHeuristic implements EvaluationFunction<RacegridState, RacegridAction>
{
	private final double[] distance_;
	private final double velocity_;
	
	private final int height_;
	private final int width_;
	
	private final ArrayList<Integer> goals = new ArrayList<Integer>();
	
	// TODO: Make this a parameter
	private final boolean allow_diagonals = true;
	
	/**
	 * Heuristic with default max velocity calculated as the maximum distance
	 * in the graph. This method of calculating velocity implies that
	 * evaluate() is in [-1, 0].
	 * @param s
	 */
	public ShortestPathHeuristic( final RacegridState s )
	{
		width_ = s.width;
		height_ = s.height;
		distance_ = new double[s.width*s.height];
		createGraph( s );
		velocity_ = Fn.max( distance_ );
	}
	
	/**
	 * Heuristic with user-supplied max velocity. The heuristic value is
	 * 	-path_length / velocity
	 * @param s
	 * @param velocity
	 */
	public ShortestPathHeuristic( final RacegridState s, final double velocity )
	{
		velocity_ = velocity;
		width_ = s.width;
		height_ = s.height;
		distance_ = new double[s.width*s.height];
		createGraph( s );
	}
	
	private void createGraph( final RacegridState s )
	{
		final SimpleGraph<Integer, DefaultEdge> g
			= new SimpleGraph<Integer, DefaultEdge>( DefaultEdge.class );
		
		for( int y = 0; y < s.height; ++y ) {
			for( int x = 0; x < s.width; ++x ) {
				final TerrainType t = s.terrain[y][x];
				if( t != TerrainType.Wall ) {
					final int cur = index( x, y );
					g.addVertex( cur );
					if( x > 0 && s.terrain[y][x-1] != TerrainType.Wall ) {
						g.addEdge( cur, index( x - 1, y ) );
					}
					if( y > 0 && s.terrain[y-1][x] != TerrainType.Wall ) {
						g.addEdge( cur, index( x, y - 1 ) );
					}
					if( allow_diagonals ) {
						if( x > 0 && y > 0 && s.terrain[y-1][x-1] != TerrainType.Wall ) {
							final DefaultEdge e = g.addEdge( cur, index( x - 1, y - 1 ) );
//							g.setEdgeWeight( e, Math.sqrt( 2 ) );
						}
					}
					
					if( t == TerrainType.Goal ) {
						goals.add( cur );
					}
				}
			}
		}
		
//		for( final Integer goal : goals ) {
//			System.out.println( "Searching from goal " + goal );
//			final BellmanFordShortestPath<Integer, DefaultEdge> paths
//				= new BellmanFordShortestPath<Integer, DefaultEdge>( g, goal );
//			for( final Integer v : g.vertexSet() ) {
//				if( !goals.contains( v ) ) {
//					final double d = paths.getCost( v );
//					distance_[v] = Math.min( distance_[v], d );
//				}
//			}
//		}
//
//		System.out.println( "Finished constructing heuristic!" );
		
		final FloydWarshallShortestPaths<Integer, DefaultEdge> paths
			= new FloydWarshallShortestPaths<Integer, DefaultEdge>( g );

		for( final Integer v : g.vertexSet() ) {
			double min_d = Double.MAX_VALUE;
			for( final Integer goal : goals ) {
				final double d = paths.shortestDistance( v, goal );
				if( d < min_d ) {
					min_d = d;
				}
			}
			distance_[v] = min_d;
		}
	}
	
	private ShortestPathHeuristic( final ShortestPathHeuristic that )
	{
		velocity_ = that.velocity_;
		width_ = that.width_;
		height_ = that.height_;
		distance_ = that.distance_;
	}
	
	public ShortestPathHeuristic create()
	{
		return new ShortestPathHeuristic( this );
	}
	
	private int index( final int x, final int y )
	{
		return y*width_ + x;
	}
	
	public double evaluate( final RacegridState s )
	{
		if( s.goal ) {
			return 0;
		}
		return -distance_[index( s.x, s.y )] / velocity_;
	}
	
	@Override
	public double[] evaluate( final Simulator<RacegridState, RacegridAction> sim )
	{
		final RacegridState s = sim.state();
		if( s.goal ) {
			return new double[] { 0 };
		}
		
//		final double[] v = new double[] { s.dx, s.dy };
//		final double[] d = new double[] { 0, 0 };
//		for( final int[] g : s.goals ) {
//			d[0] += g[0];
//			d[1] += g[1];
//		}
//		d[0] /= s.goals.size();
//		d[1] /= s.goals.size();
//		d[0] -= s.x;
//		d[1] -= s.y;
//		final double vd = Fn.scalar_projection( v, d );
//		return new double[] { -distance_[index( s.x, s.y )] / (1.0 + vd) };
		
		return new double[] { -distance_[index( s.x, s.y )] / velocity_ };
	}
}
