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
			
			public T() { super( I, 4 ); }

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
		public Tetromino create()
		{ return new T(); }
	},
	
	O {
		class T extends Tetromino
		{
			private final int[][] mask =
			parseMask( new String[] { "....",
									  ".oo.",
									  ".oo.",
									  "...." } );
			
			public T() { super( O, 4 ); }

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
		public Tetromino create()
		{ return new T(); }
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
			
			public T() { super( T, 3 ); }

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
		public Tetromino create()
		{ return new T(); }
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
			
			public T() { super( J, 3 ); }

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
		public Tetromino create()
		{ return new T(); }
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
			
			public T() { super( L, 3 ); }

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
		public Tetromino create()
		{ return new T(); }
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
			
			public T() { super( S, 3 ); }

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
		public Tetromino create()
		{ return new T(); }
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
			
			public T() { super( Z, 3 ); }

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
		public Tetromino create()
		{ return new T(); }
	};
	
	// -----------------------------------------------------------------------
	
	private static int threeWideX( final int x )
	{
		return Math.max( Math.min( x, 3 ), -4 );
	}
	
	public abstract Tetromino create();

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
	
	private static int tallThreeY( final int y )
	{
		return Math.min( y, TetrisState.Nrows-1 - 1 );
	}
	
	private static int tallFourY( final int y )
	{
		return Math.min( y, TetrisState.Nrows-1 - 2 );
	}
	
}
