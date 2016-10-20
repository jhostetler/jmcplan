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

package edu.oregonstate.eecs.mcplan.domains.tetris;

import java.util.Arrays;


public abstract class Tetromino
{
	public static class BoundingBox
	{
		public final int left;
		public final int bottom;
		public final int right;
		public final int top;
		
		public BoundingBox( final int left, final int bottom, final int right, final int top )
		{
			assert( left <= right );
			assert( bottom <= top );
			
			this.left = left;
			this.bottom = bottom;
			this.right = right;
			this.top = top;
		}
	}
	
	public final TetrisParameters params;
	public final TetrominoType type;
	public final int dim;
	public int rotation = 0;
	public int x = 0;
	public int y = 0;
	
	public Tetromino( final TetrisParameters params, final TetrominoType type, final int dim )
	{
		assert( dim == 3 || dim == 4 );
		this.params = params;
		this.type = type;
		this.dim = dim;
	}
	
	@Override
	public String toString()
	{
		final int[][] m = mask();
		final StringBuilder sb = new StringBuilder();
		for( int y = m.length - 1; y >= 0; --y ) {
			sb.append( Arrays.toString( m[y] ) );
			sb.append( "\n" );
		}
		return sb.toString();
	}
	
	protected int tallThreeY( final int y )
	{
		return Math.min( y, params.Nrows-1 - 1 );
	}
	
	protected int tallFourY( final int y )
	{
		return Math.min( y, params.Nrows-1 - 2 );
	}
	
	protected abstract void setPositionImpl( final int x, final int y );
	
	public void setPosition( final int x, final int y )
	{
		// Coordinate change
		setPositionImpl( x - 5, y );
	}
	
	protected abstract int[][] mask();
	
	public abstract BoundingBox getBoundingBox();
	
	protected int[][] parseMask( final String[] mask )
	{
		final int[][] r = new int[mask.length][];
		for( int i = 0; i < mask.length; ++i ) {
			final String s = mask[i];
			final int[] row = new int[s.length()];
			for( int j = 0; j < s.length(); ++j ) {
				if( s.charAt( j ) == '.' ) {
					row[j] = 0;
				}
				else {
					row[j] = 1;
				}
			}
			r[r.length - 1 - i] = row;
		}
		return r;
	}
	
	public boolean setCells( final TetrisState s, final int component )
	{
		if( this.dim == 3 ) {
//			return setCells3( s.cells, mask(), component );
			return setCells3( s, mask(), component );
		}
		else {
//			return setCells4( s.cells, mask(), component );
			return setCells4( s, mask(), component );
		}
	}
	
	private boolean setCells3( final TetrisState s, final int[][] mask, final int component )
	{
		final BoundingBox bb = getBoundingBox();
		for( int dx = bb.left; dx <= bb.right; ++dx ) {
			final int corrected_x = this.x + dx + 5;
			for( int dy = bb.bottom; dy <= bb.top; ++dy ) {
				final int corrected_y = this.y + dy;
//				if( cells[corrected_y][corrected_x] != 0 ) {
				if( s.cell( corrected_y, corrected_x ) ) {
					return false;
				}
				if( mask[dy + 1][dx + 1] > 0 ) {
//					cells[corrected_y][corrected_x] = (byte) component;
					s.setCell( corrected_y, corrected_x );
					s.params.scratch[corrected_y][corrected_x] = (byte) component;
				}
			}
		}
		return true;
	}
	
	private boolean setCells4( final TetrisState s, final int[][] mask, final int component )
	{
		final BoundingBox bb = getBoundingBox();
		for( int dx = bb.left; dx <= bb.right; ++dx ) {
			final int corrected_x = this.x + dx + 5;
			for( int dy = bb.bottom; dy <= bb.top; ++dy ) {
				final int corrected_y = this.y + dy;
//				if( cells[corrected_y][corrected_x] != 0 ) {
				if( s.cell( corrected_y, corrected_x ) ) {
					return false;
				}
				if( mask[dy + 1][dx + 2] > 0 ) {
//					cells[corrected_y][corrected_x] = (byte) component;
					s.setCell( corrected_y, corrected_x );
					s.params.scratch[corrected_y][corrected_x] = (byte) component;
				}
			}
		}
		return true;
	}

//	private boolean setCells3( final byte[][] cells, final int[][] mask, final int component )
//	{
//		final BoundingBox bb = getBoundingBox();
//		for( int dx = bb.left; dx <= bb.right; ++dx ) {
//			final int corrected_x = this.x + dx + 5;
//			for( int dy = bb.bottom; dy <= bb.top; ++dy ) {
//				final int corrected_y = this.y + dy;
//				if( cells[corrected_y][corrected_x] != 0 ) {
//					return false;
//				}
//				if( mask[dy + 1][dx + 1] > 0 ) {
//					cells[corrected_y][corrected_x] = (byte) component;
//				}
//			}
//		}
//		return true;
//	}
	
	private boolean setCells4( final byte[][] cells, final int[][] mask, final int component )
	{
		final BoundingBox bb = getBoundingBox();
		for( int dx = bb.left; dx <= bb.right; ++dx ) {
			final int corrected_x = this.x + dx + 5;
			for( int dy = bb.bottom; dy <= bb.top; ++dy ) {
				final int corrected_y = this.y + dy;
				if( cells[corrected_y][corrected_x] != 0 ) {
					return false;
//					throw new TetrisGameOver( corrected_x, corrected_y );
				}
				if( mask[dy + 1][dx + 2] > 0 ) {
					cells[corrected_y][corrected_x] = (byte) component;
				}
//				cells[corrected_y][corrected_x] = mask[dy + 1][dx + 2];
			}
		}
		return true;
	}
	
	
	/**
	 * Note: This shared implementation implicitly uses "wall kicks" (if a
	 * rotation would put part of the tetromino out of bounds, it is
	 * automatically moved inbounds).
	 * 
	 * @param rotation
	 */
	public void setRotation( final int rotation )
	{
		this.rotation = rotation;
		setPositionImpl( x, y );
	}
}
