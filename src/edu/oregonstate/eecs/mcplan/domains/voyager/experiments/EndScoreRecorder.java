package edu.oregonstate.eecs.mcplan.domains.voyager.experiments;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.domains.voyager.Planet;
import edu.oregonstate.eecs.mcplan.domains.voyager.Player;
import edu.oregonstate.eecs.mcplan.domains.voyager.Unit;
import edu.oregonstate.eecs.mcplan.domains.voyager.Voyager;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerState;
import edu.oregonstate.eecs.mcplan.sim.EpisodeListener;
import edu.oregonstate.eecs.mcplan.util.Fn;

public class EndScoreRecorder implements EpisodeListener<VoyagerState, VoyagerAction>
{
	public Player winner = null;
	public int[] population_difference = new int[Unit.values().length];
	
	@Override
	public <P extends Policy<VoyagerState, JointAction<VoyagerAction>>> void
	startState( final VoyagerState s, final P policies )
	{ }
	
	@Override
	public void preGetAction()
	{ }

	@Override
	public void postGetAction( final JointAction<VoyagerAction> action )
	{ }

	@Override
	public void onActionsTaken( final VoyagerState sprime )
	{ }

	@Override
	public void endState( final VoyagerState s )
	{
		for( final Planet p : s.planets ) {
			if( p.owner() == Player.Min ) {
				Fn.vminus_inplace( population_difference, p.population() );
			}
			else if( p.owner() == Player.Max ) {
				Fn.vplus_inplace( population_difference, p.population() );
			}
		}
		winner = Voyager.winner( s );
	}
}
