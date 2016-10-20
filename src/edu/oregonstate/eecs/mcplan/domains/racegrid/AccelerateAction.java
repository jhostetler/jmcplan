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
package edu.oregonstate.eecs.mcplan.domains.racegrid;

import org.apache.commons.math3.random.RandomGenerator;


/**
 * @author jhostetler
 *
 */
public class AccelerateAction extends RacegridAction
{
	public final int ddx;
	public final int ddy;
	
	private boolean old_crashed_ = false;
	private boolean done_ = false;
	
	public AccelerateAction( final int ddx, final int ddy )
	{
		this.ddx = ddx;
		this.ddy = ddy;
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof AccelerateAction) ) {
			return false;
		}
		final AccelerateAction that = (AccelerateAction) obj;
		return ddx == that.ddx && ddy == that.ddy;
	}
	
	@Override
	public int hashCode()
	{
		return 13 + 17 * (ddx + 19 * ddy);
	}
	
	@Override
	public AccelerateAction create()
	{
		return new AccelerateAction( ddx, ddy );
	}

	@Override
	public void doAction( final RandomGenerator rng, final RacegridState s )
	{
		assert( !done_ );
		
		old_crashed_ = s.crashed;
		
		s.ddx = ddx;
		s.ddy = ddy;
		s.crashed = false;
		done_ = true;
	}

	@Override
	public boolean isDone()
	{
		return done_;
	}

	@Override
	public void undoAction( final RacegridState s )
	{
		assert( done_ );
		s.ddx = 0;
		s.ddy = 0;
		s.crashed = old_crashed_;
		done_ = false;
	}
	
	@Override
	public String toString()
	{
		return "AccelerateAction(" + ddx + "; " + ddy + ")";
	}
}
