/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.spbj;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.domains.cards.InfiniteSpanishDeck;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssModel;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class SpBjFsssModel extends FsssModel<SpBjState, SpBjAction>
{
	private final RandomGenerator rng;
	private final SpBjNullRepresenter base_repr = new SpBjNullRepresenter();
	private final SpBjActionSetRepresenter action_repr = new SpBjActionSetRepresenter();
	
	private int sample_count = 0;
	
	private final InfiniteSpanishDeck deck;
	
	public SpBjFsssModel( final RandomGenerator rng )
	{
		this.rng = rng;
		deck = new InfiniteSpanishDeck( rng );
	}
	
	@Override
	public SpBjFsssModel create( final RandomGenerator rng )
	{
		return new SpBjFsssModel( rng );
	}
	
	@Override
	public double Vmin( final SpBjState s )
	{ return -Vmax( s ); }

	@Override
	public double Vmax( final SpBjState s )
	{ return SpBjHand.max_bet * SpBjHand.max_hands; }
	
	@Override
	public double Vmin( final SpBjState s, final SpBjAction a )
	{ return -Vmax( s, a ); }

	@Override
	public double Vmax( final SpBjState s, final SpBjAction a )
	{ return SpBjHand.max_bet *SpBjHand.max_hands; }
	
	@Override
	public double heuristic( final SpBjState s )
	{ return 0; }

	@Override
	public double discount()
	{ return 1.0; }
	
	@Override
	public SpBjState initialState()
	{
		final SpBjState s0 = new SpBjState( deck );
		s0.init();
		return s0;
	}

	@Override
	public Iterable<SpBjAction> actions( final SpBjState s )
	{
		final SpBjActionGenerator actions = new SpBjActionGenerator();
		actions.setState( s, 0L );
		return Fn.in( actions );
	}

	@Override
	public SpBjState sampleTransition( final SpBjState s, final SpBjAction a )
	{
		sample_count += 1;
		final SpBjState sprime = s.copy();
		a.create().doAction( rng, sprime );
		return sprime;
	}
	
	@Override
	public double reward( final SpBjState s )
	{
		return s.r;
	}

	@Override
	public double reward( final SpBjState s, final SpBjAction a )
	{
//		return s.r;
		return 0;
	}

	@Override
	public FactoredRepresenter<SpBjState, ? extends FactoredRepresentation<SpBjState>> base_repr()
	{
		return base_repr;
	}

	@Override
	public Representer<SpBjState, ? extends Representation<SpBjState>> action_repr()
	{
		return action_repr;
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

	@Override
	public RandomGenerator rng()
	{
		return rng;
	}
}
