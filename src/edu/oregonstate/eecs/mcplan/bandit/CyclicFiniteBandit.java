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
package edu.oregonstate.eecs.mcplan.bandit;

import java.util.ArrayList;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.LoggerManager;

/**
 * Samples each arm sequentially, then repeats. Useful for deterministic
 * problems.
 */
public class CyclicFiniteBandit<T> extends FiniteBandit<T>
{
	private final ch.qos.logback.classic.Logger LogAgent = LoggerManager.getLogger( "log.agent" );
	
	private int next = 0;
	private boolean all = false;
	
	private final double Vmax;
	private static final double Vmax_default = Double.POSITIVE_INFINITY;
	
	public CyclicFiniteBandit()
	{
		super();
		this.Vmax = Vmax_default;
	}
	
	public CyclicFiniteBandit( final ArrayList<T> arms, final StochasticEvaluator<T> eval )
	{
		this( arms, eval, Vmax_default );
	}
	
	public CyclicFiniteBandit( final ArrayList<T> arms, final StochasticEvaluator<T> eval, final double Vmax )
	{
		super( arms, eval );
		this.Vmax = Vmax;
	}
	
	@Override
	public CyclicFiniteBandit<T> create( final ArrayList<T> arms, final StochasticEvaluator<T> eval )
	{
		return new CyclicFiniteBandit<>( arms, eval );
	}

	@Override
	public int selectArm( final RandomGenerator rng )
	{
		final int i = next++;
		
		if( next >= arms.size() ) {
			next = 0;
			all = true;
		}
		
		return i;
	}

	@Override
	public boolean convergenceTest( final double epsilon, final double delta )
	{
		if( n() == 0 ) {
			return false;
		}
		else if( all ) {
			return true;
		}
		else {
			// This bandit is deterministic, so an arm that reaches the max
			// value is optimal.
			final int istar = bestIndex();
			return mean( istar ) == Vmax;
		}
	}
}
