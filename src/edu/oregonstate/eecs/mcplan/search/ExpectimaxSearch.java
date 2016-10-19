/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;

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
		final int turn = sim_.turn();
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
}
