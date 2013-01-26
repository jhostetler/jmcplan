/**
 * 
 */
package edu.oregonstate.eecs.mcplan.agents.galcon;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.apache.commons.math3.random.MersenneTwister;

import edu.oregonstate.eecs.mcplan.domains.galcon.FastGalconEvent;
import edu.oregonstate.eecs.mcplan.domains.galcon.FastGalconState;
import edu.oregonstate.eecs.mcplan.domains.galcon.GalconSimulator;
import edu.oregonstate.eecs.mcplan.domains.galcon.LookaheadLeafHeuristic;
import edu.oregonstate.eecs.mcplan.domains.galcon.graphics.Monitor;
import edu.oregonstate.eecs.mcplan.domains.galcon.graphics.MonitorUpdater;
import edu.oregonstate.eecs.mcplan.experiments.Parameters;
import edu.oregonstate.eecs.mcplan.search.NegamaxSearch;
import edu.oregonstate.eecs.mcplan.search.NegamaxVisitor;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveRunner;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveSimulator;
import edu.oregonstate.eecs.mcplan.util.Countdown;

/**
 * @author jhostetler
 *
 */
public class IterativeDeepeningHeuristicPolicy<S, A extends UndoableAction<S, A>>
	implements AnytimePolicy<S, A>
{
	private final int max_depth_;
	private final UndoSimulator<S, A> sim_;
	private final ActionGenerator<S, A> action_gen_;
	private final NegamaxVisitor<S, A> visitor_;
	private final AnytimePolicy<S, A> default_policy_;
	
	/**
	 * @param switch_epoch
	 */
	public IterativeDeepeningHeuristicPolicy(
			final int max_depth,
			final UndoSimulator<S, A> sim,
			final ActionGenerator<S, A> action_gen,
			final NegamaxVisitor<S, A> visitor,
			final AnytimePolicy<S, A> default_policy )
	{
		max_depth_ = max_depth;
		sim_ = sim;
		action_gen_ = action_gen;
		visitor_ = visitor;
		default_policy_ = default_policy;
	}

	@Override
	public String getName()
	{
		return "IterativeDeepening";
	}

	@Override
	public long minControl()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long maxControl()
	{
		return Long.MAX_VALUE;
	}
	
	@Override
	public void setState( final S s )
	{ }

	@Override
	public A getAction()
	{
		return getAction( maxControl() );
	}
	
	// -----------------------------------------------------------------------

	@Override
	public A getAction( final long control )
	{
		final BoundedVisitor<S, A> bv = new BoundedVisitor<S, A>( visitor_, new Countdown( control ) );
		final IterativeDeepeningHeuristic<S, A> heuristic
			= new IterativeDeepeningHeuristic<S, A>( bv, max_depth_, sim_, action_gen_.create() );
		final NegamaxSearch<S, A> search = new NegamaxSearch<S, A>(
			sim_, sim_.getNumAgents() /* One level at normal time discretization */,
			action_gen_.create(), heuristic );
		search.run();
		
		if( search.principalVariation() != null && search.principalVariation().actions.get( 0 ) != null ) {
			// Choose the first action in the best PV.
			return search.principalVariation().actions.get( 0 );
		}
		else {
			default_policy_.setState( sim_.state() );
			return default_policy_.getAction();
		}
	}
	
	// -----------------------------------------------------------------------

	@Override
	public void actionResult( final A a, final S sprime, final double r )
	{ }
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "ID_" ).append( max_depth_ );
		return sb.toString();
	}
	
	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof IterativeDeepeningHeuristicPolicy) ) {
			return false;
		}
		final IterativeDeepeningHeuristicPolicy<?, ?> that
			= (IterativeDeepeningHeuristicPolicy<?, ?>) obj;
		return toString().equals( that.toString() );
	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] args ) throws FileNotFoundException
	{
		final Parameters params = new Parameters();
		final MersenneTwister rng = new MersenneTwister( params.master_seed );
		final GalconSimulator sim = new GalconSimulator(
			params.horizon, params.primitive_epoch, false, false, params.master_seed,
			params.min_launch_percentage, params.launch_size_steps );
		final FastGalconState fast_state = new FastGalconState(
			sim, params.policy_epoch, params.horizon, params.min_launch_percentage, params.launch_size_steps );
		final SimultaneousMoveSimulator<FastGalconState, FastGalconEvent> fast_sim = fast_state;
		
		final ArrayList<DurativeUndoableAction<FastGalconState, FastGalconEvent>> policies
			= Util.durativeWrapPolicies( params.Pi(), params.policy_epoch );
		
		final ConstantActionGenerator<
			FastGalconState,
			DurativeUndoableAction<FastGalconState, FastGalconEvent>
		> cgen;
		cgen = new ConstantActionGenerator<
			FastGalconState,
			DurativeUndoableAction<FastGalconState, FastGalconEvent>>( policies );
		final CoarseSimulation<FastGalconState, FastGalconEvent> durative_sim
			= new CoarseSimulation<FastGalconState, FastGalconEvent>( fast_sim, params.policy_epoch );
		
		final IterativeDeepeningHeuristicPolicy<
			FastGalconState, DurativeUndoableAction<FastGalconState, FastGalconEvent>
		> agent0
		= new IterativeDeepeningHeuristicPolicy<
			FastGalconState, DurativeUndoableAction<FastGalconState, FastGalconEvent>>(
				params.id_max_depth, durative_sim, cgen,
				new LookaheadLeafHeuristic<DurativeUndoableAction<FastGalconState, FastGalconEvent>>(
					params.id_lookahead, System.out ),
				new RandomPolicy<FastGalconState, DurativeUndoableAction<FastGalconState, FastGalconEvent>>(
					rng.nextInt(), cgen ));
		
		final RandomPolicy<FastGalconState, DurativeUndoableAction<FastGalconState, FastGalconEvent>>
		agent1 = new RandomPolicy<FastGalconState, DurativeUndoableAction<FastGalconState, FastGalconEvent>>(
			params.master_seed, cgen.create() );
		
		final ArrayList<AnytimePolicy<FastGalconState, DurativeUndoableAction<FastGalconState, FastGalconEvent>>>
			agents = new ArrayList<AnytimePolicy<FastGalconState, DurativeUndoableAction<FastGalconState, FastGalconEvent>>>();
		agents.add( agent0 );
		agents.add( agent1 );

		try {
			Monitor.createMonitor( sim.getState().getMapWidth(), sim.getState().getMapHeight() );
		}
		catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		Monitor.getInstance().showWindow( true );
		
		final SimultaneousMoveRunner<
			FastGalconState,
			DurativeUndoableAction<FastGalconState, FastGalconEvent>
		> runner
			= new SimultaneousMoveRunner<FastGalconState,
										 DurativeUndoableAction<FastGalconState, FastGalconEvent>>(
				durative_sim, agents, params.T, params.max_time );
		runner.addListener( new MonitorUpdater<DurativeUndoableAction<FastGalconState, FastGalconEvent>>() );
		
		runner.run();
	}

}
