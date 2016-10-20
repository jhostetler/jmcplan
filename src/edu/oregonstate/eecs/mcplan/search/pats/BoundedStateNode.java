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
package edu.oregonstate.eecs.mcplan.search.pats;

import java.util.ArrayList;

import com.google.common.collect.Iterables;

import edu.oregonstate.eecs.mcplan.State;

/**
 * @author jhostetler
 *
 */
public class BoundedStateNode<S extends State, A> implements AutoCloseable
{
	public final S s;
	public final double r;
	private double U;
	private double L;
	public final int depth;
	
	private final ArrayList<BoundedActionNode<S, A>> successors = new ArrayList<>();
	
	//FIXME: Memory debugging
//	public final char[] deadweight = new char[1000];
	
	public BoundedStateNode( final BoundedActionNode<S, A> predecessor,
							 final S s, final double r, final BoundedValueModel<S, A> bounds )
	{
		this.s = s;
		this.r = r;
		this.U = bounds.U( s );
		this.L = bounds.L( s );
		this.depth = predecessor.depth - 1;
	}
	
	public boolean isLeaf()
	{
		return depth == 0 || s.isTerminal();
	}
	
	public Iterable<BoundedActionNode<S, A>> successors()
	{
		return Iterables.unmodifiableIterable( successors );
	}
	
	public int Nsuccessors()
	{
		return successors.size();
	}
	
	@Override
	public void close()
	{
		s.close();
		
		for( final BoundedActionNode<S, A> an : successors ) {
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
		assert( Nsuccessors() > 0 );
		double max_u = -Double.MAX_VALUE;
		double max_l = -Double.MAX_VALUE;
		for( final BoundedActionNode<S, A> an : successors() ) {
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
	
	/**
	 * Returns all actions that achieve the maximum value of U(s, a).
	 * @return
	 */
	public ArrayList<BoundedActionNode<S, A>> greatestUpperBound()
	{
		final ArrayList<BoundedActionNode<S, A>> best = new ArrayList<>();
		double Ustar = -Double.MAX_VALUE;
		for( final BoundedActionNode<S, A> an : successors() ) {
			final double U = an.U();
			if( U > Ustar ) {
				Ustar = U;
				best.clear();
				best.add( an );
			}
			else if( U >= Ustar ) {
				best.add( an );
			}
		}
		return best;
	}
	
	/**
	 * Returns all actions that achieve the maximum value of L(s, a).
	 * @return
	 */
	public ArrayList<BoundedActionNode<S, A>> greatestLowerBound()
	{
		final ArrayList<BoundedActionNode<S, A>> best = new ArrayList<>();
		double Lstar = -Double.MAX_VALUE;
		for( final BoundedActionNode<S, A> an : successors() ) {
			final double L = an.L();
			if( L > Lstar ) {
				Lstar = L;
				best.clear();
				best.add( an );
			}
			else if( L >= Lstar ) {
				best.add( an );
			}
		}
		return best;
	}
}
