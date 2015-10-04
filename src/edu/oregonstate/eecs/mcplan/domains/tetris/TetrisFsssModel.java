/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.tetris;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssModel;
import edu.oregonstate.eecs.mcplan.util.Fn;
import gnu.trove.list.TIntList;

/**
 * @author jhostetler
 *
 */
public class TetrisFsssModel extends FsssModel<TetrisState, TetrisAction>
{
	private final RandomGenerator rng;
	
	private final TetrisParameters params;
	
	private int sample_count = 0;
	
	// "Standard" video game scoring: larger score for more lines and bonus for consecutive tetri
	// FIXME: It just occurred to me that "consecutive" tetri might mean on
	// consecutive *drops*, not only due to a chain reaction as it is now.
//	private final int[] rewards = new int[] { 0, 100, 300, 500, 800 };
//	private final double chain_bonus_multplier = 1.5;
//	private final double step_reward = 1.0;
	
	// "Bertsekas" rewards: reward of 1 for each line cleared, no bonus for consecutive tetri
	private final int[] rewards = new int[] { 0, 1, 2, 3, 4 };
	private final double chain_bonus_multplier = 1.0;
	private final double step_reward = 0;
	
	private final FactoredRepresenter<TetrisState, ? extends FactoredRepresentation<TetrisState>> base_repr;
	private final TetrisActionSetRepresenter action_repr = new TetrisActionSetRepresenter();
	
	public TetrisFsssModel( final RandomGenerator rng, final TetrisParameters params,
							final FactoredRepresenter<TetrisState, ? extends FactoredRepresentation<TetrisState>> base_repr )
	{
		this.rng = rng;
		this.params = params;
		this.base_repr = base_repr;
	}
	
	@Override
	public FsssModel<TetrisState, TetrisAction> create( final RandomGenerator rng )
	{
		return new TetrisFsssModel( rng, params, base_repr );
	}
	
	private double futureVmax( final TetrisState s )
	{
		final int remaining_blocks = (s.T - s.t) * 4;
		final int total_blocks = s.Nblocks + remaining_blocks;
		// Maximum number of lines we could clear in remaining time
		final int lines = total_blocks / 10;
		// Most Tetri we could have
		final int ntetri = lines / 4;
		// Assume the rest go in one batch
		final int spare = lines % 4;
		// Assume as many Tetri as possible occur as double-Tetri
		return (ntetri / 2)*chain_bonus_multplier*rewards[4] + (ntetri % 2)*rewards[4] + rewards[spare] + (s.T - s.t);
	}
	
	@Override
	public double Vmin( final TetrisState s )
	{
		return 0;
	}

	@Override
	public double Vmax( final TetrisState s )
	{
		return s.r + futureVmax( s );
	}

	@Override
	public double Vmin( final TetrisState s, final TetrisAction a )
	{
		return 0;
	}

	@Override
	public double Vmax( final TetrisState s, final TetrisAction a )
	{
		// FIXME: This could be tighter, but it requires knowing the outcome
		// of a.
		return futureVmax( s );
	}

	@Override
	public double discount()
	{
		return 1.0;
	}

