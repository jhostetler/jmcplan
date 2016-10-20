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
 * Adapts an ActionGenerator to generate DurativeUndoableActions.
 */
public class DurativeActionGenerator<S, A>
	extends ActionGenerator<S, DurativeAction<S, A>>
{
	public static <S, A> DurativeActionGenerator<S, A> create(
		final ActionGenerator<S, ? extends Policy<S, A>> base, final int epoch )
	{
		return new DurativeActionGenerator<S, A>( base, epoch );
	}
	
	private final ActionGenerator<S, ? extends Policy<S, A>> base_;
	private final int epoch_;
	
	/**
	 * 
	 */
	public DurativeActionGenerator( final ActionGenerator<S, ? extends Policy<S, A>> base, final int epoch )
	{
		base_ = base;
		epoch_ = epoch;
	}

	@Override
	public boolean hasNext()
	{
		return base_.hasNext();
	}

	@Override
	public DurativeAction<S, A> next()
	{
		return new DurativeAction<S, A>( base_.next(), epoch_ );
	}

	@Override
	public void setState( final S s, final long t, final int[] turn )
	{
		base_.setState( s, t, turn );
	}

	@Override
	public ActionGenerator<S, DurativeAction<S, A>> create()
	{
		return new DurativeActionGenerator<S, A>( base_.create(), epoch_ );
	}

	@Override
	public int size()
	{
		return base_.size();
	}

}
