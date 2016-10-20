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

import java.util.ArrayList;
import java.util.Iterator;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class SmartActionGenerator extends ActionGenerator<YahtzeeState, YahtzeeAction>
{
	private YahtzeeState s_ = null;
	private long t_ = 0L;
	
	private final ArrayList<YahtzeeAction> actions_
		= new ArrayList<YahtzeeAction>();
	private Iterator<YahtzeeAction> itr_ = null;
	
	@Override
	public SmartActionGenerator create()
	{
		return new SmartActionGenerator();
	}

	@Override
	public void setState( final YahtzeeState s, final long t )
	{
		s_ = s;
		t_ = t;
		
		actions_.clear();
		
		if( s.rerolls > 0 ) {
//			for( int i = 1; i <= Hand.Nfaces; ++i ) {
//				actions_.add( new JointAction<YahtzeeAction>( new KeepAllAction( i ) ) );
//			}
			
			actions_.add( new KeepMostAction() );
			actions_.add( new KeepStraightAction() );
			// Reroll all action
			actions_.add( new KeepAction( Fn.repeat( 0, Hand.Nfaces ) ) );
		}
		
		// Add ScoreAction for each open category
//		for( final YahtzeeScores category : YahtzeeScores.values() ) {
//			if( !s.filled[category.ordinal()] ) {
//				actions_.add( new JointAction<YahtzeeAction>( new ScoreAction( category ) ) );
//			}
//		}
//		actions_.add( new ScoreHighestAction() );
		
		for( final YahtzeeScores category : YahtzeeScores.values() ) {
			if( !s.filled[category.ordinal()] && category.isUpper() ) {
				actions_.add( new ScoreMaxUpperAction() );
				break;
			}
		}
		
		for( final YahtzeeScores category : YahtzeeScores.values() ) {
			if( !s.filled[category.ordinal()] && !category.isUpper() ) {
				actions_.add( new ScoreMaxLowerAction() );
				break;
			}
		}
		
		actions_.add( new ScoreMinMaxAction() );
		
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
	public YahtzeeAction next()
	{
		return itr_.next();
	}
}
