/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.ListIterator;

import edu.oregonstate.eecs.mcplan.agents.galcon.UndoSimulator;
import edu.oregonstate.eecs.mcplan.agents.galcon.UndoableAction;
import edu.oregonstate.eecs.mcplan.util.ListUtil;

/**
 * @author jhostetler
 *
 */
public abstract class SimultaneousMoveSimulator<S, A extends UndoableAction<S, A>> implements UndoSimulator<S, A>
{
	protected final Deque<ArrayList<A>> action_history_ = new ArrayDeque<ArrayList<A>>();
	protected final Deque<Deque<A>> event_history_ = new ArrayDeque<Deque<A>>();
	private long depth_ = 0;
	private int turn_ = 0;
	private final boolean[] turn_taken_;
	
	public SimultaneousMoveSimulator()
	{
		pushActionSet( getNumAgents() );
		event_history_.push( new ArrayDeque<A>() );
		turn_taken_ = new boolean[getNumAgents()];
	}
	
	protected SimultaneousMoveSimulator( final int num_agents )
	{
		pushActionSet( num_agents );
		event_history_.push( new ArrayDeque<A>() );
		turn_taken_ = new boolean[num_agents];
	}
	
	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.agents.galcon.UndoSimulator#state()
	 */
	@Override
	public abstract S state();
	
	/**
	 * Sets the current turn. Does NOT clear any moves taken in the last step.
	 * @param turn
	 */
	public void setTurn( final int turn )
	{
		assert( turn >= 0 );
		assert( turn < getNumAgents() );
		turn_ = turn;
	}
	
	private void pushActionSet( final int num_agents )
	{
		action_history_.push( new ArrayList<A>() );
		ListUtil.populateList( action_history_.peek(), null, num_agents );
	}
	
	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.agents.galcon.UndoSimulator#takeAction(edu.oregonstate.eecs.mcplan.agents.galcon.UndoableAction)
	 */
	@Override
	public final void takeAction( final A a )
	{
		assert( !turn_taken_[turn_] );
		turn_taken_[turn_] = true;
		action_history_.peek().set( turn_, a );
		turn_ += 1;
		if( turn_ == getNumAgents() ) {
			turn_ = 0;
		}
		
		boolean step = true;
		for( final boolean b : turn_taken_ ) {
			if( !b ) {
				step = false;
				break;
			}
		}
		
		if( step ) {
			final Iterator<A> itr = action_history_.peek().iterator();
			while( itr.hasNext() ) {
				final A ai = itr.next();
//				System.out.println( "SimultaneousMoveSimulator.takeAction( " + ai.toString() + " )" );
				ai.doAction( state() );
			}
			// Delegate world update to subclass.
			advance();
			pushActionSet( getNumAgents() );
			event_history_.push( new ArrayDeque<A>() );
			Arrays.fill( turn_taken_, false );
		}
		++depth_;
	}
	
	protected abstract void advance();
	
	protected abstract void unadvance();
	
	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.agents.galcon.UndoSimulator#untakeLastAction()
	 */
	@Override
	public final void untakeLastAction()
	{
		turn_ -= 1;
		if( turn_ < 0 ) {
			turn_ = getNumAgents() - 1;
		}
		
		boolean unstep = true;
		for( final boolean b : turn_taken_ ) {
			if( b ) {
				unstep = false;
				break;
			}
		}
		
		if( unstep ) {
			unadvance();
			
//			System.out.println( "un-advance()" );
			// Remove the (empty) stacks of actions and events for this turn.
//			assert( action_history_.peek().isEmpty() );
			action_history_.pop();
			assert( event_history_.peek().isEmpty() );
			event_history_.pop();
			
			// Revert world to previous turn
			while( !event_history_.peek().isEmpty() ) {
				event_history_.peek().pop().undoAction( state() );
			}
			
			// Undo, but do not remove, actions from this turn.
			final ListIterator<A> action_itr
				= action_history_.peek().listIterator( action_history_.peek().size() );
			while( action_itr.hasPrevious() ) {
//				System.out.println( "SimultaneousMoveSimulator.untakeAction( " + a.toString() + " )" );
				action_itr.previous().undoAction( state() );
			}
			Arrays.fill( turn_taken_, true );
		}
		// Now "untaking" the action just means removing it from the stack.
		assert( turn_taken_[turn_] );
		turn_taken_[turn_] = false;
		action_history_.peek().set( turn_, null );
		--depth_;
	}
	
	protected final void applyEvent( final A e )
	{
		e.doAction( state() );
		event_history_.peek().push( e );
	}
	
	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.agents.galcon.UndoSimulator#depth()
	 */
	@Override
	public final long depth()
	{
		return depth_;
	}
	
	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.agents.galcon.UndoSimulator#getTurn()
	 */
	@Override
	public final int getTurn()
	{
		return turn_;
	}
	
	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.agents.galcon.UndoSimulator#getReward()
	 */
	@Override
	public abstract double getReward();
	
	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.agents.galcon.UndoSimulator#detailString()
	 */
	@Override
	public String detailString()
	{
		return "";
	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] args )
	{
		
	}
}
