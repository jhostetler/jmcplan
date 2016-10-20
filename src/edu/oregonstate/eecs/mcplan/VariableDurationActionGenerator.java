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

import edu.oregonstate.eecs.mcplan.search.DepthRecorder;

/**
 * @author jhostetler
 *
 */
public class VariableDurationActionGenerator<S, A extends UndoableAction<S>>
	extends ActionGenerator<S, DurativeAction<S, A>>
{
	private final ActionGenerator<S, AnytimePolicy<S, A>> base_;
	private final int[] epochs_;
	private final DepthRecorder depth_;
	
	/**
	 * @param base Primitive action generator.
	 * @param epochs List of epochs.
	 * @param depth An object that knows what depth the search is at.
	 */
	public VariableDurationActionGenerator( final ActionGenerator<S, AnytimePolicy<S, A>> base, final int[] epochs,
											final DepthRecorder depth )
	{
		base_ = base.create();
		epochs_ = epochs;
		depth_ = depth;
	}

	@Override
	public boolean hasNext()
	{
		return base_.hasNext();
	}

	@Override
	public DurativeAction<S, A> next()
	{
		final int d = depth_.getDepth();
		return new DurativeAction<S, A>( base_.next(), epochs_[d] );
	}

	@Override
	public void setState( final S s, final long t )
	{
		base_.setState( s, t );
	}

	@Override
	public ActionGenerator<S, DurativeAction<S, A>> create()
	{
		return new VariableDurationActionGenerator<S, A>( base_.create(), epochs_, depth_ );
	}

	@Override
	public int size()
	{
		return base_.size();
	}

}
