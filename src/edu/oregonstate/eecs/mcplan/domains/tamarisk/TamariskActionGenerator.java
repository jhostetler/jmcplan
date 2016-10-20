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
package edu.oregonstate.eecs.mcplan.domains.tamarisk;

import edu.oregonstate.eecs.mcplan.ActionGenerator;

/**
 * @author jhostetler
 *
 */
public class TamariskActionGenerator extends ActionGenerator<TamariskState, TamariskAction>
{
	int category_ = 0;
	int reach_ = 0;
	int Nreaches_ = 0;
	
	public final boolean enable_eradicate_restore_actions = false;
	public final int Ncategories = (enable_eradicate_restore_actions ? 3 : 2);
	
	@Override
	public ActionGenerator<TamariskState, TamariskAction> create()
	{
		return new TamariskActionGenerator();
	}

	@Override
	public void setState( final TamariskState s, final long t )
	{
		category_ = 0;
		reach_ = 0;
		Nreaches_ = s.params.Nreaches;
	}

	@Override
	public int size()
	{
		// Categories, plus the Nothing action.
		return Ncategories*Nreaches_ + 1;
	}

	@Override
	public boolean hasNext()
	{
		return category_ <= Ncategories && reach_ < Nreaches_;
	}

	@Override
	public TamariskAction next()
	{
		if( category_ == 0 ) {
			category_ += 1;
			return new NothingAction();
		}
		
		final int r = reach_;
		final int c = category_;
		reach_ += 1;
		if( reach_ == Nreaches_ ) {
			reach_ = 0;
			category_ += 1;
		}
		
		switch( c ) {
		case 1:
			return new EradicateAction( r );
		case 2:
			return new RestoreAction( r );
//		case 3:
//			return new EradicateRestoreAction( r );
		default:
			throw new IllegalStateException( "hasNext() == false" );
		}
	}
}
