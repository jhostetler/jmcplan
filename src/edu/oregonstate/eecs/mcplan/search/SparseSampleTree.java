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

import java.util.Map;

import org.apache.commons.math3.random.MersenneTwister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.RandomPolicy;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.domains.toy.Irrelevance.ActionGen;
import edu.oregonstate.eecs.mcplan.domains.toy.Irrelevance.IdentityRepresenter;
import edu.oregonstate.eecs.mcplan.domains.toy.Irrelevance.Simulator;
import edu.oregonstate.eecs.mcplan.domains.toy.Irrelevance.State;
import edu.oregonstate.eecs.mcplan.domains.toy.Irrelevance.Visitor;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.Countdown;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public abstract class SparseSampleTree<S, X extends Representation<S>, A extends VirtualConstructor<A>>
	implements Runnable, GameTree<X, A>
{
	private static final Logger log = LoggerFactory.getLogger( SparseSampleTree.class );
	
	public static final class Factory<S, X extends Representation<S>, A extends VirtualConstructor<A>>
		implements GameTreeFactory<S, X, A>
	{
		private final UndoSimulator<S, A> sim_;
		private final Representer<S, X> repr_;
		private final ActionGenerator<S, JointAction<A>> actions_;
		private final int width_;
		private final int depth_;
		private final Policy<S, JointAction<A>> rollout_policy_;
		private final int rollout_width_;
		private final int rollout_depth_;
		private final BackupRule<X, A> backup_;
		
		public Factory( final UndoSimulator<S, A> sim,
						  final Representer<S, X> repr,
						  final ActionGenerator<S, JointAction<A>> actions,
						  final int width, final int depth,
						  final Policy<S, JointAction<A>> rollout_policy,
						  final int rollout_width, final int rollout_depth,
						  final BackupRule<X, A> backup )
		{
			sim_ = sim;
			repr_ = repr;
			actions_ = actions;
			width_ = width;
			depth_ = depth;
			rollout_policy_ = rollout_policy;
			rollout_width_ = rollout_width;
			rollout_depth_ = rollout_depth;
			backup_ = backup;
		}
		
		@Override
		public GameTree<X, A> create( final MctsVisitor<S, X, A> visitor )
		{
			return new SparseSampleTree<S, X, A>( sim_, repr_.create(), actions_, width_, depth_,
												  rollout_policy_, rollout_width_, rollout_depth_, visitor ) {
				@Override
				protected MutableStateNode<S, X, A> createStateNode(
					final MutableActionNode<S, X, A> an, final X x, final int nagents, final int[] turn )
				{
					return new DelegateStateNode<S, X, A>( backup_, x, nagents, turn );
				}
				
				@Override
				protected MutableStateNode<S, X, A> fetchStateNode(
					final MutableActionNode<S, X, A> an, final X x, final int nagents, final int[] turn )
				{
					for( final MutableStateNode<S, X, A> agg : Fn.in( an.successors() ) ) {
						if( agg.token.equals( x ) ) {
							return agg;
						}
					}
					return null;
				}
			};
		}
	}
	
	// -----------------------------------------------------------------------
	
	protected final UndoSimulator<S, A> sim_;
	protected final Representer<S, X> repr_;
	protected final ActionGenerator<S, JointAction<A>> actions_;
	protected final MctsVisitor<S, X, A> visitor_;
	protected final int width_;
	protected final int depth_;
	protected final Policy<S, JointAction<A>> rollout_policy_;
	protected final int rollout_width_;
	protected final int rollout_depth_;
	
	// TODO: Should be a parameter
	protected final double discount_ = 1.0;
	
	private boolean complete_ = false;
	private StateNode<X, A> root_ = null;
	private final Map<A, Double> qtable_ = null;
	
	public SparseSampleTree( final UndoSimulator<S, A> sim,
					  final Representer<S, X> repr,
					  final ActionGenerator<S, JointAction<A>> actions,
					  final int width, final int depth,
					  final Policy<S, JointAction<A>> rollout_policy,
					  final int rollout_width, final int rollout_depth,
					  final MctsVisitor<S, X, A> visitor )
	{
		sim_ = sim;
		repr_ = repr;
		actions_ = actions;
		width_ = width;
		depth_ = depth;
		rollout_policy_ = rollout_policy;
		rollout_width_ = rollout_width;
		rollout_depth_ = rollout_depth;
		visitor_ = visitor;
	}
	
	protected abstract MutableStateNode<S, X, A> createStateNode(
		final MutableActionNode<S, X, A> an, final X x, final int nagents, final int[] turn );
	
	protected abstract MutableStateNode<S, X, A> fetchStateNode(
		final MutableActionNode<S, X, A> an, final X x, final int nagents, final int[] turn );

	@Override
	public StateNode<X, A> root()
	{
		return root_;
	}

	@Override
	public void run()
	{
		root_ = createSubtree( null, depth_, visitor_ );
//		qtable_ = makeQTable( root_ );
		complete_ = true;
	}
	
	private MutableActionNode<S, X, A> rollout(
		final MutableStateNode<S, X, A> sn, final int depth, final MctsVisitor<S, X, A> visitor )
	{
//		System.out.println( "rollout()" );
		final int nagents = sim_.nagents();
		final MutableActionNode<S, X, A> an = new MutableActionNode<S, X, A>( null, nagents, repr_.create() );
		for( int w = 0; w < rollout_width_; ++w ) {
			an.visit();
			int count = 0;
			final double[] q = Fn.repeat( 0.0, nagents );
			double running_discount = 1.0;
			while( true ) {
				final S s = sim_.state();
				final int[] turn = sim_.turn();
				final X x = an.repr().encode( s ); //repr_.encode( s );
				running_discount *= discount_;
				if( sim_.isTerminalState() ) {
					final double[] r = visitor.terminal( s, turn );
					touchLeafNode( r, an, x, turn );
					Fn.scalar_multiply_inplace( r, running_discount );
					Fn.vplus_inplace( q, r );
					break;
				}
				else if( depth == 0 ) {
					// TODO: We should have a way of giving e.g. an "optimistic"
					// reward (Vmax) here. Like a 'getDefaultReward( double r )' method.
					final double[] r = sim_.reward();
					touchLeafNode( r, an, x, turn );
					Fn.scalar_multiply_inplace( r, running_discount );
					Fn.vplus_inplace( q, r );
					break;
				}
				else {
					// TODO: Should rollout_policy be a policy over X's? Current
					// approach (Policy<S, A>) is more flexible since the policy
					// can use a different representation internally.
					final double[] r = sim_.reward();
					final Policy<S, JointAction<A>> pi = rollout_policy_;
					pi.setState( sim_.state(), sim_.t() );
					final JointAction<A> a = pi.getAction();
					sim_.takeAction( a );
					count += 1;
					final S sprime = sim_.state();
					visitor.defaultAction( a, sprime, sim_.turn() );
					pi.actionResult( sprime, r );
					Fn.scalar_multiply_inplace( r, running_discount );
					Fn.vplus_inplace( q, r );
				}
			}
//			System.out.println( "\tterminated at depth " + count );
			for( int i = 0; i < count; ++i ) {
				sim_.untakeLastAction();
			}
			an.updateQ( q );
		}
		return an;
	}
	
	/**
	 * To achieve our goal of having StateNodes with different backup rules
	 * be different types, we need to implement the recursive tree-building
	 * function somewhat "backwards". Instead of creating a new node and then
	 * visiting it recursively, we instead visit the last *ActionNode*, and
	 * create the appropriate StateNode subtype under it. To create the
	 * root node, call createSubtree() with 'an' = null.
	 * @param an
	 * @param depth
	 * @param visitor
	 * @return
	 */
	private MutableStateNode<S, X, A> createSubtree(
		final MutableActionNode<S, X, A> an, final int depth, final MctsVisitor<S, X, A> visitor )
	{
		final S s = sim_.state();
		final int[] turn = sim_.turn();
		final int nagents = sim_.nagents();
		
		final X x;
		if( an != null ) {
			visitor.treeAction( an.a(), s, turn );
			x = an.repr().encode( s );
		}
		else {
			visitor.startEpisode( s, nagents, turn );
			x = repr_.encode( s );
		}
		
		if( sim_.isTerminalState() ) {
			final MutableStateNode<S, X, A> sn = touchLeafNode( visitor.terminal( s, turn ), an, x, turn );
			return sn;
		}
		else if( visitor.halt() ) {
			final MutableStateNode<S, X, A> sn = touchLeafNode( sim_.reward(), an, x, turn );
			return sn;
		}
		else if( depth == 0 ) {
			final MutableStateNode<S, X, A> sn = touchRolloutNode( an, x, turn );
			final MutableActionNode<S, X, A> an_prime = rollout( sn, rollout_depth_, visitor );
			sn.attachSuccessor( an_prime.a(), an_prime );
			visitor.checkpoint();
			return sn;
		}
		else {
			final MutableStateNode<S, X, A> sn = touchStateNode( an, x, turn );
			// Sample below 'sn' only if this is the first visit
			if( sn.n() == 1 ) {
				final ActionGenerator<S, JointAction<A>> action_gen = actions_.create();
				action_gen.setState( s, sim_.t(), turn );
				while( action_gen.hasNext() ) {
					final JointAction<A> a = action_gen.next();
					final MutableActionNode<S, X, A> sa = requireActionNode( sn, a );
					System.out.println( "depth " + depth + ", action " + a );
					for( int w = 0; w < width_; ++w ) {
						sa.visit();
						sim_.takeAction( sa.a().create() );
						final double[] r = sim_.reward();
						final MutableStateNode<S, X, A> snprime = createSubtree( sa, depth - 1, visitor );
						sa.updateQ( snprime.v() );
						sa.updateR( r );
						sim_.untakeLastAction();
					}
				}
			}
			else {
//				System.out.println( "! Aggregator hit!" );
			}
			return sn;
		}
	}
	
	/*
	private double[] visit( final ActionNode<X, A> an, final X x, final int turn, final MctsVisitor<S, A> visitor )
	{
		final S s = sim_.state();
		sn.visit();
		if( sim_.isTerminalState() ) {
//			System.out.println( "***** terminal" );
			// Note: This is not a backed-up value
			return visitor.terminal( s, sim_.getTurn() );
		}
		else if( depth == 0 ) {
			// Sparse sampling depth exceeded
			// TODO: Return state reward?
//			return new double[sim_.getNumAgents()];
			System.out.println( "***** Starting rollout()" );
			final StateNode<X, A> sn = requireRolloutNode( an, x, turn );
			return rollout( sn, rollout_depth_, visitor );
		}
		else {
			final int turn = sim_.getTurn();
//			assert( sn.turn == turn );
			final ActionGenerator<S, ? extends A> action_gen = actions_.create();
			action_gen.setState( s, sim_.t(), turn );
			
			while( action_gen.hasNext() ) {
				final A a = action_gen.next();
				final ActionNode<X, A> sa = requireActionNode( sn, a ); //sn.action( a );
				System.out.println( "depth " + depth + ", action " + a );
				for( int w = 0; w < width_; ++w ) {
					sa.visit();
					sim_.takeAction( sa.a );
					final double[] r = sim_.getReward();
					final S sprime = sim_.state();
					visitor.treeAction( sa.a, sprime, sim_.getTurn() );
					final X x = repr_.encode( sprime );
					final StateNode<X, A> snprime = requireStateNode( sa, x, sim_.getTurn() ); //sa.stateNode( x, sim_.getTurn() );
					final double[] qi = visit( snprime, depth - 1, visitor );
					sa.updateQ( qi );
					sa.updateR( r );
					sim_.untakeLastAction();
				}
			}
			return sn.v();
//			return backup( sn );
		}
	}
	*/
	
	private MutableStateNode<S, X, A> touchStateNode( final MutableActionNode<S, X, A> an, final X x, final int[] turn )
	{
		MutableStateNode<S, X, A> sn = null;
		if( an == null ) {
			sn = createStateNode( an, x, sim_.nagents(), turn );
		}
		else {
//			sn = an.getStateNode( x, turn );
			sn = fetchStateNode( an, x, sim_.nagents(), turn );
			if( sn == null ) {
				sn = createStateNode( an, x, sim_.nagents(), turn ); //new StateNode<X, A>( x, sim_.getNumAgents(), turn );
				an.attachSuccessor( x, turn, sn );
			}
		}
		sn.visit();
		return sn;
	}
	
	private MutableStateNode<S, X, A> touchRolloutNode( final MutableActionNode<S, X, A> an, final X x, final int[] turn )
	{
		MutableStateNode<S, X, A> sn = null;
		if( an == null ) {
			sn = new MeanStateNode<S, X, A>( x, sim_.nagents(), turn );
		}
		else {
			sn = an.getStateNode( x, turn );
			if( sn == null ) {
				sn = new MeanStateNode<S, X, A>( x, sim_.nagents(), turn );
				an.attachSuccessor( x, turn, sn );
			}
		}
		sn.visit();
		return sn;
	}
	
	private MutableStateNode<S, X, A> touchLeafNode(
		final double[] v, final MutableActionNode<S, X, A> an, final X x, final int[] turn )
	{
		MutableStateNode<S, X, A> sn = null;
		if( an == null ) {
			sn = new LeafStateNode<S, X, A>( v, x, sim_.nagents(), turn );
		}
		else {
			sn = an.getStateNode( x, turn );
			if( sn == null ) {
				sn = new LeafStateNode<S, X, A>( v, x, sim_.nagents(), turn );
				an.attachSuccessor( x, turn, sn );
			}
		}
		sn.visit();
		return sn;
	}
	
	private MutableActionNode<S, X, A> requireActionNode( final MutableStateNode<S, X, A> sn, final JointAction<A> a )
	{
		MutableActionNode<S, X, A> an = sn.getActionNode( a );
		if( an == null ) {
			an = new MutableActionNode<S, X, A>( a, sim_.nagents(), repr_.create() ); //new StateNode<X, A>( x, sim_.getNumAgents(), turn );
			sn.attachSuccessor( a, an );
		}
		return an;
	}
	
//	@Override
//	public abstract double[] backup( final StateNode<X, A> s );
	
	/**
	 * Computes the backed-up reward during the rollout stage. This is
	 * usually a discounted value, possibly undiscounted.
	 * @param r
	 * @return
	 */
//	public abstract double[] backupRollout( final double[] r );
	
//	private Map<A, Double> makeQTable( final StateNode<X, A> root )
//	{
//		final Map<A, Double> q = new HashMap<A, Double>();
//		for( final ActionNode<X, A> an : Fn.in( root.successors() ) ) {
//			// TODO: This is where representing rollouts as ActionNodes with
//			// a = null is problematic. We could adopt the convention that
//			// ActionNodes with a = null are never counted, but then we need
//			// null tests everywhere.
//			if( an.a() != null ) {
//				q.put( an.a().create(), an.q( root.turn ) );
//			}
//		}
//		return q;
//	}
	
	public static void main( final String[] args )
	{
		final MersenneTwister rng = new MersenneTwister( 42 );
		final Simulator sim = new Simulator();
		final int width = 40;
		final int depth = 1;
		final Policy<State, UndoableAction<State>> rollout_policy
			= new RandomPolicy<State, UndoableAction<State>>( 0, rng.nextInt(), new ActionGen( rng ) );
		final int rollout_depth = -1;
		final int rollout_width = 10;
		
		final SparseSampleTree<State, Representation<State>, UndoableAction<State>> tree
			= new SparseSampleTree<State, Representation<State>, UndoableAction<State>>(
				sim, new IdentityRepresenter(), new ActionGen( rng ), width, depth,
				rollout_policy, rollout_width, rollout_depth,
				TimeLimitMctsVisitor.create( new Visitor<UndoableAction<State>>(), new Countdown( 1000 ) ) )
			{
				@Override
				protected MutableStateNode<State, Representation<State>, UndoableAction<State>> createStateNode(
						final MutableActionNode<State, Representation<State>, UndoableAction<State>> an,
						final Representation<State> x, final int nagents, final int turn )
				{
					return new MaxStateNode<State, Representation<State>, UndoableAction<State>>( 0, x, nagents, turn );
				}

				@Override
				protected MutableStateNode<State, Representation<State>, UndoableAction<State>> fetchStateNode(
						final MutableActionNode<State, Representation<State>, UndoableAction<State>> an,
						final Representation<State> x, final int nagents, final int turn )
				{
					// TODO Auto-generated method stub
					return null;
				}
			};
		tree.run();
		tree.root().accept( new TreePrinter<Representation<State>, UndoableAction<State>>() );
	}

}
