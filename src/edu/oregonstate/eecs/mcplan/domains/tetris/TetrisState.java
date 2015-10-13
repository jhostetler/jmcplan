/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.tetris;

import java.util.ArrayList;
import java.util.BitSet;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.State;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * @author jhostetler
 *
 */
public final class TetrisState implements State
{
	public final TetrisParameters params;
	
	/**
	 * Row-major order, bottom-up. Encoding: 0 means empty, > 0 means not empty, cells
	 * with the same non-zero number are part of the same connected component.
	 */
//	public final byte[][] cells;
//	public final ArrayList<BitSet> cells;
	private final BitSet cells;
	
	public int t = 0;
	public double r = 0.0;
	
	private final BitSet frozen = new BitSet();
	private int Ncomponents = 0;
	
	private boolean game_over = false;
	
	public Tetromino queued_tetro = null;
	
	public TetrisState( final TetrisState that )
	{
		this( that.params );
		
//		Fn.memcpy( this.cells, that.cells );
//		for( int i = 0; i < params.Nrows; ++i ) {
//			this.cells.get( i ).or( that.cells.get( i ) );
//		}
		this.cells.or( that.cells );
		
		this.frozen.or( that.frozen );
		this.Ncomponents = that.Ncomponents;
		this.game_over = that.game_over;
		this.queued_tetro = that.queued_tetro.type.create( params );
		this.t = that.t;
		this.r = that.r;
	}
	
	public TetrisState( final TetrisParameters params )
	{
		this.params = params;
//		cells = new byte[params.Nrows][params.Ncolumns];
//		cells = new ArrayList<BitSet>( params.Nrows );
//		for( int i = 0; i < params.Nrows; ++i ) {
//			cells.set( i, new BitSet( params.Ncolumns ) );
//		}
		cells = new BitSet( params.Nrows * params.Ncolumns );
	}
	
	@Override
	public void close()
	{ }
	
//	private static int nfinalized = 0;
//	@Override
//	public void finalize()
//	{
//		System.out.println( "finalize(): " + (nfinalized++) + " TetrisState" );
//	}

	public final boolean cell( final int y, final int x )
	{
		return cells.get( y * params.Ncolumns + x );
	}
	
	private final void clear( final int y, final int x )
	{
		cells.clear( y * params.Ncolumns + x );
	}
	
	private final void clearRow( final int y )
	{
		cells.clear( y * params.Ncolumns, y * params.Ncolumns + params.Nrows );
	}
	
	/*package*/ final void setCell( final int y, final int x )
	{
		cells.set( y * params.Ncolumns + x );
	}
	
	public final int Nblocks()
	{
		int sum = 0;
		for( int i = cells.nextSetBit( 0 ); i >= 0; i = cells.nextSetBit( i + 1 ) ) {
			sum += 1;
		}
		return sum;
	}
	
	public void advanceTetrominoQueue( final RandomGenerator rng )
	{
		final int t = rng.nextInt( TetrominoType.values().length );
		final int r = rng.nextInt( 4 );
		final Tetromino tetro = TetrominoType.values()[t].create( params );
		tetro.setPosition( 5, params.Nrows - 1 - tetro.getBoundingBox().top );
		tetro.setRotation( r );
		queued_tetro = tetro;
	}
	
	public Tetromino getCurrentTetromino()
	{
		return queued_tetro;
	}
	
	/**
	 * Used for tracking equivalence class relationships.
	 */
	private static class Partition
	{
		private final ArrayList<TIntSet> parts = new ArrayList<TIntSet>();
		
		/**
		 * Return a mapping from a cell's current value to the index of its
		 * equivalence class.
		 * @return
		 */
		public byte[] makeReducedMap()
		{
			int n = 0;
			for( final TIntSet p : parts ) {
				n += p.size();
			}
			
			final byte[] r = new byte[n];
			for( int i = 0; i < parts.size(); ++i ) {
				final TIntSet p = parts.get( i );
				final TIntIterator itr = p.iterator();
				while( itr.hasNext() ) {
					r[itr.next() - 1] = (byte) (i + 1);
				}
			}
			
			return r;
		}
		
