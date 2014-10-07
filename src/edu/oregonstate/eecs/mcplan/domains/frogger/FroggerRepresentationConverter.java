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
