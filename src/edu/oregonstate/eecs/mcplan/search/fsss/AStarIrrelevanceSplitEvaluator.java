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
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * The evaluation is
 * 		score = D + \lambda*R
 * where D is the L1 distance between the Q-functions of the two resulting
 * abstract states, R measures the size balance of the two states, and
 * \lambda is a regularization parameter.
 */
public class AStarIrrelevanceSplitEvaluator<S extends State, A extends VirtualConstructor<A>>
	implements SplitEvaluator<S, A>
{
	private final double size_regularization;
	
	public AStarIrrelevanceSplitEvaluator( final double size_regularization )
	{
		this.size_regularization = size_regularization;
	}
	
	@Override
	public String toString()
	{
		return "AStarIrrelevanceSplitEvaluator(" + size_regularization + ")";
	}
	
	@Override
	public double evaluateSplit( final FsssAbstractStateNode<S, A> asn,
								 final ArrayList<FsssStateNode<S, A>> U, final ArrayList<FsssStateNode<S, A>> V )
	{
		final int Nactions = asn.nsuccessors();
		final MeanVarianceAccumulator[] QU = new MeanVarianceAccumulator[Nactions];
		final MeanVarianceAccumulator[] QV = new MeanVarianceAccumulator[Nactions];
		
		for( int i = 0; i < Nactions; ++i ) {
			QU[i] = new MeanVarianceAccumulator();
			QV[i] = new MeanVarianceAccumulator();
		}
		
		// Heuristic choice: Using upper bound (U) for comparisons.
		// Justification: We want to increase U so that it is over L(a*)
		for( final FsssStateNode<S, A> u : U ) {
			int i = 0;
			for( final FsssActionNode<S, A> a : u.successors() ) {
				QU[i].add( a.U() );
				i += 1;
			}
		}
		for( final FsssStateNode<S, A> v : V ) {
			int i = 0;
			for( final FsssActionNode<S, A> a : v.successors() ) {
				QV[i].add( a.U() );
				i += 1;
			}
		}
		
		int au_max = -1;
		int av_max = -1;
		double umax = -Double.MAX_VALUE;
		double vmax = -Double.MAX_VALUE;
		for( int i = 0; i < Nactions; ++i ) {
			if( QU[i].mean() > umax ) {
				umax = QU[i].mean();
				au_max = i;
			}
			if( QV[i].mean() > vmax ) {
				vmax = QV[i].mean();
				av_max = i;
			}
		}
		
		final double Du = Math.abs( umax - QV[au_max].mean() );
		final double Dv = Math.abs( vmax - QU[av_max].mean() );
		final double R = sizeBalance( U, V );
		
		return Du + Dv + size_regularization*R;
	}
	
	private double sizeBalance( final ArrayList<FsssStateNode<S, A>> U,
								final ArrayList<FsssStateNode<S, A>> V )
	{
		final int Nu = U.size();
		final int Nv = V.size();
		
		if( Nu == 0 && Nv == 0 ) {
			return 0;
		}
		else {
			return Math.min( Nu, Nv ) / ((double) Math.max( Nu, Nv ));
		}
	}

}