		/**
		 * Make i and j equivalent. Be sure to call addEquivalence( x, x )
		 * the first time an index x is used.
		 * @param i
		 * @param j
		 */
		public void addEquivalence( final int i, final int j )
		{
			int ii = -1;
			int jj = -1;
			
			for( int pidx = 0; pidx < parts.size(); ++pidx ) {
				final TIntSet p = parts.get( pidx );
				if( p.contains( i ) ) {
					ii = pidx;
				}
				else if( p.contains( j ) ) {
					jj = pidx;
				}
			}
			
			if( ii >= 0 ) {
				if( jj >= 0 ) {
					parts.get( ii ).addAll( parts.get( jj ) );
					parts.remove( jj );
				}
				else {
					parts.get( ii ).add( j );
				}
			}
			else if( jj >= 0 ) {
				parts.get( jj ).add( i );
			}
			else {
				final TIntSet p = new TIntHashSet();
				p.add( i );
				p.add( j );
				parts.add( p );
			}
		}

		public int nparts()
		{
			return parts.size();
		}
	}
	
	/**
	 * Computes connected components and freezes all components that are
	 * touching the bottom.
	 * <p>
	 * The members of each component are all given the same index. Indices are
	 * ordered first by the y-coordinate of the lowest block in the class,
	 * then by the x-coordinate of the left-most block in the class.
	 */
	/*package*/ final void assignComponents()
	{
		byte next_comp = 1;
		final Partition partition = new Partition();
		
		// Bottom row
//		final byte[] bottom_row = cells[0];
//		final BitSet bottom_row = cells.get( 0 );
//		if( bottom_row != 0 ) {
//		if( bottom_row.get( 0 ) ) {
		if( cell( 0, 0 ) ) {
			partition.addEquivalence( next_comp, next_comp );
//			bottom_row[0] = next_comp++;
			params.scratch[0][0] = next_comp++;
		}
		else {
			// NOTE: This and the similar 'else 0' clauses don't seem to be
			// necessary, it's difficult to tell if the algorithm is
			// correct without them, and I don't want to risk it.
			params.scratch[0][0] = 0;
		}
		for( int x = 1; x < params.Ncolumns; ++x ) {
//			if( bottom_row[x] != 0 ) {
//			if( bottom_row.get( x ) ) {
			if( cell( 0, x ) ) {
//				if( bottom_row[x-1] != 0 ) {
//				if( bottom_row.get( x - 1 ) ) {
				if( cell( 0, x - 1 ) ) {
//					bottom_row[x] = bottom_row[x-1];
					params.scratch[0][x] = params.scratch[0][x-1];
				}
				else {
					partition.addEquivalence( next_comp, next_comp );
//					bottom_row[x] = next_comp++;
					params.scratch[0][x] = next_comp++;
				}
			}
			else {
				params.scratch[0][x] = 0;
			}
		}
		
//		System.out.println( "Row 0:" );
//		System.out.println( this );
		
		// Other rows
//		byte[] prev_row = bottom_row;
//		BitSet prev_row = bottom_row;
		for( int y = 1; y < params.Nrows; ++y ) {
//			final byte[] row = cells[y];
//			final BitSet row = cells.get( y );
			
			for( int x = 0; x < params.Ncolumns; ++x ) {
//				if( row[x] != 0 ) {
//				if( row.get( x ) ) {
				if( cell( y, x ) ) {
					boolean connected = false;
//					if( prev_row[x] != 0 ) {
//					if( prev_row.get( x ) ) {
					if( cell( y - 1, x ) ) {
						// Connected to block below it
//						row[x] = prev_row[x];
						params.scratch[y][x] = params.scratch[y-1][x];
						connected = true;
					}
//					if( x > 0 && row[x-1] != 0 ) {
//					if( x > 0 && row.get( x - 1 ) ) {
					if( x > 0 && cell( y, x - 1 ) ) {
						// Connected to block to the left
//						if( prev_row[x] != 0 && row[x-1] != prev_row[x] ) {
//						if( prev_row.get( x ) && params.scratch[y][x-1] != params.scratch[y-1][x] ) {
						if( cell( y - 1, x ) && params.scratch[y][x-1] != params.scratch[y-1][x] ) {
							// Different comp index indicates that left and
							// down are in different eq classes. Current
							// block joins them, so make them equivalent.
//							partition.addEquivalence( prev_row[x], row[x] );
							partition.addEquivalence( params.scratch[y-1][x], params.scratch[y][x] );
//							partition.addEquivalence( row[x-1], row[x] );
							partition.addEquivalence( params.scratch[y][x-1], params.scratch[y][x] );
							
//							System.out.println( "" + prev_row[x] + " ~= " + row[x-1] );
						}
						else {
//							row[x] = row[x-1];
							params.scratch[y][x] = params.scratch[y][x-1];
						}
						connected = true;
					}
					if( !connected ) {
						partition.addEquivalence( next_comp, next_comp );
//						row[x] = next_comp++;
						params.scratch[y][x] = next_comp++;
					}
				}
				else {
					params.scratch[y][x] = 0;
				}
			}
			
//			prev_row = row;
			
//			System.out.println( "Row " + y + ":" );
//			System.out.println( this );
		}
		
		// Minimize index set size
		final byte[] reduced = partition.makeReducedMap();
		
//		System.out.println( "Reduced:" );
//		System.out.println( Arrays.toString( reduced ) );
		
		// Assign minimal indices
		for( int y = 0; y < params.Nrows; ++y ) {
			for( int x = 0; x < params.Ncolumns; ++x ) {
//				if( cells[y][x] != 0 ) {
//				if( cells.get( y ).get( x ) ) {
				if( cell( y, x ) ) {
//					cells[y][x] = reduced[cells[y][x] - 1];
					params.scratch[y][x] = reduced[params.scratch[y][x] - 1];
				}
			}
		}
		
		// Components touching the ground don't move
		frozen.clear();
		for( int x = 0; x < params.Ncolumns; ++x ) {
//			if( cells[0][x] > 0 ) {
//			if( cells.get( 0 ).get( x ) ) {
			if( cell( 0, x ) ) {
//				System.out.println( "Frozen: " + cells[0][x] );
//				frozen.set( cells[0][x] );
				frozen.set( params.scratch[0][x] );
			}
		}
		
		Ncomponents = partition.nparts();
	}
	
