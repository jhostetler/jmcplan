/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.Option;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.UndoableAction;

/**
 * @author jhostetler
 *
 */
public class OptionRunner<S, A extends UndoableAction<S>>
{
	private final SimultaneousMoveSimulator<S, A> sim_;
	private final ArrayList<Policy<S, Option<S, A>>> policies_;
	private final int T_;
	
	private final ArrayList<Option<S, A>> active_;
	// TODO: seed
	private final RandomGenerator rng_ = new MersenneTwister();
	private final ArrayList<OptionListener<S, A>> listeners_
		= new ArrayList<OptionListener<S, A>>();
	
	public OptionRunner( final SimultaneousMoveSimulator<S, A> sim,
							final ArrayList<Policy<S, Option<S, A>>> policies,
							final int T )
	{
		sim_ = sim;
		policies_ = policies;
		T_ = T;
		active_ = new ArrayList<Option<S, A>>( sim_.getNumAgents() );
		Collections.fill( active_, null );
	}
	
	public boolean terminate( final S s, final Option<S, A> o )
	{
		final double beta = o.terminate( s );
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
	
	public void run()
	{
		final S s0 = sim_.state();
		for( int i = 0; i < policies_.size(); ++i ) {
			final Policy<S, Option<S, A>> pi = policies_.get( i );
			pi.setState( s0, 0L );
			final Option<S, A> o = pi.getAction();
			active_.set( i, o );
			o.start( s0 );
			fireOptionInitiated( s0, i, o );
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
	
	public void addListener( final OptionListener<S, A> listener )
	{
		listeners_.add( listener );
	}
	
	private void fireStartState( final S s )
	{
		final ArrayList<Policy<S, A>> Pi = new ArrayList<Policy<S, A>>();
		for( final Option<S, A> o : active_ ) {
			Pi.add( o.pi );
		}
		for( final OptionListener<S, A> listener : listeners_ ) {
			listener.startState( s, Pi );
		}
	}
	
	private void firePreGetAction( final int i )
	{
		for( final OptionListener<S, A> listener : listeners_ ) {
			listener.preGetAction( i );
		}
	}
	
	private void fireOptionTerminated( final S s, final int i, final Option<S, A> o )
	{
		for( final OptionListener<S, A> listener : listeners_ ) {
			listener.optionTerminated( s, i, o );
		}
	}
	
	private void fireOptionInitiated( final S s, final int i, final Option<S, A> o )
	{
		for( final OptionListener<S, A> listener : listeners_ ) {
			listener.optionInitiated( s, i, o );
		}
	}
	
	private void firePostGetAction( final int i, final UndoableAction<S> a )
	{
		for( final OptionListener<S, A> listener : listeners_ ) {
			listener.postGetAction( i, a );
		}
	}
	
	private void fireActionsTaken( final S sprime )
	{
		for( final OptionListener<S, A> listener : listeners_ ) {
			listener.onActionsTaken( sprime );
		}
	}
	
	private void fireEndState( final S s )
	{
		for( final OptionListener<S, A> listener : listeners_ ) {
			listener.endState( s );
		}
	}
}
