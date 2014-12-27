/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.domains.cards.InfiniteSpanishDeck;
import edu.oregonstate.eecs.mcplan.domains.spbj.SpBjAction;
import edu.oregonstate.eecs.mcplan.domains.spbj.SpBjActionGenerator;
import edu.oregonstate.eecs.mcplan.domains.spbj.SpBjHand;
import edu.oregonstate.eecs.mcplan.domains.spbj.SpBjState;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class FsssTest
{
	private static class SpBjFsssModel extends FsssModel<SpBjState, SpBjAction>
	{
		private final int depth;
		private final int width;
		
		public SpBjFsssModel( final int depth, final int width )
		{
			this.depth = depth;
			this.width = width;
		}
		
		@Override
		public int depth()
		{ return depth; }

		@Override
		public int width()
		{ return width; }

		@Override
		public double Vmin()
		{ return -Vmax(); }

		@Override
		public double Vmax()
		{ return SpBjHand.max_bet * SpBjHand.max_hands; }

		@Override
		public double discount()
		{ return 1.0; }

		@Override
		public Iterable<SpBjAction> actions( final FsssStateNode<SpBjState, SpBjAction> sn )
		{
			final SpBjActionGenerator actions = new SpBjActionGenerator();
			actions.setState( sn.s(), 0L );
			return Fn.takeAll( actions );
		}

		@Override
		public FsssStateNode<SpBjState, SpBjAction> sampleTransition(
				final SpBjState s, final SpBjAction a )
		{
			final SpBjState sprime = s.copy();
			a.create().doAction( sprime );
			return new FsssStateNode<SpBjState, SpBjAction>( this, sprime );
		}

		@Override
		public double reward( final SpBjState s, final SpBjAction a )
		{
			return s.r;
		}
	}
	
	/**
	 * @param args
	 */
	public static void main( final String[] args )
	{
		final SpBjFsssModel model = new SpBjFsssModel( 10, 10 );
		final RandomGenerator rng = new MersenneTwister( 43 );
		final InfiniteSpanishDeck deck = new InfiniteSpanishDeck( rng );
		final SpBjState s0 = new SpBjState( deck );
		s0.init();
		model.buildTree( s0 );
	}
}
