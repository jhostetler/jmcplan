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

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;


/**
 * Ground state node.
 */
public final class FsssStateNode<S extends State, A extends VirtualConstructor<A>> implements AutoCloseable
{
	private final FsssModel<S, A> model;
	private final S s;
	private int nvisits = 0;
	
	private double U;
	private double L;
	public final double r;
	public final int depth;
	
	//FIXME: Memory debugging
//	public final char[] deadweight = new char[1000];
	
	private final ArrayList<FsssActionNode<S, A>> successors = new ArrayList<FsssActionNode<S, A>>();
	
	public FsssStateNode( final FsssActionNode<S, A> predecessor, final FsssModel<S, A> model, final S s )
	{
//		this.predecessor = predecessor;
		this.model = model;
		this.s = s;
		this.r = model.reward( s );
		this.U = model.Vmax( s );
		this.L = model.Vmin( s );
		this.depth = predecessor.depth - 1;
	}
	
	public FsssStateNode( final int depth, final FsssModel<S, A> model, final S s )
	{
//		this.predecessor = null;
		this.model = model;
		this.s = s;
		this.r = model.reward( s );
		this.U = model.Vmax( s );
		this.L = model.Vmin( s );
		this.depth = depth;
	}
	
//	private static int nfinalized = 0;
//	@Override
//	public void finalize()
//	{
//		System.out.println( "finalize(): " + (nfinalized++) + " FsssStateNode" );
//	}
	
	@Override
	public void close()
	{
		s.close();
		
		for( final FsssActionNode<S, A> an : successors ) {
			an.close();
		}
		successors.clear();
		successors.trimToSize();
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "[@" ).append( Integer.toHexString( hashCode() ) )
		  .append( ": " ).append( s )
		  .append( "; nvisits: " ).append( nvisits )
		  .append( "; r: " ).append( r )
		  .append( "; U: " ).append( U )
		  .append( "; L: " ).append( L )
		  .append( "]" );
		return sb.toString();
	}
	
	public S s()
	{
		return s;
	}
	
	public FactoredRepresentation<S> x()
	{
		return model.base_repr().encode( s );
	}
	
	public int nvisits()
	{
		return nvisits;
	}
	
	public void visit()
	{
		nvisits += 1;
	}
	
	public double U()
	{
		return U;
	}
	
	public double L()
	{
		return L;
	}
	
	public void backup()
	{
		assert( nsuccessors() > 0 );
		double max_u = -Double.MAX_VALUE;
		double max_l = -Double.MAX_VALUE;
		for( final FsssActionNode<S, A> an : successors() ) {
			final double u = an.U();
			if( u > max_u ) {
				max_u = u;
			}
			
			final double l = an.L();
			if( l > max_l ) {
				max_l = l;
			}
		}
		U = r + max_u;
		L = r + max_l;
	}
	
	public FsssActionNode<S, A> astar()
	{
		FsssActionNode<S, A> astar = null;
		double ustar = -Double.MAX_VALUE;
		for( final FsssActionNode<S, A> an : successors() ) {
			final double u = an.U();
			if( u > ustar ) {
				ustar = u;
				astar = an;
			}
		}
		assert( astar != null );
		return astar;
	}
	
	/**
	 * Successors are returned in insertion order.
	 * @return
	 */
	public Iterable<FsssActionNode<S, A>> successors()
	{
		return successors;
	}
	
	public int nsuccessors()
	{
		return successors.size();
	}
	
	public void expand( final Iterable<A> actions, final int width )
	{
		createActionNodes( actions );
		for( final FsssActionNode<S, A> an : successors() ) {
			for( int i = 0; i < width; ++i ) {
				an.sample();
			}
		}
	}
	
	public FsssActionNode<S, A> createActionNode( final A a )
	{
		final FsssActionNode<S, A> an = new FsssActionNode<S, A>( this, model, s, a );
		successors.add( an );
		return an;
	}
	
	public void createActionNodes( final Iterable<A> actions )
	{
		for( final A a : actions ) {
			final FsssActionNode<S, A> an = new FsssActionNode<S, A>( this, model, s, a );
			successors.add( an );
		}
	}
	
	public void sample()
	{
		for( final FsssActionNode<S, A> an : successors() ) {
			an.sample();
		}
	}
	
	public void leaf()
	{
		final double V = r + model.heuristic( s );
		U = L = V;
	}
	
	public boolean isTerminal()
	{
		return depth == 1 || s.isTerminal();
	}
}
