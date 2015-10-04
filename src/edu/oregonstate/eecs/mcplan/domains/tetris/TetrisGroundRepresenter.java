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
public final class TetrisGroundRepresenter implements FactoredRepresenter<TetrisState, FactoredRepresentation<TetrisState>>
{
	private final ArrayList<Attribute> attributes;
	
	public TetrisGroundRepresenter( final TetrisParameters params )
	{
		attributes = new ArrayList<Attribute>();
		for( final TetrominoType t : TetrominoType.values() ) {
			attributes.add( new Attribute( "type" + t ) );
		}
		attributes.add( new Attribute( "tx" ) );
		attributes.add( new Attribute( "rotation" ) );
		for( int y = 0; y < params.Nrows; ++y ) {
			for( int x = 0; x < params.Ncolumns; ++x ) {
				attributes.add( new Attribute( "b" + y + "_" + x ) );
			}
		}
	}
	
	private TetrisGroundRepresenter( final TetrisGroundRepresenter that )
	{
		this.attributes = that.attributes;
	}
	
	@Override
	public FactoredRepresenter<TetrisState, FactoredRepresentation<TetrisState>> create()
	{
		return new TetrisGroundRepresenter( this );
	}

	@Override
	public FactoredRepresentation<TetrisState> encode( final TetrisState s )
	{
		final float[] phi = new float[attributes.size()];
		final Tetromino t = s.getCurrentTetromino();
		phi[t.type.ordinal()] = 1;
		int idx = TetrominoType.values().length;
		phi[idx++] = t.x;
		phi[idx++] = t.rotation;
		for( int y = 0; y < s.params.Nrows; ++y ) {
			for( int x = 0; x < s.params.Ncolumns; ++x ) {
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