	/**
	 * Advance the dynamics to the instant just before the next block appears.
	 * @return The number of lines cleared, including lines cleared by
	 * cascading drops.
	 */
	public TIntList drop()
	{
		final TIntList counts = new TIntArrayList();
		while( true ) {
			while( step() ); // Drop everything to the bottom
			
//			System.out.println( "\nSteps\n" );
//			System.out.println( this );
			
			final int subcount = clearCompleteRows();
			
//			System.out.println( "\nClear\n" );
//			System.out.println( this );
//			System.out.println( "\tclears = " + subcount );
			
			assignComponents(); // Reassign connected components
			
//			System.out.println( "\nAssign\n" );
//			System.out.println( this );
			
			counts.add( subcount );
			
			// Cascade drops if a row was cleared
			if( subcount == 0 ) {
				break;
			}
		}
		
		return counts;
	}
	
	/**
	 * Clears all complete rows and returns the number of rows cleared.
	 * @return
	 */
	private int clearCompleteRows()
	{
		int count = 0;
		for( int y = 0; y < params.Nrows; ++y ) {
//			final byte[] row = cells[y];
//			final BitSet row = cells.get( y );
			boolean full = true;
			for( int x = 0; x < params.Ncolumns; ++x ) {
//				if( row[x] == 0 ) {
//				if( !row.get( x ) ) {
				if( !cell( y, x ) ) {
					full = false;
					break;
				}
			}
			if( full ) {
//				Fn.assign( row, (byte) 0 );
//				row.clear();
				clearRow( y );
				++count;
			}
		}
		return count;
	}
	
