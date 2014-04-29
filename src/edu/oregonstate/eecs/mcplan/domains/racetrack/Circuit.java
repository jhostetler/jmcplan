/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racetrack;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.distance.DistanceOp;

import edu.oregonstate.eecs.mcplan.util.Tuple.Tuple2;

/**
 * @author jhostetler
 *
 */
public class Circuit
{
	public final int width;
	public final int height;
	public final Polygon track;
	public final Polygon wall;
	public final ArrayList<Polygon> sectors;
	public final Coordinate start;
	public final double orientation;
	
	public final GeometryFactory geom_factory;
	
	private final ArrayList<Polygon> segments_ = new ArrayList<Polygon>();
	private final ArrayList<Point> reference_points_ = new ArrayList<Point>();
	public final double[] circuit_length;
	
	public Circuit( final GeometryFactory geom_factory,
					final int width, final int height,
					final Polygon track, final Polygon wall,
					final ArrayList<Polygon> sectors,
					final double orientation )
	{
		this.geom_factory = geom_factory;
		this.width = width;
		this.height = height;
		this.track = track;
		this.wall = wall;
		this.sectors = sectors;
		this.orientation = orientation;
		
		assert( track.getNumInteriorRing() == 1 );
		final LineString outer_track = track.getExteriorRing();
		final LineString inner_track = track.getInteriorRingN( 0 );
		assert( outer_track.getNumPoints() == inner_track.getNumPoints() );
		// numPoints - 1 because the start point is duplicated in the list
		for( int i = 0; i < outer_track.getNumPoints() - 1; ++i ) {
			final int next = (i + 1) % outer_track.getNumPoints();
			final Coordinate[] coords = new Coordinate[5];
			int idx = 0;
			Point p = outer_track.getPointN( i );
			coords[idx++] = new Coordinate( p.getX(), p.getY() );
			p = outer_track.getPointN( next );
			coords[idx++] = new Coordinate( p.getX(), p.getY() );
			p = inner_track.getPointN( next );
			coords[idx++] = new Coordinate( p.getX(), p.getY() );
			p = inner_track.getPointN( i );
			coords[idx++] = new Coordinate( p.getX(), p.getY() );
			coords[idx++] = new Coordinate( coords[0] );
			assert( idx == coords.length );
			final LinearRing r = geom_factory.createLinearRing( coords );
			segments_.add( geom_factory.createPolygon( r, new LinearRing[] { } ) );
		}
		
		for( int i = 0; i < segments_.size(); ++i ) {
			final Polygon poly = segments_.get( i );
			final int next = (i + 1) % segments_.size();
			final Polygon next_poly = segments_.get( next );
			final LineString ray = geom_factory.createLineString( new Coordinate[] {
				new Coordinate( poly.getCentroid().getCoordinate() ),
				new Coordinate( next_poly.getCentroid().getCoordinate() )
			} );
			reference_points_.add( (Point) ray.intersection( next_poly.getBoundary() ) );
		}
		this.start = reference_points_.get( reference_points_.size() - 1 ).getCoordinate();
		
		circuit_length = new double[segments_.size()];
		double d = 0.0;
		Point lag = reference_points_.get( segments_.size() - 1 );
		for( int i = segments_.size() - 2; i >= 0; --i ) {
//			final Polygon poly = segments_.get( i );
			final Point p = reference_points_.get( i );
			d += p.distance( lag );
			circuit_length[i] = d;
			System.out.println( "circuit_length[" + i + "] = " + d );
			lag = p;
		}
		d += reference_points_.get( reference_points_.size() - 1 ).distance( lag );
		circuit_length[reference_points_.size() - 1] = d; // Total distance;
	}
	
	public Tuple2<Integer, Point> nearestTrackPoint( final double x, final double y )
	{
		final Point p = geom_factory.createPoint( new Coordinate( x, y ) );
		double best_d = Double.MAX_VALUE;
		int best_i = 0;
		Point best_p = null;
		for( int i = 0; i < segments_.size(); ++i ) {
			final int j = i % segments_.size();
//			System.out.println( "Segment " + j );
			final Polygon seg = segments_.get( j );
			// Find all closest points in segment
			final Coordinate[] points = DistanceOp.closestPoints( p, seg );
			// Among them, find the one that is closest to the center of
			// the *next* segment
			double best_coord_d = Double.MAX_VALUE;
			Point best_coord = null;
			final int next = (j + 1) % segments_.size();
			final Point next_center = reference_points_.get( next ); //segments_.get( next ).getCentroid();
			// Skip the first one, which is always the query point.
			for( int k = 1; k < points.length; ++k ) {
				final Coordinate c = points[k];
//				System.out.println( c );
				final Point cp = geom_factory.createPoint( c );
				final double d = cp.distance( next_center );
				if( d < best_coord_d ) {
					best_coord_d = d;
					best_coord = cp;
				}
			}
//			System.out.println( "Best coord = " + best_coord );
			// See if the preferred point is closer to the car
			final double d = best_coord.distance( p );
			if( d < best_d ) {
				best_d = d;
				best_i = j;
				best_p = best_coord;
			}
		}
		return Tuple2.of( best_i, best_p );
	}
	
	public int segment( final double x, final double y )
	{
		final Point p = geom_factory.createPoint( new Coordinate( x, y ) );
		for( int i = 0; i < segments_.size(); ++i ) {
			final Polygon poly = segments_.get( i );
			if( poly.contains( p ) ) { //|| poly.getBoundary().contains( p ) ) {
				return i;
			}
		}
		return -1;
	}
	
	public ArrayList<Polygon> segments()
	{
		return segments_;
	}
	
	public ArrayList<Point> reference_points()
	{
		return reference_points_;
	}
	
	private Point hits( final LineString path, final Polygon poly )
	{
		final Geometry intersection = path.intersection( poly.getBoundary() );
		if( intersection.isEmpty() ) {
			return null;
		}
		else {
			final Point origin = path.getStartPoint();
			final Coordinate[] coords = intersection.getCoordinates();
//			System.out.println( Arrays.toString( coords ) );
			double min_d = Double.MAX_VALUE;
			Point nearest = null;
			for( int i = 0; i < coords.length; ++i ) {
				final Point p = geom_factory.createPoint( coords[i] );
//				System.out.println( p );
				final double d = origin.distance( p );
				if( d < min_d ) {
					min_d = d;
					nearest = p;
				}
			}
			return nearest;
		}
	}
	
	public Point crossesTrackBoundary( final LineString path )
	{
		return hits( path, track );
	}
	
	public Point hitsWall( final LineString path )
	{
		return hits( path, wall );
	}
}
