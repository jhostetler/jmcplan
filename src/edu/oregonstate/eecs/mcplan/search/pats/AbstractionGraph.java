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
