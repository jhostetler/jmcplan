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
package edu.oregonstate.eecs.mcplan.domains.frogger;

import java.io.File;
import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import edu.oregonstate.eecs.mcplan.abstraction.WekaUtil;

/**
 * @author jhostetler
 *
 */
public class FroggerRepresentationConverter
{
	public static Instances absoluteToRelative( final FroggerParameters params, final Instances src, final int vision )
	{
		final ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add( new Attribute( "x" ) );
		attributes.add( new Attribute( "y" ) );
		for( int i = vision; i >= -vision; --i ) {
			for( int j = -vision; j <= vision; ++j ) {
				if( i == 0 && j == 0 ) {
					continue;
				}
				
				final String name = "car_x" + (j >= 0 ? "+" : "") + j + "_y" + (i >= 0 ? "+" : "") + i;
				attributes.add( new Attribute( name ) );
			}
		}
		attributes.add( src.classAttribute() );
		
		final Instances dest = new Instances( src.relationName() + "_relative", attributes, src.size() );
		for( final Instance inst : src ) {
			final double[] phi = new double[attributes.size()];
			int idx = 0;
			
			final int x = (int) inst.value( 0 );
			final int y = (int) inst.value( 1 );
			phi[idx++] = x;
			phi[idx++] = y;
			
			for( int i = vision; i >= -vision; --i ) {
				for( int j = -vision; j <= vision; ++j ) {
					if( i == 0 && j == 0 ) {
						continue;
					}
					
					final int xoff = x + j;
					final int yoff = y + i;
					
					if( xoff >= 0 && xoff < params.road_length && yoff >= 1 && yoff <= params.lanes ) {
						final int car = (int) inst.value( 2 + (yoff - 1)*params.road_length + xoff );
						phi[idx] = car; // s.grid[dy][dx] == Tile.Car ? 1.0 : 0.0; // fv[2 + (dy-1)*road_length + dx]
					}
					idx += 1;
				}
			}
			
			phi[idx++] = inst.classValue();
			assert( idx == phi.length );
			
			WekaUtil.addInstance( dest, new DenseInstance( inst.weight(), phi ) );
		}
		
		return dest;
	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] argv )
	{
		final File src_file = new File( argv[0] );
		final Instances src = WekaUtil.readLabeledDataset( src_file );
		
		final Instances dest = absoluteToRelative( new FroggerParameters(), src, 3 );
		WekaUtil.writeDataset( src_file.getParentFile(), dest );
	}
}
