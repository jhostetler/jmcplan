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
	
	public ShortestPathHeuristic( final RacegridState s, final double velocity )
	{
		velocity_ = velocity;
		width_ = s.width;
		height_ = s.height;
		
		final UndirectedGraph<Integer, DefaultEdge> g
			= new SimpleGraph<Integer, DefaultEdge>( DefaultEdge.class );
		
		final ArrayList<Integer> goals = new ArrayList<Integer>();
		for( int y = 0; y < s.height; ++y ) {
			for( int x = 0; x < s.width; ++x ) {
				final TerrainType t = s.terrain[y][x];
				if( t != TerrainType.Wall ) {
					final int cur = index( x, y );
					g.addVertex( cur );
					if( y > 0 && s.terrain[y-1][x] != TerrainType.Wall ) {
						g.addEdge( cur, index( x, y - 1 ) );
						if( x > 0 && s.terrain[y-1][x-1] != TerrainType.Wall ) {
							g.addEdge( cur, index( x - 1, y - 1 ) );
						}
					}
					if( x > 0 && s.terrain[y][x-1] != TerrainType.Wall ) {
						g.addEdge( cur, index( x - 1, y ) );
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
		return y*height_ + x;
	}
	
	@Override
	public double[] evaluate( final Simulator<RacegridState, RacegridAction> sim )
	{
		final RacegridState s = sim.state();
		return new double[] { -distance_[index( s.x, s.y )] / velocity_ };
	}
}
