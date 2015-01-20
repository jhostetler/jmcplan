/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.spbj;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssModel;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class SpBjFsssModel extends FsssModel<SpBjState, SpBjAction>
{
	private final SpBjNullRepresenter base_repr = new SpBjNullRepresenter();
	private final SpBjActionSetRepresenter action_repr = new SpBjActionSetRepresenter();
	
	private int sample_count = 0;
	
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
	public Iterable<SpBjAction> actions( final SpBjState s )
	{
		final SpBjActionGenerator actions = new SpBjActionGenerator();
		actions.setState( s, 0L );
		return Fn.takeAll( actions );
	}

	@Override
	public SpBjState sampleTransition( final SpBjState s, final SpBjAction a )
	{
		sample_count += 1;
		final SpBjState sprime = s.copy();
		a.create().doAction( sprime );
		return sprime;
	}

	/**
	 * @see edu.oregonstate.eecs.mcplan.search.fsss.FsssModel#reward(edu.oregonstate.eecs.mcplan.State, java.lang.Object)
	 */
	@Override
	public double reward( final SpBjState s, final SpBjAction a )
	{
		return s.r;
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
}
