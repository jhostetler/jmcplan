/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.oregonstate.eecs.mcplan.search.ActionNode;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public final class JointAction<A extends VirtualConstructor<A>>
	implements VirtualConstructor<JointAction<A>>, Iterable<A>
{
	public static final class Builder<A extends VirtualConstructor<A>>
	{
		private final ArrayList<A> actions_;
		
		public Builder( final int nagents )
		{
			actions_ = new ArrayList<A>( nagents );
			for( int i = 0; i < nagents; ++i ) {
				actions_.add( null );
			}
		}
		
		public Builder( final int nagents, final A nothing_action )
		{
			actions_ = new ArrayList<A>( nagents );
			for( int i = 0; i < nagents; ++i ) {
				actions_.add( nothing_action.create() );
			}
		}
		
		public Builder<A> a( final int turn, final A a )
		{
			actions_.set( turn, a );
			return this;
		}
		
		public JointAction<A> finish()
		{
			for( final A a : actions_ ) {
				assert( a != null );
			}
			return new JointAction<A>( actions_ );
		}
	}
	
	/**
	 * "Conditions" a JointAction collection on the value of the *last action*
	 * (ie. the action with the largest index). The resulting
	 * @param a
	 * @param as
	 * @return
	 */
	public static final <A extends VirtualConstructor<A>>
	Generator<JointAction<A>> condition( final A a, final Generator<JointAction<A>> as )
	{
		return Fn.map( new Fn.Function1<JointAction<A>, JointAction<A>>() {
			@Override
			public JointAction<A> apply( final JointAction<A> a )
			{
				final JointAction.Builder<A> b = new JointAction.Builder<A>( a.nagents - 1 );
				for( int i = 0; i < a.nagents - 1; ++i ) {
					b.a( i, a.get( i ).create() );
				}
				return b.finish();
			}
		}
		, filterLast( a, as ) );
	}
	
	public static final <S, A extends VirtualConstructor<A>>
	Generator<JointAction<A>> filter( final A a, final int i, final Generator<JointAction<A>> as )
	{
		return Fn.filter( new Fn.Predicate<JointAction<A>>() {
			@Override
			public boolean apply( final JointAction<A> t )
			{ return t.get( i ).equals( a ); }
		}
		, as );
	}
	
	public static final <S, A extends VirtualConstructor<A>>
	Generator<JointAction<A>> filterLast( final A a, final Generator<JointAction<A>> as )
	{
		return Fn.filter( new Fn.Predicate<JointAction<A>>() {
			@Override
			public boolean apply( final JointAction<A> t )
			{ return t.get( t.nagents - 1 ).equals( a ); }
		}
		, as );
	}
	
//	public static final <S, A extends VirtualConstructor<A>>
//	Generator<Generator<JointAction<A>>> partition( final int i, final Generator<JointAction<A>> as )
//	{
//		final HashMap<A, List<JointAction<A>>> parts = new HashMap<A, List<JointAction<A>>>();
//		for( final JointAction<A> j : Fn.in( as ) ) {
//			List<JointAction<A>> p = parts.get( j.get( i ) );
//			if( p == null ) {
//				p = new ArrayList<JointAction<A>>();
//			}
//			p.add( j );
//		}
//		final Iterator<List<JointAction<A>>> pitr = parts.values().iterator();
//		return new Generator<Generator<JointAction<A>>>() {
//			@Override
//			public boolean hasNext()
//			{ return pitr.hasNext(); }
//
//			@Override
//			public Generator<JointAction<A>> next()
//			{ return Generator.fromIterator( pitr.next().iterator() ); }
//		};
//	}
	
	// TODO: If we're not going to use the JointAction class, this probably
	// belongs somewhere else?
	public static final <S, A extends VirtualConstructor<A>>
	Generator<Generator<ActionNode<S, A>>> partition(
		final int i, final Generator<? extends ActionNode<S, A>> as )
	{
		final HashMap<A, List<ActionNode<S, A>>> parts
			= new HashMap<A, List<ActionNode<S, A>>>();
		for( final ActionNode<S, A> j : Fn.in( as ) ) {
			final A a = j.a( i );
			List<ActionNode<S, A>> p = parts.get( a );
			if( p == null ) {
				p = new ArrayList<ActionNode<S, A>>();
				parts.put( a, p );
			}
			p.add( j );
		}
		final Iterator<List<ActionNode<S, A>>> pitr = parts.values().iterator();
		return new Generator<Generator<ActionNode<S, A>>>() {
			@Override
			public boolean hasNext()
			{ return pitr.hasNext(); }

			@Override
			public Generator<ActionNode<S, A>> next()
			{ return Generator.fromIterator( pitr.next().iterator() ); }
		};
	}
	
	public final int nagents;
	
	private final List<A> actions_;
	private final String repr_;
	
	public JointAction( final A... actions )
	{
		this( Arrays.asList( actions ) );
	}
	
	private JointAction( final List<A> actions )
	{
		actions_ = actions;
		nagents = actions_.size();
		final StringBuilder sb = new StringBuilder();
		sb.append( "JointAction" );
		sb.append( actions ); // List.toString() adds surrounding []
		repr_ = sb.toString();
	}
	
	public A get( final int i )
	{
		return actions_.get( i );
	}

//	@Override
//	public void doAction( final S s )
//	{
//		for( final UndoableAction<S> a : actions_ ) {
//			a.doAction( s );
//		}
//		done_ = true;
//	}
//
//	@Override
//	public boolean isDone()
//	{
//		return done_;
//	}
//
//	@Override
//	public void undoAction( final S s )
//	{
//		for( final UndoableAction<S> a : Fn.reverse( actions_ ) ) {
//			a.undoAction( s );
//		}
//		done_ = false;
//	}

	@Override
	public JointAction<A> create()
	{
		final List<A> copy = Fn.takeAll( Fn.map(
			new Fn.Function1<A, A>() {
				@Override public A apply( final A a ) { return a.create(); }
			}, actions_ ) );
		return new JointAction<A>( copy );
	}

	@Override
	public Iterator<A> iterator()
	{
		return actions_.iterator();
	}
	
	// @Override
	public int size()
	{
		return actions_.size();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
//		System.out.println( "JointAction.equals()" );
		if( obj == null ) { // || !(obj instanceof JointAction<?>) ) {
			return false;
		}
		@SuppressWarnings( "unchecked" )
		final JointAction<A> that = (JointAction<A>) obj;
		if( size() != that.size() ) {
			return false;
		}
		for( int i = 0; i < size(); ++i ) {
			if( !get( i ).equals( that.get( i ) ) ) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int hashCode()
	{
		final HashCodeBuilder hb = new HashCodeBuilder( 1103, 1109 );
		for( final A a : this ) {
			hb.append( a );
		}
		return hb.toHashCode();
	}
	
	@Override
	public String toString()
	{
		return repr_;
	}
}
