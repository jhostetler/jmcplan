/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.tetris;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.ArrayFactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public class TetrisGroundRepresenter implements FactoredRepresenter<TetrisState, FactoredRepresentation<TetrisState>>
{
	private static ArrayList<Attribute> attributes;
	static {
		attributes = new ArrayList<Attribute>();
		for( final TetrominoType t : TetrominoType.values() ) {
			attributes.add( new Attribute( "type" + t ) );
		}
		attributes.add( new Attribute( "rotation" ) );
		for( int y = 0; y < TetrisState.Nrows; ++y ) {
			for( int x = 0; x < TetrisState.Ncolumns; ++x ) {
				attributes.add( new Attribute( "b" + y + "_" + x ) );
			}
		}
	}
	
	@Override
	public FactoredRepresenter<TetrisState, FactoredRepresentation<TetrisState>> create()
	{
		return new TetrisGroundRepresenter();
	}

	@Override
	public FactoredRepresentation<TetrisState> encode( final TetrisState s )
	{
		final double[] phi = new double[attributes.size()];
		final Tetromino t = s.getCurrentTetromino();
		phi[t.type.ordinal()] = 1;
		int idx = TetrominoType.values().length;
		phi[idx++] = t.rotation;
		for( int y = 0; y < TetrisState.Nrows; ++y ) {
			for( int x = 0; x < TetrisState.Ncolumns; ++x ) {
				if( s.cells[y][x] > 0 ) {
					phi[idx] = 1;
				}
				++idx;
			}
		}
		assert( idx == attributes.size() );
		return new ArrayFactoredRepresentation<TetrisState>( phi );
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes;
	}
}
