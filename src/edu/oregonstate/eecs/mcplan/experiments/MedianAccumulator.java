/**
 * 
 */
package edu.oregonstate.eecs.mcplan.experiments;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * @author jhostetler
 *
 */
public class MedianAccumulator
{
	private static class Multiple implements Comparable<Multiple>
	{
		public double value;
		public int multiplicity = 1;
		
		public Multiple( final double value )
		{
			this.value = value;
		}

		@Override
		public int compareTo( final Multiple that )
		{
			return Double.compare( value, that.value );
		}
	}
	
	private final NavigableSet<Multiple> elements_ = new TreeSet<Multiple>();
	private int count_ = 0;
	
	public void add( final double x )
	{
		final Multiple m = elements_.floor( new Multiple( x ) );
		if( m == null || m.value != x ) {
			elements_.add( new Multiple( x ) );
		}
		else {
			m.multiplicity += 1;
		}
		count_ += 1;
	}
	
	public double min()
	{
		return elements_.first().value;
	}
	
	public double max()
	{
		return elements_.last().value;
	}
	
	public double median()
	{
		int mid = (count_ - 1) / 2; // Break ties by choosing smaller element.
		int i = 0;
		double x = 0.0;
		final Iterator<Multiple> itr = elements_.iterator();
		while( mid-- > 0 ) {
			if( i == 0 ) {
				final Multiple m = itr.next();
				i = m.multiplicity;
				x = m.value;
			}
			i -= 1;
		}
		if( i == 0 ) {
			return itr.next().value;
		}
		else {
			return x;
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] args )
	{
		final MedianAccumulator acc = new MedianAccumulator();
		
		acc.add( 5 );
		System.out.println( acc.median() ); // 5
		
		acc.add( 5 );
		System.out.println( acc.median() ); // 5
		
		acc.add( 2 );
		System.out.println( acc.median() ); // 5
		
		acc.add( 4 );
		System.out.println( acc.median() ); // 4
		
		acc.add( 7 );
		System.out.println( acc.median() ); // 5
		
		acc.add( 7 );
		System.out.println( acc.median() ); // 5
	}
}
