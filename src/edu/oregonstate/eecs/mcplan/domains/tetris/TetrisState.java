/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.tetris;

import java.util.ArrayList;
import java.util.BitSet;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.util.Fn;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * @author jhostetler
 *
 */
public class TetrisState implements State
{
	/**
	 * Can be changed safely.
	 */
	public static final int Nrows = 8;
	
	/**
	 * Must be =10 in current implementation. If you want to change this, you
	 * need to alter the bounds checking in TetrominoTypes.
	 */
	public static final int Ncolumns = 10;
	
	/**
	 * Row-major order, bottom-up. Encoding: 0 means empty, > 0 means not empty, cells
	 * with the same non-zero number are part of the same connected component.
	 */
	public final byte[][] cells = new byte[Nrows][Ncolumns];
	public int t = 0;
	public final int T = 50;
	public double r = 0.0;
	public int height = 0;
	public int Nblocks = 0;
	
	private final BitSet frozen = new BitSet();
	private int Ncomponents = 0;
	
	private boolean game_over = false;
	
	public Tetromino queued_tetro = null;
	
	public TetrisState( final TetrisState that )
	{
		Fn.memcpy( this.cells, that.cells );
		this.frozen.or( that.frozen );
		this.Ncomponents = that.Ncomponents;
		this.game_over = that.game_over;
		this.queued_tetro = that.queued_tetro.type.create();
		this.t = that.t;
		this.r = that.r;
		this.height = that.height;
		this.Nblocks = that.Nblocks;
	}
	
	public TetrisState()
	{ }

	public void advanceTetrominoQueue( final RandomGenerator rng )
	{
		final int t = rng.nextInt( TetrominoType.values().length );
		final int r = rng.nextInt( 4 );
		final Tetromino tetro = TetrominoType.values()[t].create();
		tetro.setPosition( 5, TetrisState.Nrows - 1 - tetro.getBoundingBox().top );
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
	public void assignComponents()
	{
		byte next_comp = 1;
		final Partition partition = new Partition();
		
		// Bottom row
		final byte[] bottom_row = cells[0];
		if( bottom_row[0] != 0 ) {
			partition.addEquivalence( next_comp, next_comp );
			bottom_row[0] = next_comp++;
			
		}
		for( int x = 1; x < Ncolumns; ++x ) {
			if( bottom_row[x] != 0 ) {
				if( bottom_row[x-1] != 0 ) {
					bottom_row[x] = bottom_row[x-1];
				}
				else {
					partition.addEquivalence( next_comp, next_comp );
					bottom_row[x] = next_comp++;
				}
			}
		}
		
//		System.out.println( "Row 0:" );
//		System.out.println( this );
		
		// Other rows
		byte[] prev_row = bottom_row;
		for( int y = 1; y < Nrows; ++y ) {
			final byte[] row = cells[y];
			
			for( int x = 0; x < Ncolumns; ++x ) {
				if( row[x] != 0 ) {
					boolean connected = false;
					if( prev_row[x] != 0 ) {
						// Connected to block below it
						row[x] = prev_row[x];
						connected = true;
					}
					if( x > 0 && row[x-1] != 0 ) {
						// Connected to block to the left
						if( prev_row[x] != 0 && row[x-1] != prev_row[x] ) {
							// Different comp index indicates that left and
							// down are in different eq classes. Current
							// block joins them, so make them equivalent.
							partition.addEquivalence( prev_row[x], row[x] );
							partition.addEquivalence( row[x-1], row[x] );
							
//							System.out.println( "" + prev_row[x] + " ~= " + row[x-1] );
						}
						else {
							row[x] = row[x-1];
						}
						connected = true;
					}
					if( !connected ) {
						partition.addEquivalence( next_comp, next_comp );
						row[x] = next_comp++;
					}
				}
			}
			
			prev_row = row;
			
//			System.out.println( "Row " + y + ":" );
//			System.out.println( this );
		}
		
		// Minimize index set size
		final byte[] reduced = partition.makeReducedMap();
		
//		System.out.println( "Reduced:" );
//		System.out.println( Arrays.toString( reduced ) );
		
		// Assign minimal indices
		// Compute and cache some other stuff while we're at it
		height = 0;
		Nblocks = 0;
		for( int y = 0; y < Nrows; ++y ) {
			for( int x = 0; x < Ncolumns; ++x ) {
				if( cells[y][x] != 0 ) {
					cells[y][x] = reduced[cells[y][x] - 1];
					height = y + 1;
					Nblocks += 1;
				}
			}
		}
		
		// Components touching the ground don't move
		frozen.clear();
		for( int x = 0; x < Ncolumns; ++x ) {
			if( cells[0][x] > 0 ) {
//				System.out.println( "Frozen: " + cells[0][x] );
				frozen.set( cells[0][x] );
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
	public int clearCompleteRows()
	{
		int count = 0;
		for( int y = 0; y < Nrows; ++y ) {
			final byte[] row = cells[y];
			boolean full = true;
			for( int x = 0; x < Ncolumns; ++x ) {
				if( row[x] == 0 ) {
					full = false;
					break;
				}
			}
			if( full ) {
				Fn.assign( row, (byte) 0 );
				++count;
			}
		}
		return count;
	}
	
	/**
	 * Drops all free blocks one row and re-calculates frozen blocks.
	 * @return
	 */
	public boolean step()
	{
//		System.out.println( "step()" );
//		System.out.println( this );
		
		byte[] prev_row = cells[0];
		for( int y = 1; y < Nrows; ++y ) {
			final byte[] row = cells[y];
			for( int x = 0; x < Ncolumns; ++x ) {
				if( row[x] != 0 && !frozen.get( row[x] ) ) {
					
					if( prev_row[x] != 0 ) {
						System.out.println( this );
						System.out.println( "(" + x + ", " + y + ")" );
						for( int i = 1; i <= Ncomponents; ++i ) {
							System.out.println( "frozen " + i + ": " + frozen.get( i ) );
						}
					}
					assert( prev_row[x] == 0 );
					prev_row[x] = row[x];
					row[x] = 0;
				}
			}
			prev_row = row;
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
		
		boolean done = true;
		for( int i = 1; i <= Ncomponents; ++i ) {
//			System.out.println( "frozen( " + i + " ): " + frozen.get( i ) );
			done = done && frozen.get( i );
		}
		
		return !done;
	}
	
	private void refreshFrozen()
	{
		frozen.clear();
		byte[] prev_row = cells[0];
		for( int x = 0; x < Ncolumns; ++x ) {
			if( prev_row[x] != 0 ) {
				frozen.set( prev_row[x] );
			}
		}
		for( int y = 1; y < Nrows; ++y ) {
			final byte[] row = cells[y];
			for( int x = 0; x < Ncolumns; ++x ) {
				if( prev_row[x] != 0 && prev_row[x] != row[x] ) {
					frozen.set( row[x] );
				}
			}
			prev_row = row;
		}
	}
	
	@Override
	public boolean isTerminal()
	{
		return game_over || t >= T;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "[t: " ).append( t ).append( ", next: " ).append( queued_tetro.type )
		  .append( "/" ).append( queued_tetro.rotation ).append( ", height: " ).append( height )
		  .append( ", Nblocks: " ).append( Nblocks ).append( ", Ncomponents: " ).append( Ncomponents ).append( "]" );
		
//		sb.append( "\n" ).append( frozen );
		
//		sb.append( "\n" );
//		for( int y = Nrows - 1; y >= 0; --y ) {
//			for( int x = 0; x < Ncolumns; ++x ) {
//				sb.append( cells[y][x] );
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
