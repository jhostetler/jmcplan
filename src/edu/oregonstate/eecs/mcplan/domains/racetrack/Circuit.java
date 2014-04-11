/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racetrack;

import java.util.ArrayList;
import java.util.Arrays;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

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
	
	public Circuit( final GeometryFactory geom_factory,
					final int width, final int height,
					final Polygon track, final Polygon wall,
					final ArrayList<Polygon> sectors,
					final Coordinate start, final double orientation )
	{
		this.geom_factory = geom_factory;
		this.width = width;
		this.height = height;
		this.track = track;
		this.wall = wall;
		this.sectors = sectors;
		this.start = start;
		this.orientation = orientation;
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
			System.out.println( Arrays.toString( coords ) );
			double min_d = Double.MAX_VALUE;
			Point nearest = null;
			for( int i = 0; i < coords.length; ++i ) {
				final Point p = geom_factory.createPoint( coords[i] );
				System.out.println( p );
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
