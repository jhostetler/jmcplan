/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import edu.oregonstate.eecs.mcplan.State;

/**
 * @author jhostetler
 *
 */
public abstract class FsssModel<S extends State, A>
{
	public abstract int depth();
	public abstract int width();
	public abstract double Vmin();
	public abstract double Vmax();
	public abstract double discount();
	
	public abstract Iterable<A> actions( final FsssStateNode<S, A> sn );
	public abstract FsssStateNode<S, A> sampleTransition( final S s, final A a );
	public abstract double reward( final S s, final A a );
	
	public FsssStateNode<S, A> buildTree( final S s )
	{
		final FsssStateNode<S, A> root = new FsssStateNode<S, A>( this, s );
		int iter = 0;
		while( true ) {
			if( iter++ % 100 == 0 ) {
				System.out.println( "Iteration " + iter );
			}
			
			fsss( root, depth() );
			
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
				sn.expand( actions( sn ) );
			}
			sn.visit();
			final FsssActionNode<S, A> astar = sn.astar();
			final FsssStateNode<S, A> sstar = astar.sstar();
			fsss( sstar, d - 1 );
			astar.backup();
		}
		else {
//			System.out.println( "\tLeaf " + sn.s() + " at depth " + d );
			sn.leaf( actions( sn ) );
		}
		sn.backup();
	}
}
