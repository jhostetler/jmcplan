/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racegrid;

import java.util.ArrayList;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import edu.oregonstate.eecs.mcplan.search.EvaluationFunction;
import edu.oregonstate.eecs.mcplan.sim.Simulator;

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
	private final boolean allow_diagonals = false;
	
	public ShortestPathHeuristic( final RacegridState s, final double velocity )
	{
		velocity_ = velocity;
		width_ = s.width;
		height_ = s.height;
		
		final UndirectedGraph<Integer, DefaultEdge> g
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
							g.addEdge( cur, index( x - 1, y - 1 ) );
						}
					}
					
					if( t == TerrainType.Goal ) {
						goals.add( cur );
					}
				}
			}
		}
		
		final FloydWarshallShortestPaths<Integer, DefaultEdge> paths
			= new FloydWarshallShortestPaths<Integer, DefaultEdge>( g );
		
		distance_ = new double[s.width*s.height];
		for( final Integer v : g.vertexSet() ) {
			int min_d = Integer.MAX_VALUE;
			for( final Integer goal : goals ) {
				final int d = (int) paths.shortestDistance( v, goal );
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
