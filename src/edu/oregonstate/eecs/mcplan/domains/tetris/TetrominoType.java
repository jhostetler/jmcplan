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



/**
 * Note: The "reference point" for the tetromino is:
 * * The center cell for 3x3 tetrominoes
 * * The cell to the bottom-right of the center point for 4x4 tetrominoes
 * @author jhostetler
 *
 */
public enum TetrominoType
{
	I {
		class T extends Tetromino
		{
			private final int[][][] masks = new int[][][] {
			parseMask( new String[] { "....",
									  "....",
									  "oooo",
									  "...." } ),
			parseMask( new String[] { "..o.",
									  "..o.",
									  "..o.",
									  "..o." } )
			};
			
			public T( final TetrisParameters params ) { super( params, I, 4 ); }

			@Override
			protected void setPositionImpl( final int x, final int y )
			{
				if( rotation % 2 == 0 ) {
					// ....
					// ....
					// oooo
					// ....
					this.x = fourWideX( x );
					this.y = y;
				}
				else {
					// ..o.
					// ..o.
					// ..o.
					// ..o.
					this.x = x;
					this.y = tallFourY( y );
				}
			}

			@Override
			protected int[][] mask()
			{ return masks[rotation % 2]; }

			@Override
			public Tetromino.BoundingBox getBoundingBox()
			{
				switch( rotation ) {
				case 0:
				case 2:
					return new Tetromino.BoundingBox( -2, 0, 1, 0 );
				case 1:
				case 3:
					return new Tetromino.BoundingBox( 0, -1, 0, 2 );
				default:
					throw new IllegalArgumentException( "rotation = " + rotation );
				}
			}
		}
		
		@Override
		public Tetromino create( final TetrisParameters params )
		{ return new T( params ); }
	},
	
	O {
		class T extends Tetromino
		{
			private final int[][] mask =
			parseMask( new String[] { "....",
									  ".oo.",
									  ".oo.",
									  "...." } );
			
			public T( final TetrisParameters params ) { super( params, O, 4 ); }

			@Override
			protected void setPositionImpl( final int x, final int y )
			{
				this.x = leftOneX( x );
				this.y = tallThreeY( y );
			}

			@Override
			protected int[][] mask()
			{
				return mask;
			}

			@Override
			public Tetromino.BoundingBox getBoundingBox()
			{
				return new Tetromino.BoundingBox( -1, 0, 0, 1 );
			}
		}

		@Override
		public Tetromino create( final TetrisParameters params )
		{ return new T( params ); }
	},
	
	T() {
		class T extends Tetromino
		{
			private final int[][][] masks = new int[][][] {
			parseMask( new String[] { "...",
									  "ooo",
									  ".o." } ),
			parseMask( new String[] { ".o.",
									  "oo.",
									  ".o." } ),
			parseMask( new String[] { ".o.",
									  "ooo",
									  "..." } ),
			parseMask( new String[] { ".o.",
									  ".oo",
									  ".o." } )
			};
			
			public T( final TetrisParameters params ) { super( params, T, 3 ); }

			@Override
			protected void setPositionImpl( final int x, final int y )
			{
				switch( rotation ) {
				case 0:
					// ...
					// ooo
					// .o.
					this.x = threeWideX( x );
					this.y = y;
					break;
				case 1:
					// .o.
					// oo.
					// .o.
					this.x = leftOneX( x );
					this.y = tallThreeY( y );
					break;
				case 2:
					// .o.
					// ooo
					// ...
					this.x = threeWideX( x );
					this.y = tallThreeY( y );
					break;
				case 3:
					// .o.
					// .oo
					// .o.
					this.x = rightOneX( x );
					this.y = tallThreeY( y );
					break;
				default:
					throw new IllegalArgumentException( "rotation = " + rotation );
				}
			}

			@Override
			protected int[][] mask()
			{
				return masks[rotation];
			}

			@Override
			public Tetromino.BoundingBox getBoundingBox()
			{
				switch( rotation ) {
				case 0:
					return new Tetromino.BoundingBox( -1, -1, 1, 0 );
				case 1:
					return new Tetromino.BoundingBox( -1, -1, 0, 1 );
				case 2:
					return new Tetromino.BoundingBox( -1, 0, 1, 1 );
				case 3:
					return new Tetromino.BoundingBox( 0, -1, 1, 1 );
				default:
					throw new IllegalArgumentException( "rotation = " + rotation );
				}
			}
		}

		@Override
		public Tetromino create( final TetrisParameters params )
		{ return new T( params ); }
	},
	
