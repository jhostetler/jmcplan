/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import edu.oregonstate.eecs.mcplan.Agent;
import edu.oregonstate.eecs.mcplan.agents.galcon.ActionGenerator;
import edu.oregonstate.eecs.mcplan.agents.galcon.ExpandPolicy;
import edu.oregonstate.eecs.mcplan.agents.galcon.UndoSimulator;
import edu.oregonstate.eecs.mcplan.agents.galcon.UndoableAction;
import edu.oregonstate.eecs.mcplan.domains.galcon.FastGalconEvent;
import edu.oregonstate.eecs.mcplan.domains.galcon.FastGalconNothingAction;
import edu.oregonstate.eecs.mcplan.domains.galcon.FastGalconState;
import edu.oregonstate.eecs.mcplan.domains.galcon.FastPlanet;
import edu.oregonstate.eecs.mcplan.domains.galcon.GalconSimulator;

/**
 * @author jhostetler
 *
 */
public class NegamaxSearch<S, A extends UndoableAction<S, A>> implements GameTreeSearch<S, A>
{
	private final UndoSimulator<S, A> sim_;
	private final int max_depth_;
	private double alpha_ = 0.0;
	private double beta_ = 0.0;
	private final ActionGenerator<S, A> action_gen_;
	private final NegamaxVisitor<S, A> visitor_;
	private double score_ = Double.NaN;
	private PrincipalVariation<S, A> complete_pv_ = null;
	private boolean complete_ = false;
	
	/**
	 * @param sim
	 * @param max_depth Maximum search depth in individual player moves.
	 * @param alpha
	 * @param beta
	 * @param action_gen
	 * @param visitor
	 */
	public NegamaxSearch( final UndoSimulator<S, A> sim,
								final int max_depth,
								final ActionGenerator<S, A> action_gen,
								final NegamaxVisitor<S, A> visitor )
	{
		sim_ = sim;
		max_depth_ = max_depth;
		alpha_ = -Double.MAX_VALUE;
		beta_ = Double.MAX_VALUE;
		action_gen_ = action_gen;
		visitor_ = visitor;
		
		System.out.println( "[Negamax search]" );
		System.out.println( "[max_depth_ = " + max_depth_ + "]" );
//		System.out.println( "[s0: " + sim_.detailString() + "]" );
	}
	
	@Override
	public double score()
	{
		return score_;
	}
	
	@Override
	public PrincipalVariation<S, A> principalVariation()
	{
		return complete_pv_;
	}
	
	@Override
	public boolean isComplete()
	{
		return complete_;
	}
	
	private void setPv( final PrincipalVariation<S, A> pv )
	{
		complete_pv_ = new PrincipalVariation<S, A>( pv );
	}
	
	@Override
	public void run()
	{
		visitor_.startVertex( sim_.state() );
		// NOTE: Making an assumption about the indices of players here.
		final int turn = sim_.getTurn();
		System.out.println( "[Negamax: starting on Turn " + turn + "]" );
		complete_ = true;
		score_ = visit( max_depth_, alpha_, beta_, 1,
						new PrincipalVariation<S, A>( max_depth_ ), visitor_ );
	}
	
