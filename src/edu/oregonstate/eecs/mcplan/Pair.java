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
package edu.oregonstate.eecs.mcplan;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author jhostetler
 *
 */
public final class Pair<T, U>
{
	public static class PartialComparator<T extends Comparable<T>, U>
		implements java.util.Comparator<Pair<T, U>>
	{
		@Override
		public int compare( final Pair<T, U> a, final Pair<T, U> b )
		{
			return a.first.compareTo( b.first );
		}
	}
	
	public static class Comparator<T extends Comparable<T>, U extends Comparable<U>>
		implements java.util.Comparator<Pair<T, U>>
	{
		@Override
		public int compare( final Pair<T, U> a, final Pair<T, U> b )
		{
			final int ac = a.first.compareTo( b.first );
			if( ac == 0 ) {
				return a.second.compareTo( b.second );
			}
			else {
				return ac;
			}
		}
	}
	
	public final T first;
	public final U second;
	
	public Pair( final T t, final U u )
	{
		first = t;
		second = u;
	}
	
	public static <T, U> Pair<T, U> makePair( final T t, final U u )
	{
		return new Pair<T, U>( t, u );
	}
	
	@Override
	public int hashCode()
	{
		final HashCodeBuilder hb = new HashCodeBuilder();
		hb.append( first );
		hb.append( second );
		return hb.toHashCode();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof Pair<?, ?>) ) {
			return false;
		}
		
		final Pair<T, U> that = (Pair<T, U>) obj;
		return ((first == null && that.first == null) || first.equals( that.first ))
				&& ((second == null && that.second == null) || second.equals( that.second ));
	}
	
	@Override
	public String toString()
	{
		return "{" + first + ", " + second + "}";
	}
}
