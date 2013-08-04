/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.oregonstate.eecs.mcplan.Agent;
import edu.oregonstate.eecs.mcplan.Simulator;
import edu.oregonstate.eecs.mcplan.OldState;
import edu.oregonstate.eecs.mcplan.agents.galcon.ExpandPolicy;
import edu.oregonstate.eecs.mcplan.domains.galcon.GalconAction;
import edu.oregonstate.eecs.mcplan.domains.galcon.GalconSimulator;
import edu.oregonstate.eecs.mcplan.domains.galcon.GalconState;
import edu.oregonstate.eecs.mcplan.util.CircularListIterator;

/**
 * Evaluates a joint policy by simulating it repeatedly.
 * 
 * @author jhostetler
 */
public final class RolloutEvaluator<S extends OldState, A> implements Runnable
{
	final Simulator<S, A> simulator_;
	final List<Agent> policies_;
	final int episodes_;
	final int max_depth_;
	
	final Map<Integer, Double> rewards_ = new TreeMap<Integer, Double>();
	
	/**
	 * Constructor.
	 * @param simulator The base simulator. Simulators for each rollout are
	 * obtained by calling Simulator.copy().
	 * @param policies The joint policy. Policies take their turns in the order
	 * specified.
	 * @param episodes The number of complete simulations.
	 * @param max_depth Maximum rollout depth. Specify -1 to simulate to the
	 * end of the game.
	 */
	public RolloutEvaluator( final Simulator<S, A> simulator, final List<Agent> policies,
							 final int episodes, final int max_depth )
	{
		simulator_ = simulator;
		policies_ = policies;
		episodes_ = episodes;
		max_depth_ = max_depth;
		for( int i = 0; i < simulator_.getNumberOfAgents(); ++i ) {
			rewards_.put( i, 0.0 );
		}
	}

	@Override
	public void run()
	{
		for( int s = 0; s < episodes_; ++s ) {
			final Simulator<S, A> sim = simulator_.copy();
			final CircularListIterator<Agent> policy_itr = new CircularListIterator<Agent>( policies_ );
			int depth = max_depth_;
			while( !sim.isTerminalState() ) {
				if( depth-- == 0 ) {
					System.out.println( "Max depth reached" );
					break;
				}
				final Agent policy = policy_itr.next();
				final A action = policy.selectAction( sim.getState(), sim );
				final int agent_id = sim.getState().getAgentTurn();
				sim.takeAction( action );
				final double cumulative_reward = rewards_.get( agent_id );
				final double instantaneous_reward = sim.getReward( agent_id );
				System.out.println( "[Agent " + agent_id + "]: instantaneous reward = " + instantaneous_reward );
				rewards_.put( agent_id, cumulative_reward + instantaneous_reward );
			}
		}
	}
	
	public int episodes()
	{
		return episodes_;
	}
	
	public double reward( final int agent )
	{
		return rewards_.get( agent );
	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] args )
	{
		final int Nplanets = 10;
		final GalconSimulator sim = new GalconSimulator( 5000, 10, false, false, 641, Nplanets, 0.1, 10 );
		final List<Agent> policies = new ArrayList<Agent>();
		policies.add( new ExpandPolicy( "true 1.0 0.1 1.0 10000 0.1 2.0".split( " " ) ) );
		policies.add( new ExpandPolicy( "true 1.0 0.1 1.0 100000 0.1 2.0".split( " " ) ) );
		final RolloutEvaluator<GalconState, GalconAction> eval =
			new RolloutEvaluator<GalconState, GalconAction>( sim, policies, 1, -1 );
		eval.run();
		System.out.println( eval.reward( 0 ) );
		System.out.println( eval.reward( 1 ) );
	}
}