	/**
	 * The heuristic calculates:
	 * one_offs - The number of rows that are missing exactly 1 block
	 * impending_doom - Equal to 0 up to threshold height, after which it
	 *     increases by 1 for each increase in s.height
	 * 
	 * We assume that we can convert each one_off into a rewards[1], and that
	 * each increment of impending_doom costs us rewards[1].
	 */
	@Override
	public double heuristic( final TetrisState s )
	{
		if( s.isTerminal() ) {
			return 0.0;
		}
		
		return 0;
		
//		int one_offs = 0;
//		for( int y = 0; y < TetrisState.Nrows; ++y ) {
//			final int[] row = s.cells[y];
//			int count = 0;
//			for( int x = 0; x < TetrisState.Ncolumns; ++x ) {
//				if( row[x] > 0 ) {
//					count += 1;
//				}
//			}
//			if( count == TetrisState.Ncolumns - 1 ) {
//				one_offs += 1;
//			}
//		}
		
//		final double threshold_height = 4.0;
//		final double height_scaling =
//			(TetrisState.Nrows - Math.max( s.height, threshold_height )) / (TetrisState.Nrows - threshold_height);
//////		System.out.println( "Heuristic: " + weight );
////		return futureVmax( s ) * weight;
//
//		final int[] tops = new int[TetrisState.Ncolumns];
//		int nholes = 0;
//		for( int y = TetrisState.Nrows - 1; y >= 0; --y ) {
//			final int[] row = s.cells[y];
//			boolean broken = false;
//			for( int x = 0; x < TetrisState.Ncolumns; ++x ) {
//				if( tops[x] == 0 ) {
//					if( row[x] > 0 ) {
//						tops[x] = y;
//					}
//				}
//				else {
//					if( row[x] == 0 ) {
//						broken = true;
//					}
//				}
//			}
//			if( broken ) {
//				nholes += 1;
//			}
//		}
//		final double hole_scaling = (TetrisState.Nrows - nholes) / (TetrisState.Nrows + 1.0);
////		System.out.println( "nholes = " + nholes );
////		System.out.println( "hole_scaling = " + hole_scaling );
//		return height_scaling * hole_scaling * futureVmax( s ); //Math.max( futureVmax( s ) - rewards[4]*nholes, 0 );
		
		// Sum of square of number of blocks in each row. Should encourage
		// building compact structures.
//		int sum = 0;
//		for( int y = 0; y < TetrisState.Nrows; ++y ) {
//			final int[] row = s.cells[y];
//			int count = 0;
//			for( int x = 0; x < TetrisState.Ncolumns; ++x ) {
//				if( row[x] > 0 ) {
//					count += 1;
//				}
//			}
//			sum += (count*count);
//		}
//		return sum;
		
//		return futureVmax( s ) - (TetrisState.Nrows*100) + sum;

//		final int impending_doom = Math.max( (TetrisState.Nrows - s.height) - 4, 0 );
//		return one_offs*rewards[1] + impending_doom*rewards[1];
		
		// (#blocks / 10) * "density". Designed to encourage placing blocks
		// while staying compact. It seems to reward aimlessly growing the
		// structure a little too much.
//		return (rewards[1] / 10.0) * s.Nblocks * (s.Nblocks / (10.0 * s.height));
	}

	@Override
	public RandomGenerator rng()
	{
		return rng;
	}

	@Override
	public FactoredRepresenter<TetrisState, ? extends FactoredRepresentation<TetrisState>> base_repr()
	{
		return base_repr;
	}

	@Override
	public Representer<TetrisState, ? extends Representation<TetrisState>> action_repr()
	{
		return action_repr;
	}

	@Override
	public TetrisState initialState()
	{
		final TetrisState s = new TetrisState( params );
		s.advanceTetrominoQueue( rng );
		return s;
	}

	@Override
	public Iterable<TetrisAction> actions( final TetrisState s )
	{
		return Fn.in( new TetrisActionGenerator( params ) );
	}

	@Override
	public TetrisState sampleTransition( final TetrisState s, final TetrisAction a )
	{
		final TetrisState sprime = new TetrisState( s );
		a.doAction( sprime );
		if( !sprime.isTerminal() ) {
			final TIntList clears = sprime.drop();
			int i = 0;
			int r = 0;
			while( i < clears.size() ) {
				// Count consecutive Tetri
				int j = i;
				int ntetri = 0;
				while( j < clears.size() ) {
					if( clears.get( j++ ) == 4 ) {
						ntetri += 1;
					}
					else {
						break;
					}
				}
				// Apply consecutive tetri bonus if applicable
				if( ntetri > 1 ) {
					r += ntetri * chain_bonus_multplier*rewards[4];
					i += ntetri;
				}
				else {
					r += rewards[clears.get( i )];
					i += 1;
				}
			}
			sprime.r = r;
			sprime.advanceTetrominoQueue( rng );
			++sample_count;
			
//			if( sample_count % 10000 == 0 ) {
//				System.out.println( "sample_count = " + sample_count );
//			}
		}
		return sprime;
	}

	@Override
	public double reward( final TetrisState s )
	{
		return s.r;
	}

	@Override
	public double reward( final TetrisState s, final TetrisAction a )
	{
		return step_reward;
	}

	@Override
	public int sampleCount()
	{
		return sample_count;
	}

	@Override
	public void resetSampleCount()
	{
		sample_count = 0;
	}

}
