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
package edu.oregonstate.eecs.mcplan.ml;

import java.io.PrintStream;
import java.util.List;

import edu.oregonstate.eecs.mcplan.util.Fn;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * A decision tree that constructs splits greedily from the top down according
 * to a loss function computed over the instances in each node. Ordinary
 * classification tree algorithms such as ID3 correspond to particular choices
 * of loss function.
 * 
 * @author jhostetler
 */
public abstract class GeneralLossDecisionTree<Data> implements Runnable
{
	public static final int leaf = -1;
	
	/**
	 * Represents a data point 'x' as a vector of Boolean-valued features.
	 * @param x
	 * @return
	 */
	public abstract boolean[] features( final Data x );
	
	/**
	 * Computes the loss due to grouping all the data points in 'x' into
	 * a single cluster.
	 * @param x
	 * @return
	 */
	public abstract double clusterLoss( final List<Data> x );
	
	/**
	 * Computes the loss due to grouping the data into two clusters. Invoked
	 * once for each candidate split at each level. This method is separate
	 * from the single-cluster loss so that you can, for example, penalize the
	 * added complexity of the model with two clusters. You can also implement
	 * a cutoff such as a minimum loss threshold by returning Double.MAX_VALUE
	 * whenever the cluster to be split is already below the threshold.
	 * @param false_x
	 * @param true_x
	 * @return
	 */
	public abstract double splitLoss( final List<Data> false_x, final List<Data> true_x );
	
	/**
	 * A cluster will be split only if
	 *     splitLoss() < (clusterLoss() - minimumImprovement())
	 * @return
	 */
	public double minimumImprovement()
	{
		return 0.0;
	}
	
	/**
	 * A cluster is considered for splitting only if
	 *     clusterLoss() > minimumClusterLoss()
	 * @return
	 */
	public double minimumClusterLoss()
	{
		return 0.0;
	}
	
	public GeneralLossDecisionTree( final Data[] x, final int nfeatures )
	{
		x_ = x;
		nfeatures_ = nfeatures;
		f_ = new boolean[x_.length][];
		for( int i = 0; i < x_.length; ++i ) {
			f_[i] = features( x_[i] );
		}
		used_features_ = Fn.repeat( false, nfeatures_ );
	}
	
	private final Data[] x_;
	private final boolean[][] f_;
	private final int nfeatures_;
	private final boolean[] used_features_;
	private Node root_ = null;
	
	public class Node
	{
		public final TIntList members;
		public final double cluster_loss;
		public final int feature;
		public final Node false_branch;
		public final Node true_branch;
		
		public Node( final TIntList members )
		{
			this.members = members;
			cluster_loss = clusterLoss( Fn.takeAll( Fn.keep( new Fn.ArraySlice<Data>( x_ ), members.toArray() ) ) );
			if( cluster_loss < minimumClusterLoss() ) {
				feature = leaf;
				false_branch = null;
				true_branch = null;
				return;
			}
			
			double best_split_loss = Double.MAX_VALUE;
			int best_feature = -1;
			TIntList best_false = null;
			TIntList best_true = null;
			for( int fi = 0; fi < nfeatures_; ++fi ) {
				if( !used_features_[fi] ) {
					final TIntList false_members = new TIntArrayList();
					final TIntList true_members = new TIntArrayList();
					for( int i = 0; i < members.size(); ++i ) {
						final int m = members.get( i );
						if( f_[m][fi] ) {
							true_members.add( m );
						}
						else {
							false_members.add( m );
						}
					}
					final double sl = splitLoss(
						Fn.takeAll( Fn.keep( new Fn.ArraySlice<Data>( x_ ), false_members.toArray() ) ),
						Fn.takeAll( Fn.keep( new Fn.ArraySlice<Data>( x_ ), true_members.toArray() ) ) );
					if( sl < best_split_loss ) {
						best_split_loss = sl;
						best_feature = fi;
						best_false = false_members;
						best_true = true_members;
					}
				}
			}
			
			if( best_split_loss < cluster_loss - minimumImprovement() ) {
				feature = best_feature;
				used_features_[feature] = true;
				false_branch = new Node( best_false );
				true_branch = new Node( best_true );
				used_features_[feature] = false;
			}
			else {
				feature = leaf;
				false_branch = null;
				true_branch = null;
			}
		}
	}
	
	@Override
	public void run()
	{
		root_ = new Node( new TIntArrayList( Fn.linspace( 0, x_.length ) ) );
	}
	
	public Node tree()
	{
		return root_;
	}
	
	public void printTree( final PrintStream out )
	{
		out.println( "[root]" );
		printTree( out, root_, 0 );
	}
	
	private void printTree( final PrintStream out, final Node node, final int depth )
	{
		final StringBuilder sb = new StringBuilder();
		for( int i = 0; i < depth; ++i ) {
			sb.append( "  " );
		}
		final String indent = sb.toString();
		out.println( indent + node.members );
		out.println( indent + "loss = " + node.cluster_loss );
		if( node.false_branch != null ) {
			out.println( indent + "  " + "[f" + node.feature + " = false]" );
			printTree( out, node.false_branch, depth + 1 );
		}
		if( node.true_branch != null ) {
			out.println( indent + "  " + "[f" + node.feature + " = true]" );
			printTree( out, node.true_branch, depth + 1 );
		}
	}
	
	public static void main( final String[] args )
	{
		final double[][] xs = new double[36][];
		int idx = 0;
		for( int x = -2; x <= 2; x += 4 ) {
			for( int y = -2; y <= 2; y += 4 ) {
				for( int xi = -1; xi <= 1; ++xi ) {
					for( int yi = -1; yi <= 1; ++yi ) {
						xs[idx++] = new double[] { x + xi, y + yi };
					}
				}
			}
		}
		
		final GeneralLossDecisionTree<double[]> udt = new GeneralLossDecisionTree<double[]>( xs, 3 )
		{
			@Override
			public boolean[] features( final double[] x )
			{
				return new boolean[] { x[0] < 0, x[1] < 0, x[0] < 2 };
			}

			@Override
			public double clusterLoss( final List<double[]> x )
			{
				final double[] mean = new double[] { 0, 0 };
				int n = 0;
				for( final double[] xi : x ) {
					mean[0] += xi[0];
					mean[1] += xi[1];
					++n;
				}
				mean[0] /= n;
				mean[1] /= n;
				double loss = 0;
				for( final double[] xi : x ) {
					final double[] delta = new double[] { mean[0] - xi[0], mean[1] - xi[1] };
					final double d = (delta[0] * delta[0]) + (delta[1] * delta[1]);
					loss += d;
				}
				return loss;
			}

			@Override
			public double splitLoss( final List<double[]> false_x, final List<double[]> true_x )
			{
				return clusterLoss( false_x ) + clusterLoss( true_x );
			}
			
			@Override
			public double minimumClusterLoss()
			{
				return 20.0;
			}
		};
		
		udt.run();
		udt.printTree( System.out );
	}
}
