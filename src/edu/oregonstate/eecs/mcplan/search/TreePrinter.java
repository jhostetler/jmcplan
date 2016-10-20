/* LICENSE
Copyright (c) 2013-2016, Jesse Hostetler (jessehostetler@gmail.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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
	
	public static <S, A extends VirtualConstructor<A>>
	TreePrinter<S, A> create( final GameTree<S, A> tree, final PrintStream out )
	{
		return new TreePrinter<S, A>( out );
	}
	
	private final PrintStream out_;
	private int d_ = 0;
	private int[] turn_ = null;
	
	private final int depth_limit;
	
	public TreePrinter()
	{
		this( System.out );
	}
	
	public TreePrinter( final PrintStream out )
	{
		this( out, Integer.MAX_VALUE );
	}
	
	public TreePrinter( final int depth_limit )
	{
		this( System.out, depth_limit );
	}
	
	public TreePrinter( final PrintStream out, final int depth_limit )
	{
		this.out_ = out;
		this.depth_limit = depth_limit;
	}

	@Override
	public void visit( final StateNode<S, A> s )
	{
		for( int i = 0; i < d_; ++i ) {
			out_.print( "  " );
		}
		out_.print( "S" );
//		out_.print( s.getClass() );
		out_.print( d_ / 2 );
		out_.print( ": n = " );
		out_.print( s.n() );
		out_.print( ": " );
		out_.print( s );
		out_.print( ": r = " );
		out_.print( Arrays.toString( s.r() ) );
//		out_.print( ": v = " );
//		out_.print( Arrays.toString( s.v() ) ); // FIXME: NP exception in v() due to no children
		out_.println();
		d_ += 1;
		final int[] old_turn = turn_;
		turn_ = s.turn;
		if( d_ < depth_limit ) {
			for( final ActionNode<S, A> a : Fn.in( s.successors() ) ) {
				a.accept( this );
			}
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
		out_.print( ": @" + Integer.toHexString( System.identityHashCode( a ) ) );
		out_.print( ": n = " );
		out_.print( a.n() );
		out_.print( ": " );
		out_.print( a.a() );
		out_.print( ": q = " );
		out_.print( Arrays.toString( a.q() ) );
		out_.print( ", var = " );
		out_.print( Arrays.toString( a.qvar() ) );
		out_.print( ", 95% = [" );
		for( final double sigma : a.qvar() ) {
			out_.print( 2 * Math.sqrt( sigma ) );
		}
		out_.print( "]" );
		out_.println();
		d_ += 1;
		if( d_ < depth_limit ) {
			for( final StateNode<S, A> s : Fn.in( a.successors() ) ) {
				s.accept( this );
			}
		}
		d_ -= 1;
	}
	
}