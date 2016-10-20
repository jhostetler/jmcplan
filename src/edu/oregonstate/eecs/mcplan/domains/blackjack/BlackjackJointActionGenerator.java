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
import java.util.Iterator;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.JointAction;

/**
 * @author jhostetler
 *
 */
public class BlackjackJointActionGenerator extends ActionGenerator<BlackjackState, JointAction<BlackjackAction>>
{
	private final ArrayList<JointAction<BlackjackAction>> actions_
		= new ArrayList<JointAction<BlackjackAction>>();
	private Iterator<JointAction<BlackjackAction>> itr_ = null;
	
	private final int nagents_;
	
	public BlackjackJointActionGenerator( final int nagents )
	{
		nagents_ = nagents;
	}
	
	@Override
	public ActionGenerator<BlackjackState, JointAction<BlackjackAction>> create()
	{
		return new BlackjackJointActionGenerator( nagents_ );
	}

	@Override
	public void setState( final BlackjackState s, final long t, final int[] turn )
	{
		actions_.clear();
		JointAction.Builder<BlackjackAction> j = new JointAction.Builder<BlackjackAction>( nagents_ );
		for( final int p : turn ) {
			j.a( p, new HitAction( p ) );
		}
		actions_.add( j.finish() );
		j = new JointAction.Builder<BlackjackAction>( nagents_ );
		for( final int p : turn ) {
			j.a( p, new PassAction( p ) );
		}
		actions_.add( j.finish() );
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
	public JointAction<BlackjackAction> next()
	{
		return itr_.next();
	}

}
