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
package edu.oregonstate.eecs.mcplan.domains.racetrack;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * @author jhostetler
 *
 */
public class Circuits
{
	private static final double one_billion = 1000000000.0;
	
	/**
	 * Drag string with no run-off space.
	 * @param width
	 * @param length
	 * @return
	 */
	public static Circuit DragStrip( final int width, final int length )
	{
		final GeometryFactory gfact = new GeometryFactory( new PrecisionModel( 10 * one_billion ) );
		final Coordinate[] coords = new Coordinate[] {
			new Coordinate( 0, 0 ),
			new Coordinate( 0, length ),
			new Coordinate( width, length ),
			new Coordinate( width, 0 ),
			new Coordinate( 0, 0 )
		};
		final LinearRing ring = gfact.createLinearRing( coords );
		final Polygon poly = gfact.createPolygon( ring, new LinearRing[] { } );
		final ArrayList<Polygon> sectors = new ArrayList<Polygon>();
		return new Circuit( gfact, width, length, poly, poly, sectors, 0 );
	}
	
	private static LinearRing createCircle( final GeometryFactory gfact, final double cx, final double cy,
											final double radius, final int subdivisions )
	{
		final Coordinate[] coords = new Coordinate[subdivisions + 1];
		coords[0] = new Coordinate( cx + radius * Math.cos( 0 ), cy + radius * Math.sin( 0 ) );
		for( int i = 1; i < subdivisions; ++i ) {
			final double theta = 2*Math.PI * i / subdivisions;
			coords[i] = new Coordinate( cx + radius * Math.cos( theta ), cy + radius * Math.sin( theta ) );
		}
		coords[subdivisions] = new Coordinate( coords[0] );
		final LinearRing ring = gfact.createLinearRing( coords );
		return ring;
	}
	
	public static Circuit Donut( final int inner_radius, final int outer_radius, final int subdivisions )
	{
		final int width = 2*(outer_radius + 10);
		final int height = 2*(outer_radius + 10);
		final GeometryFactory gfact = new GeometryFactory( new PrecisionModel( 10 * one_billion ) );
		final double r = outer_radius + 10;
		final LinearRing outer = createCircle( gfact, r, r, outer_radius, subdivisions );
		final LinearRing inner = createCircle( gfact, r, r, inner_radius, subdivisions );
		final LinearRing wall = createCircle( gfact, r, r, outer_radius + 10, subdivisions );
		final double start_r = inner_radius + ((outer_radius - inner_radius) / 2.0);
		final ArrayList<Polygon> sectors = new ArrayList<>();
		final Coordinate[] bounds = new Coordinate[] {
			new Coordinate( 0, 0 ),
			new Coordinate( width, 0 ),
			new Coordinate( width, height ),
			new Coordinate( 0, height )
		};
		sectors.add( gfact.createPolygon( gfact.createLinearRing( bounds ), new LinearRing[] { } ) );
		return new Circuit( gfact, width, height,
							gfact.createPolygon( outer, new LinearRing[] { inner } ),
							gfact.createPolygon( wall, new LinearRing[] { } ),
							sectors, -Math.PI / 2 );
	}
	
