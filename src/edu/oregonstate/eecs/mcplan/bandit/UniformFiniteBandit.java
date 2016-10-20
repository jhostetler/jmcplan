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

import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * @author jhostetler
 *
 */
public class UniformFiniteBandit<T> extends FiniteBandit<T>
{
	private final ArrayList<MeanVarianceAccumulator> r;
	private double rstar = -Double.MAX_VALUE;
	private T tstar = null;
	
	public UniformFiniteBandit()
	{
		super();
		r = null;
	}
	
	public UniformFiniteBandit( final ArrayList<T> arms, final StochasticEvaluator<T> eval )
	{
		super( arms, eval );
		
		r = new ArrayList<MeanVarianceAccumulator>();
		for( int i = 0; i < arms.size(); ++i ) {
			r.add( null );
		}
	}
	
	@Override
	public UniformFiniteBandit<T> create( final ArrayList<T> arms, final StochasticEvaluator<T> eval )
	{
		return new UniformFiniteBandit<>( arms, eval );
	}

	@Override
	public void sampleArm( final RandomGenerator rng )
	{
		final int i = rng.nextInt( arms.size() );
		MeanVarianceAccumulator ri = r.get( i );
		if( ri == null ) {
			ri = new MeanVarianceAccumulator();
			r.set( i, ri );
		}
		final T t = arms.get( i );
		final double rsample = eval.evaluate( rng, t );
		System.out.println( "UniformFiniteBandit: arm " + t + " => " + rsample );
		ri.add( rsample );
		if( ri.mean() > rstar ) {
			rstar = ri.mean();
			tstar = t;
		}
	}

	@Override
	public T bestArm()
	{
		return tstar;
	}
}
