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
public class IterativeRefinementHeuristicPolicy<S, A extends UndoableAction<S, A>>
	implements AnytimePolicy<S, A>
{
	private final int max_depth_;
	private final int max_horizon_;
	private final SimultaneousMoveSimulator<S, A> sim_;
	private final ActionGenerator<S, A> action_gen_;
	private final NegamaxVisitor<S, A> visitor_;
	private final AnytimePolicy<S, A> default_policy_;
	
	private S s_ = null;
	
	/**
	 * @param switch_epoch
	 */
	public IterativeRefinementHeuristicPolicy(
			final int max_depth, final int max_horizon,
			final SimultaneousMoveSimulator<S, A> sim,
			final ActionGenerator<S, A> action_gen,
			final NegamaxVisitor<S, A> visitor,
			final AnytimePolicy<S, A> default_policy )
	{
		max_depth_ = max_depth;
		max_horizon_ = max_horizon;
		sim_ = sim;
		action_gen_ = action_gen;
		visitor_ = visitor;
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
		return getAction( maxControl() );
	}

	@Override
	public void actionResult( final A a, final S sprime, final double r )
	{ }

	@Override
	public String getName()
	{
		return "IterativeRefinement";
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
		final BoundedVisitor<S, A> bv
			= new BoundedVisitor<S, A>( visitor_, new Countdown( control ) );
		final IterativeRefinementHeuristic<S, A> heuristic
			= new IterativeRefinementHeuristic<S, A>( bv, max_depth_, max_horizon_, sim_, action_gen_.create() );
		final NegamaxSearch<S, A> search = new NegamaxSearch<S, A>(
			sim_, sim_.getNumAgents() /* One level */, action_gen_, heuristic );
		System.out.println( "*** Running root search" );
		search.run();
		System.out.println( "*** Root search done" );
		
		if( search.principalVariation() == null ) {
			System.out.println( "[IterativeRefinementPolicy] ! search.principalVariation() = NULL" );
		}
		else if( search.principalVariation().actions.get( 0 ) == null ) {
			System.out.println( "[IterativeRefinementPolicy] ! First action is NULL" );
		}
		
		if( search.principalVariation() != null && search.principalVariation().actions.get( 0 ) != null ) {
			// Choose the first action in the best PV.
			return search.principalVariation().actions.get( 0 );
		}
		else {
			System.out.println( "[IterativeRefinementPolicy] ! Using default policy" );
			assert( s_ != null );
			default_policy_.setState( s_ );
			final A a = default_policy_.getAction( control );
			// FIXME: How do we get actionResult() to the default policy (or do we)?
			return a;
		}
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "IR_" ).append( max_depth_ ).append( "_" ).append( max_horizon_ );
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
		if( obj == null || !(obj instanceof IterativeRefinementHeuristicPolicy) ) {
			return false;
		}
		final IterativeRefinementHeuristicPolicy<?, ?> that
			= (IterativeRefinementHeuristicPolicy<?, ?>) obj;
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

		final IterativeRefinementHeuristicPolicy<
			FastGalconState, DurativeUndoableAction<FastGalconState, FastGalconEvent>
		> agent0
		= new IterativeRefinementHeuristicPolicy<
			FastGalconState, DurativeUndoableAction<FastGalconState, FastGalconEvent>>(
				params.ir_max_depth, params.ir_horizon, durative_sim, cgen.create(), visitor,
				new RandomPolicy<FastGalconState, DurativeUndoableAction<FastGalconState, FastGalconEvent>>
					( rng.nextInt(), cgen ) );
		
		final RandomPolicy<FastGalconState, DurativeUndoableAction<FastGalconState, FastGalconEvent>>
		agent1 = new RandomPolicy<FastGalconState, DurativeUndoableAction<FastGalconState, FastGalconEvent>>(
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
	}
}
