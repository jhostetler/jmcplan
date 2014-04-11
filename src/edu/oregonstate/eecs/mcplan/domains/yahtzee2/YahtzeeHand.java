/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

/**
 * @deprecated
 * @author jhostetler
 */
@Deprecated
public abstract class YahtzeeHand
{
	public static final int Ndice = 5;
	public static final int Nfaces = 6;
	
	public static YahtzeeHand of( final int[] dice )
	{
		return new FullHouse( dice, 1, 2 );
	}
	
	// -----------------------------------------------------------------------
	
	public final int[] dice;
	public final int sum;
	
	protected YahtzeeHand( final int[] dice )
	{
		this.dice = dice;
		this.sum = sum( dice );
	}
	
	public int threeOfKindScore( final YahtzeeState s ) { return 0; }
	public int fourOfKindScore( final YahtzeeState s ) { return 0; }
	public int smallStraightScore( final YahtzeeState s ) { return 0; }
	public int largeStraightScore( final YahtzeeState s ) { return 0; }
	public int fullHouseScore( final YahtzeeState s ) { return 0; }
	public int yahtzeeScore( final YahtzeeState s ) { return 0; }
	
	private static int sum( final int[] dice )
	{
		int s = 0;
		for( int i = 0; i < Ndice; ++i ) {
			s += dice[i] * (i+1);
		}
		return s;
	}
	
	// -----------------------------------------------------------------------
	
	public static final class ThreeOfKind extends YahtzeeHand
	{
		public final int i;
		public ThreeOfKind( final int[] dice, final int i )
		{ super( dice ); this.i = i; }
		
		@Override
		public int threeOfKindScore( final YahtzeeState s )
		{ return sum; }
	}
	
	public static final class FourOfKind extends YahtzeeHand
	{
		public final int i;
		public FourOfKind( final int[] dice, final int i )
		{ super( dice ); this.i = i; }
		
		@Override
		public int threeOfKindScore( final YahtzeeState s )
		{ return sum; }
		
		@Override
		public int fourOfKindScore( final YahtzeeState s )
		{ return sum; }
	}
	
	private static final class FullHouse extends YahtzeeHand
	{
		public final int i;
		public final int j;
		public FullHouse( final int[] dice, final int i, final int j )
		{ super( dice ); this.i = i; this.j = j; }
		
		@Override
		public int threeOfKindScore( final YahtzeeState s )
		{ return sum; }
		
		@Override
		public int fullHouseScore( final YahtzeeState s )
		{ return 25; }
	}
	
	private static final class SmallStraight extends YahtzeeHand
	{
		public final int i;
		public SmallStraight( final int[] dice, final int i )
		{ super( dice ); this.i = i; }
		
		@Override
		public int smallStraightScore( final YahtzeeState s )
		{ return 30; }
	}
	
	private static final class LargeStraight extends YahtzeeHand
	{
		public final int i;
		public LargeStraight( final int[] dice, final int i )
		{ super( dice ); this.i = i; }
		
		@Override
		public int smallStraightScore( final YahtzeeState s )
		{ return 30; }
		
		@Override
		public int largeStraightScore( final YahtzeeState s )
		{ return 40; }
	}
	
	public static final class Yahtzee extends YahtzeeHand
	{
		public final int i;
		public Yahtzee( final int[] dice, final int i )
		{ super( dice ); this.i = i; }
		
		@Override
		public int threeOfKindScore( final YahtzeeState s )
		{ return sum; }
		
		@Override
		public int fourOfKindScore( final YahtzeeState s )
		{ return sum; }
		
		
	}
}
