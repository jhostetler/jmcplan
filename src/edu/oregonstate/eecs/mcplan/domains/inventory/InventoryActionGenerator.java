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
package edu.oregonstate.eecs.mcplan.domains.inventory;

import java.util.ArrayList;
import java.util.Iterator;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.PreferredNumbers;

/**
 * @author jhostetler
 *
 */
public class InventoryActionGenerator extends ActionGenerator<InventoryState, InventoryAction>
{
	private final ArrayList<InventoryAction> actions = new ArrayList<InventoryAction>();
	private Iterator<InventoryAction> itr = null;
	int next = 0;
	
	@Override
	public ActionGenerator<InventoryState, InventoryAction> create()
	{
		return new InventoryActionGenerator();
	}

	@Override
	public void setState( final InventoryState s, final long t )
	{
		actions.clear();
		next = 0;
		
		actions.add( new InventoryNothingAction() );
		
		final Fn.IntSlice g = PreferredNumbers.Series_1_2_5();
		while( next < s.problem.min_order ) {
			next = g.next();
		}
		do {
			if( next > s.problem.max_order ) {
				break;
			}
			for( int i = 0; i < s.problem.Nproducts; ++i ) {
				actions.add( new InventoryOrderAction( i, next, s.problem.cost[i] ) );
			}
			next = g.next();
		} while( g.hasNext() );
		itr = actions.iterator();
	}

	@Override
	public int size()
	{
		return actions.size();
	}

	@Override
	public boolean hasNext()
	{
		return itr.hasNext();
	}

	@Override
	public InventoryAction next()
	{
		return itr.next();
	}
}
