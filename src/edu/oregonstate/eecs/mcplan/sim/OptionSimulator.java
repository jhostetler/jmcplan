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
package edu.oregonstate.eecs.mcplan.sim;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Option;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.ListUtil;
import edu.oregonstate.eecs.mcplan.util.WrapListIterator;
import gnu.trove.list.array.TIntArrayList;

/**
 * Adapts a simulator of primitive actions to simulate options over those
 * actions.
 * <p>
 * The takeAction() function sets the active option for the current player.
 * If all players have active options, it then calls takeAction() on the
 * base simulator using the policies from the active options until one or
 * more options terminate. It then returns.
 * <p>
 * The getTurn() function returns the index of the next player in the move
 * ordering who does not have an active option. This is not necessarily the
 * next player to move in the base simulator.
 */
public class OptionSimulator<S, A extends VirtualConstructor<A>> implements UndoSimulator<S, Option<S, A>>
{
	private final UndoSimulator<S, A> base_;
	
	private final ArrayList<Option<S, A>> active_;
	private TIntArrayList turn_ = new TIntArrayList();
	private final int Nplayers_;
	private final RandomGenerator rng_;
	
	/**
	 * Stores information needed to undo an action.
	 */
	private class StackFrame
	{
		public final TIntArrayList turn;
		public final ArrayList<Option<S, A>> option;
		public int steps = 0;
		
		public StackFrame( final TIntArrayList turn, final ArrayList<Option<S, A>> option )
		{
			this.turn = turn;
			this.option = option;
		}
	}
	
	private final ArrayList<Option<S, A>> terminated_;
	private final Deque<StackFrame> history_ = new ArrayDeque<StackFrame>();
	
	private int action_count_ = 0;
	
	public OptionSimulator( final UndoSimulator<S, A> base, final long seed )
	{
		base_ = base;
		Nplayers_ = base_.nagents();
		active_ = new ArrayList<Option<S, A>>( Nplayers_ );
		ListUtil.populateList( active_, null, Nplayers_ );
		terminated_ = new ArrayList<Option<S, A>>( Nplayers_ );
		ListUtil.populateList( terminated_, null, Nplayers_ );
		rng_ = new MersenneTwister( seed );
	}
	
//	public OptionSimulator( final UndoSimulator<S, A> base, final long seed, final int turn_shift )
//	{
//		this( base, seed );
//		turn_ = turn_shift;
//	}
	
	@Override
	public S state()
	{
		return base_.state();
	}
	
	private boolean terminate( final S s, final long t, final Option<S, A> o )
	{
		final double beta = o.terminate( s, t );
		if( beta == 1.0 ) {
			return true;
		}
		else if( beta == 0.0 ) {
			return false;
		}
		else {
			return rng_.nextDouble() < beta;
		}
	}
	
	private int nextTurn( final int current )
	{
		int next = current + 1;
		if( next == Nplayers_ ) {
			next = 0;
		}
		return next;
	}
	
	private int nextNullOption( final List<Option<S, A>> os, final int i )
	{
		final int k = 0; // TODO: debugging
		final WrapListIterator<Option<S, A>> itr = new WrapListIterator<Option<S, A>>( os, i );
		while( itr.hasNext() ) {
			final Option<S, A> o = itr.next();
//			System.out.println( "Option " + (itr.previousIndex()) + " is " + (o == null ? "NULL" : "not null") );
			if( o == null ) {
				return itr.previousIndex();
			}
		}
		return -1;
	}
	
	private TIntArrayList nullOptions( final List<Option<S, A>> os )
	{
		final TIntArrayList result = new TIntArrayList();
		for( int i = 0; i < os.size(); ++i ) {
			if( os.get( i ) == null ) {
				result.add( i );
			}
		}
		return result;
	}
	
	private void pushFrame( final TIntArrayList turn, final ArrayList<Option<S, A>> option )
	{
		history_.push( new StackFrame( turn, option ) );
	}

