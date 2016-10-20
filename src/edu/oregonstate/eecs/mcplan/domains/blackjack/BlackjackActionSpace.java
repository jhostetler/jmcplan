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
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.ActionSet;
import edu.oregonstate.eecs.mcplan.ActionSpace;

/**
 * @author jhostetler
 *
 */
public class BlackjackActionSpace extends ActionSpace<BlackjackMdpState, BlackjackAction>
{
	private final ArrayList<BlackjackAction> pass_hit_ = new ArrayList<BlackjackAction>();
	private final ArrayList<BlackjackAction> pass_only_ = new ArrayList<BlackjackAction>();
	
	private BlackjackMdpState s_ = null;
	private final ArrayList<BlackjackAction> actions_ = null;
	
	private final BlackjackParameters params_;
	
	public BlackjackActionSpace( final BlackjackParameters params )
	{
		pass_hit_.add( new HitAction( 0 ) );
		pass_hit_.add( new PassAction( 0 ) );
		
		pass_only_.add( new PassAction( 0 ) );
		
		params_ = params;
	}
	
	@Override
	public ActionSet<BlackjackMdpState, BlackjackAction> getActionSet( final BlackjackMdpState s )
	{
		s_ = s;
		if( s_.dealer_value > params_.max_score || s_.player_value > params_.max_score || s_.player_passed
			|| s_ == BlackjackMdpState.TheAbsorbingState ) {
//			actions_ = pass_only_;
			return ActionSet.wrap( pass_only_ );
		}
		else {
//			actions_ = pass_hit_;
			return ActionSet.wrap( pass_hit_ );
		}
	}
	
	@Override
	public int cardinality()
	{
		return actions_.size();
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
	public int index( final BlackjackAction a )
	{
		throw new UnsupportedOperationException();
	}

//	@Override
//	public Generator<BlackjackAction> generator()
//	{
//		return Generator.fromIterator( actions_.iterator() );
//	}

}
