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

import edu.oregonstate.eecs.mcplan.ActionSet;
import edu.oregonstate.eecs.mcplan.ActionSpace;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * The action space containing TripBus actions for every bus. The legal action
 * set is the same in all non-terminal states, that is we allow tripping a
 * bus that is already tripped.
 */
public final class TripBusActionSpace extends ActionSpace<CosmicState, CosmicAction>
{
	private static final class TripBusActionSet extends ActionSet<CosmicState, CosmicAction>
	{
		private static final class G extends Generator<CosmicAction>
		{
			private int i = 0;
			private final int N;
			
			public G( final int N )
			{ this.N = N; }
			
			@Override
			public boolean hasNext()
			{ return i <= N; }

			@Override
			public CosmicAction next()
			{
				final CosmicAction a;
				if( i == 0 ) {
					a = new CosmicNothingAction();
				}
				else {
					a = new TripBusAction( i - 1 );
				}
				i += 1;
				return a;
			}
		}
		
		private final CosmicParameters params;
		private final int repr;
		
		public TripBusActionSet( final CosmicParameters params, final CosmicState s )
		{
			this.params = params;
			repr = s.isTerminal() ? 0 : 1;
		}
		
		private TripBusActionSet( final TripBusActionSet that )
		{
			this.params = that.params;
			this.repr = that.repr;
		}

		@Override
		public Iterator<CosmicAction> iterator()
		{
			return new G( params.Nbus );
		}

		@Override
		public Representation<CosmicState> copy()
		{
			return new TripBusActionSet( this );
		}

		@Override
		public boolean equals( final Object obj )
		{
			final TripBusActionSet that = (TripBusActionSet) obj;
			return repr == that.repr;
		}

		@Override
		public int hashCode()
		{
			return 3 + repr;
		}

		@Override
		public int size()
		{
			return params.Nbus;
		}
	}

	// -----------------------------------------------------------------------
	
	private final CosmicParameters params;
	
	int i = 0;
	
	public TripBusActionSpace( final CosmicParameters params )
	{
		this.params = params;
	}

	@Override
	public ActionSet<CosmicState, CosmicAction> getActionSet( final CosmicState s )
	{
		return new TripBusActionSet( params, s );
	}

	@Override
	public int cardinality()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isFinite()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCountable()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int index( final CosmicAction a )
	{
		// TODO Auto-generated method stub
		return 0;
	}

}
