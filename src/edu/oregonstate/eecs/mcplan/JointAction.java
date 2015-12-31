/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;

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
	
	public final int nagents;
	
	private final List<A> actions_;
	
	public JointAction( final A... actions )
	{
		this( Arrays.asList( actions ) );
	}
	
	private JointAction( final List<A> actions )
	{
		actions_ = actions;
		nagents = actions_.size();
	}
	
	public A get( final int i )
	{
		return actions_.get( i );
	}

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
		// Note: I removed the instanceof because I'm going to force a cast
		// to JointAction<A> and I'd rather throw an exception than return
		// false.
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
		final StringBuilder sb = new StringBuilder();
		sb.append( "JointAction" );
		sb.append( actions_ ); // List.toString() adds surrounding []
		return sb.toString();
//		return repr_;
	}
}