	@Override
	public void takeAction( final JointAction<Option<S, A>> j )
	{
		action_count_ += 1;
//		System.out.println( "Setting option " + o + " for player " + turn_ );
		
		// Find all options that terminated
		final ArrayList<Option<S, A>> term = new ArrayList<Option<S, A>>();
		for( int i = 0; i < turn_.size(); ++i ) {
			assert( active_.get( i ) == null );
			term.add( terminated_.get( i ) );
			terminated_.set( i, null );
		}
		pushFrame( turn_, term );
		
		// Activate new options
		for( int i = 0; i < turn_.size(); ++i ) {
			assert( j.get( i ) != null );
			final Option<S, A> o = j.get( i );
			active_.set( i, o );
			o.start( base_.state(), base_.t() );
		}
		
		assert( history_.size() == action_count_ ); // TODO: Debugging
		
		// Take actions according to active options until one or more
		// options terminates.
		final long told = base_.t();
		final long tnew = told;
		while( true ) {
			final S s = base_.state();
			if( base_.isTerminalState( ) ) {
				return;
			}
			
			// See if any options terminate
			final ListIterator<Option<S, A>> check_itr = active_.listIterator();
			final TIntArrayList next_turn = new TIntArrayList();
			while( check_itr.hasNext() ) {
				final Option<S, A> ocheck = check_itr.next();
				if( terminate( s, base_.t(), ocheck ) ) {
//					System.out.println( "! Option " + (check_itr.previousIndex()) + " terminated" );
					check_itr.set( null );
					final int pidx = check_itr.previousIndex();
					terminated_.set( pidx, ocheck );
					next_turn.add( pidx );
				}
			}
			
			// If they do, wait for new options
			if( !next_turn.isEmpty() ) {
				turn_ = next_turn;
				return;
			}
			
			// Construct a JointAction over primitive actions and execute it
			final JointAction.Builder<A> ab = new JointAction.Builder<A>( Nplayers_ );
			for( int i = 0; i < Nplayers_; ++i ) {
				final Option<S, A> o = active_.get( i );
				assert( o != null );
				o.setState( s, base_.t() );
				ab.a( i, o.getAction() );
			}
			base_.takeAction( ab.finish() );
			
//			System.out.println( "Take action " + base_.getTurn() + " " + a );
			history_.peek().steps += 1;
			// TODO: Give pi its reward
			// pi.actionResult( ??? );
		}
	}
	
//	public void old_takeAction( final JointAction<Option<S, A>> o )
//	{
//		assert( active_.get( turn_ ) == null );
//		action_count_ += 1;
////		System.out.println( "Setting option " + o + " for player " + turn_ );
//		pushFrame( turn_, terminated_.get( turn_ ) );
//		terminated_.set( turn_, null );
//		active_.set( turn_, o );
//		o.start( base_.state(), base_.t() );
//
//		assert( history_.size() == action_count_ ); // TODO: Debugging
//
//		// Do some players still need to choose an option?
//		final int next_null = nextNullOption( active_, nextTurn( turn_ ) );
//		if( next_null != -1 ) {
//			turn_ = next_null;
////			System.out.println( "Player " + turn_ + " still needs an option" );
//			return;
//		}
//
////		System.out.println( "Everyone has an option!" );
//		// Take actions according to active options until one or more
//		// options terminates.
//		final long told = base_.t();
//		final long tnew = told;
//		while( true ) {
//			final S s = base_.state();
//			if( base_.isTerminalState( ) ) {
////				System.out.println( "! terminal" );
//				return;
//			}
//
//			// See if any options terminate
////			final WrapListIterator<Option<S, A>> check_itr
////				= new WrapListIterator<Option<S, A>>( active_, base_.getTurn() );
//			final ListIterator<Option<S, A>> check_itr = active_.listIterator();
//			int null_idx = Integer.MAX_VALUE;
//			while( check_itr.hasNext() ) {
//				final Option<S, A> ocheck = check_itr.next();
//				if( terminate( s, base_.t(), ocheck ) ) {
////					System.out.println( "! Option " + (check_itr.previousIndex()) + " terminated" );
//					check_itr.set( null );
//					final int pidx = check_itr.previousIndex();
//					terminated_.set( pidx, ocheck );
//					if( pidx < null_idx ) {
//						null_idx = pidx;
//					}
//				}
//			}
//			// If they do, wait for new options
//			if( null_idx != Integer.MAX_VALUE ) {
////				System.out.println( "! Next option choice " + null_idx );
//				turn_ = null_idx;
//				return;
//			}
//
//			// Execute current option policy
//			final Option<S, A> oi = active_.get( base_.turn() );
//			oi.setState( s, base_.t() );
//			final A a = oi.getAction();
////			System.out.println( "Take action " + base_.getTurn() + " " + a );
//			base_.takeAction( a );
//			history_.peek().steps += 1;
//			// TODO: Give pi its reward
//			// pi.actionResult( ??? );
//			turn_ = nextTurn( turn_ );
//		}
//	}

	@Override
	public void untakeLastAction()
	{
		action_count_ -= 1;
		final StackFrame f = history_.pop();
		assert( action_count_ == history_.size() );
		while( f.steps-- > 0 ) {
			base_.untakeLastAction();
		}
		turn_ = f.turn;
		for( int i = 0; i < turn_.size(); ++i ) {
			final int one_turn = turn_.get( i );
			active_.set( one_turn, null );
			terminated_.set( one_turn, f.option.get( i ) );
		}
	}

	@Override
	public long depth()
	{
		// TODO: Do we actually want the Option depth instead?
		return base_.depth();
	}

	@Override
	public long t()
	{
		return base_.t();
	}

	@Override
	public int nagents()
	{
		return base_.nagents();
	}

	@Override
	public int[] turn()
	{
//		final TIntArrayList null_options = nullOptions( active_ );
//		final int base_turn = base_.getTurn();
//		System.out.println( "" + base_turn + "; " + null_options );
//		assert( null_options.contains( base_turn ) );
//		return base_turn;
		return turn_.toArray();
	}

	@Override
	public double[] reward()
	{
		// TODO: How do we implement this?
		return new double[nagents()];
	}

	@Override
	public boolean isTerminalState( )
	{
		return base_.isTerminalState( );
	}

	@Override
	public long horizon()
	{
		return base_.horizon();
	}

	@Override
	public String detailString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "OptionSimulator[" ).append( base_.detailString() ).append( "]" );
		return sb.toString();
	}

}
