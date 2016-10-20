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

import edu.oregonstate.eecs.mcplan.ActionGenerator;

/**
 * @author jhostetler
 *
 */
public class RacegridActionGenerator extends ActionGenerator<RacegridState, RacegridAction>
{
	private final int range_ = 1;
	private int x_ = -range_;
	private int y_ = -range_;
	
	@Override
	public ActionGenerator<RacegridState, RacegridAction> create()
	{
		return new RacegridActionGenerator();
	}

	@Override
	public void setState( final RacegridState s, final long t )
	{
		x_ = -range_;
		y_ = -range_;
	}

	@Override
	public int size()
	{
		return (2*range_+1)*(2*range_+1);
	}

	@Override
	public boolean hasNext()
	{
		return x_ <= range_ && y_ <= range_;
	}

	@Override
	public RacegridAction next()
	{
		final AccelerateAction a = new AccelerateAction( x_, y_ );
		x_ += 1;
		if( x_ > range_ ) {
			x_ = -range_;
			y_ += 1;
		}
		return a;
	}
}
