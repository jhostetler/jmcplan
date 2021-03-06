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

import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
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
public class ShedZoneActionSpace extends ActionSpace<CosmicState, CosmicAction>
{
	private static final class A extends ActionSet<CosmicState, CosmicAction>
	{
		private class Itr implements Iterator<CosmicAction>
		{
			int i = 0;
			int zone = 1; // 'zone' is a Matlab index so it starts at 1
			int amt = 0;
			
			@Override
			public boolean hasNext()
			{
				return i < size();
			}

			@Override
			public CosmicAction next()
			{
				assert( hasNext() );
				final CosmicAction a;
				if( i == 0 ) {
					a = new CosmicNothingAction();
				}
				else {
					while( !live_zones.contains( zone ) ) {
						zone += 1;
						if( zone > params.Nzones ) {
							System.out.println( "! Zones: " + live_zones );
							throw new IllegalStateException( "No next element" );
						}
					}
					
					a = new ShedZoneAction( zone, amounts[amt++] );
					
					if( amt == amounts.length ) {
						amt = 0;
						zone += 1;
					}
				}
				
				i += 1;
				return a;
			}

			@Override
			public void remove()
			{ throw new UnsupportedOperationException(); }
		}
		
		final CosmicParameters params;
		final TIntSet live_zones;
		final double[] amounts;
		
		public A( final CosmicState s, final double[] amounts )
		{
			params = s.params;
			this.amounts = amounts;
			
			live_zones = new TIntHashSet();
			for( final Shunt sh : s.shunts() ) {
				// Exclude shunts that are already tripped, and shunts that
				// generate negative power.
				if( sh.factor() > 0 && sh.P() > 0 ) {
					final Bus bus = s.bus( sh.bus() );
					live_zones.add( bus.zone() );
					if( live_zones.size() == params.Nzones ) {
						// All zones are alive
						break;
					}
				}
			}
		}
		
		private A( final A that )
		{
			params = that.params;
			live_zones = that.live_zones;
			amounts = that.amounts;
		}

		@Override
		public Iterator<CosmicAction> iterator()
		{
			return new Itr();
		}

		@Override
		public int size()
		{
			return 1 + (live_zones.size() * amounts.length);
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
			return live_zones.equals( that.live_zones ) && Arrays.equals( amounts, that.amounts );
		}

		@Override
		public int hashCode()
		{
			final HashCodeBuilder hb = new HashCodeBuilder();
			hb.append( getClass().hashCode() ).append( live_zones ).append( amounts );
			return hb.toHashCode();
		}
		
	}
	
	// -----------------------------------------------------------------------

	private final double[] amounts;
	
	public ShedZoneActionSpace( final double[] amounts )
	{
		this.amounts = amounts;
	}
	
	@Override
	public ActionSet<CosmicState, CosmicAction> getActionSet( final CosmicState s )
	{
		return new A( s,  amounts );
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "ShedZone[" );
		sb.append( StringUtils.join( amounts, ';'  ) );
		sb.append( "]" );
		return sb.toString();
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
