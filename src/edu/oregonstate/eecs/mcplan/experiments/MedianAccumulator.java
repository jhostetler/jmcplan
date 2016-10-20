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
