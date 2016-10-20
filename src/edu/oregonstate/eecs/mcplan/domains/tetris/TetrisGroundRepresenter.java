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
//				if( s.cells[y][x] > 0 ) {
//				if( s.cells.get( y ).get( x ) ) {
				if( s.cell( y, x ) ) {
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
