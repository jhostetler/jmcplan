/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;

/**
 * @author jhostetler
 *
 */
public class NegamaxSearch<S, A extends VirtualConstructor<A>> implements GameTreeSearch<S, A>
{
	private static final Logger log = LoggerFactory.getLogger( NegamaxSearch.class );
	
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
		final int turn = sim_.turn();
		System.out.println( "[Negamax: starting on Turn " + turn + "]" );
		complete_ = true;
		score_ = visit( max_depth_, alpha_, beta_, 1,
						new PrincipalVariation<S, A>( max_depth_ ), visitor_ );
	}
	
	private void setPvLeafState( final PrincipalVariation<S, A> pv, final double alpha, final double beta )
	{
		pv.setState( 0, sim_.toString() );
		pv.cmove = 0;
		pv.alpha = alpha;
		pv.beta = beta;
	}
	
	private double visit(
		final int depth, double alpha, final double beta, final int color,
		final PrincipalVariation<S, A> pv,
		final NegamaxVisitor<S, A> visitor )
	{
		assert( alpha <= beta );
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
			pv.alpha = alpha;
			pv.beta = beta;
//			ret = alpha; // This is the "default" return value.
			ret = color * visitor.heuristic( s );
			complete_ = false;
		}
		else if( visitor.isGoal( s ) ) {
			final double value = visitor.goal( s );
			pv.setState( 0, sim_.toString() );
			pv.cmove = 0;
			pv.alpha = alpha;
			pv.beta = beta;
			ret = color * value;
//			ret = value;
		}
		else if( depth == 0 ) {
			visitor.depthLimit( s );
			pv.setState( 0, sim_.toString() );
			pv.cmove = 0;
			pv.alpha = alpha;
			pv.beta = beta;
			ret = color * visitor.heuristic( s );
//			ret = visitor.heuristic( s );
		}
		else {
			// Must make a copy to preserve state
			final ActionGenerator<S, A> local_action_gen = action_gen_.create();
			local_action_gen.setState( s, sim_.depth(), sim_.turn() );
			final Iterator<A> actions = visitor.orderActions( s, local_action_gen );
			while( actions.hasNext() ) {
				final A a = actions.next();
				log.info( "Turn {}: Considering {}", sim_.turn(), a.toString() );
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
						pv.beta = beta; //-future.alpha; //beta;
						if( pv.alpha > pv.beta ) {
							System.out.println( "!!! alpha = " + pv.alpha + ", beta = " + pv.beta );
							throw new AssertionError();
						}
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
}
