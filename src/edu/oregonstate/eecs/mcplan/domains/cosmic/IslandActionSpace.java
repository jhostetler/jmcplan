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
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import java.util.Iterator;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.oregonstate.eecs.mcplan.ActionSet;
import edu.oregonstate.eecs.mcplan.ActionSpace;
import edu.oregonstate.eecs.mcplan.Representation;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * @author jhostetler
 *
 */
public class IslandActionSpace extends ActionSpace<CosmicState, CosmicAction>
{
	private static final class A extends ActionSet<CosmicState, CosmicAction>
	{
		private class Itr implements Iterator<CosmicAction>
		{
			int i = 0;
			
			@Override
			public boolean hasNext()
			{
				return i < size();
			}

			@Override
			public CosmicAction next()
			{
				final CosmicAction a;
				if( i == 0 ) {
					a = new CosmicNothingAction();
				}
				else {
					while( islands.contains( i ) ) {
						i += 1;
					}
					a = new IslandAction( i, params.zoneCutset( i ).toArray() );
				}
				
				i += 1;
				return a;
			}

			@Override
			public void remove()
			{ throw new UnsupportedOperationException(); }
		}
		
		final CosmicParameters params;
		final TIntSet islands;
		
		public A( final CosmicState s )
		{
			params = s.params;
			islands = new TIntHashSet( s.islands );
			if( islands.size() == params.Nzones - 1 ) {
				// Only one zone not islanded, so actually it is islanded
				// since all branches are disconnected. Note that 0 is not a
				// valid zone, but adding it increases the size of 'islands'
				// so that no Island action will be generated by 'Itr'.
				islands.add( 0 );
			}
		}
		
		private A( final A that )
		{
			params = that.params;
			islands = that.islands;
		}

		@Override
		public Iterator<CosmicAction> iterator()
		{
			return new Itr();
		}

		@Override
		public int size()
		{
			return 1 + (params.Nzones - islands.size());
		}

		@Override
		public Representation<CosmicState> copy()
		{
			return new A( this );
		}

		@Override
		public boolean equals( final Object obj )
		{
			if( !(obj instanceof A) ) {
				return false;
			}
			final A that = (A) obj;
			return islands.equals( that.islands );
		}

		@Override
		public int hashCode()
		{
			final HashCodeBuilder hb = new HashCodeBuilder();
			hb.append( getClass().hashCode() ).append( islands );
			return hb.toHashCode();
		}
		
	}
	
	// -----------------------------------------------------------------------

	@Override
	public ActionSet<CosmicState, CosmicAction> getActionSet( final CosmicState s )
	{
		return new A( s );
	}
	
	@Override
	public String toString()
	{
		return "Island";
	}

	@Override
	public int cardinality()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isFinite()
	{
		return true;
	}

	@Override
	public boolean isCountable()
	{
		return true;
	}

	@Override
	public int index( final CosmicAction a )
	{
		throw new UnsupportedOperationException();
	}

}
