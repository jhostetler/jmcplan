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
package edu.oregonstate.eecs.mcplan.domains.fuelworld;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.ActionSet;
import edu.oregonstate.eecs.mcplan.ActionSpace;
import edu.oregonstate.eecs.mcplan.util.Fn;
import gnu.trove.list.TIntList;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * @author jhostetler
 *
 */
public class FuelWorldActionSpace extends ActionSpace<FuelWorldState, FuelWorldAction>
{
	private final FuelWorldActionGenerator action_gen_ = new FuelWorldActionGenerator();
	
	public final FuelWorldState s0;
	
	private final TObjectIntMap<FuelWorldAction> index_ = new TObjectIntHashMap<FuelWorldAction>();
	
	public FuelWorldActionSpace( final FuelWorldState s0 )
	{
		this.s0 = s0;
		
		int c = 0;
		for( int i = 0; i < s0.adjacency.size(); ++i ) {
			final TIntList succ = s0.adjacency.get( i );
			for( int j = 0; j < succ.size(); ++j ) {
				index_.put( new MoveAction( i, succ.get( j ) ), c++ );
			}
		}
		index_.put( new RefuelAction(), c++ );
	}

	@Override
	public int cardinality()
	{
		return index_.size();
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
	public int index( final FuelWorldAction a )
	{
		return index_.get( a );
	}

	@Override
	public ActionSet<FuelWorldState, FuelWorldAction> getActionSet(
			final FuelWorldState s )
	{
		final ActionGenerator<FuelWorldState, FuelWorldAction> g = action_gen_.create();
		g.setState( s, 0L );
		return ActionSet.constant( Fn.in(g) );
	}
}
