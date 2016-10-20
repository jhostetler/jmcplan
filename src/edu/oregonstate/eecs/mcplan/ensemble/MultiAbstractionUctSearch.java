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
package edu.oregonstate.eecs.mcplan.ensemble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.rl.UcbBandit;
import edu.oregonstate.eecs.mcplan.search.EvaluationFunction;
import edu.oregonstate.eecs.mcplan.search.LeafStateNode;
import edu.oregonstate.eecs.mcplan.search.MctsVisitor;
import edu.oregonstate.eecs.mcplan.search.MutableActionNode;
import edu.oregonstate.eecs.mcplan.search.MutableStateNode;
import edu.oregonstate.eecs.mcplan.search.SimpleMutableActionNode;
import edu.oregonstate.eecs.mcplan.sim.ResetSimulator;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * This class implements our new ensemble abstraction scheme, where the
 * abstraction to use for each trajectory is chosen according to a bandit
 * rule at the root.
 * 
 * The idea is that abstract UCT with projection-based abstraction is an
 * admissible heuristic. Thus, we should try to use the abstraction that
 * gives the best return. It's conceivable that this is different for
 * every action, so we use a nested bandit.
 * 
 * @author jhostetler
 *
 */
public class MultiAbstractionUctSearch<S extends State, A extends VirtualConstructor<A>>
	implements Runnable
{
	private final ResetSimulator<S, A> sim_;
	private final ActionGenerator<S, JointAction<A>> actions_;
	private final MctsVisitor<S, A> visitor_;
	private final double c_;
	private final int episode_limit_;
	private final EvaluationFunction<S, A> eval_;
	
	// TODO: Should be a parameter
	protected final double discount_ = 1.0;
	
	private boolean complete_ = false;
	
	private int action_visits_ = 0;
	private int max_action_visits_ = Integer.MAX_VALUE;
	
	private int max_depth_ = Integer.MAX_VALUE;
	
	private final ArrayList<MutableActionNode<S, A>> search_roots = new ArrayList<MutableActionNode<S, A>>();
	
	private final UcbBandit<JointAction<A>> root_action;
	public final Map<JointAction<A>, UcbBandit<MutableActionNode<S, A>>> action_abstractions
		= new HashMap<JointAction<A>, UcbBandit<MutableActionNode<S, A>>>();
	
	public MultiAbstractionUctSearch( final ResetSimulator<S, A> sim,
					  final ArrayList<Representer<S, ? extends Representation<S>>> reprs,
					  final ActionGenerator<S, JointAction<A>> actions,
					  final double c, final int episode_limit,
					  final EvaluationFunction<S, A> eval,
					  final MctsVisitor<S, A> visitor )
	{
		sim_ = sim;
		actions_ = actions;
		c_ = c;
		episode_limit_ = episode_limit;
		eval_ = eval;
		visitor_ = visitor;
		
		for( final Representer<S, ? extends Representation<S>> repr : reprs ) {
			search_roots.add( new SimpleMutableActionNode<S, A>( null, sim.nagents(), repr ) );
		}
		
		actions.setState( sim.state(), sim.t() );
		final ArrayList<JointAction<A>> as = Fn.takeAll( actions );
		root_action = new UcbBandit<JointAction<A>>( as, c );
		for( final JointAction<A> a : as ) {
			action_abstractions.put( a, new UcbBandit<MutableActionNode<S, A>>( search_roots, c ) );
		}
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
	
	public JointAction<A> astar()
	{
		double max_value = -Double.MAX_VALUE;
		JointAction<A> max_a = null;
		for( int i = 0; i < root_action.arms.size(); ++i ) {
			if( root_action.values[i].mean() > max_value ) {
				max_value = root_action.values[i].mean();
				max_a = root_action.arms.get( i );
			}
		}
		return max_a.create();
	}
	
	@Override
	public void run()
	{
		int episode_count = 0;
		while( episode_count++ < episode_limit_ && action_visits_ < max_action_visits_ ) {
			
			final int ai = root_action.sample();
			final JointAction<A> a = root_action.arms.get( ai );
			
			final UcbBandit<MutableActionNode<S, A>> ai_bandit = action_abstractions.get( a );
			final int root_i = ai_bandit.sample();
			final MutableActionNode<S, A> root = ai_bandit.arms.get( root_i );
			
			sim_.takeAction( a.create() );
			
			final double[] v = visit( root, 0, visitor_ );
			sim_.reset();
			
			ai_bandit.update( v[singleAgent( sim_.turn() )] );
			root_action.update( v[singleAgent( sim_.turn() )] );
			
//			System.out.println( Arrays.toString( visit( null, Integer.MAX_VALUE, visitor_ ) ) );
			
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
		
		if( depth == 0 ) {
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
			final MutableActionNode<S, A> sa = selectAction( sn, s, sim_.t(), turn, an.repr() );
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
													 final long t, final int[] turn,
													 final Representer<S, ? extends Representation<S>> repr )
	{
//		System.out.println( sn.token );
		assert( !(sn instanceof LeafStateNode<?, ?>) );
		
		double max_value = -Double.MAX_VALUE;
		MutableActionNode<S, A> max_an = null;
		sn.action_gen_.setState( s, t );
		while( sn.action_gen_.hasNext() ) {
			final JointAction<A> a = sn.action_gen_.next();
			final MutableActionNode<S, A> an = sn.successor( a, sim_.nagents(), repr.create() );
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
}