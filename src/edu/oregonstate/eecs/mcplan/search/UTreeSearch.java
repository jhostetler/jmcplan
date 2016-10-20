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
package edu.oregonstate.eecs.mcplan.search;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.RandomPolicy;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.SingleAgentJointActionGenerator;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.domains.fuelworld.FuelWorldAction;
import edu.oregonstate.eecs.mcplan.domains.fuelworld.FuelWorldActionGenerator;
import edu.oregonstate.eecs.mcplan.domains.fuelworld.FuelWorldState;
import edu.oregonstate.eecs.mcplan.domains.taxi.PrimitiveTaxiRepresenter;
import edu.oregonstate.eecs.mcplan.domains.taxi.TaxiAction;
import edu.oregonstate.eecs.mcplan.domains.taxi.TaxiActionGenerator;
import edu.oregonstate.eecs.mcplan.domains.taxi.TaxiRolloutEvaluator;
import edu.oregonstate.eecs.mcplan.domains.taxi.TaxiSimulator;
import edu.oregonstate.eecs.mcplan.domains.taxi.TaxiState;
import edu.oregonstate.eecs.mcplan.domains.taxi.TaxiWorlds;
import edu.oregonstate.eecs.mcplan.domains.toy.ChainWalk;
import edu.oregonstate.eecs.mcplan.domains.toy.CliffWorld;
import edu.oregonstate.eecs.mcplan.domains.toy.CliffWorld.Path;
import edu.oregonstate.eecs.mcplan.sim.ResetAdapter;
import edu.oregonstate.eecs.mcplan.sim.ResetSimulator;
import edu.oregonstate.eecs.mcplan.sim.Simulator;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.Generator;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;
import edu.oregonstate.eecs.mcplan.util.VectorMeanVarianceAccumulator;

/**
 * Implements incremental splitting using a slightly adapted version of the
 * UTree algorithm:
 * 
 * @phdthesis{mccallum1996reinforcement,
 *   title={Reinforcement learning with selective perception and hidden state},
 *   author={McCallum, Andrew Kachites},
 *   year={1996},
 *   school={University of Rochester}
 * }
 * 
 * Algorithm:
 * 
 * When visiting an action node a:
 * 	1. Find the state node successor s' that the instance belongs in according
 * 		to the current abstraction
 * 	2. If s' needs to be split:
 * 		a. Retrieve all primitive states associated with s'
 * 		b. Find best split point
 * 		c. Create two new children s1 and s2 and split the instances from s'
 * 			between them according to the chosen test
 * 		d. Remove s' as a child of a and add the two children s1 and s2
 * 		e. Set s' = s1 or s2 as appropriate
 * 	3. visit s'
 *
 * Need to be careful not to re-order the state node information
 * 
 * FIXME: I tried to implement this algorithm within the established
 * GameTree API, but it was getting confusing and difficult. This implementation
 * obviously copy-pastes a lot of similar interfaces with different names,
 * and in the long term it should be reconciled with the GameTree API.
 * 
 * @author jhostetler
 */
