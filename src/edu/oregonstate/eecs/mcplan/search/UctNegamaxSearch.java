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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;


/**
 * Adversarial UCT search for two-player, zero-sum games. Implemented in the
 * "negamax" style.
 *
 * @param <S> State type
 * @param <T> State token type
 * @param <A> Action type
 */
public class UctNegamaxSearch<S, F extends Representer<S, F>, A extends VirtualConstructor<A>>
	implements GameTreeSearch<S, F, A>
{
	private static final Logger log = LoggerFactory.getLogger( UctNegamaxSearch.class );
	
	private class ActionNode
	{
		public final A a;
		public int n = 0;
		private final MeanVarianceAccumulator mv_ = new MeanVarianceAccumulator();
		public final Map<Representation<S, F>, StateNode> m_ = new HashMap<Representation<S, F>, StateNode>();
		
		public ActionNode( final A a )
		{
			this.a = a;
		}
		
		public StateNode stateNode( final Representation<S, F> token, final int turn )
		{
			StateNode si = m_.get( token );
			if( si == null ) {
//				System.out.println( "miss " + toString() + " " + hashCode() + " " + token.hashCode() );
				si = new StateNode( token, turn );
				m_.put( token, si );
			}
//			else {
//				System.out.println( "hit " + toString() + " " + hashCode() + " " + token.hashCode() );
//			}
			return si;
		}
		
		public void updateQ( final double q )
		{ mv_.add( q ); }
		
		public double q()
		{ return mv_.mean(); }
		
		public double qvar()
		{ return mv_.variance(); }
		
		@Override
		public String toString()
		{ return a.toString(); }
	}
	
	private class StateNode
	{
		public final Map<A, ActionNode> a_ = new HashMap<A, ActionNode>();
		
		public int n = 0;
		public final int turn;
		public final Representation<S, F> token;
		
		public StateNode( final Representation<S, F> token, final int turn )
		{
			this.token = token;
			this.turn = turn;
		}
		
		public ActionNode action( final A a )
		{
			ActionNode sa = a_.get( a );
			if( sa == null ) {
				sa = new ActionNode( a );
				a_.put( a, sa );
			}
			return sa;
		}
		
		public ActionNode bestAction( final RandomGenerator rng )
		{
			// Find all actions with maximal Q-value, and choose one at
			// random. Randomness is necessary because in the late game, if
			// the rollout policy always wins, all actions look the same, but
			// the UCT policy might always pick one that never actually wins
			// the game due to fixed action generator ordering.
			double max_q = -Double.MAX_VALUE;
			final ArrayList<ActionNode> action_pool = new ArrayList<ActionNode>();
			for( final Map.Entry<A, ActionNode> e : a_.entrySet() ) {
				log.info( "Action {}: n = {}, q = {}, 95% = {}",
						  e.getKey(), e.getValue().n, e.getValue().q(), 2 * Math.sqrt( e.getValue().qvar() ) );
				final double q = e.getValue().q();
				if( q > max_q ) {
					max_q = q;
					action_pool.clear();
				}
				if( q == max_q ) {
					action_pool.add( e.getValue() );
				}
			}
			log.info( "Action pool: {}", action_pool );
			final int random_action = rng_.nextInt( action_pool.size() );
			System.out.println( "--> Selected " + action_pool.get( random_action ) );
			return action_pool.get( random_action );
		}
	}
	
	// -----------------------------------------------------------------------
	
	private final UndoSimulator<S, A> sim_;
	private final Representer<S, F> repr_;
	private final ActionGenerator<S, ? extends A> actions_;
	private final ArrayList<Policy<S, A>> rollout_policies_;
	private final MctsNegamaxVisitor<S, A> visitor_;
	private final double c_;
	private final RandomGenerator rng_;
	
	private boolean complete_ = false;
	private StateNode root_ = null;
	private PrincipalVariation<Representation<S, F>, A> pv_ = null;
	
	public UctNegamaxSearch( final UndoSimulator<S, A> sim,
					  final Representer<S, F> repr,
					  final ActionGenerator<S, ? extends A> actions,
					  final double c, final RandomGenerator rng,
					  final ArrayList<Policy<S, A>> rollout_policies,
					  final MctsNegamaxVisitor<S, A> visitor )
	{
		sim_ = sim;
		repr_ = repr;
		actions_ = actions;
		rollout_policies_ = rollout_policies;
		c_ = c;
		rng_ = rng;
		visitor_ = visitor;
	}
	
	@Override
	public void run()
	{
		final S s0 = sim_.state();
		final Representation<S, F> x0 = repr_.encode( s0 );
		root_ = new StateNode( x0, sim_.turn() );
		// NOTE: Making an assumption about the indices of players here.
		final int turn = sim_.turn();
		System.out.println( "[Uct: starting on Turn " + turn + "]" );
		visitor_.startEpisode( s0 );
		int rollout_count = 0;
		while( visitor_.startRollout( s0 ) ) {
			visit( root_, 0 /* TODO: depth limit */, 1, visitor_ );
			rollout_count += 1;
		}
		log.info( "rollout_count = {}", rollout_count );
		pv_ = new PrincipalVariation<Representation<S, F>, A>( 1 );
		final ActionNode astar = root_.bestAction( rng_ );
		pv_.states.set( 0, x0.copy() );
		pv_.actions.set( 0, astar.a.create() );
		pv_.score = astar.q();
		complete_ = true;
	}
	
	public void printTree( final PrintStream out )
	{
		printStateNode( root_, 0, out );
	}
	
	private void printStateNode( final StateNode sn, final int depth, final PrintStream out )
	{
		for( int i = 0; i < depth; ++i ) {
			out.print( "  " );
		}
		out.print( "S" );
		out.print( depth / 2 );
		out.print( ": n = " );
		out.print( sn.n );
		out.print( ": " );
		out.println( sn.token );
		for( final Map.Entry<A, ActionNode> e : sn.a_.entrySet() ) {
			printActionNode( e.getValue(), depth + 1, out );
		}
	}
	
	private void printActionNode( final ActionNode an, final int depth, final PrintStream out )
	{
		for( int i = 0; i < depth; ++i ) {
			out.print( "  " );
		}
		out.print( "A" );
		out.print( depth / 2 );
		out.print( ": n = " );
		out.print( an.n );
		out.print( ": " );
		out.print( an.a );
		out.print( ": q = " );
		out.print( an.q() );
		out.print( ", var = " );
		out.print( an.qvar() );
		out.print( ", 95% = " );
		out.print( 2 * Math.sqrt( an.qvar() ) );
		out.println();
		for( final Map.Entry<Representation<S, F>, StateNode> e : an.m_.entrySet() ) {
			printStateNode( e.getValue(), depth + 1, out );
		}
	}
	
	private ActionNode selectAction( final StateNode sn, final ActionGenerator<S, ? extends A> actions )
	{
		assert( actions.size() > 0 );
		double max_value = -Double.MAX_VALUE;
		ActionNode max_sa = null;
		while( actions.hasNext() ) {
			final A a = actions.next();
			final ActionNode sa = sn.action( a );
			if( sa.n == 0 ) {
				max_sa = sa;
				break;
			}
			else {
				final double exploit = sa.q();
				final double explore = c_ * Math.sqrt( Math.log( sn.n ) / sa.n );
				final double v = explore + exploit;
				if( v > max_value ) {
					max_sa = sa;
					max_value = v;
				}
			}
		}
		return max_sa;
	}
	
	private double rollout( final int color, final MctsNegamaxVisitor<S, A> visitor )
	{
		final S s = sim_.state();
		final double r;
		if( sim_.isTerminalState( ) ) {
			r = color * visitor.terminal( s );
		}
		else {
			final Policy<S, A> pi = rollout_policies_.get( sim_.turn() );
			pi.setState( sim_.state(), sim_.t() );
			final A a = pi.getAction();
			sim_.takeAction( a );
			final S sprime = sim_.state();
			visitor.defaultAction( a, sprime );
			r = -rollout( -color, visitor );
			pi.actionResult( sprime, r );
			sim_.untakeLastAction();
		}
		return r;
	}
	
	private double visit( final StateNode sn, final int depth, final int color, final MctsNegamaxVisitor<S, A> visitor )
	{
		final S s = sim_.state();
		sn.n += 1;
		final double r;
		if( sim_.isTerminalState( ) ) {
			r = color * visitor.terminal( s );
		}
		else {
			assert( sn.turn == sim_.turn() );
			final ActionGenerator<S, ? extends A> action_gen = actions_.create();
			action_gen.setState( s, sim_.t(), sim_.turn() );
			final ActionNode sa = selectAction( sn, action_gen );
			sa.n += 1;
			sim_.takeAction( sa.a );
			final S sprime = sim_.state();
			visitor.treeAction( sa.a, sprime );
			if( sa.n == 1 ) {
				// Leaf node
				r = -rollout( -color, visitor );
			}
			else {
				final Representation<S, F> x = repr_.encode( sprime );
				final StateNode snprime = sa.stateNode( x, sim_.turn() );
				r = -visit( snprime, depth - 1, -color, visitor );
			}
			sa.updateQ( r );
			sim_.untakeLastAction();
		}
		return r;
	}

	@Override
	public double score()
	{
		return pv_.score;
	}

	@Override
	public PrincipalVariation<Representation<S, F>, A> principalVariation()
	{
		return pv_;
	}

	@Override
	public boolean isComplete()
	{
		return complete_;
	}
}
