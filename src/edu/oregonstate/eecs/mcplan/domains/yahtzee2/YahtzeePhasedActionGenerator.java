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
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * Generates KeepAction if rerolls left > 0, otherwise generates ScoreAction.
 */
public class YahtzeePhasedActionGenerator extends ActionGenerator<YahtzeeState, YahtzeeAction>
{
	private boolean rerolls = false;
	private Hand h = null;
	private Fn.MultisetPowerSetGenerator power_set = null;
	private int cat_idx = 0;
	private boolean[] filled = null;
	private int Nunfilled = 0;
	
	@Override
	public ActionGenerator<YahtzeeState, YahtzeeAction> create()
	{
		return new YahtzeePhasedActionGenerator();
	}

	@Override
	public void setState( final YahtzeeState s, final long t )
	{
		rerolls = s.rerolls > 0;
		h = s.hand();
		if( rerolls ) {
			power_set = new Fn.MultisetPowerSetGenerator( Arrays.copyOf( h.dice, h.dice.length ) );
		}
		cat_idx = 0;
		filled = Arrays.copyOf( s.filled, s.filled.length );
		Nunfilled = filled.length - Fn.sum( filled );
	}

	@Override
	public int size()
	{
		if( rerolls ) {
			return Fn.multisetPowerSetCardinality( h.dice );
		}
		else {
			return Nunfilled;
		}
	}

	@Override
	public boolean hasNext()
	{
		if( rerolls ) {
			return power_set.hasNext();
		}
		else {
			return (cat_idx < YahtzeeScores.values().length && Nunfilled > 0);
		}
	}

	@Override
	public YahtzeeAction next()
	{
		if( rerolls ) {
			return new KeepAction( power_set.next() );
		}
		else {
			while( cat_idx < YahtzeeScores.values().length ) {
				final YahtzeeScores category = YahtzeeScores.values()[cat_idx++];
				if( !filled[category.ordinal()] ) {
					filled[category.ordinal()] = true;
					Nunfilled -= 1;
					return new ScoreAction( category );
				}
			}
		}
		
		throw new IllegalStateException( "hasNext() was false" );
	}

}