public class UTreeSearch<S extends State, A extends VirtualConstructor<A>>
	implements Runnable, GameTree<S, A>
{
	private class PrimitiveStateNode extends MutableStateNode<S, A>
	{
		private final FactoredRepresentation<S> factored_x;
		
		public PrimitiveStateNode( final FactoredRepresentation<S> x, final int nagents,
				final int[] turn, final ActionGenerator<S, JointAction<A>> action_gen )
		{
			super( nagents, turn, action_gen );
			factored_x = x;
		}
		
		protected MutableActionNode<S, A> createSuccessor(
			final JointAction<A> a, final int nagents, final Representer<S, ? extends Representation<S>> repr )
		{
			return new PrimitiveActionNode( a, nagents, repr );
		}

		@Override
		public MutableActionNode<S, A> successor( final JointAction<A> a,
				final int nagents, final Representer<S, ? extends Representation<S>> repr )
		{
			final MutableActionNode<S, A> an = getActionNode( a );
			if( an != null ) {
				return an;
			}
			else {
				final MutableActionNode<S, A> succ = createSuccessor( a, nagents, repr.create() );
				attachSuccessor( a, succ );
				return succ;
			}
		}
		
		@Override
		public String toString()
		{ return Integer.toHexString( System.identityHashCode( this ) ) + ":" + n() + "x" + factored_x.toString(); }

		public boolean isTerminal()
		{ return factored_x instanceof LeafRepresentation<?>; }
		
//		@Override
//		public boolean equals( final Object obj )
//		{
//			if( !(obj instanceof UTreeSearch.PrimitiveStateNode) ) {
//				return false;
//			}
//			final UTreeSearch<S, A>.PrimitiveStateNode that = (UTreeSearch<S, A>.PrimitiveStateNode) obj;
//			return factored_x.equals( that.factored_x );
//		}
//
//		@Override
//		public int hashCode()
//		{
//			return factored_x.hashCode();
//		}
	}
	
	private class PrimitiveActionNode extends MutableActionNode<S, A>
	{
		public PrimitiveActionNode( final JointAction<A> a, final int nagents,
				final Representer<S, ? extends Representation<S>> repr )
		{
			super( a, nagents, repr );
		}
		
		protected MutableStateNode<S, A> createSuccessor(
			final S s, final Representation<S> x, final int nagents, final int[] turn,
			final ActionGenerator<S, JointAction<A>> action_gen )
		{
			return new PrimitiveStateNode( (FactoredRepresentation<S>) x, nagents, turn, action_gen );
		}

		@Override
		public MutableActionNode<S, A> create()
		{ return new PrimitiveActionNode( a(), nagents, repr_ ); }

		@Override
		public MutableStateNode<S, A> successor( final S s, final int nagents, final int[] turn,
				final ActionGenerator<S, JointAction<A>> action_gen )
		{
			if( s.isTerminal() ) {
				final LeafRepresentation<S> x = new LeafRepresentation<S>();
				MutableStateNode<S, A> leaf = getStateNode( x, turn );
				if( leaf == null ) {
					leaf = new PrimitiveStateNode( x, nagents, turn, action_gen );
					attachSuccessor( x, turn, leaf );
				}
				
				return leaf;
			}
			
			final Representation<S> x = repr_.encode( s );
			
			final MutableStateNode<S, A> sn = getStateNode( x, turn );
			if( sn != null ) {
				return sn;
			}
			else {
				final MutableStateNode<S, A> succ = createSuccessor( s, x, nagents, turn, action_gen );
				attachSuccessor( x, turn, succ );
				return succ;
			}
		}
	}
	
	private class AggregateStateNode extends StateNode<S, A>
	{
		public final Set<PrimitiveStateNode> nodes;
		public final ActionGenerator<S, JointAction<A>> actions;
		
		private int n_ = 0;
		
		private final VectorMeanVarianceAccumulator rv_;
		
		private final Map<JointAction<A>, AggregateActionNode> children = new HashMap<JointAction<A>, AggregateActionNode>();
		
		private int split_threshold_ = split_threshold;
		
		private final double[] v;
		
		public AggregateStateNode( final Set<PrimitiveStateNode> nodes,
								   final ActionGenerator<S, JointAction<A>> actions,
								   final int nagents, final int[] turn )
		{
			super( nagents, turn );
			this.nodes = nodes;
			this.actions = actions;
			
			v = new double[nagents];
			
//			check();
			
			rv_ = new VectorMeanVarianceAccumulator( nagents );
			
			for( final PrimitiveStateNode ps : nodes ) {
				addSubtree( ps );
			}
		}
		
		public void setV( final double[] v )
		{
			Fn.memcpy( this.v, v, nagents );
		}
		
		public double[] v()
		{
			return v;
		}
		
		public void addPrimitiveState( final PrimitiveStateNode ps )
		{
			nodes.add( ps );
//			check();
		}
		
		private void check()
		{
			final Set<Representation<S>> xs = new HashSet<Representation<S>>();
			for( final PrimitiveStateNode ps : nodes ) {
				if( xs.contains( ps.factored_x ) ) {
					System.out.println( "!! Duplicates in as@" + this );
					System.out.println( "\t" + ps.factored_x );
					System.out.println( "\t" + nodes );
					throw new IllegalStateException( "Duplicates in as@" + this );
				}
				xs.add( ps.factored_x );
			}
		}
		
		public int splitThreshold()
		{ return split_threshold_; }
		
		public void setSplitThreshold( final int threshold )
		{ split_threshold_ = threshold; }
		
		@Override
		public String toString()
		{
			return "{" + Integer.toHexString( System.identityHashCode( this ) )
					+ "; r: " + Arrays.toString( r() )
					+ "; v: " + Arrays.toString( v ) + "->" + nodes.toString() + "}";
		}
		
		public AggregateActionNode successor( final JointAction<A> a, final int nagents,
				final FactoredRepresenter<S, ? extends FactoredRepresentation<S>> repr )
		{
			final AggregateActionNode an = (AggregateActionNode) getActionNode( a );
			if( an != null ) {
				return an;
			}
			else {
				final AggregateActionNode succ = new AggregateActionNode( a, nagents, repr );
				children.put( a, succ );
				return succ;
			}
		}
		
		// FIXME: This function should update variance for the subtree also
		public void addSubtree( final PrimitiveStateNode ps )
		{
			for( int i = 0; i < ps.n(); ++i ) {
				visit();
				updateR( ps.r() );
			}
			for( final MutableActionNode<S, A> pa : Fn.in( ps.successors() ) ) {
				@SuppressWarnings( "unchecked" )
				final AggregateActionNode aa = successor(
					pa.a(), pa.nagents, (FactoredRepresenter<S, ? extends FactoredRepresentation<S>>) pa.repr_ );
				for( int i = 0; i < pa.n(); ++i ) {
					aa.visit();
					aa.updateQ( pa.q() );
					aa.updateR( pa.r() );
				}
				
				for( final MutableStateNode<S, A> msprime : Fn.in( pa.successors() ) ) {
					final PrimitiveStateNode psprime = (PrimitiveStateNode) msprime;
					final AggregateStateNode asprime;
					if( psprime.isTerminal() ) {
						asprime = aa.successorTerminal( psprime );
					}
					else {
						asprime = aa.successorNonTerminal( psprime.factored_x, psprime );
						asprime.addSubtree( psprime );
					}
				}
			}
			
			final ActionNode<S, A> an = BackupRules.MaxAction( this );
			if( an != null ) {
				this.setV( Fn.copy( an.q() ) );
			}
			// TODO: What should we do if it is null?
		}

		public void visit()
		{ n_ += 1; }
		
		@Override
		public int n()
		{ return n_; }

		@Override
		public double[] r()
		{ return rv_.mean(); }
		
		@Override
		public double r( final int i )
		{ return rv_.mean()[i]; }
		
		@Override
		public double[] rvar()
		{ return rv_.variance(); }
		
		@Override
		public double rvar( final int i )
		{ return rv_.variance()[i]; }
		
		public void updateR( final double[] r )
		{
			assert( r.length == rv_.Ndim );
			rv_.add( r );
		}

		@Override
		public Generator<? extends ActionNode<S, A>> successors()
		{ return Generator.fromIterator( children.values().iterator() ); }
		
		public int nsuccessors()
		{
			return children.size();
		}

		@Override
		public ActionNode<S, A> getActionNode( final JointAction<A> a )
		{ return children.get( a ); }
	}
	
	private class AggregateActionNode extends ActionNode<S, A>
	{
		private final FactoredRepresenter<S, ? extends FactoredRepresentation<S>> factored_repr;
		private final DataNode dt_root;
		
		private AggregateStateNode TheTerminalNode = null;
		
		private int n_ = 0;
		private VectorMeanVarianceAccumulator qv_;
		private VectorMeanVarianceAccumulator rv_;
		
		private final Set<AggregateStateNode> children = new HashSet<AggregateStateNode>();
		
		@Override
		public String toString()
		{
			return "[@" + Integer.toHexString( System.identityHashCode( this ) ) + "] " + super.toString();
		}
		
		public AggregateActionNode( final JointAction<A> a, final int nagents,
				final FactoredRepresenter<S, ? extends FactoredRepresentation<S>> repr )
		{
			super( a, nagents );
			factored_repr = repr;
			dt_root = new DataNode();
			
			qv_ = new VectorMeanVarianceAccumulator( nagents );
			rv_ = new VectorMeanVarianceAccumulator( nagents );
		}
		
		public void setQ( final double[] q, final int n )
		{
			qv_ = new VectorMeanVarianceAccumulator( nagents );
			qv_.add( q, n );
		}
		
		public void setR( final double[] r, final int n )
		{
			rv_ = new VectorMeanVarianceAccumulator( nagents );
			rv_.add( r, n );
		}
		
		@Override
		public Generator<AggregateStateNode> successors()
		{ return Generator.fromIterator( children.iterator() ); }
		
		public void visit()
		{ n_ += 1; }

		public AggregateStateNode successor( final S s, final PrimitiveStateNode ps )
		{
			if( s.isTerminal() ) {
				return successorTerminal( ps );
			}
			else {
				final FactoredRepresentation<S> x = factored_repr.encode( s );
				return successorNonTerminal( x, ps );
			}
		}
		
		public AggregateStateNode successorTerminal( final PrimitiveStateNode ps )
		{
			if( TheTerminalNode == null ) {
				TheTerminalNode = new AggregateStateNode(
					new HashSet<PrimitiveStateNode>(), null, ps.nagents, ps.turn );
			}
			TheTerminalNode.addPrimitiveState( ps );
			return TheTerminalNode;
		}
		
		public AggregateStateNode successorNonTerminal( final FactoredRepresentation<S> x, final PrimitiveStateNode ps )
		{
			final DataNode dn = classify( x, ps.nagents, ps.turn, ps.action_gen_ );
			dn.aggregate.addPrimitiveState( ps );
			return dn.aggregate;
		}
		
		private boolean new_split = false;
		
		public boolean consumeNewSplitFlag()
		{
			final boolean b = new_split;
			new_split = false;
			return b;
		}
		
		public DataNode classify( final FactoredRepresentation<S> x, final int nagents,
								  final int[] turn, final ActionGenerator<S, JointAction<A>> action_gen )
		{
			new_split = false;
			
			final double[] phi = x.phi();
			DataNode dn = dt_root;
			while( dn.split != null ) {
				dn = dn.split.child( phi );
			}
			
			if( dn.aggregate == null ) {
				dn.aggregate = new AggregateStateNode( new HashSet<PrimitiveStateNode>(),
													   action_gen.create(), nagents, turn );
				children.add( dn.aggregate );
			}
			
			if( dn.aggregate.n() >= dn.aggregate.splitThreshold() ) {
//				System.out.println( " Splitting " + x );
//				System.out.println( "! dn.aggregate.n() = " + dn.aggregate.n() );
//				System.out.println( "! " + dn.aggregate );
				
				final SplitNode split = createSplit( dn, nagents, turn, action_gen );
				if( split != null ) {
					
//					System.out.println( "****************************************" );
//					System.out.println( "*** Before split ***********************" );
//					root().accept( new TreePrinter<S, A>( 8 ) );
//					System.out.println( "----------------------------------------" );
					
					Nsplits_accepted += 1;
					
					dn.split = split;
					// Allow GC to collect references
					children.remove( dn.aggregate );
					dn.aggregate = null;
					dn = split.child( phi );
					for( final DataNode succ : Fn.in( split.children() ) ) {
						assert( succ.aggregate != null );
						children.add( succ.aggregate );
					}
					
					// FIXME: Here we need to update the value of the action
					// since it will be higher after splitting. It would
					// also be a good place to mess with the exploration constant.
					
					new_split = true;
					
//					System.out.println( "****************************************" );
//					System.out.println( "****************************************" );
//					root().accept( new TreePrinter<S, A>() );
//					System.out.println( "----------------------------------------" );
//					Ptree_root_action_.successors().next().accept( new TreePrinter<S, A>() );
//					System.out.println( "****************************************" );
				}
				else {
					// Didn't find a good split -> increase threshold
					// TODO: This is a somewhat significant design choice,
					// should probably cross-validate.
					dn.aggregate.setSplitThreshold( (int) Math.ceil( backoff * dn.aggregate.splitThreshold() ) );
				}
			}
			
			return dn;
		}
		
		private SplitNode createSplit( final DataNode dn, final int nagents,
									   final int[] turn, final ActionGenerator<S, JointAction<A>> action_gen )
		{
			// Find the most-played actions
			final Set<JointAction<A>> relevant_actions = new HashSet<JointAction<A>>();
			
			final int Nactions = dn.aggregate.nsuccessors();
			if( Nactions == 1 ) {
				return null;
			}
			else if( Nactions <= top_actions ) {
				for( final ActionNode<S, A> an : Fn.in( dn.aggregate.successors() ) ) {
					relevant_actions.add( an.a() );
				}
			}
			else {
				final PriorityQueue<ActionNode<S, A>> pq = new PriorityQueue<ActionNode<S, A>>( Nactions,
					new Comparator<ActionNode<S, A>>() {
						@Override
						public int compare( final ActionNode<S, A> a, final ActionNode<S, A> b )
						{ return (int) -Math.signum( a.n() - b.n() ); }
					} );
				for( final ActionNode<S, A> an : Fn.in( dn.aggregate.successors() ) ) {
					pq.add( an );
				}

				for( int i = 0; i < Math.min( top_actions, pq.size() ); ++i ) {
					relevant_actions.add( pq.poll().a() );
				}
			}
			
			int split_idx = -1;
			double split_value = 0;
			double value = -Double.MAX_VALUE;
			ArrayList<PrimitiveStateNode> Ustar = null;
			ArrayList<PrimitiveStateNode> Vstar = null;
			final ArrayList<PrimitiveStateNode> dn_list = new ArrayList<PrimitiveStateNode>( dn.aggregate.nodes );
			
			// Test all attributes for split quality
			for( int i = 0; i < factored_repr.attributes().size(); ++i ) {
//				System.out.println( "\tTesting attribute " + i );
				
				final int ii = i;
				Collections.sort( dn_list, new Comparator<PrimitiveStateNode>() {
					@Override
					public int compare( final PrimitiveStateNode a, final PrimitiveStateNode b )
					{ return (int) Math.signum( a.factored_x.phi()[ii] - b.factored_x.phi()[ii] ); }
				} );
				
				// Test all split points for quality
				final int start = 0; //(int) Math.floor( dn_list.size() / 3.0 );
				final int end = dn_list.size(); //(int) Math.ceil( dn_list.size() / 1.5 );
				double v0 = dn_list.get( start ).factored_x.phi()[i];
				for( int j = start + 1; j < end; ++j ) {
					final double v1 = dn_list.get( j ).factored_x.phi()[i];
					if( v1 > v0 ) {
						final ArrayList<PrimitiveStateNode> U = new ArrayList<PrimitiveStateNode>();
						final ArrayList<PrimitiveStateNode> V = new ArrayList<PrimitiveStateNode>();
						final double split = (v1 + v0) / 2;
						
//						System.out.println( "\t\tTesting split point " + split );
						
						for( int k = 0; k < j; ++k ) {
							U.add( dn_list.get( k ) );
						}
						for( int k = j; k < dn_list.size(); ++k ) {
							V.add( dn_list.get( k ) );
						}
						final double score = evaluateSplit( U, V, relevant_actions );
						
//						System.out.println( "\t\tscore = " + score );
						
//						final double score = Math.min( j, dn_list.size() - j )
//											 / ((double) Math.max( j, dn_list.size() - j ));
						
						assert( score >= 0.0 );
						if( score > value ) {
							split_idx = i;
							split_value = split;
							value = score;
							Ustar = U;
							Vstar = V;
						}
						
						v0 = v1;
					}
				}
			}
			
//			System.out.println( "\t" + dn_list );
			
			if( value == 0.0 ) {
//				System.out.println( "\t ! No profitable splits" );
				return null;
			}
			
			if( Ustar == null || Vstar == null ) {
//				System.out.println( "\t ! Homogeneous cluster" );
				return null;
			}
			
			System.out.println( "\tSplit below: " + this );
			System.out.println( "\tState: " + dn.aggregate );
			System.out.println( "\tActions: " + relevant_actions );
			System.out.println( "\t Selected " + split_idx + " @ " + split_value );
			System.out.println( "\t score = " + value );
			final double R = sizeBalance( Ustar, Vstar );
			final double N = primitiveCount( Ustar ) + primitiveCount( Vstar );
			System.out.println( "\t R = " + R );
			System.out.println( "\t N = " + N );
			
			final BinarySplitNode split = new BinarySplitNode( split_idx, split_value );
			
			split.left = new DataNode();
			split.left.aggregate = new AggregateStateNode( new HashSet<PrimitiveStateNode>( Ustar ),
														   action_gen.create(), nagents, turn );
			
			split.right = new DataNode();
			split.right.aggregate = new AggregateStateNode( new HashSet<PrimitiveStateNode>( Vstar ),
															action_gen.create(), nagents, turn );
			
			return split;
		}
		
		private double sizeBalance( final ArrayList<PrimitiveStateNode> U,
									  final ArrayList<PrimitiveStateNode> V )
		{
			int Nu = 0;
			for( final PrimitiveStateNode u : U ) {
				Nu += u.n();
			}
			
			int Nv = 0;
			for( final PrimitiveStateNode v : V ) {
				Nv += v.n();
			}
			return Math.min( Nu, Nv ) / ((double) Math.max( Nu, Nv ));
		}
		
		private int primitiveCount( final ArrayList<PrimitiveStateNode> U )
		{
			int Nu = 0;
			for( final PrimitiveStateNode u : U ) {
				Nu += u.n();
			}
			return Nu;
		}
		
		private double evaluateSplit( final ArrayList<PrimitiveStateNode> U,
									  final ArrayList<PrimitiveStateNode> V,
									  final Collection<JointAction<A>> relevant_actions )
		{
			final double R = sizeBalance( U, V );
			final MeanVarianceAccumulator[] QU = new MeanVarianceAccumulator[relevant_actions.size()];
			final MeanVarianceAccumulator[] QV = new MeanVarianceAccumulator[relevant_actions.size()];
			for( int i = 0; i < relevant_actions.size(); ++i ) {
				QU[i] = new MeanVarianceAccumulator();
				QV[i] = new MeanVarianceAccumulator();
			}
			
			// Note: Implementation assumes single-agent
			for( final PrimitiveStateNode u : U ) {
				int i = 0;
				for( final JointAction<A> a : relevant_actions ) {
					final ActionNode<S, A> an = u.getActionNode( a );
					if( an != null ) {
						QU[i].add( an.q( 0 ) );
					}
					i += 1;
				}
			}
			for( final PrimitiveStateNode v : V ) {
				int i = 0;
				for( final JointAction<A> a : relevant_actions ) {
					final ActionNode<S, A> an = v.getActionNode( a );
					if( an != null ) {
						QV[i].add( an.q( 0 ) );
					}
					i += 1;
				}
			}
			
			final double[] qu = new double[relevant_actions.size()];
			final double[] qv = new double[relevant_actions.size()];
			for( int i = 0; i < relevant_actions.size(); ++i ) {
				qu[i] = QU[i].mean();
				qv[i] = QV[i].mean();
			}
			
			final int ustar = Fn.argmax( qu );
			final int vstar = Fn.argmax( qv );
			
			if( ustar != vstar ) {
				// D = maximum value discrepancy caused by aggregation
				final double du = qu[ustar] - qu[vstar];
				final double dv = qv[vstar] - qv[ustar];
				final double D = Math.max( du, dv );
				if( D < Delta ) {
					System.out.println( "! D (" + D + ") < Delta (" + Delta + ")" );
					return 0;
				}
//				return D;
//				return D*R;
				return D + size_regularization*R;
			}
			else {
				return 0;
			}
		}

		@Override
		public int n()
		{ return n_; }
		
		@Override
		public double[] r()
		{ return rv_.mean(); }
		
		@Override
		public double r( final int i )
		{ return rv_.mean()[i]; }
		
		@Override
		public double[] rvar()
		{ return rv_.variance(); }
		
		@Override
		public double rvar( final int i )
		{ return rv_.variance()[i]; }
		
		public void updateR( final double[] r )
		{
			assert( r.length == rv_.Ndim );
			rv_.add( r );
		}
		
		@Override
		public double[] q()
		{ return qv_.mean(); }
		
		@Override
		public double q( final int i )
		{ return qv_.mean()[i]; }
		
		@Override
		public double[] qvar()
		{ return qv_.variance(); }
		
		@Override
		public double qvar( final int i )
		{ return qv_.variance()[i]; }
		
		public void updateQ( final double[] q )
		{
			assert( q.length == qv_.Ndim );
			qv_.add( q );
		}

		/**
		 * Corrects node statistics after splitting a child node. The idea is
		 * that if the node was split, then the old value of this action is
		 * too low because not splitting the child states led to an
		 * under-estimate. This function changes this node's statistics to
		 * include only the max Q values of successor states. This will be
		 * an optimistic estimate, and combined with the reduction in sample
		 * count for this node will result in additional exploration.
		 * @param path
		 */
		public void correctForNewSplit(
			final ArrayList<Pair<AggregateActionNode, AggregateStateNode>> path )
		{
//			System.out.println( "!!!!! Old Q value for " + a() + ": " + qv_ );
			
			qv_ = new VectorMeanVarianceAccumulator( nagents );
			rv_ = new VectorMeanVarianceAccumulator( nagents );
			
			int n = 0;
			for( final StateNode<S, A> s : Fn.in( successors() ) ) {
				final ActionNode<S, A> astar = BackupRules.MaxAction( s );
				if( astar == null ) {
					continue;
				}
				n += astar.n();
				qv_.add( astar.q(), astar.n() );
			}
			
			rv_.add( new double[nagents], n );
			
//			n_ = n;
			
			// FIXME: Single agent only
			final double q = qv_.mean()[0];
			
			for( int i = path.size() - 1; i >= 0; --i ) {
				final Pair<AggregateActionNode, AggregateStateNode> t = path.get( i );
				final AggregateActionNode an = t.first;
				final AggregateStateNode sn = t.second;
				
				final double[] maxq = Fn.copy( BackupRules.MaxQ( sn ) );
				sn.setV( maxq );
				
				final double[] qa = new double[nagents];
				for( final AggregateStateNode succ : Fn.in( an.successors() ) ) {
					final double p = succ.n() / ((double) an.n());
					final double[] vi = succ.v();
					Fn.vplus_ax_inplace( qa, p, vi );
				}
//				System.out.println( "!!! Setting q = " + Arrays.toString( qa ) + " " + an );
				an.setQ( qa, an.n() );
				// TODO: ActionNode R is never non-zero in the current implementation,
				// so I'm not bothering to compute it here.
				an.setR( new double[nagents], an.n() );
			}
			
//			System.out.println( "!!!!! New Q value for " + a() + ": " + qv_ );
		}
	}
	
	private abstract class SplitNode
	{
		public abstract DataNode child( final double[] phi );
		public abstract Generator<? extends DataNode> children();
	}
	
	private class DataNode
	{
		public SplitNode split = null;
		public AggregateStateNode aggregate = null;
	}
	
	private class BinarySplitNode extends SplitNode
	{
		public final int attribute;
		public final double threshold;
		
		public DataNode left = null;
		public DataNode right = null;
		
		public BinarySplitNode( final int attribute, final double threshold )
		{
			this.attribute = attribute;
			this.threshold = threshold;
		}
		
		@Override
		public DataNode child( final double[] phi )
		{
			if( phi[attribute] < threshold ) {
				return left;
			}
			else {
				return right;
			}
		}

		@Override
		public Generator<? extends DataNode> children()
		{
			return new Generator<DataNode>() {
				int i = 0;
				
				@Override
				public boolean hasNext()
				{ return i < 2; }

				@Override
				public DataNode next()
				{
					switch( i++ ) {
					case 0: return left;
					case 1: return right;
					default: throw new IllegalStateException( "hasNext() == false" );
					}
				}
			};
		}
	}
	
	// -----------------------------------------------------------------------
	
	private final ResetSimulator<S, A> sim_;
	private final FactoredRepresenter<S, ? extends FactoredRepresentation<S>> repr_;
	private final ActionGenerator<S, JointAction<A>> actions_;
	private final MctsVisitor<S, A> visitor_;
	private final double c_;
	private final int episode_limit_;
	private final EvaluationFunction<S, A> eval_;
	
	// TODO: Should be parameters
	private final double discount = 1.0;
	/** Minimum # of samples before considering a split. */
	private final int split_threshold;
	/** Split threshold multiplier applied after a rejected split. */
	private final double backoff = 2.0;
	/** Split based on actions with # of trials in the top quantile. */
//	private final double action_quantile = 1.0;
	private final int top_actions;
	/** Regularization factor; larger value encourages similar cluster sizes. */
	private final double size_regularization; // = 0; //1000.0; //10000.0;
	
	// FIXME: Make this a parameter!
	private final double Delta = 0.1;
	
	// TODO: Make this a parameter
	private final boolean max_uct = false;
	
	private boolean complete_ = false;
	
	private final PrimitiveActionNode Ptree_root_action_;
	private final AggregateActionNode Atree_root_action_;
	
	private int action_visits_ = 0;
	private int max_action_visits_ = Integer.MAX_VALUE;
	
	private int max_depth_ = Integer.MAX_VALUE;
	
	private int Nsplits_accepted = 0;
	
	public UTreeSearch( final ResetSimulator<S, A> sim,
					  final FactoredRepresenter<S, ? extends FactoredRepresentation<S>> repr,
					  final ActionGenerator<S, JointAction<A>> actions,
					  final double c, final int episode_limit,
					  final EvaluationFunction<S, A> eval,
					  final MctsVisitor<S, A> visitor,
					  final int split_threshold, final int top_actions,
					  final double size_regularization )
	{
		sim_ = sim;
		repr_ = repr;
		actions_ = actions;
		c_ = c;
		episode_limit_ = episode_limit;
		eval_ = eval;
		visitor_ = visitor;
		
		this.split_threshold = split_threshold;
		this.top_actions = top_actions;
		this.size_regularization = size_regularization;
		
		Ptree_root_action_ = new PrimitiveActionNode( null, 0, repr.create() );
		Atree_root_action_ = new AggregateActionNode( null, 0, repr.create() );
	}
	
	public int getMaxActionVisits()
	{ return max_action_visits_; }
	
	public void setMaxActionVisits( final int max )
	{ max_action_visits_ = max; }
	
	public int getMaxDepth()
	{ return max_depth_; }
	
	public void setMaxDepth( final int max )
	{ max_depth_ = max; }
	
	public int Nsplits_accepted()
	{
		return Nsplits_accepted;
	}
	
	@Override
	public void run()
	{
		int episode_count = 0;
		while( episode_count++ < episode_limit_ && action_visits_ < max_action_visits_ ) {
			visit( Ptree_root_action_, Atree_root_action_, 0, visitor_,
				   new ArrayList<Pair<AggregateActionNode, AggregateStateNode>>() );
			sim_.reset();
			
//			System.out.println( "****************************************" );
//			System.out.println( "****************************************" );
//			root().accept( new TreePrinter<S, A>() );
//			System.out.println( "----------------------------------------" );
//			Ptree_root_action_.successors().next().accept( new TreePrinter<S, A>() );
//			System.out.println( "****************************************" );
		}
		
		complete_ = true;
	}
	
	private double[] visit(
		final PrimitiveActionNode P_an, final AggregateActionNode A_an,
		final int depth, final MctsVisitor<S, A> visitor,
		final ArrayList<Pair<AggregateActionNode, AggregateStateNode>> path )
	{
		final S s = sim_.state();
		final int[] turn = sim_.turn();
		final int nagents = sim_.nagents();
		
		if( P_an == Ptree_root_action_ ) {
			visitor.startEpisode( s, nagents, turn );
		}
		else {
			action_visits_ += 1;
			visitor.treeAction( P_an.a(), s, turn );
		}

		final PrimitiveStateNode P_sn = (PrimitiveStateNode) P_an.successor( s, nagents, turn, actions_.create() );
		P_sn.visit();
		final AggregateStateNode A_sn = A_an.successor( s, P_sn );
		if( A_an.consumeNewSplitFlag() ) {
//			System.out.println( "****************************************" );
//			root().accept( new TreePrinter<S, A>( 8 ) );
//			System.out.println( "----------------------------------------" );
			
			// A split was actually added. The value estimate for A_an is likely
			// to be an under-estimate. We need to increase it to make sure
			// that A_an is adequately explored after the split.
			A_an.correctForNewSplit( path );
//			System.out.println( "!!!!! There was a split" );
			
//			System.out.println( "****************************************" );
//			root().accept( new TreePrinter<S, A>( 8 ) );
//			System.out.println( "----------------------------------------" );
			
//			System.exit( 0 );
		}
		A_sn.visit();
		path.add( Pair.makePair( A_an, A_sn ) );
		
		final double[] r = sim_.reward();
		P_sn.updateR( r );
		
		if( s.isTerminal() ) {
			assert( P_sn.isTerminal() );
			return r;
		}
		// If we've reached the fringe of the tree, use the evaluation function
		else if( A_sn.n() == 1 || depth == max_depth_ ) {
			assert( !P_sn.isTerminal() );
			final double[] v = eval_.evaluate( sim_ );
			visitor.checkpoint();
			return v;
		}
		else {
			assert( !P_sn.isTerminal() );
			// Sample below 'sn'
			final AggregateActionNode A_sa = selectAction( A_sn, s, sim_.t(), turn );
			A_sa.visit();
			final PrimitiveActionNode P_sa = (PrimitiveActionNode) P_sn.successor( A_sa.a(), nagents, repr_.create() );
			P_sa.visit();
			
			// FIXME: Our current implementation assumes that rewards correspond
			// to states, but not to actions. The following line has to be
			// called so that the appropriate number of updates to R are made.
			// Ideally, we should define a different semantics for ActionNode.r(),
			// perhaps so that it holds R(s, a) (but not R(s)).
			final double[] zero = new double[nagents];
			P_sa.updateR( zero );
			A_sa.updateR( zero );
			
			sim_.takeAction( A_sa.a().create() );
			final double[] z = visit( P_sa, A_sa, depth + 1, visitor, path );
			P_sa.updateQ( z );
			A_sa.updateQ( z );
			
			final double[] maxq = Fn.copy( BackupRules.MaxQ( A_sn ) );
			Fn.scalar_multiply_inplace( maxq, discount );
			final double[] rr  = Fn.copy( r );
			Fn.vplus_inplace( rr, maxq );
			A_sn.setV( rr );
			
			if( max_uct ) {
				return rr;
			}
			else {
				Fn.scalar_multiply_inplace( z, discount );
				Fn.vplus_inplace( r, z );
				return r;
			}
		}
	}
	
	private int singleAgent( final int[] turn )
	{
		if( turn.length == 1 ) {
			return turn[0];
		}
		else {
			System.out.println( "! turn.length = " + turn.length );
			throw new IllegalStateException( "Not designed for simultaneous moves right now!" );
		}
	}
	
	/**
	 * Chooses an action within the tree according to the UCB rule.
	 * @param sn
	 * @param s
	 * @param t
	 * @param turn
	 * @return
	 */
	private AggregateActionNode selectAction( final AggregateStateNode sn, final S s,
													 final long t, final int[] turn )
	{
		double max_value = -Double.MAX_VALUE;
		AggregateActionNode max_an = null;
		sn.actions.setState( s, t, turn );
		while( sn.actions.hasNext() ) {
			final JointAction<A> a = sn.actions.next();
			final AggregateActionNode an = sn.successor( a, sim_.nagents(), repr_.create() );
			if( an.n() == 0 ) {
				return an;
			}
			else {
				final double exploit = an.q( singleAgent( turn ) );
				final double explore = c_ * Math.sqrt( Math.log( sn.n() ) / an.n() );
				final double v = explore + exploit;
				if( v > max_value ) {
					max_an = an;
					max_value = v;
				}
			}
		}
		assert( max_an != null );
		return max_an;
	}
	
	public boolean isComplete()
	{ return complete_; }

	@Override
	public StateNode<S, A> root()
	{
		final Generator<? extends StateNode<S, A>> g = Atree_root_action_.successors();
		final StateNode<S, A> root = g.next();
		assert( !g.hasNext() );
		return root;
	}
	
	// -----------------------------------------------------------------------
	
	public static EvaluationFunction<ChainWalk.State, ChainWalk.Action>
	getChainWalkEvaluator( final RandomGenerator rng, final ChainWalk.ActionGen action_gen )
	{
		final int rollout_width = 1;
		final int rollout_depth = Integer.MAX_VALUE;
		final Policy<ChainWalk.State, JointAction<ChainWalk.Action>> rollout_policy
			= new RandomPolicy<ChainWalk.State, JointAction<ChainWalk.Action>>(
				0 /*Player*/, rng.nextInt(),
				SingleAgentJointActionGenerator.create( action_gen ) );
		final EvaluationFunction<ChainWalk.State, ChainWalk.Action> heuristic
			= new EvaluationFunction<ChainWalk.State, ChainWalk.Action>() {
			@Override
			public double[] evaluate( final Simulator<ChainWalk.State, ChainWalk.Action> sim )
			{
				return new double[] { 0.0 };
			}
		};
		final double discount = 1.0;
		final EvaluationFunction<ChainWalk.State, ChainWalk.Action> rollout_evaluator
			= RolloutEvaluator.create( rollout_policy, discount,
									   rollout_width, rollout_depth, heuristic );
		return rollout_evaluator;
	}
	
	public static EvaluationFunction<FuelWorldState, FuelWorldAction>
	getFuelWorldEvaluator( final RandomGenerator rng, final FuelWorldActionGenerator action_gen )
	{
		final int rollout_width = 1;
		final int rollout_depth = Integer.MAX_VALUE;
		final Policy<FuelWorldState, JointAction<FuelWorldAction>> rollout_policy
			= new RandomPolicy<FuelWorldState, JointAction<FuelWorldAction>>(
				0 /*Player*/, rng.nextInt(),
				SingleAgentJointActionGenerator.create( action_gen ) );
		final EvaluationFunction<FuelWorldState, FuelWorldAction> heuristic
			= new EvaluationFunction<FuelWorldState, FuelWorldAction>() {
			@Override
			public double[] evaluate( final Simulator<FuelWorldState, FuelWorldAction> sim )
			{
				return new double[] { 0.0 };
			}
		};
		final double discount = 1.0;
		final EvaluationFunction<FuelWorldState, FuelWorldAction> rollout_evaluator
			= RolloutEvaluator.create( rollout_policy, discount,
									   rollout_width, rollout_depth, heuristic );
		return rollout_evaluator;
	}
	
	public static EvaluationFunction<CliffWorld.State, CliffWorld.Action>
	getEvaluator( final RandomGenerator rng, final CliffWorld.Actions action_gen )
	{
		final int rollout_width = 1;
		final int rollout_depth = 0; //Integer.MAX_VALUE;
		final Policy<CliffWorld.State, JointAction<CliffWorld.Action>> rollout_policy
			= new RandomPolicy<CliffWorld.State, JointAction<CliffWorld.Action>>(
				0 /*Player*/, rng.nextInt(),
				SingleAgentJointActionGenerator.create( action_gen ) );
		final EvaluationFunction<CliffWorld.State, CliffWorld.Action> heuristic
			= new EvaluationFunction<CliffWorld.State, CliffWorld.Action>() {
			@Override
			public double[] evaluate( final Simulator<CliffWorld.State, CliffWorld.Action> sim )
			{
				final double d;
				if( sim.state().path == Path.Road ) {
					d = -(sim.state().L * 3 - sim.state().location);
				}
				else {
					d = -(sim.state().L - sim.state().location);
				}
				return new double[] { d };
			}
		};
		final double discount = 1.0;
		final EvaluationFunction<CliffWorld.State, CliffWorld.Action> rollout_evaluator
			= RolloutEvaluator.create( rollout_policy, discount,
									   rollout_width, rollout_depth, heuristic );
		return rollout_evaluator;
	}
	
	public static EvaluationFunction<TaxiState, TaxiAction>
	getEvaluator( final RandomGenerator rng, final TaxiActionGenerator action_gen )
	{
		return new TaxiRolloutEvaluator( 1, 0, action_gen, rng, 1.0 );
	}
	
	public static void main( final String[] argv ) throws FileNotFoundException
	{
		System.setOut( new PrintStream( new FileOutputStream( new File( "UTreeSearch.log" ) ) ) );
		
		final MersenneTwister rng = new MersenneTwister( 44 );
		final double c = 100.0;
		final int Ngames = 1;
		final int Nepisodes = 2<<11;
		final int split_threshold = 2<<9;
		final int top_actions = 2;
		final double size_regularization = 1;
		
//		final ChainWalk cw = new ChainWalk( 2, 0.2, 10 );
//		final ChainWalk.Simulator sim = cw.new Simulator( cw.new State() );
//		final ChainWalk.ActionGen action_gen = cw.new ActionGen( rng );
//		final UTreeSearch<ChainWalk.State, ChainWalk.Action> ut
//			= new UTreeSearch<ChainWalk.State, ChainWalk.Action>(
//				ResetAdapter.of( sim ), new ChainWalk.IdentityRepresenter(),
//				SingleAgentJointActionGenerator.create( action_gen ), c, Nepisodes,
//				getChainWalkEvaluator( rng, action_gen.create() ),
//				new DefaultMctsVisitor<ChainWalk.State, ChainWalk.Action>() );
//		ut.run();
		
//		for( int i = 0; i < Ngames; ++i ) {
//			final FuelWorldState fw = FuelWorldState.createDefaultWithChoices( rng );
//			final FuelWorldSimulator sim = new FuelWorldSimulator( fw );
//			final FuelWorldActionGenerator action_gen = new FuelWorldActionGenerator();
//
//			while( !fw.isTerminal() ) {
//				final UTreeSearch<FuelWorldState, FuelWorldAction> ut
//					= new UTreeSearch<FuelWorldState, FuelWorldAction>(
//						ResetAdapter.of( sim ), new PrimitiveFuelWorldRepresenter(),
//						SingleAgentJointActionGenerator.create( action_gen ), c, Nepisodes,
//						getFuelWorldEvaluator( rng, action_gen.create() ),
//						new DefaultMctsVisitor<FuelWorldState, FuelWorldAction>(),
//						split_threshold, top_actions, size_regularization );
//
//				ut.run();
//
//				final JointAction<FuelWorldAction> astar = BackupRules.MaxAction( ut.root() ).a();
//				System.out.println( astar );
//				System.out.println( "********************" );
//				sim.takeAction( astar );
//
//				break;
//			}
//		}
		
//		final int L = 3;
//		final int W = 4;
//		final int F = 2;
//		for( int i = 0; i < Ngames; ++i ) {
//			final CliffWorld.State s = new CliffWorld.State( L, W, F );
//			final CliffWorld.Simulator sim = new CliffWorld.Simulator( s, rng );
//			final CliffWorld.Actions actions = new CliffWorld.Actions( rng );
//
//			int t = 0;
//			while( !s.isTerminal() ) {
//				final UTreeSearch<CliffWorld.State, CliffWorld.Action> ut
//					= new UTreeSearch<CliffWorld.State, CliffWorld.Action>(
//						ResetAdapter.of( sim ), new CliffWorld.PrimitiveRepresenter(),
//						SingleAgentJointActionGenerator.create( actions ), c, Nepisodes,
//						getEvaluator( rng, actions.create() ),
//						new DefaultMctsVisitor<CliffWorld.State, CliffWorld.Action>(),
//						split_threshold, top_actions, size_regularization );
//
//				ut.run();
//
//
//				final JointAction<CliffWorld.Action> astar = BackupRules.MaxAction( ut.root() ).a();
//				System.out.println( astar );
//				System.out.println( "********************" );
//				sim.takeAction( astar );
//
//				System.out.println( "****************************************" );
//				System.out.println( "** Final tree **************************" );
//				ut.root().accept( new TreePrinter<CliffWorld.State, CliffWorld.Action>( 8 ) );
//				System.out.println( "----------------------------------------" );
//
//				if( t++ == 1 ) {
//					break;
//				}
//			}
//		}
		
		final int Nother_taxis = 1;
		final double slip = 0.2;
		for( int i = 0; i < Ngames; ++i ) {
			final TaxiState s = TaxiWorlds.dietterich2000( rng, Nother_taxis, slip );
			final TaxiSimulator sim = new TaxiSimulator( rng, s, slip, 40 );
			final TaxiActionGenerator actions = new TaxiActionGenerator();

			int t = 0;
			while( !s.isTerminal() ) {
				final UTreeSearch<TaxiState, TaxiAction> ut
					= new UTreeSearch<TaxiState, TaxiAction>(
						ResetAdapter.of( sim ), new PrimitiveTaxiRepresenter( s ),
						SingleAgentJointActionGenerator.create( actions ), c, Nepisodes,
						getEvaluator( rng, actions.create() ),
						new DefaultMctsVisitor<TaxiState, TaxiAction>(),
						split_threshold, top_actions, size_regularization );

				ut.run();

				
				final JointAction<TaxiAction> astar = BackupRules.MaxAction( ut.root() ).a();
				System.out.println( astar );
				System.out.println( "********************" );
				sim.takeAction( astar );
			
				System.out.println( "****************************************" );
				System.out.println( "** Final tree **************************" );
				ut.root().accept( new TreePrinter<TaxiState, TaxiAction>( 8 ) );
				System.out.println( "----------------------------------------" );
				
				if( t++ == 10 ) {
					break;
				}
			}
		}
	}
}
