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

package edu.oregonstate.eecs.mcplan.domains.yahtzee2.subtask;

import java.util.Map;

import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.Hand;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.YahtzeeAction;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.YahtzeeState;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class FocusLargeStraightPolicy extends Policy<YahtzeeState, YahtzeeAction>
{
	private final Map<YahtzeeDiceState, YahtzeeAction> actions;
	private YahtzeeDiceState s = null;
	
	public FocusLargeStraightPolicy( final Map<YahtzeeDiceState, YahtzeeAction> actions )
	{
		this.actions = actions;
	}
	
	@Override
	public void setState( final YahtzeeState s, final long t )
	{
		assert( s.rerolls > 0 );
		this.s = new YahtzeeDiceState( new Hand( Fn.copy( s.hand().dice ) ), s.rerolls );
	}

	@Override
	public YahtzeeAction getAction()
	{
		return actions.get( s );
	}

	@Override
	public void actionResult( final YahtzeeState sprime, final double[] r )
	{ }

	@Override
	public String getName()
	{
		return "FocusLargeStraightPolicy";
	}

	@Override
	public int hashCode()
	{
		return FocusSmallStraightPolicy.class.hashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		return obj.getClass().equals( FocusSmallStraightPolicy.class );
	}
}
