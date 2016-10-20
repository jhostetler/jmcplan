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
public abstract class Policy<S, A>
{
	/**
	 * Called when a new "episode" is about to begin.
	 * <p>
	 * A non-stationary policy can use this as an opportunity to start
	 * recording a new history. Default implementation does nothing.
	 */
	public void reset()
	{ }
	
	/**
	 * Must return false if the policy exploits stored history information to
	 * choose an action.
	 * <p>
	 * Generally, if you've overridden reset(), you should override
	 * isStationary() to return false.
	 * @return
	 */
	public boolean isStationary()
	{ return true; }
	
	public abstract void setState( final S s, final long t );
	
	public abstract A getAction();
	
	/**
	 * This function may be called by the execution environment to provide
	 * reward feedback. The default implementation is a no-op.
	 * 
	 * We adopt the most general reward model and assume that the reward is a
	 * function of the entire transition (s, a, s') -> r. Thus the r given
	 * to actionResult() is the reward "in" s'.
	 * 
	 * @deprecated This should be part of a separate "policy learner" interface.
	 * 
	 * @param sprime
	 * @param r
	 * @param s
	 */
	@Deprecated
	public abstract void actionResult( final S sprime, final double[] r );
	
	public abstract String getName();
	
	@Override
	public abstract int hashCode();
	
	@Override
	public abstract boolean equals( final Object that );
}
