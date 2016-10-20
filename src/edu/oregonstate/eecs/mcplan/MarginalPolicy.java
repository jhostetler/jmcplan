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
package edu.oregonstate.eecs.mcplan;

/**
 * @author jhostetler
 *
 */
public class MarginalPolicy<S, A extends VirtualConstructor<A>> extends Policy<S, A>
{
	private final Policy<S, JointAction<A>> joint_policy_;
	private final int turn_;
	
	public <P extends Policy<S, JointAction<A>>> MarginalPolicy( final P joint_policy, final int turn )
	{
		joint_policy_ = joint_policy;
		turn_ = turn;
	}
	
	@Override
	public int hashCode()
	{
		return joint_policy_.hashCode() * 51 + turn_;
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof MarginalPolicy<?, ?>) ) {
			return false;
		}
		final MarginalPolicy<S, A> that = (MarginalPolicy<S, A>) obj;
		return turn_ == that.turn_ && joint_policy_.equals( that.joint_policy_ );
	}
	
	@Override
	public void setState( final S s, final long t )
	{
		joint_policy_.setState( s, t );
	}

	@Override
	public A getAction()
	{
		return joint_policy_.getAction().get( turn_ );
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		joint_policy_.actionResult( sprime, r );
	}

	@Override
	public String getName()
	{
		return "MarginalPolicy";
	}
}
