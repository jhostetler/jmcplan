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

import edu.oregonstate.eecs.mcplan.Representation;

/**
 * @author jhostetler
 *
 */
public class AStarAbstraction extends Representation<BlackjackState>
{
	private final BlackjackAction astar_;
	
	private final int dv_;
	private final int pv_;
	
	public AStarAbstraction( final BlackjackAction astar, final int dv, final int pv )
	{
		astar_ = astar;
		dv_ = dv;
		pv_ = pv;
	}
	
	@Override
	public Representation<BlackjackState> copy()
	{
		return new AStarAbstraction( astar_.create(), dv_, pv_ );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof AStarAbstraction) ) {
			return false;
		}
		final AStarAbstraction that = (AStarAbstraction) obj;
		if( astar_ == null ) {
			return that.astar_ == null;
		}
		else {
			return astar_.equals( that.astar_ );
		}
	}

	@Override
	public int hashCode()
	{
		if( astar_ == null ) {
			return 3;
		}
		else {
			return 5 * astar_.hashCode();
		}
	}
	
	@Override
	public String toString()
	{
		final String s = (astar_ == null ? "null" : astar_.toString());
		return "AStarAbstraction[" + s + "] d:" + dv_ + ", p:" + pv_;
	}

}
