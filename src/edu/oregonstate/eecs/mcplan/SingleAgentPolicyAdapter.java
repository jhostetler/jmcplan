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
 * Extracts a single-agent policy from a joint policy.
 */
public class SingleAgentPolicyAdapter<S, A extends VirtualConstructor<A>>
	extends Policy<S, A>
{
	private final int i_;
	private final Policy<S, JointAction<A>> joint_;
	
	public SingleAgentPolicyAdapter( final int i, final Policy<S, JointAction<A>> joint )
	{
		i_ = i;
		joint_ = joint;
	}

	@Override
	public void setState( final S s, final long t )
	{ joint_.setState( s, t ); }

	@Override
	public A getAction()
	{
		final JointAction<A> j = joint_.getAction();
		return j.get( i_ );
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{ }

	@Override
	public String getName()
	{
		return "SingleAgentPolicy[" + joint_.getName() + "/" + i_ + "]";
	}

	@Override
	public int hashCode()
	{
		return 7 + 11 * (joint_.hashCode() + 13 * i_);
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof SingleAgentPolicyAdapter<?, ?>) ) {
			return false;
		}
		
		@SuppressWarnings( "unchecked" )
		final SingleAgentPolicyAdapter<S, A> that = (SingleAgentPolicyAdapter<S, A>) obj;
		return joint_.equals( that.joint_ ) && i_ == that.i_;
	}
}
