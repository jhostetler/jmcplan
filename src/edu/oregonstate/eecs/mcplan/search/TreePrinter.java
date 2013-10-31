package edu.oregonstate.eecs.mcplan.search;

import java.io.PrintStream;
import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Fn;

public class TreePrinter<S, A extends VirtualConstructor<A>>
	implements GameTreeVisitor<S, A>
{
	public static <S, A extends VirtualConstructor<A>>
	TreePrinter<S, A> create( final GameTree<S, A> tree )
	{
		return new TreePrinter<S, A>();
	}
	
	private final PrintStream out_;
	private int d_ = 0;
	private int[] turn_ = null;
	
	public TreePrinter()
	{
		this( System.out );
	}
	
	public TreePrinter( final PrintStream out )
	{
		out_ = out;
	}
	
	@Override
	public void visit( final StateNode<S, A> s )
	{
		for( int i = 0; i < d_; ++i ) {
			out_.print( "  " );
		}
		out_.print( "S" );
		out_.print( d_ / 2 );
		out_.print( ": n = " );
		out_.print( s.n() );
		out_.print( ": " );
		out_.print( s.token );
		out_.print( ": v = " );
		out_.print( Arrays.toString( s.v() ) );
		out_.println();
		d_ += 1;
		final int[] old_turn = turn_;
		turn_ = s.turn;
		for( final ActionNode<S, A> a : Fn.in( s.successors() ) ) {
			a.accept( this );
		}
		d_ -= 1;
		turn_ = old_turn;
	}

	@Override
	public void visit( final ActionNode<S, A> a )
	{
		for( int i = 0; i < d_; ++i ) {
			out_.print( "  " );
		}
		out_.print( "A" );
		out_.print( d_ / 2 );
		out_.print( ": n = " );
		out_.print( a.n() );
		out_.print( ": " );
		out_.print( a.a() );
		out_.print( ": q = " );
		out_.print( a.q() );
		out_.print( ", var = " );
		out_.print( a.qvar() );
		out_.print( ", 95% = [" );
		for( final double sigma : a.qvar() ) {
			out_.print( 2 * Math.sqrt( sigma ) );
		}
		out_.print( "]" );
		out_.println();
		d_ += 1;
		for( final StateNode<S, A> s : Fn.in( a.successors() ) ) {
			s.accept( this );
		}
		d_ -= 1;
	}
	
}