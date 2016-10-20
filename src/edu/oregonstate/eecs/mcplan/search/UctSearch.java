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

import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.sim.ResetAdapter;
import edu.oregonstate.eecs.mcplan.sim.ResetSimulator;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.Generator;


/**
 * UCT search for multiagent, general-reward domains.
 *
 * @param <S> State type
 * @param <X> State representation type
 * @param <A> Action type
 */
public class UctSearch<S extends State, A extends VirtualConstructor<A>>
	implements Runnable, GameTree<S, A>
{
	private static final Logger log = LoggerFactory.getLogger( UctSearch.class );
	
	public static final class Factory<S extends State, A extends VirtualConstructor<A>>
		implements GameTreeFactory<S, A>
	{
		private final UndoSimulator<S, A> sim_;
		private final Representer<S, ? extends Representation<S>> repr_;
		private final ActionGenerator<S, JointAction<A>> actions_;
		private final double c_;
		private final int episode_limit_;
		private final RandomGenerator rng_;
		private final EvaluationFunction<S, A> eval_;
		private final MutableActionNode<S, A> root_action_;
		
		private int max_action_visits_ = Integer.MAX_VALUE;
		private int max_depth_ = Integer.MAX_VALUE;
		
		public Factory( final UndoSimulator<S, A> sim,
						  final Representer<S, ? extends Representation<S>> repr,
						  final ActionGenerator<S, JointAction<A>> actions,
						  final double c, final int episode_limit,
						  final RandomGenerator rng,
						  final EvaluationFunction<S, A> eval,
						  final MutableActionNode<S, A> root_action )
		{
			sim_ = sim;
			repr_ = repr;
			actions_ = actions;
			c_ = c;
			episode_limit_ = episode_limit;
			rng_ = rng;
			eval_ = eval;
			root_action_ = root_action;
		}
		
		public Factory<S, A> setMaxActionVisits( final int max )
		{
			max_action_visits_ = max;
			return this;
		}
		
		public Factory<S, A> setMaxDepth( final int max )
		{
			max_depth_ = max;
			return this;
		}
		
		@Override
		public GameTree<S, A> create( final MctsVisitor<S, A> visitor )
		{
			// FIXME: ResetAdapter records the current depth of the tree in
			// its constructor, so you have to create a new adapter for every
			// search, to make sure it resets to the right place. This means
			// that Factory has to take an UndoSimulator parameter, which is
			// not quite right (overly restrictive).
			final UctSearch<S, A> uct = new UctSearch<S, A>(
				new ResetAdapter<S, A>( sim_ ), repr_.create(), actions_,
				c_, episode_limit_, rng_, eval_, visitor, root_action_.create() );
			uct.setMaxActionVisits( max_action_visits_ );
			uct.setMaxDepth( max_depth_ );
			return uct;
		}
	}
	
	// -----------------------------------------------------------------------
	
	private final ResetSimulator<S, A> sim_;
	private final Representer<S, ? extends Representation<S>> repr_;
	private final ActionGenerator<S, JointAction<A>> actions_;
	private final MctsVisitor<S, A> visitor_;
	private final double c_;
	private final int episode_limit_;
	private final RandomGenerator rng_;
	private final EvaluationFunction<S, A> eval_;
	
	// TODO: Should be a parameter
	protected final double discount_ = 1.0;
	
	private boolean complete_ = false;
	private final MutableActionNode<S, A> root_action_;
	
	private int action_visits_ = 0;
	private int max_action_visits_ = Integer.MAX_VALUE;
	
	private int max_depth_ = Integer.MAX_VALUE;
	
	public UctSearch( final ResetSimulator<S, A> sim,
					  final Representer<S, ? extends Representation<S>> repr,
					  final ActionGenerator<S, JointAction<A>> actions,
					  final double c, final int episode_limit,
					  final RandomGenerator rng,
					  final EvaluationFunction<S, A> eval,
					  final MctsVisitor<S, A> visitor,
					  final MutableActionNode<S, A> root_action )
	{
		sim_ = sim;
		repr_ = repr;
		actions_ = actions;
		c_ = c;
		episode_limit_ = episode_limit;
		rng_ = rng;
		eval_ = eval;
		visitor_ = visitor;
		
		root_action_ = root_action;
	}
	
	public int getMaxActionVisits()
	{
		return max_action_visits_;
	}
	
	public void setMaxActionVisits( final int max )
	{
		max_action_visits_ = max;
	}
	
	public int getMaxDepth()
	{
		return max_depth_;
	}
	
	public void setMaxDepth( final int max )
	{
		max_depth_ = max;
	}
	
	@Override
	public void run()
	{
		int episode_count = 0;
		while( episode_count++ < episode_limit_ && action_visits_ < max_action_visits_ ) {
			// FIXME: Remove the 'depth' parameter if we're not going to
			// pay attention to it.
			// FIXME: The way you're handling creating the root node is stupid.
			// It requires special checks for 'null' everyplace that state
			// nodes get created.
//			System.out.println( Arrays.toString( visit( null, Integer.MAX_VALUE, visitor_ ) ) );
			visit( root_action_, 0, visitor_ );
			sim_.reset();
			
//			System.out.println( "********************" );
//			root().accept( new TreePrinter<S, A>() );
		}
		
		complete_ = true;
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
	private double[] visit(
		final MutableActionNode<S, A> an, final int depth, final MctsVisitor<S, A> visitor )
	{
		final S s = sim_.state();
//		System.out.println( "UctSearch.visit( " + s + " )" );
		final int[] turn = sim_.turn();
		final int nagents = sim_.nagents();
		
		if( an == root_action_ ) {
			visitor.startEpisode( s, nagents, turn );
		}
		else {
			action_visits_ += 1;
			visitor.treeAction( an.a(), s, turn );
		}
		
		// FIXME: If your Representer maps states with different 'turn'
		// vectors to the same X, then the search can fail here with a
		// (spurious) assertion.
		// 1. touchLeafNode( an, x, [] ) is called in a terminal state,
		// 	  storing [0] for turn
		// 2. touchStateNode( an, x, [0] ) is called, not in a terminal
		//    state. This retrieves the LeafStateNode from before! The leaf
		//	  state node has turn == [], and we get a (spurious) error because
		//    we think we're in a terminal state.
		// This is a leak in the abstraction, since whose turn it is is
		// supposed to be Simulator's responsibility, not State. It's also
		// an error in design for UctSearch, since you shouldn't even be
		// able to take an action in a leaf state!
		
		final MutableStateNode<S, A> sn = an.successor( s, nagents, turn, actions_.create() );
		sn.visit();
		
		final double[] r = sim_.reward();
		sn.updateR( r );
		
		if( s.isTerminal() ) {
			assert( sn instanceof LeafStateNode<?, ?> );
			return r;
		}
		// If we've reached the fringe of the tree, use the evaluation function
		else if( sn.n() == 1 || depth == max_depth_ ) {
			assert( !(sn instanceof LeafStateNode<?, ?>) );
			final double[] v = eval_.evaluate( sim_ );
			visitor.checkpoint();
			return v;
		}
		else {
			assert( !(sn instanceof LeafStateNode<?, ?>) );
			// Sample below 'sn'
			final MutableActionNode<S, A> sa = selectAction( sn, s, sim_.t(), turn );
			sa.visit();
			
			// FIXME: Our current implementation assumes that rewards correspond
			// to states, but not to actions. The following line has to be
			// called so that the appropriate number of updates to R are made.
			// Ideally, we should define a different semantics for ActionNode.r(),
			// perhaps so that it holds R(s, a) (but not R(s)).
			sa.updateR( new double[nagents] );
			
			sim_.takeAction( sa.a().create() );
			final double[] z = visit( sa, depth + 1, visitor );
			sa.updateQ( z );
			Fn.scalar_multiply_inplace( z, discount_ );
			Fn.vplus_inplace( r, z );
			return r;
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
	private MutableActionNode<S, A> selectAction( final MutableStateNode<S, A> sn, final S s,
													 final long t, final int[] turn )
	{
//		System.out.println( sn.token );
		assert( !(sn instanceof LeafStateNode<?, ?>) );
		
		double max_value = -Double.MAX_VALUE;
		MutableActionNode<S, A> max_an = null;
		sn.action_gen_.setState( s, t );
		while( sn.action_gen_.hasNext() ) {
			final JointAction<A> a = sn.action_gen_.next();
			final MutableActionNode<S, A> an = sn.successor( a, sim_.nagents(), repr_.create() );
			if( an.n() == 0 ) {
				return an;
			}
			else {
				final double exploit = an.q( singleAgent( sn.turn ) );
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
	{
		return complete_;
	}

	@Override
	public StateNode<S, A> root()
	{
		final Generator<MutableStateNode<S, A>> g = root_action_.successors();
		final MutableStateNode<S, A> root = g.next();
		assert( !g.hasNext() );
		return root;
	}
}