	J {
		class T extends Tetromino
		{
			private final int[][][] masks = new int[][][] {
				parseMask( new String[] { "...",
										  "ooo",
										  "..o" } ),
				parseMask( new String[] { ".o.",
										  ".o.",
										  "oo." } ),
				parseMask( new String[] { "o..",
										  "ooo",
										  "..." } ),
				parseMask( new String[] { ".oo",
										  ".o.",
										  ".o." } ),
			};
			
			public T( final TetrisParameters params ) { super( params, J, 3 ); }

			@Override
			protected void setPositionImpl( final int x, final int y )
			{
				switch( rotation ) {
				case 0:
					// ...
					// ooo
					// ..o
					this.x = threeWideX( x );
					this.y = y;
					break;
				case 1:
					// .o.
					// .o.
					// oo.
					this.x = leftOneX( x );
					this.y = tallThreeY( y );
					break;
				case 2:
					// o..
					// ooo
					// ...
					this.x = threeWideX( x );
					this.y = tallThreeY( y );
					break;
				case 3:
					// .oo
					// .o.
					// .o.
					this.x = rightOneX( x );
					this.y = tallThreeY( y );
					break;
				default:
					throw new IllegalArgumentException( "rotation = " + rotation );
				}
			}

			@Override
			protected int[][] mask()
			{
				return masks[rotation];
			}

			@Override
			public Tetromino.BoundingBox getBoundingBox()
			{
				switch( rotation ) {
				case 0:
					return new Tetromino.BoundingBox( -1, -1, 1, 0 );
				case 1:
					return new Tetromino.BoundingBox( -1, -1, 0, 1 );
				case 2:
					return new Tetromino.BoundingBox( -1, 0, 1, 1 );
				case 3:
					return new Tetromino.BoundingBox( 0, -1, 1, 1 );
				default:
					throw new IllegalArgumentException( "rotation = " + rotation );
				}
			}
		}

		@Override
		public Tetromino create( final TetrisParameters params )
		{ return new T( params ); }
	},
	
	L {
		class T extends Tetromino
		{
			private final int[][][] masks = new int[][][] {
				parseMask( new String[] { "...",
										  "ooo",
										  "o.." } ),
				parseMask( new String[] { "oo.",
										  ".o.",
										  ".o." } ),
				parseMask( new String[] { "..o",
										  "ooo",
										  "..." } ),
				parseMask( new String[] { ".o.",
										  ".o.",
										  ".oo" } ),
			};
			
			public T( final TetrisParameters params ) { super( params, L, 3 ); }

			@Override
			protected void setPositionImpl( final int x, final int y )
			{
				switch( rotation ) {
				case 0:
					// ...
					// ooo
					// o..
					this.x = threeWideX( x );
					this.y = y;
					break;
				case 1:
					// oo.
					// .o.
					// .o.
					this.x = leftOneX( x );
					this.y = tallThreeY( y );
					break;
				case 2:
					// ..o
					// ooo
					// ...
					this.x = threeWideX( x );
					this.y = tallThreeY( y );
					break;
				case 3:
					// .o.
					// .o.
					// .oo
					this.x = rightOneX( x );
					this.y = tallThreeY( y );
					break;
				default:
					throw new IllegalArgumentException( "rotation = " + rotation );
				}
			}

			@Override
			protected int[][] mask()
			{
				return masks[rotation];
			}

			@Override
			public Tetromino.BoundingBox getBoundingBox()
			{
				switch( rotation ) {
				case 0:
					return new Tetromino.BoundingBox( -1, -1, 1, 0 );
				case 1:
					return new Tetromino.BoundingBox( -1, -1, 0, 1 );
				case 2:
					return new Tetromino.BoundingBox( -1, 0, 1, 1 );
				case 3:
					return new Tetromino.BoundingBox( 0, -1, 1, 1 );
				default:
					throw new IllegalArgumentException( "rotation = " + rotation );
				}
			}
		}

		@Override
		public Tetromino create( final TetrisParameters params )
		{ return new T( params ); }
	},
	
