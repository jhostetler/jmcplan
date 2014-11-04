/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2.subtask;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.StateSpace;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.Hand;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public class YahtzeeSubtaskStateSpace extends StateSpace<YahtzeeDiceState>
{
	private static final ArrayList<Attribute> attributes;
	
	static {
		attributes = new ArrayList<Attribute>();
		for( int i = 0; i < Hand.Nfaces; ++i ) {
			attributes.add( new Attribute( "h" + i ) );
		}
		attributes.add( new Attribute( "rerolls" ) );
	}
	
	public static ArrayList<Attribute> attributes()
	{
		return attributes;
	}
	
	// -----------------------------------------------------------------------
	
	@Override
	public int cardinality()
	{
		return (Hand.Nrerolls + 1) * 252; // 252 = binom{10}{5} = binom{Hand.Nfaces + Hand.Ndice - 1}{Hand.Nfaces}
	}

	@Override
	public boolean isFinite()
	{
		return true;
	}

	@Override
	public boolean isCountable()
	{
		return true;
	}

	@Override
	public Generator<YahtzeeDiceState> generator()
	{
		return new G();
	}
	
	private static final class G extends Generator<YahtzeeDiceState>
	{
		private Fn.MultinomialTermGenerator g;
		
		int rerolls = Hand.Nrerolls;
		
		public G()
		{
			g = initG();
		}

		private Fn.MultinomialTermGenerator initG()
		{
			return new Fn.MultinomialTermGenerator( Hand.Ndice, Hand.Nfaces );
		}
		
		@Override
		public boolean hasNext()
		{
			return rerolls >= 0;
		}
		
		@Override
		public YahtzeeDiceState next()
		{
			assert( rerolls >= 0 );
			
			final YahtzeeDiceState s = new YahtzeeDiceState( new Hand( g.next() ), rerolls );
			
			if( !g.hasNext() ) {
				rerolls -= 1;
				g = initG();
			}
			
			return s;
		}
		
	}
	
//	private static final class G extends Generator<YahtzeeDiceState>
//	{
//		final int[] bars = new int[Hand.Nfaces - 1];
//		final int[] faces = new int[Hand.Nfaces];
//
//		int rerolls = Hand.Nrerolls;
//
//		public G()
//		{
//			faces[0] = Hand.Ndice;
//		}
//
//		@Override
//		public boolean hasNext()
//		{
//			return rerolls >= 0;
//		}
//
//		@Override
//		public YahtzeeDiceState next()
//		{
//			assert( rerolls >= 0 );
//
//			final YahtzeeDiceState s = new YahtzeeDiceState( new Hand( Fn.copy( faces ) ), rerolls );
//
//			// We're implementing the "stars and bars" counting method.
//			// Imagine the 5 dice are stars, and the 6 bins are represented by 5 dividing "bars":
//			//    ||**|**|*| = {0, 0, 2, 2, 1, 0}
//			//    *|*||*|*|* = {1, 1, 0, 1, 1, 1}
//			// The algorithm moves the bars around to create different combinations
//
//			int i = 0;
//			for( ; i < bars.length; ++i ) {
//				if( bars[i] < Hand.Ndice ) {
//					// Bar can be moved. e.g.:
//					// ||**|***||
//					//     ^
//					bars[i] += 1;
//					// => ||***|**||
//					for( int j = 0; j < i; ++j ) {
//						// Move all lower-order bars to the same position
//						// => ||***|||**
//						bars[j] = bars[i];
//					}
//					break;
//				}
//			}
//
//			if( i == bars.length ) {
//				// All bars were all the way advanced -> next reroll
//				rerolls -= 1;
//				Fn.assign( bars, 0 );
//			}
//
//			// Recompute totals. This could be done incrementally for greater
//			// efficiency.
//			faces[0] = Hand.Ndice - bars[0];
//			int sum = faces[0];
//			for( int j = 1; j < bars.length; ++j ) {
//				faces[j] = bars[j - 1] - bars[j];
//				sum += faces[j];
//			}
//			faces[Hand.Nfaces - 1] = Hand.Ndice - sum;
//
//			// Return state computed above
//			return s;
//		}
//
////		@Override
////		public YahtzeeDiceState next()
////		{
////			assert( rerolls >= 0 );
////
////			final YahtzeeDiceState s = new YahtzeeDiceState( new Hand( faces ), rerolls );
////
////			while( dice[i] == Hand.Nfaces - 1 ) {
////				dice[i] = 0;
////				i += 1;
////				if( i == Hand.Ndice ) {
////					rerolls -= 1;
////					i = 0;
////					Fn.assign( dice, 0 );
////					Fn.assign( faces, 0 );
////					faces[0] = Hand.Ndice; // All dice are 0 now
////					break;
////				}
////			}
////
////			dice[i] += 1; // Change value of least-significant dice
////			faces[dice[i] - 1] -= 1;
////			faces[dice[i]] += 1;
////
////			return s;
////		}
//
//	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] argv )
	{
		final G g = new G();
		int c = 0;
		while( g.hasNext() ) {
			System.out.println( (c++) + " : " + g.next() );
		}
	}
}
