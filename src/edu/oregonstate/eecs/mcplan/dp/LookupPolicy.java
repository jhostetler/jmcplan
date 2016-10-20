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
package edu.oregonstate.eecs.mcplan.dp;

import java.util.Map;

import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class LookupPolicy<S, A extends VirtualConstructor<A>> extends Policy<S, A>
{
	private final Map<S, A> actions_;
	
	private final int hash_code;
	
	private A a_ = null;
	
	/**
	 * @param actions Must not be modified after calling the constructor.
	 */
	public LookupPolicy( final Map<S, A> actions )
	{
		actions_ = actions;
		hash_code = actions_.hashCode();
	}
	
	@Override
	public void setState( final S s, final long t )
	{
		a_ = actions_.get( s );
	}

	@Override
	public A getAction()
	{
		return a_.create();
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{ }

	@Override
	public String getName()
	{
		return "LookupPolicy";
	}

	@Override
	public int hashCode()
	{
		return hash_code;
	}

	@Override
	public boolean equals( final Object obj )
	{
		final LookupPolicy<?, ?> that = (LookupPolicy<?, ?>) obj;
		if( this.hashCode() != that.hashCode() ) {
			return false;
		}
		return actions_.equals( that.actions_ );
	}
}
