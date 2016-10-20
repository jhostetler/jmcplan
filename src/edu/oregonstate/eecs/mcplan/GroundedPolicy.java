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

package edu.oregonstate.eecs.mcplan;

public class GroundedPolicy<S, A> implements AnytimePolicy<S>
{
	public static <S, A> GroundedPolicy<S, A> create( final AnytimePolicy<S> lifted )
	{
		return new GroundedPolicy<S, A>( lifted );
	}
	
	private final AnytimePolicy<S> lifted_;
	private AnytimePolicy<S> choice_ = null;
	
	public GroundedPolicy( final AnytimePolicy<S> lifted )
	{
		lifted_ = lifted;
	}
	
	@Override
	public void setState( final S s, final long t )
	{
		lifted_.setState( s, t );
	}

	@Override
	public UndoableAction<S> getAction()
	{
		choice_ = lifted_.getAction();
		return choice_.getAction();
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		choice_.actionResult( sprime, r );
	}

	@Override
	public String getName()
	{
		return "GroundedPolicy[" + lifted_.getName() + "]";
	}

	@Override
	public long minControl()
	{
		return lifted_.minControl();
	}

	@Override
	public long maxControl()
	{
		return lifted_.maxControl();
	}

	@Override
	public A getAction( final long control )
	{
		// FIXME: We're giving it 2x the control here.
		choice_ = lifted_.getAction( control );
		return choice_.getAction( control );
	}

}
