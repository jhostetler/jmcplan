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
package edu.oregonstate.eecs.mcplan.util;

/**
 * WARNING: If either type is an array, the hashCode()/equals() implementation
 * will depend on the *identity* of the array, and *not* on Arrays.equals().
 * I am considering how to work around that.
 * 
 * @deprecated
 * @author jhostetler
 *
 */
@Deprecated
public class Tuple
{
	public static final class Tuple2<A, B>
	{
		public static <A, B> Tuple2<A, B> of( final A a, final B b )
		{
			return new Tuple2<A, B>( a, b );
		}
		
		public final A _1;
		public final B _2;
		
		public Tuple2( final A _1, final B _2 )
		{
			this._1 = _1;
			this._2 = _2;
		}
		
		@Override
		public int hashCode()
		{
			return 499 * _1.hashCode() + _2.hashCode();
		}
		
		@Override
		public boolean equals( final Object obj )
		{
			if( obj == null || !(obj instanceof Tuple2) ) {
				return false;
			}
			
			final Tuple2<?, ?> that = (Tuple2<?, ?>) obj;
			return that._1.equals( _1 ) && that._2.equals( _2 );
		}
		
		@Override
		public String toString()
		{
			final StringBuilder sb = new StringBuilder();
			sb.append( "(" ).append( (_1 == null ? "null" :_1.toString()) )
			  .append( ", " ).append( (_2 == null ? "null" : _2.toString()) ).append( ")" );
			return sb.toString();
		}
	}
}
