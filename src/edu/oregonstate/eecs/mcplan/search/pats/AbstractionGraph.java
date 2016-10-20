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
import java.util.LinkedHashMap;

import com.google.common.collect.Iterables;

import edu.oregonstate.eecs.mcplan.Representation;

/**
 * @author jhostetler
 *
 */
public class AbstractionGraph<S, A>
{
	public interface Listener<S, A>
	{
		public abstract void updateAbstraction( final StateAbstraction<S> X, final ArrayList<Representation<S>> changed );
		public abstract void updateAbstraction( final ActionSet<A> Y );
	}
	
	public static class SNode<S, A>
	{
		public final StateAbstraction<S> abstraction;
		private final LinkedHashMap<Representation<S>, ANode<S, A>> successors = new LinkedHashMap<>();
		
		private final ArrayList<Listener<S, A>> listeners = new ArrayList<>();
		
		public SNode( final StateAbstraction<S> abstraction )
		{
			this.abstraction = abstraction;
		}
		
		public ANode<S, A> successor( final S s )
		{
			final Representation<S> x = abstraction.encode( s );
			return successors.get( x );
		}
		
		public Iterable<ANode<S, A>> successors()
		{
			return Iterables.unmodifiableIterable( successors.values() );
		}
	}
	
	public static class ANode<S, A>
	{
		public final ActionSet<A> abstraction;
		private final LinkedHashMap<A, SNode<S, A>> successors = new LinkedHashMap<>();
		
		private final ArrayList<Listener<S, A>> listeners = new ArrayList<>();
		
		public ANode( final ActionSet<A> abstraction )
		{
			this.abstraction = abstraction;
		}
		
		public SNode<S, A> successor( final A a )
		{
			return successors.get( a );
		}
		
		public Iterable<SNode<S, A>> successors()
		{
			return Iterables.unmodifiableIterable( successors.values() );
		}
	}
	
	public final ArrayList<SNode<S, A>> snodes = new ArrayList<SNode<S, A>>();
	public final ArrayList<ANode<S, A>> anodes = new ArrayList<ANode<S, A>>();
	
	private SNode<S, A> current_s = null;
	private ANode<S, A> current_a = null;
	
	public ANode<S, A> transitionS( final S s )
	{
		assert( current_s != null );
		assert( current_a == null );
		final ANode<S, A> an = current_s.successor( s );
		current_s = null;
		current_a = an;
		return an;
	}
	
	public SNode<S, A> transitionA( final A a )
	{
		assert( current_s == null );
		assert( current_a != null );
		final SNode<S, A> sn = current_a.successor( a );
		current_s = sn;
		current_a = null;
		return sn;
	}
	
}