	S {
		class T extends Tetromino
		{
			private final int[][][] masks = new int[][][] {
				parseMask( new String[] { "...",
										  ".oo",
										  "oo." } ),
				parseMask( new String[] { ".o.",
										  ".oo",
										  "..o" } )
			};
			
			public T( final TetrisParameters params ) { super( params, S, 3 ); }

			@Override
			protected void setPositionImpl( final int x, final int y )
			{
				switch( rotation ) {
				case 0:
				case 2:
					// ...
					// .oo
					// oo.
					this.x = threeWideX( x );
					this.y = y;
					break;
				case 1:
				case 3:
					// .o.
					// .oo
					// ..o
					this.x = rightOneX( x );
					this.y = tallThreeY( y );
					break;
				default:
					throw new IllegalArgumentException( "rotation = " + rotation );
				}
			}

			@Override
			protected int[][] mask()
			{
				return masks[rotation % 2];
			}

			@Override
			public Tetromino.BoundingBox getBoundingBox()
			{
				switch( rotation ) {
				case 0:
				case 2:
					return new Tetromino.BoundingBox( -1, -1, 1, 0 );
				case 1:
				case 3:
					return new Tetromino.BoundingBox( 0, -1, 1, 1 );
				default:
					throw new IllegalArgumentException( "rotation = " + rotation );
				}
			}
		}

		@Override
		public Tetromino create( final TetrisParameters params )
		{ return new T( params ); }
	},
	
	Z {
		class T extends Tetromino
		{
			private final int[][][] masks = new int[][][] {
				parseMask( new String[] { "...",
										  "oo.",
										  ".oo" } ),
				parseMask( new String[] { "..o",
										  ".oo",
										  ".o." } )
			};
			
			public T( final TetrisParameters params ) { super( params, Z, 3 ); }

			@Override
			protected void setPositionImpl( final int x, final int y )
			{
				switch( rotation ) {
				case 0:
				case 2:
					// ...
					// oo.
					// .oo
					this.x = threeWideX( x );
					this.y = y;
					break;
				case 1:
				case 3:
					// ..o
					// .oo
					// .o.
					this.x = rightOneX( x );
					this.y = tallThreeY( y );
					break;
				default:
					throw new IllegalArgumentException( "rotation = " + rotation );
				}
			}

			@Override
			protected int[][] mask()
			{
				return masks[rotation % 2];
			}

			@Override
			public Tetromino.BoundingBox getBoundingBox()
			{
				switch( rotation ) {
				case 0:
				case 2:
					return new Tetromino.BoundingBox( -1, -1, 1, 0 );
				case 1:
				case 3:
					return new Tetromino.BoundingBox( 0, -1, 1, 1 );
				default:
					throw new IllegalArgumentException( "rotation = " + rotation );
				}
			}
		}

		@Override
		public Tetromino create( final TetrisParameters params )
		{ return new T( params ); }
	};
	
	// -----------------------------------------------------------------------
	
	private static int threeWideX( final int x )
	{
		return Math.max( Math.min( x, 3 ), -4 );
	}
	
	public abstract Tetromino create( final TetrisParameters params );

	private static int rightOneX( final int x )
	{
		return Math.min( x, 3 );
	}
	
	private static int leftOneX( final int x )
	{
		return Math.max( x, -4 );
	}
	
	private static int fourWideX( final int x )
	{
		return Math.max( Math.min( x, 3 ), -3 );
	}
	
}
