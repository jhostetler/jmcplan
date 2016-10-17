package edu.oregonstate.eecs.mcplan.domains.planetwars.a;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.domains.planetwars.PwEvent;
import edu.oregonstate.eecs.mcplan.domains.planetwars.PwGame;
import edu.oregonstate.eecs.mcplan.domains.planetwars.PwGroundRepresenter;
import edu.oregonstate.eecs.mcplan.domains.planetwars.PwPlayer;
import edu.oregonstate.eecs.mcplan.domains.planetwars.PwSimulator;
import edu.oregonstate.eecs.mcplan.domains.planetwars.PwState;
import edu.oregonstate.eecs.mcplan.domains.spbj.SpBjActionSetRepresenter;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssModel;

/**
 * In treating Pw as a single-agent problem, we assume the perspective of
 * PwPlayer.Min.
 */
public class PwA_FsssModel extends FsssModel<PwState, JointAction<PwEvent>>
{
	private final PwGame game;
	private final PwGroundRepresenter base_repr;
	private final SpBjActionSetRepresenter action_repr = new SpBjActionSetRepresenter();
	
	private int sample_count = 0;
	
	public PwA_FsssModel( final PwState s )
	{
		game = s.game;
		base_repr = new PwGroundRepresenter( s );
	}
	
	@Override
	public FsssModel<PwState, JointAction<PwEvent>> create( final RandomGenerator rng )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public double Vmin( final PwState s, final JointAction<PwEvent> a )
	{
		return -1;
	}

	@Override
	public double Vmax( final PwState s, final JointAction<PwEvent> a )
	{
		return 1;
	}
	
	@Override
	public double Vmin( final PwState s )
	{
		return -1;
	}

	@Override
	public double Vmax( final PwState s )
	{
		return 1;
	}

	@Override
	public double discount()
	{
		return 1.0;
	}

	@Override
	public double heuristic( final PwState s )
	{
		return (s.supply( PwPlayer.Min ) - s.supply( PwPlayer.Max )) / (double) game.max_population;
	}

	@Override
	public RandomGenerator rng()
	{
		return game.rng;
	}

	@Override
	public FactoredRepresenter<PwState, ? extends FactoredRepresentation<PwState>> base_repr()
	{
		return base_repr;
	}

	@Override
	public Representer<PwState, ? extends Representation<PwState>> action_repr()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PwState initialState()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<JointAction<PwEvent>> actions( final PwState s )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PwState sampleTransition( final PwState s, final JointAction<PwEvent> a )
	{
		sample_count += 1;
		
		final PwState sprime = new PwState( s );
		final PwSimulator sim = new PwSimulator( game, sprime );
		sim.takeAction( a.create() );
		return sprime;
	}

	@Override
	public double reward( final PwState s )
	{
		final PwPlayer winner = s.winner();
		if( winner != null ) {
			if( winner == PwPlayer.Min ) {
				return 1;
			}
			else {
				return -1;
			}
		}
		return 0;
	}

	@Override
	public double reward( final PwState s, final JointAction<PwEvent> a )
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
