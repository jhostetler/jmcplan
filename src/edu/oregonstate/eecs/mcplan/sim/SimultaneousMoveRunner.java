package edu.oregonstate.eecs.mcplan.sim;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.UndoableAction;

public class SimultaneousMoveRunner<S, A extends UndoableAction<S>> implements Runnable
{
	private final SimultaneousMoveSimulator<S, A> sim_;
	private final ArrayList<? extends Policy<S, A>> agents_;
	private final int T_;
	private final ArrayList<SimultaneousMoveListener<S, A>> listeners_
		= new ArrayList<SimultaneousMoveListener<S, A>>();
	private final boolean use_burnin_ = false; // FIXME: Burn-in is messing up first move; disabling for now
	
	public SimultaneousMoveRunner( final SimultaneousMoveSimulator<S, A> sim,
								   final ArrayList<? extends Policy<S, A>> agents,
								   final int T )
	{
		sim_ = sim;
		agents_ = agents;
		T_ = T;
	}
	
	@Override
	public void run()
	{
		fireStartState( sim_.state() );
		
		if( use_burnin_ ) {
			// The first player to move always ends up achieving fewer rollouts
			// on his first turn compared to the second player. I think this
			// is due to startup costs of the JVM or some kind of
			// profiling-based optimizations. My solution is to do a "practice"
			// round for all players before starting the experiment. This
			// appears to work.
			// TODO: The visitors passed to policies like UctPolicy will still
			// be called for the fake searches. What are the consequences?
			for( int i = 0; i < agents_.size(); ++i ) {
				final Policy<S, A> policy = agents_.get( i );
				System.out.println( "[SimultaneousMoveRunner] Burn-in: Player " + i );
				sim_.setTurn( i );
				final int t0 = 0;
				policy.setState( sim_.state(), t0 );
				final A a = policy.getAction();
			}
		}
				
		for( int t = 0; t < T_; ++t ) {
			final ArrayList<A> actions = new ArrayList<A>( agents_.size() );
			for( int i = 0; i < agents_.size(); ++i ) {
				final Policy<S, A> policy = agents_.get( i );
				System.out.println( "[SimultaneousMoveRunner] Action selection: setTurn( " + i + " )" );
				sim_.setTurn( i );
				policy.setState( sim_.state(), t );
				firePreGetAction( i );
				final A a = policy.getAction();
				firePostGetAction( i, a );
				actions.add( a );
				System.out.println( "!!! [t = " + t + "] a" + i + " = " + a.toString() );
			}
			System.out.println( "[SimultaneousMoveRunner] Execution: setTurn( 0 )" );
			sim_.setTurn( 0 );
			for( final A a : actions ) {
				sim_.takeAction( a );
			}
			fireActionsTaken( sim_.state() );
			if( sim_.isTerminalState( sim_.state() ) ) {
				break;
			}
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
	
	private void firePostGetAction( final int i, final UndoableAction<S> a )
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
