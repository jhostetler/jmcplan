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
public class SingleAgentJointActionGenerator<S, A extends VirtualConstructor<A>>
	extends ActionGenerator<S, JointAction<A>>
{
	public static <S, A extends VirtualConstructor<A>>
	SingleAgentJointActionGenerator<S, A> create( final ActionGenerator<S, A> base )
	{
		return new SingleAgentJointActionGenerator<S, A>( base );
	}
	
	private final ActionGenerator<S, A> base_;
	
	public SingleAgentJointActionGenerator( final ActionGenerator<S, A> base )
	{
		base_ = base;
	}
	
	@Override
	public ActionGenerator<S, JointAction<A>> create()
	{
		return new SingleAgentJointActionGenerator<S, A>( base_.create() );
	}

	@Override
	public void setState( final S s, final long t )
	{
		base_.setState( s, t );
	}

	@Override
	public int size()
	{
		return base_.size();
	}

	@Override
	public boolean hasNext()
	{
		return base_.hasNext();
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public JointAction<A> next()
	{
		return new JointAction<A>( base_.next() );
	}
}
