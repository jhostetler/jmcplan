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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.collect.Lists;

/**
 * @author jhostetler
 *
 */
public class SequencePolicy<S, A> extends AnytimePolicy<S, A>
{
	private final List<Policy<S, A>> policies;
	private final int[] switch_times;
	
	private int t = 0;
	private int i = 0;
	
	public SequencePolicy( final List<Policy<S, A>> policies, final int[] switch_times )
	{
		assert( policies.size() - 1 == switch_times.length );
		this.policies = policies;
		this.switch_times = switch_times;
	}
	
	@SafeVarargs
	public SequencePolicy( final int[] switch_times, final Policy<S, A>... policies )
	{
		assert( policies.length - 1 == switch_times.length );
		this.policies = Lists.newArrayList( policies );
		this.switch_times = switch_times;
	}
	
	private SequencePolicy( final SequencePolicy<S, A> that )
	{
		this.policies = new ArrayList<>();
		for( final Policy<S, A> pi : that.policies ) {
			this.policies.add( pi.copy() );
		}
		this.switch_times = that.switch_times;
		this.t = that.t;
		this.i = that.i;
	}
	
	@Override
	public SequencePolicy<S, A> copy()
	{
		return new SequencePolicy<>( this );
	}
	
	@Override
	public void setState( final S s, final long t )
	{
		if( i < switch_times.length && this.t == switch_times[i] ) {
			i += 1;
//			policies.get( i ).reset();
		}
		for( final Policy<S, A> pi : policies ) {
			pi.setState( s, t );
		}
//		policies.get( i ).setState( s, t );
		this.t += 1; // FIXME: Assuming discrete time steps
	}
	
	@Override
	public boolean improvePolicy()
	{
		return false;
	}

	@Override
	public A getAction()
	{
		return policies.get( i ).getAction();
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		policies.get( i ).actionResult( sprime, r );
	}

	@Override
	public String getName()
	{
		return "SequencePolicy";
	}

	@Override
	public int hashCode()
	{
		final HashCodeBuilder hb = new HashCodeBuilder();
		hb.append( getClass() ).append( policies ).append( Arrays.hashCode( switch_times ) );
		return hb.toHashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof SequencePolicy<?, ?>) ) {
			return false;
		}
		@SuppressWarnings( "unchecked" )
		final SequencePolicy<S, A> that = (SequencePolicy<S, A>) obj;
		return policies.equals( that.policies ) && Arrays.equals( switch_times, that.switch_times );
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( getName() ).append( "([" ).append( StringUtils.join( switch_times, ';'  ) )
		  .append( "]; [" ).append( StringUtils.join( policies, ';' ) ).append( "])" );
		return sb.toString();
	}

}
