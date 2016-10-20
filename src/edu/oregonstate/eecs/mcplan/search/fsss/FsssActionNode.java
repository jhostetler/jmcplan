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

/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;


/**
 * Ground action node.
 */
public class FsssActionNode<S extends State, A extends VirtualConstructor<A>> implements AutoCloseable
{
//	private final FsssStateNode<S, A> predecessor;
	private final FsssModel<S, A> model;
	private final S s;
	private final A a;
	public final double r;
	public final int depth;
	
	private double U;
	private double L;
	
	private final ArrayList<FsssStateNode<S, A>> successors = new ArrayList<FsssStateNode<S, A>>();
	
	public FsssActionNode( final FsssStateNode<S, A> predecessor, final FsssModel<S, A> model, final S s, final A a )
	{
//		this.predecessor = predecessor;
		this.model = model;
		this.s = s;
		this.a = a;
		this.r = model.reward( s, a );
		this.U = model.Vmax( s, a );
		this.L = model.Vmin( s, a );
		this.depth = predecessor.depth;
	}
	
	@Override
	public void close()
	{
		successors.clear();
		successors.trimToSize();
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "[" ).append( a )
		  .append( "; r: " ).append( r )
		  .append( "; U: " ).append( U )
		  .append( "; L: " ).append( L )
		  .append( "]" );
		return sb.toString();
	}
	
	public A a()
	{
		return a;
	}
	
	public double U()
	{
		return U;
	}
	
	public double L()
	{
		return L;
	}
	
	public FsssStateNode<S, A> sstar()
	{
		FsssStateNode<S, A> sstar = null;
		double bstar = 0;
		for( final FsssStateNode<S, A> s : successors() ) {
			final double b = s.U() - s.L();
			assert( b >= 0 );
			if( b > bstar ) {
				bstar = b;
				sstar = s;
			}
		}
		assert( sstar != null );
		return sstar;
	}
	
	public void backup()
	{
		assert( nsuccessors() > 0 );
		
		double u = 0;
		double l = 0;
		for( final FsssStateNode<S, A> sn : successors() ) {
			u += sn.U();
			l += sn.L();
		}
		U = r + model.discount() * u / nsuccessors();
		L = r + model.discount() * l / nsuccessors();
	}
	
	/**
	 * Successors are returned in insertion order.
	 * @return
	 */
	public Iterable<FsssStateNode<S, A>> successors()
	{
		return successors;
	}
	
	public int nsuccessors()
	{
		return successors.size();
	}
	
	/**
	 * Samples a state transition, adds the result state to 'successors',
	 * and returns the result state.
	 * @return
	 */
	public FsssStateNode<S, A> sample()
	{
//		System.out.println( "Sampling " + a + " in " + s + "; nsuccessors = " + nsuccessors() );
		final S sprime = model.sampleTransition( s, a );
		final FsssStateNode<S, A> snprime = new FsssStateNode<S, A>( this, model, sprime );
		snprime.visit();
		successors.add( snprime );
		return snprime;
	}
	
//	public void leaf()
//	{
//		L = r;
//		U = L;
//	}
}