	/**
	 * Drops all free blocks one row and re-calculates frozen blocks.
	 * @return
	 */
	private boolean step()
	{
//		System.out.println( "step()" );
//		System.out.println( this );
		
//		byte[] prev_row = cells[0];
//		BitSet prev_row = cells.get( 0 );
		for( int y = 1; y < params.Nrows; ++y ) {
//			final byte[] row = cells[y];
//			final BitSet row = cells.get( y );
			for( int x = 0; x < params.Ncolumns; ++x ) {
//				if( row[x] != 0 && !frozen.get( row[x] ) ) {
//				if( row.get( x ) && !frozen.get( params.scratch[y][x] ) ) {
				if( cell( y, x ) && !frozen.get( params.scratch[y][x] ) ) {
//					if( prev_row[x] != 0 ) {
//					if( prev_row.get( x ) ) {
					if( cell( y-1, x ) ) {
						System.out.println( this );
						System.out.println( "(" + x + ", " + y + ")" );
						for( int i = 1; i <= Ncomponents; ++i ) {
							System.out.println( "frozen " + i + ": " + frozen.get( i ) );
						}
					}
//					assert( prev_row[x] == 0 );
//					assert( !prev_row.get( x ) );
					assert( !cell( y-1, x ) );
//					prev_row[x] = row[x];
//					prev_row.set( x );
					setCell( y-1, x );
					params.scratch[y-1][x] = params.scratch[y][x];
//					row[x] = 0;
//					row.clear( x );
					clear( y, x );
					params.scratch[y][x] = 0;
				}
			}
//			prev_row = row;
		}
		
//		prev_row = cells[0];
//		for( int x = 0; x < Ncolumns; ++x ) {
//			if( prev_row[x] != 0 ) {
//				frozen.set( prev_row[x] );
//			}
//		}
//		for( int y = 1; y < Nrows; ++y ) {
//			final byte[] row = cells[y];
//			for( int x = 0; x < Ncolumns; ++x ) {
//				if( prev_row[x] != 0 && prev_row[x] != row[x] ) {
//					frozen.set( row[x] );
//				}
//			}
//			prev_row = row;
//		}
		
		refreshFrozen();
		
//		for( int y = params.Nrows - 1; y >= 0; --y ) {
//			System.out.println( "\n" + Arrays.toString( params.scratch[y] ) );
//		}
		
		boolean done = true;
		for( int i = 1; i <= Ncomponents; ++i ) {
//			System.out.println( "frozen( " + i + " ): " + frozen.get( i ) );
			done = done && frozen.get( i );
		}
		
		return !done;
	}
	
	/*package*/ final void refreshFrozen()
	{
		frozen.clear();
		
//		byte[] prev_row = cells[0];
//		BitSet prev_row = cells.get( 0 );
		
		// Cells touching the bottom are frozen
		for( int x = 0; x < params.Ncolumns; ++x ) {
//			if( prev_row[x] != 0 ) {
//			if( prev_row.get( x ) ) {
			if( cell( 0, x ) ) {
//				frozen.set( prev_row[x] );
				frozen.set( params.scratch[0][x] );
			}
		}
		
		for( int y = 1; y < params.Nrows; ++y ) {
//			final byte[] row = cells[y];
//			final BitSet row = cells.get( y );
			for( int x = 0; x < params.Ncolumns; ++x ) {
//				if( prev_row[x] != 0 && prev_row[x] != row[x] ) {
//				if( prev_row.get( x ) && params.scratch[y-1][x] != params.scratch[y][x] ) {
				
				// A member of A is frozen if it is on top of a member of
				// B and B is frozen.
				if( cell( y-1, x ) && params.scratch[y-1][x] != params.scratch[y][x] ) {
//					frozen.set( row[x] );
					frozen.set( params.scratch[y][x] );
				}
			}
//			prev_row = row;
		}
	}
	
	@Override
	public boolean isTerminal()
	{
		return game_over || t >= params.T;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "[t: " ).append( t ).append( ", r: " ).append( r ).append( ", next: " ).append( queued_tetro.type )
		  .append( "/" ).append( queued_tetro.rotation )
		  .append( ", Ncomponents: " ).append( Ncomponents ).append( "]" );
		
//		sb.append( "\n" ).append( new TetrisBertsekasRepresenter( params ).encode( this ) );
		
//		sb.append( "\n" ).append( frozen );
		
//		sb.append( "\n" );
//		for( int y = params.Nrows - 1; y >= 0; --y ) {
//			for( int x = 0; x < params.Ncolumns; ++x ) {
////				sb.append( cells[y][x] );
////				sb.append( cells.get( y ).get( x ) ? "X" : "." );
//				sb.append( cell( y, x ) ? "X" : "." );
//			}
//			sb.append( "\n" );
//		}
		
		return sb.toString();
	}

	public void createTetromino( final Tetromino tetro )
	{
//		try {
			Ncomponents += 1;
//			System.out.println( "before setCells(): Ncomponents = " + Ncomponents );
			final boolean success = tetro.setCells( this, Ncomponents );
//		}
//		catch( final TetrisGameOver ex ) {
		if( !success ) {
			game_over = true;
			--Ncomponents;
			return;
//			System.out.println( "Game Over!" );
//			System.out.println( "\tconflict (" + ex.conflict_x + ", " + ex.conflict_y + ")" );
//			System.out.println( this );
		}
		
//		System.out.println( "createTetromino()" );
//		System.out.println( this );
		
		refreshFrozen();
	}

}
