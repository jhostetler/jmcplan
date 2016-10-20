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
 * Implements the "Bertsekas features", which are fairly standard in
 * "Tetris research"
 * 
 * @article{bertsekas1996temporal,
 *   title={Temporal differences-based policy iteration and applications in neuro-dynamic programming},
 *   author={Bertsekas, Dimitri P and Ioffe, Sergey},
 *   journal={Lab. for Info. and Decision Systems Report LIDS-P-2349, MIT, Cambridge, MA},
 *   year={1996}
 * }
 */
public class TetrisBertsekasRepresenter implements FactoredRepresenter<TetrisState, FactoredRepresentation<TetrisState>>
{
	private final ArrayList<Attribute> attributes;
	
	public TetrisBertsekasRepresenter( final TetrisParameters params )
	{
		attributes = new ArrayList<Attribute>();
		for( int i = 0; i < params.Ncolumns; ++i ) {
			attributes.add( new Attribute( "h" + i ) );
		}
		for( int i = 1; i < params.Ncolumns; ++i ) {
			attributes.add( new Attribute( "d" + i ) );
		}
		attributes.add( new Attribute( "maxh" ) );
		attributes.add( new Attribute( "holes" ) );
	}
	
	private TetrisBertsekasRepresenter( final TetrisBertsekasRepresenter that )
	{
		this.attributes = that.attributes;
	}

	@Override
	public FactoredRepresenter<TetrisState, FactoredRepresentation<TetrisState>> create()
	{
		return new TetrisBertsekasRepresenter( this );
	}

	@Override
	public FactoredRepresentation<TetrisState> encode( final TetrisState s )
	{
		final float[] phi = new float[attributes.size()];
		int maxh = 0;
		int holes = 0;
		// Height of each column
		for( int i = 0; i < s.params.Ncolumns; ++i ) {
			for( int j = s.params.Nrows - 1; j >= 0; --j ) {
//				if( s.cells[j][i] == 0 ) {
//				if( !s.cells.get( j ).get( i ) ) {
				if( !s.cell( j, i ) ) {
					if( phi[i] > 0 ) {
						// Empty cell with full cell somewhere above it
						holes += 1;
					}
				}
				else {
					if( phi[i] == 0 ) {
						// Full cell and column height not set yet
						final int height = j + 1;
						phi[i] = height;
						if( height > maxh ) {
							maxh = height;
						}
					}
				}
			}
		}
		
		int idx = s.params.Ncolumns;
		// Height difference
		for( int i = 1; i < s.params.Ncolumns; ++i ) {
			phi[idx++] = Math.abs( phi[i] - phi[i-1] );
		}
		
		phi[idx++] = maxh;
		phi[idx++] = holes;
		
		assert( idx == attributes.size() );
		return new ArrayFactoredRepresentation<TetrisState>( phi );
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes;
	}

}