	public static Circuit PaperClip( final int length, final int width )
	{
		final GeometryFactory gfact = new GeometryFactory( new PrecisionModel( 10 * one_billion ) );
		final double radius = width / 2.0;
		final LinearRing wall = gfact.createLinearRing( new Coordinate[] {
			new Coordinate( 0, 0 ), new Coordinate( length, 0 ),
			new Coordinate( length, width ), new Coordinate( 0, width ),
			new Coordinate( 0, 0 )
		} );
		final int subdivisions = 16;
		final int Nsegments = 2*subdivisions + 2*2 + 1;
		
		final Coordinate[] outer_coords = new Coordinate[Nsegments];
		int idx = 0;
		outer_coords[idx++] = new Coordinate( length/2, 0 );
		outer_coords[idx++] = new Coordinate( length - radius, 0 );
		for( int i = 1; i < subdivisions; ++i ) {
			final double theta = -Math.PI/2 + (Math.PI * i / subdivisions);
			outer_coords[idx++] = new Coordinate( length - radius + radius * Math.cos( theta ),
											      radius + radius * Math.sin( theta ) );
		}
		outer_coords[idx++] = new Coordinate( length - radius, width );
		outer_coords[idx++] = new Coordinate( length/2, width );
		outer_coords[idx++] = new Coordinate( radius, width );
		for( int i = 1; i < subdivisions; ++i ) {
			final double theta = Math.PI/2 + (Math.PI * i / subdivisions);
			outer_coords[idx++] = new Coordinate( radius + radius * Math.cos( theta ),
											      radius + radius * Math.sin( theta ) );
		}
		outer_coords[idx++] = new Coordinate( radius, 0 );
		outer_coords[idx++] = new Coordinate( outer_coords[0] );
		assert( idx == Nsegments );
		final LinearRing outer = gfact.createLinearRing( outer_coords );
		
		final Coordinate[] inner_coords = new Coordinate[Nsegments];
		final int track_width = 20;
		idx = 0;
		inner_coords[idx++] = new Coordinate( length/2, track_width  );
		inner_coords[idx++] = new Coordinate( length - radius, track_width );
		for( int i = 1; i < subdivisions; ++i ) {
			final double theta = -Math.PI/2 + (Math.PI * i / subdivisions);
			inner_coords[idx++] = new Coordinate( length - radius + (radius - track_width) * Math.cos( theta ),
											      radius + (radius - track_width) * Math.sin( theta ) );
		}
		inner_coords[idx++] = new Coordinate( length - radius, width - track_width );
		inner_coords[idx++] = new Coordinate( length/2, width - track_width );
		inner_coords[idx++] = new Coordinate( radius, width - track_width );
		for( int i = 1; i < subdivisions; ++i ) {
			final double theta = Math.PI/2 + (Math.PI * i / subdivisions);
			inner_coords[idx++] = new Coordinate( radius + (radius - track_width) * Math.cos( theta ),
											      radius + (radius - track_width) * Math.sin( theta ) );
		}
		inner_coords[idx++] = new Coordinate( radius, track_width );
		inner_coords[idx++] = new Coordinate( inner_coords[0] );
		assert( idx == Nsegments );
		final LinearRing inner = gfact.createLinearRing( inner_coords );
		
		final Coordinate[] inner_wall = new Coordinate[Nsegments];
		final int runoff = track_width + 20;
		idx = 0;
		inner_wall[idx++] = new Coordinate( length/2, runoff );
		inner_wall[idx++] = new Coordinate( length - radius, runoff );
		for( int i = 1; i < subdivisions; ++i ) {
			final double theta = -Math.PI/2 + (Math.PI * i / subdivisions);
			inner_wall[idx++] = new Coordinate( length - radius + (radius - runoff) * Math.cos( theta ),
											      radius + (radius - runoff) * Math.sin( theta ) );
		}
		inner_wall[idx++] = new Coordinate( length - radius, width - runoff );
		inner_wall[idx++] = new Coordinate( length/2, width - runoff );
		inner_wall[idx++] = new Coordinate( radius, width - runoff );
		for( int i = 1; i < subdivisions; ++i ) {
			final double theta = Math.PI/2 + (Math.PI * i / subdivisions);
			inner_wall[idx++] = new Coordinate( radius + (radius - runoff) * Math.cos( theta ),
											      radius + (radius - runoff) * Math.sin( theta ) );
		}
		inner_wall[idx++] = new Coordinate( radius, runoff  );
		inner_wall[idx++] = new Coordinate( inner_wall[0] );
		assert( idx == Nsegments );
		final LinearRing middle = gfact.createLinearRing( inner_wall );
		
		final ArrayList<Polygon> sectors = new ArrayList<Polygon>();
//		final Coordinate[] s0 = new Coordinate[5];
//		idx = 0;
//		s0[idx++] = new Coordinate( length / 2, 0 );
//		s0[idx++] = new Coordinate( length, 0 );
//		s0[idx++] = new Coordinate( length, width );
//		s0[idx++] = new Coordinate( length / 2, width );
//		s0[idx++] = new Coordinate( s0[0] );
		sectors.add( makeRectangle( gfact, length/2, 0, length, width/2 ) );
//		final Coordinate[] s1 = new Coordinate[5];
//		idx = 0;
//		s1[idx++] = new Coordinate( 0, 0 );
//		s1[idx++] = new Coordinate( length / 2, 0 );
//		s1[idx++] = new Coordinate( length / 2, width );
//		s1[idx++] = new Coordinate( 0, width );
//		s1[idx++] = new Coordinate( s1[0] );
		sectors.add( makeRectangle( gfact, length/2, width/2, length, width ) );
		sectors.add( makeRectangle( gfact, 0, width/2, length/2, width ) );
		sectors.add( makeRectangle( gfact, 0, 0, length/2, width/2 ) );
		
		return new Circuit( gfact, length, width,
							gfact.createPolygon( outer, new LinearRing[] { inner } ),
							gfact.createPolygon( wall, new LinearRing[] { middle } ),
							sectors, 0 );
	}
	
	private static Polygon makeRectangle( final GeometryFactory gfact,
								   final double x1, final double y1, final double x2, final double y2 )
	{
		final Coordinate[] r = new Coordinate[5];
		int idx = 0;
		r[idx++] = new Coordinate( x1, y1 );
		r[idx++] = new Coordinate( x2, y1 );
		r[idx++] = new Coordinate( x2, y2 );
		r[idx++] = new Coordinate( x1, y2 );
		r[idx++] = new Coordinate( r[0] );
		return gfact.createPolygon( gfact.createLinearRing( r ), new LinearRing[] { } );
	}
}
