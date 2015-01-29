/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.domains.cards.InfiniteSpanishDeck;
import edu.oregonstate.eecs.mcplan.domains.spbj.SpBjAction;
import edu.oregonstate.eecs.mcplan.domains.spbj.SpBjFsssAbstractionTrivial;
import edu.oregonstate.eecs.mcplan.domains.spbj.SpBjFsssModel;
import edu.oregonstate.eecs.mcplan.domains.spbj.SpBjState;

/**
 * @author jhostetler
 *
 */
public class FsssTreeBuilder<S extends State, A>
{
	private final FsssModel<S, A> m;
	private final int width;
	private final int depth;
	
	public FsssTreeBuilder( final FsssModel<S, A> m, final int width, final int depth )
	{
		this.m = m;
		this.width = width;
		this.depth = depth;
	}
	
	public FsssStateNode<S, A> buildTree( final S s )
	{
		final FsssStateNode<S, A> root = new FsssStateNode<S, A>( depth, m, s );
		int iter = 0;
		while( true ) {
			if( iter++ % 100 == 0 ) {
				System.out.println( "Iteration " + iter );
			}
			
			fsss( root, depth );
			
			// Stop search if L(root, a*) >= U(root, a) forall a != a*
			final FsssActionNode<S, A> astar = root.astar();
			final double Lstar = astar.L();
			boolean done = true;
			for( final FsssActionNode<S, A> alt : root.successors() ) {
				if( alt != astar && alt.U() > Lstar ) {
					done = false;
					break;
				}
			}
			if( done ) {
				break;
			}
		}
		return root;
	}
	
	private void fsss( final FsssStateNode<S, A> sn, final int d )
	{
		if( !sn.isTerminal() ) {
			if( sn.nvisits() == 0 ) {
//				System.out.println( "Expanding " + sn.s() + " at depth " + d );
				sn.expand( m.actions( sn.s() ), width );
			}
			sn.visit();
			final FsssActionNode<S, A> astar = sn.astar();
			final FsssStateNode<S, A> sstar = astar.sstar();
			fsss( sstar, d - 1 );
			astar.backup();
			sn.backup();
		}
		else {
//			System.out.println( "\tLeaf " + sn.s() + " at depth " + d );
//			sn.leaf( m.actions( sn.s() ) );
			sn.leaf();
		}
//		sn.backup();
	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] argv )
	{
		final int width = 20;
		final int depth = 4;
		
		final SpBjFsssModel model = new SpBjFsssModel();
		final SpBjFsssAbstractionTrivial abstraction = new SpBjFsssAbstractionTrivial( model );
		final RandomGenerator rng = new MersenneTwister( 76925342 );
		final InfiniteSpanishDeck deck = new InfiniteSpanishDeck( rng );
		final SpBjState s0 = new SpBjState( deck );
		s0.init();
		final FsssTreeBuilder<SpBjState, SpBjAction> tb
			= new FsssTreeBuilder<SpBjState, SpBjAction>( model, width, depth );
		
		final long then = System.currentTimeMillis();
		final FsssStateNode<SpBjState, SpBjAction> root = tb.buildTree( s0 );
		final long now = System.currentTimeMillis();
		
		FsssTest.printTree( root, System.out, 1 );
		
		System.out.println( "Elapsed time: " + (now - then) + " ms" );
		
//		final InventoryProblem problem = InventoryProblem.TwoProducts();
//		final InventoryFsssModel model = new InventoryFsssModel( problem );
//		final InventoryFsssAbstractionTrivial abstraction = new InventoryFsssAbstractionTrivial( problem, model );
//		final RandomGenerator rng = new MersenneTwister( 45 );
//		final InventoryState s0 = new InventoryState( rng, problem );
//		final FsssParTreeBuilder<InventoryState, InventoryAction> tb
//			= new FsssParTreeBuilder<InventoryState, InventoryAction>( model, abstraction, width, depth, s0 );
//		tb.buildTree();
	}
}
