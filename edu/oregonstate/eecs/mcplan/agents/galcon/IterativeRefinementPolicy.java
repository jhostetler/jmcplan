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
import edu.oregonstate.eecs.mcplan.search.IterativeRefinementSearch;
import edu.oregonstate.eecs.mcplan.search.NegamaxVisitor;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveRunner;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveSimulator;
import edu.oregonstate.eecs.mcplan.util.Countdown;

/**
 * @author jhostetler
 *
 */
public class IterativeRefinementPolicy<S, A extends UndoableAction<S, A>>
	implements AnytimePolicy<S, A>
{
	private final NegamaxVisitor<S, A> visitor_;
	private final int max_depth_;
	private final int max_horizon_;
	private final SimultaneousMoveSimulator<S, A> sim_;
	private final ActionGenerator<S, A> action_gen_;
	private final Policy<S, A> default_policy_;
	
	private S s_ = null;
	private Policy<S, A> policy_used_ = null;
	
	public IterativeRefinementPolicy( final int max_depth,
									  final int max_horizon,
									  final SimultaneousMoveSimulator<S, A> sim,
									  final ActionGenerator<S, A> action_gen,
									  final NegamaxVisitor<S, A> visitor,
									  final Policy<S, A> default_policy )
	{
		visitor_ = visitor;
		max_depth_ = max_depth;
		max_horizon_ = max_horizon;
		sim_ = sim;
		action_gen_ = action_gen;
		default_policy_ = default_policy;
	}

	@Override
	public void setState( final S s )
	{
		s_ = s;
	}

	@Override
	public A getAction()
	{
		return getAction( Long.MAX_VALUE );
	}

	@Override
	public void actionResult( final A a, final S sprime, final double r )
	{
		assert( policy_used_ != null );
		policy_used_.actionResult( a, sprime, r );
	}

	@Override
	public String getName()
	{
		return "DirectIterativeRefinement";
	}

	@Override
	public long minControl()
	{
		return 0;
	}

	@Override
	public long maxControl()
	{
		return Long.MAX_VALUE;
	}

	@Override
	public A getAction( final long control )
	{
		System.out.println( "[DirectIterativeRefinement] getAction()" );
		final BoundedVisitor<S, A> bv
			= new BoundedVisitor<S, A>( visitor_, new Countdown( control ) );
		final IterativeRefinementSearch<S, A> search
			= new IterativeRefinementSearch<S, A>( sim_, action_gen_, bv, max_depth_, max_horizon_ );
		search.run();
		
		if( search.principalVariation() != null && search.principalVariation().actions.get( 0 ) != null ) {
			System.out.println( "[DirectIterativeRefinement] PV: " + search.principalVariation() );
			policy_used_ = search.principalVariation().actions.get( 0 ).policy_;
		}
		else {
			System.out.println( "[DirectIterativeRefinement] ! Using leaf heuristic" );
			policy_used_ = default_policy_;
		}
		
		policy_used_.setState( s_ );
		return policy_used_.getAction();
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
		final NegamaxVisitor<
			FastGalconState,
			DurativeUndoableAction<FastGalconState, FastGalconEvent>
		> visitor
			= new LookaheadLeafHeuristic<DurativeUndoableAction<FastGalconState, FastGalconEvent>>(
				params.ir_lookahead, System.out );
		
		final ConstantActionGenerator<
			FastGalconState,
			DurativeUndoableAction<FastGalconState, FastGalconEvent>
		> cgen;
		cgen = new ConstantActionGenerator<
			FastGalconState,
			DurativeUndoableAction<FastGalconState, FastGalconEvent>>( policies );
		final CoarseSimulation<FastGalconState, FastGalconEvent> durative_sim
			= new CoarseSimulation<FastGalconState, FastGalconEvent>( fast_sim, params.policy_epoch );

		final AnytimePolicy<
			FastGalconState, DurativeUndoableAction<FastGalconState, FastGalconEvent>
		> agent0
			= new IterativeRefinementPolicy<
				FastGalconState, DurativeUndoableAction<FastGalconState, FastGalconEvent>>(
					params.ir_max_depth, params.ir_horizon, durative_sim, cgen.create(), visitor,
					new RandomPolicy<FastGalconState, DurativeUndoableAction<FastGalconState, FastGalconEvent>>(
						rng.nextInt(), cgen ) );
		
		final AnytimePolicy<
			FastGalconState, DurativeUndoableAction<FastGalconState, FastGalconEvent>
		> agent1
			= new RandomPolicy<FastGalconState, DurativeUndoableAction<FastGalconState, FastGalconEvent>>(
				rng.nextInt(), cgen.create() );
		
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
		
		System.exit( 0 );
	}
}
