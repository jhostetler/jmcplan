package edu.oregonstate.eecs.mcplan.abstraction;

import java.util.ArrayList;
import java.util.Iterator;

import edu.oregonstate.eecs.mcplan.Representation;

/**
 * An AggregateState consists of a set of primitive states. It has
 * *reference semantics*, which is different from typical usage for
 * Representation types. We can get away with this because AggregateState
 * instances are only created by the Aggregator class.
 */
public class AggregateState<S> extends Representation<S> implements Iterable<Representation<S>>
{
	private final ArrayList<Representation<S>> xs_
		= new ArrayList<Representation<S>>();
	
//		private final HashCodeBuilder hash_builder_ = new HashCodeBuilder( 139, 149 );
	
	public <R extends Representation<S>> void add( final R x )
	{
		xs_.add( x );
//			hash_builder_.append( x );
	}
	
	@Override
	public AggregateState<S> copy()
	{
//			System.out.println( "AggregateState.copy()" );
		final AggregateState<S> cp = new AggregateState<S>();
		for( final Representation<S> x : xs_ ) {
			cp.add( x );
		}
		return cp;
	}

	@Override
	public boolean equals( final Object obj )
	{
//			if( obj == null || !(obj instanceof AggregateState) ) {
//				return false;
//			}
//			final AggregateState that = (AggregateState) obj;
//			if( xs_.size() != that.xs_.size() ) {
//				return false;
//			}
//			for( int i = 0; i < xs_.size(); ++i ) {
//				if( !xs_.get( i ).equals( that.xs_.get( i ) ) ) {
//					return false;
//				}
//			}
//			return true;
		return this == obj;
	}

	@Override
	public int hashCode()
	{
//			return hash_builder_.toHashCode();
		return System.identityHashCode( this );
	}

	@Override
	public Iterator<Representation<S>> iterator()
	{
		return xs_.iterator();
	}
}