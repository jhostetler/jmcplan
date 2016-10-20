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

import java.util.ArrayList;
import java.util.Iterator;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import gnu.trove.iterator.TIntIterator;

/**
 * @author jhostetler
 *
 */
public class FuelWorldActionGenerator extends ActionGenerator<FuelWorldState, FuelWorldAction>
{
	// FIXME: This is public to make it easier to implement FuelWorldActionSpace.
	// Long run fix is to combine the best of both interfaces.
	public final ArrayList<FuelWorldAction> actions_ = new ArrayList<FuelWorldAction>();
	private Iterator<FuelWorldAction> itr_ = null;
	
	@Override
	public FuelWorldActionGenerator create()
	{
		return new FuelWorldActionGenerator();
	}

	@Override
	public void setState( final FuelWorldState s, final long t )
	{
		actions_.clear();
		
		final TIntIterator itr = s.adjacency.get( s.location ).iterator();
		while( itr.hasNext() ) {
			final int dest = itr.next();
			actions_.add( new MoveAction( s.location, dest ) );
		}
		
		if( s.fuel_depots.contains( s.location ) ) {
			actions_.add( new RefuelAction() );
		}
		
		itr_ = actions_.iterator();
	}

	@Override
	public int size()
	{
		return actions_.size();
	}

	@Override
	public boolean hasNext()
	{
		return itr_.hasNext();
	}

	@Override
	public FuelWorldAction next()
	{
		return itr_.next();
	}
}
