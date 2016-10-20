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
package edu.oregonstate.eecs.mcplan.domains.increment;

import edu.oregonstate.eecs.mcplan.AnytimePolicy;

/**
 * This policy for the IncrementGame increments the specified counter in
 * every state.
 * 
 * @author jhostetler
 */
public class IncrementFocusPolicy extends AnytimePolicy<IncrementState, IncrementEvent>
{
	public final int player;
	public final int counter;
	
	public IncrementFocusPolicy( final int player, final int counter )
	{
		this.player = player;
		this.counter = counter;
	}
	
	@Override
	public void setState( final IncrementState s, final long t )
	{ }

	@Override
	public IncrementEvent getAction()
	{
		return new IncrementAction( player, counter );
	}

	@Override
	public void actionResult( final IncrementState sprime, final double[] r )
	{ }

	@Override
	public String getName()
	{
		return "IncrementFocusPolicy[" + this.player + ", " + this.counter + "]";
	}

	@Override
	public boolean improvePolicy()
	{
		return false;
	}

	@Override
	public int hashCode()
	{
		return getName().hashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		final IncrementFocusPolicy that = (IncrementFocusPolicy) obj;
		return player == that.player && counter == that.counter;
	}

}