	private double visit(
		final int depth, double alpha, final double beta, final int color,
		final PrincipalVariation<S, A> pv,
		final NegamaxVisitor<S, A> visitor )
	{
		final S s = sim_.state();
		// It's a tree, so these are 1-to-1
		visitor.initializeVertex( s );
		final boolean stop = visitor.discoverVertex( s );
		
		// Return value (NOTE: except when pruning an edge -- see below)
		final double ret;
		final PrincipalVariation<S, A> future = new PrincipalVariation<S, A>( max_depth_ );
		
		if( stop ) {
			pv.setState( 0, sim_.toString() );
			pv.cmove = 0;
			ret = alpha; // This is the "default" return value.
			complete_ = false;
		}
		else if( visitor.isGoal( s ) ) {
			final double value = visitor.goal( s );
			pv.setState( 0, sim_.toString() );
			pv.cmove = 0;
			ret = color * value;
		}
		else if( depth == 0 ) {
			visitor.depthLimit( s );
			pv.setState( 0, sim_.toString() );
			pv.cmove = 0;
			ret = color * visitor.heuristic( s );
		}
		else {
			final ActionGenerator<S, A> local_action_gen = action_gen_.create();
			local_action_gen.setState( s );
			while( local_action_gen.hasNext() ) {
				final A a = local_action_gen.next();
				sim_.takeAction( a );
				final S sprime = sim_.state();
				visitor.examineEdge( a, sprime );
				final double value = -visit( depth - 1, -beta, -alpha, -color, future, visitor );
				
				if( value >= beta ) {
					visitor.prunedEdge( a, sprime );
					// NOTE: We're not returning via the common return at the bottom.
					sim_.untakeLastAction();
					visitor.finishVertex( s );
					return value;
				}
				else {
					visitor.treeEdge( a, sprime );
					sim_.untakeLastAction();
					if( value > alpha ) {
//						System.out.println( "Improvement (" + value + " > " + alpha + ")" );
						alpha = value;
						pv.alpha = alpha;
						pv.beta = beta;
						pv.score = value;
						pv.cmove = future.cmove + 1;
						pv.setState( 0, sim_.toString() );
						for( int i = 0; i < future.cmove + 1; ++i ) {
							pv.states.set( i + 1, future.states.get( i ) );
						}
						pv.actions.set( 0, a );
						for( int i = 0; i < future.cmove; ++i ) {
							pv.actions.set( i + 1, future.actions.get( i ) );
						}
						
						if( depth == max_depth_ ) {
							setPv( pv );
							visitor.principalVariation( pv );
						}
					}
				}
			}
			ret = alpha;
		}
		
		visitor.finishVertex( s );
		return ret;
	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] args ) throws FileNotFoundException
	{
		final int epoch = 10;
		final int horizon = 5000;
		final double min_launch_percentage = 0.2;
		final int launch_size_steps = 10;
		final GalconSimulator sim = new GalconSimulator(
			horizon, epoch, false, false, 641, min_launch_percentage, launch_size_steps );
		final FastGalconState fast_state = new FastGalconState(
			sim, horizon, epoch, min_launch_percentage, launch_size_steps );
		final List<Agent> policies = new ArrayList<Agent>();
		policies.add( new ExpandPolicy( "true 1.0 0.1 1.0 10000 0.1 2.0".split( " " ) ) );
		policies.add( new ExpandPolicy( "true 1.0 0.1 1.0 100000 0.1 2.0".split( " " ) ) );
		final NegamaxVisitor<FastGalconState, FastGalconEvent> visitor =
			new LoggingNegamaxVisitor<FastGalconState, FastGalconEvent>( System.out )
		{
			@Override
			public boolean isGoal( final FastGalconState s )
			{
				return false;
			}
			
			@Override
			public double heuristic( final FastGalconState s )
			{
				final int player = s.getTurn();
				final int lookahead = 40;
				final FastGalconEvent nothing = new FastGalconNothingAction();
				// Run simulator forward
				for( int i = 0; i < lookahead; ++i ) {
					s.takeAction( nothing );
				}
				// Compute heuristic
				int friendly_pop = 0;
				int enemy_pop = 0;
				for( final FastPlanet p : s.planets_ ) {
					if( p.owner_ == player ) {
						friendly_pop += p.population_;
					}
					else if( p.owner_ == (1 - player) ) {
						enemy_pop += p.population_;
					}
				}
				// Undo simulation
				for( int i = lookahead - 1; i >= 0; --i ) {
					s.untakeLastAction();
				}

				return (double) friendly_pop - enemy_pop;
			}
		};
		final ActionGenerator<FastGalconState, FastGalconEvent> action_gen
			= new FastGalconState.Actions();
		
		final NegamaxSearch<FastGalconState, FastGalconEvent> search
			= new NegamaxSearch<FastGalconState, FastGalconEvent>(
				fast_state, 4,
				action_gen, visitor );
//				new NegamaxVisitorBase<GalconState, GalconAction>() );
//				new LoggingNegamaxVisitor<GalconState, GalconAction>( System.out ) );
		final long start = System.currentTimeMillis();
		search.run();
		final long stop = System.currentTimeMillis();
		System.out.println( "PV: " + search.principalVariation() );
		System.out.println( "Time: " + (stop - start) + " ms" );
	}
}
