/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.io.PrintStream;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.domains.cards.InfiniteSpanishDeck;
import edu.oregonstate.eecs.mcplan.domains.spbj.SpBjAction;
import edu.oregonstate.eecs.mcplan.domains.spbj.SpBjFsssModel;
import edu.oregonstate.eecs.mcplan.domains.spbj.SpBjState;

/**
 * @author jhostetler
 *
 */
public class FsssTest
{
	public static void printTree( final FsssStateNode<?, ?> sn, final PrintStream out, final int ws )
	{
		for( int i = 0; i < ws; ++i ) {
			out.print( "-+" );
		}
		out.println( sn );
		for( final FsssActionNode<?, ?> an : sn.successors() ) {
			printTree( an, out, ws + 1 );
		}
	}
	
	public static void printTree( final FsssActionNode<?, ?> an, final PrintStream out, final int ws )
	{
		for( int i = 0; i < ws; ++i ) {
			out.print( "-+" );
		}
		out.println( an );
		for( final FsssStateNode<?, ?> sn : an.successors() ) {
			printTree( sn, out, ws + 1 );
		}
	}
	
	public static void printTree( final FsssAbstractStateNode<?, ?> sn, final PrintStream out, final int ws )
	{
		for( int i = 0; i < ws; ++i ) {
			out.print( "-+" );
		}
		out.println( sn );
		for( final FsssAbstractActionNode<?, ?> an : sn.successors() ) {
			printTree( an, out, ws + 1 );
		}
	}
	
	public static void printTree( final FsssAbstractActionNode<?, ?> an, final PrintStream out, final int ws )
	{
		for( int i = 0; i < ws; ++i ) {
			out.print( "-+" );
		}
		out.println( an );
		for( final FsssAbstractStateNode<?, ?> sn : an.successors() ) {
			printTree( sn, out, ws + 1 );
		}
	}
	
	/**
	 * @param args
	 */
	public static void main( final String[] args )
	{
		final int width = 10;
		final int depth = 10;
		final SpBjFsssModel model = new SpBjFsssModel();
		final RandomGenerator rng = new MersenneTwister( 43 );
		final InfiniteSpanishDeck deck = new InfiniteSpanishDeck( rng );
		final SpBjState s0 = new SpBjState( deck );
		s0.init();
		final FsssTreeBuilder<SpBjState, SpBjAction> tb
			= new FsssTreeBuilder<SpBjState, SpBjAction>( model, width, depth );
		tb.buildTree( s0 );
	}
}
