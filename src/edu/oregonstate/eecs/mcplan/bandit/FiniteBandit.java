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

package edu.oregonstate.eecs.mcplan.bandit;

import java.util.ArrayList;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.LoggerManager;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * The base class for multi-armed bandit algorithms over a finite set.
 */
public abstract class FiniteBandit<T>
{
	private final ch.qos.logback.classic.Logger LogAgent = LoggerManager.getLogger( "log.agent" );
	
	protected final ArrayList<T> arms;
	protected final StochasticEvaluator<T> eval;
	
	private final ArrayList<MeanVarianceAccumulator> r;
	private double rstar = Double.NEGATIVE_INFINITY;
	private int istar = 0;
	private int n = 0;
	
	public FiniteBandit()
	{
		arms = null;
		eval = null;
		r = null;
	}
	
	public FiniteBandit( final ArrayList<T> arms, final StochasticEvaluator<T> eval )
	{
		this.arms = arms;
		this.eval = eval;
		
		r = new ArrayList<MeanVarianceAccumulator>();
		for( int i = 0; i < arms.size(); ++i ) {
			r.add( null );
		}
	}
	
	public abstract FiniteBandit<T> create( final ArrayList<T> arms, final StochasticEvaluator<T> eval );
	
	// -----------------------------------------------------------------------
	
	public final int n()
	{
		return n;
	}
	
	public final int n( final int i )
	{
		final MeanVarianceAccumulator ri = r.get( i );
		return (ri != null ? ri.n() : 0);
	}
	
	public final double mean( final int i )
	{
		return r.get( i ).mean();
	}
	
	public final int Narms()
	{
		return arms.size();
	}
	
	public final T arm( final int i )
	{
		return arms.get( i );
	}
	
	public final T bestArm()
	{
		return arms.get( istar );
	}
	
	public final void sampleArm( final RandomGenerator rng )
	{
		final int i = selectArm( rng );
		MeanVarianceAccumulator ri = r.get( i );
		if( ri == null ) {
			ri = new MeanVarianceAccumulator();
			r.set( i, ri );
		}
		
		final T t = arms.get( i );
		LogAgent.debug( "FiniteBandit: sample arm {}", t );
		final double rsample = eval.evaluate( rng, t );
		LogAgent.debug( "FiniteBandit: sample arm {} => {}", t, rsample );
		ri.add( rsample );
		if( ri.mean() > rstar ) {
			LogAgent.info( "FiniteBandit: lead change: {} => {}", bestArm(), t );
			LogAgent.info( "\tr: {} => {}", rstar, ri.mean() );
			rstar = ri.mean();
			istar = i;
		}
		n += 1;
	}
	
	// -----------------------------------------------------------------------
	// Subclass interface
	
	protected int bestIndex()
	{
		return istar;
	}
	
	protected abstract int selectArm( final RandomGenerator rng );
	
	public boolean convergenceTest( final double epsilon, final double delta )
	{
		return false;
	}
}
