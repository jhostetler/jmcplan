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
public class ExpectimaxSearch<S, A extends UndoableAction<S, A>> implements Runnable
{
	private final UndoSimulator<S, A> sim_;
	private final int max_depth_;
	private double alpha_ = 0.0;
	private double beta_ = 0.0;
	private final ActionGenerator<S, A> action_gen_;
	private final NegamaxVisitor<S, A> visitor_;
	private double score_ = Double.NaN;
	private final PrincipalVariation<S, A> pv_;
	
	/**
	 * @param sim
	 * @param max_depth Maximum search depth in individual player moves.
	 * @param alpha
	 * @param beta
	 * @param action_gen
	 * @param visitor
	 */
	public ExpectimaxSearch( final UndoSimulator<S, A> sim,
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
		pv_ = new PrincipalVariation<S, A>( max_depth_ );
	}
	
	public double score()
	{
		return score_;
	}
	
	public PrincipalVariation<S, A> principalVariation()
	{
		return pv_;
	}
	
	@Override
	public void run()
	{
		visitor_.startVertex( sim_.state() );
		// NOTE: Making an assumption about the indices of players here.
		final int turn = sim_.getTurn();
		if( turn == 0 ) {
			// Player 0 is the maximizing player
			score_ = visit( max_depth_ - turn, alpha_, beta_, 1, pv_, visitor_ );
		}
		else if( turn == 1 ) {
			// Player 1 is the minimizing player
			score_ = visit( max_depth_ - turn, alpha_, beta_, -1, pv_, visitor_ );
		}
		else {
			throw new AssertionError( "Simulator started on player " + turn + "'s turn" );
		}
	}
	
	private double visit(
		final int depth, double alpha, final double beta, final int color,
		final PrincipalVariation<S, A> pv,
		final NegamaxVisitor<S, A> visitor )
	{
		final S s = sim_.state();
		// It's a tree, so these are 1-to-1
		visitor.initializeVertex( s );
		visitor.discoverVertex( s );
		// Return value (NOTE: except when pruning an edge -- see below)
		final double ret;
		final PrincipalVariation<S, A> future
			= new PrincipalVariation<S, A>( max_depth_ );
		
		if( visitor.isGoal( s ) ) {
			visitor.goal( s );
			pv.setState( 0, sim_.toString() );
			pv.cmove = 0;
			ret = color * visitor.heuristic( s );
		}
		else if( depth == 0 ) {
			visitor.depthLimit( s );
			pv.setState( 0, sim_.toString() );
			pv.cmove = 0;
			ret = color * visitor.heuristic( s );
		}
		else {
			final ActionGenerator<S, A> local_action_gen = action_gen_.create();
			local_action_gen.setState( s, sim_.depth() );
			while( local_action_gen.hasNext() ) {
				final A a = local_action_gen.next();
				sim_.takeAction( a );
				visitor.examineEdge( a, s );
				final double value = -visit( depth - 1, -beta, -alpha, -color, future, visitor );
				
				if( value >= beta ) {
					visitor.prunedEdge( a, s );
					// NOTE: We're not returning via the common return at the bottom.
					sim_.untakeLastAction();
					visitor.finishVertex( s );
					return value;
				}
				else {
					visitor.treeEdge( a, s );
					sim_.untakeLastAction();
					if( value > alpha ) {
//						System.out.println( "Improvement (" + value + " > " + alpha + ")" );
						alpha = value;
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
		final int Nplanets = 10;
		final double min_launch_percentage = 0.2;
		final int launch_size_steps = 10;
		final GalconSimulator sim = new GalconSimulator(
			horizon, epoch, false, false, 641, Nplanets, min_launch_percentage, launch_size_steps );
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
