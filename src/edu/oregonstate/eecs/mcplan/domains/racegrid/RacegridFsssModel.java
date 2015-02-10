/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racegrid;

import org.apache.commons.math3.random.RandomGenerator;

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
public class RacegridFsssModel extends FsssModel<RacegridState, RacegridAction>
{
	private final RandomGenerator rng;
	private final RacegridState ex;
	private final double slip;
	private final ShortestPathHeuristic h;
	
	private final TerminatingRacegridRepresenter base_repr = new TerminatingRacegridRepresenter();
	private final RacegridActionSetRepresenter action_repr = new RacegridActionSetRepresenter();
	
	private int sample_count = 0;
	
	public RacegridFsssModel( final RandomGenerator rng, final RacegridState ex, final double slip )
	{
		this.rng = rng;
		this.ex = ex;
		this.slip = slip;
		this.h = new ShortestPathHeuristic( ex );
	}
	
	@Override
	public double Vmin( final RacegridState s )
	{
		return -s.T;
	}

	@Override
	public double Vmax( final RacegridState s )
	{
		return h.evaluate( s );
	}
	
	@Override
	public double heuristic( final RacegridState s )
	{
//		return 4*h.evaluate( s );
//		return h.evaluate( s ) + Math.sqrt( s.dx*s.dx + s.dy*s.dy );
		return h.evaluate( s );
	}

	@Override
	public double discount()
	{
		return 1.0;
	}

	@Override
	public RandomGenerator rng()
	{
		return rng;
	}

	@Override
	public FactoredRepresenter<RacegridState, ? extends FactoredRepresentation<RacegridState>> base_repr()
	{
		return base_repr;
	}

	@Override
	public Representer<RacegridState, ? extends Representation<RacegridState>> action_repr()
	{
		return action_repr;
	}

	@Override
	public RacegridState initialState()
	{
		final RacegridState s = new RacegridState( ex );
		s.setRandomStartState( rng );
		return s;
	}

	@Override
	public Iterable<RacegridAction> actions( final RacegridState s )
	{
		final RacegridActionGenerator actions = new RacegridActionGenerator();
		return Fn.in( actions );
	}

	@Override
	public RacegridState sampleTransition( final RacegridState s, final RacegridAction a )
	{
		sample_count += 1;
		final RacegridState sprime = new RacegridState( s );
		a.create().doAction( sprime );
		RacegridSimulator.applyDynamics( rng, sprime, slip );
		return sprime;
	}

	@Override
	public double reward( final RacegridState s )
	{
		if( s.crashed ) {
			return -s.T;
		}
		else if( s.goal ) {
			return 0;
		}
		else {
			return -1;
		}
	}

	@Override
	public double reward( final RacegridState s, final RacegridAction a )
	{
		return 0;
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
