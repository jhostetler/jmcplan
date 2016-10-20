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
package edu.oregonstate.eecs.mcplan.domains.taxi;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.ActionGenerator;

/**
 * @author jhostetler
 *
 */
public class TaxiActionGenerator extends ActionGenerator<TaxiState, TaxiAction>
{
	public static final ArrayList<TaxiAction> actions = new ArrayList<TaxiAction>();
	
	static {
		actions.add( new MoveAction( 1, 0 ) );
		actions.add( new MoveAction( 0, 1 ) );
		actions.add( new MoveAction( -1, 0 ) );
		actions.add( new MoveAction( 0, -1 ) );
		actions.add( new PickupAction() );
		actions.add( new PutdownAction() );
	}
	
	private int idx_ = 0;
	
	@Override
	public TaxiActionGenerator create()
	{
		return new TaxiActionGenerator();
	}

	@Override
	public void setState( final TaxiState s, final long t )
	{
		idx_ = 0;
	}

	@Override
	public int size()
	{
		return actions.size();
	}

	@Override
	public boolean hasNext()
	{
		return idx_ < size();
	}

	@Override
	public TaxiAction next()
	{
		return actions.get( idx_++ ).create();
	}
}
