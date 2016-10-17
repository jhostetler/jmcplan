/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author jhostetler
 *
 */
public class SeriesAction<S, A extends UndoableAction<S>> extends UndoableAction<S>
{
	private final List<A> actions_;
	private boolean done_ = false;
	
	public SeriesAction( final A... actions )
	{
		actions_ = Arrays.asList( actions );
	}
	
	@Override
	public void doAction( final RandomGenerator rng, final S s )
	{
		assert( !done_ );
		final ListIterator<A> itr = actions_.listIterator();
		while( itr.hasNext() ) {
			itr.next().doAction( s );
		}
		done_ = true;
	}

	@Override
	public boolean isDone()
	{
		return done_;
	}

	@Override
	public void undoAction( final S s )
	{
		assert( done_ );
		final ListIterator<A> itr = actions_.listIterator( actions_.size() );
		while( itr.hasPrevious() ) {
			itr.previous().undoAction( s );
		}
		done_ = false;
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof SeriesAction<?, ?>) ) {
			return false;
		}
		final SeriesAction<?, ?> that = (SeriesAction<?, ?>) obj;
		return actions_.equals( that.actions_ );
	}

	@Override
	public int hashCode()
	{
		return actions_.hashCode();
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "SeriesAction" ).append( actions_ );
		return sb.toString();
	}

}
