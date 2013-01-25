package edu.oregonstate.eecs.mcplan.sim;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.agents.galcon.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.agents.galcon.UndoableAction;

public class SimultaneousMoveRunner<S, A extends UndoableAction<S, A>> implements Runnable
{
	private final SimultaneousMoveSimulator<S, A> sim_;
	private final ArrayList<AnytimePolicy<S, A>> agents_;
	private final int T_;
	private final int control_;
	private final ArrayList<SimultaneousMoveListener<S, A>> listeners_
		= new ArrayList<SimultaneousMoveListener<S, A>>();
	
	public SimultaneousMoveRunner( final SimultaneousMoveSimulator<S, A> sim,
								   final ArrayList<AnytimePolicy<S, A>> agents,
								   final int T, final int control )
	{
		sim_ = sim;
		agents_ = agents;
		T_ = T;
		control_ = control;
	}
	
	@Override
	public void run()
	{
		System.out.println( "run()" );
		fireStartState( sim_.state() );
		for( int t = 0; t < T_; ++t ) {
			final ArrayList<A> actions = new ArrayList<A>( agents_.size() );
			for( int i = 0; i < agents_.size(); ++i ) {
				final AnytimePolicy<S, A> policy = agents_.get( i );
				sim_.setTurn( i );
				policy.setState( sim_.state() );
				firePreGetAction( i );
				final A a = policy.getAction( control_ );
				firePostGetAction( i, a );
				actions.add( a );
				System.out.println( "!!! [t = " + t + "] a" + i + " = " + a.toString() );
			}
			
			sim_.setTurn( 0 );
			for( final A a : actions ) {
				sim_.takeAction( a );
			}
			fireActionsTaken( sim_.state() );
		}
		fireEndState( sim_.state() );
	}
	
	public void addListener( final SimultaneousMoveListener<S, A> listener )
	{
		listeners_.add( listener );
	}
	
	private void fireStartState( final S s )
	{
		for( final SimultaneousMoveListener<S, A> listener : listeners_ ) {
			listener.startState( s, agents_ );
		}
	}
	
	private void firePreGetAction( final int i )
	{
		for( final SimultaneousMoveListener<S, A> listener : listeners_ ) {
			listener.preGetAction( i );
		}
	}
	
	private void firePostGetAction( final int i, final A a )
	{
		for( final SimultaneousMoveListener<S, A> listener : listeners_ ) {
			listener.postGetAction( i, a );
		}
	}
	
	private void fireActionsTaken( final S sprime )
	{
		for( final SimultaneousMoveListener<S, A> listener : listeners_ ) {
			listener.onActionsTaken( sprime );
		}
	}
	
	private void fireEndState( final S s )
	{
		for( final SimultaneousMoveListener<S, A> listener : listeners_ ) {
			listener.endState( s );
		}
	}
}
