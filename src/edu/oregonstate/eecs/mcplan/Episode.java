/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;

/**
 * @author jhostetler
 *
 */
public class Episode<S, A>
{
	private final UndoSimulator<S, A> sim_;
	private final Controller<S, A> controller_;
	
	public Episode( final UndoSimulator<S, A> sim, final Controller<S, A> controller )
	{
		sim_ = sim;
		controller_ = controller;
	}
	
	public void run()
	{
		episode: while( true ) {
			for( int i = 0; i < sim_.getNumAgents(); ++i ) {
				final S s = sim_.state();
				final boolean stop = controller_.setState( s, i, 0L );
				if( stop ) {
					break episode;
				}
				final A a = controller_.getAction();
				sim_.takeAction( a );
				final S sprime = sim_.state();
				final double r = sim_.getReward();
				controller_.actionResult( i, a, sprime, r );
			}
		}
		
		fireStartState( s0 );
		for( int t = 0; t < T_; ++t ) {
			final ArrayList<A> actions = new ArrayList<A>( policies_.size() );
			for( int i = 0; i < policies_.size(); ++i ) {
				final S s = sim_.state();
				Option<S, A> option = active_.get( i );
				if( terminate( s, option ) ) {
					// TODO: How to split 'control' up between option choice
					// and action choice?
					fireOptionTerminated( s, i, option );
					option = policies_.get( i ).getAction();
					option.start( s );
					fireOptionInitiated( s, i, option );
				}
				final Policy<S, A> policy = option.pi;
				System.out.println( "[SimultaneousMoveRunner] Action selection: setTurn( " + i + " )" );
				sim_.setTurn( i );
				policy.setState( s, t );
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
			if( sim_.isTerminalState( ) ) {
				break;
			}
		}
		fireEndState( sim_.state() );
	}
}
