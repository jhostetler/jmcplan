/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import edu.oregonstate.eecs.mcplan.State;

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
		final FsssStateNode<S, A> root = new FsssStateNode<S, A>( m, s );
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
		if( d > 1 && !sn.s().isTerminal() ) {
			if( sn.n() == 0 ) {
//				System.out.println( "Expanding " + sn.s() + " at depth " + d );
				sn.expand( m.actions( sn.s() ), width );
			}
			sn.visit();
			final FsssActionNode<S, A> astar = sn.astar();
			final FsssStateNode<S, A> sstar = astar.sstar();
			fsss( sstar, d - 1 );
			astar.backup();
		}
		else {
//			System.out.println( "\tLeaf " + sn.s() + " at depth " + d );
			sn.leaf( m.actions( sn.s() ) );
		}
		sn.backup();
	}
}
