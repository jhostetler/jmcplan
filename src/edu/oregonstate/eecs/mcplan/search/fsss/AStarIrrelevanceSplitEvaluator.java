/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * @author jhostetler
 *
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
		
		double umax = -Double.MAX_VALUE;
		double vmax = -Double.MAX_VALUE;
		for( int i = 0; i < Nactions; ++i ) {
			umax = Math.max( umax, QU[i].mean() );
			vmax = Math.max( vmax, QV[i].mean() );
		}
		
		final double D = Math.abs( umax - vmax );
		final double R = sizeBalance( U, V );
		
		return D + size_regularization*R;
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
