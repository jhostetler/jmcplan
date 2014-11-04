/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class Episode<S, A extends VirtualConstructor<A>> implements Runnable
{
	private final Simulator<S, A> sim_;
	private final Policy<S, JointAction<A>> pi_;
	private final int T_;
	private final ArrayList<EpisodeListener<S, A>> listeners_ = new ArrayList<EpisodeListener<S, A>>();
	private final boolean use_burnin_ = false; // FIXME: Burn-in is messing up first move; disabling for now
	
	public Episode( final Simulator<S, A> sim, final Policy<S, JointAction<A>> pi, final int T )
	{
		sim_ = sim;
		pi_ = pi;
		T_ = T;
	}
	
	public Episode( final Simulator<S, A> sim, final Policy<S, JointAction<A>> pi )
	{
		this( sim, pi, Integer.MAX_VALUE );
	}
	
	@Override
	public void run()
	{
		fireStartState( sim_.state(), sim_.reward() );
		
		if( use_burnin_ ) {
			// The first player to move always ends up achieving fewer rollouts
			// on his first turn compared to the second player. I think this
			// is due to startup costs of the JVM or some kind of
			// profiling-based optimizations. My solution is to do a "practice"
			// round for all players before starting the experiment. This
			// appears to work.
			// TODO: The visitors passed to policies like UctPolicy will still
			// be called for the fake searches. What are the consequences?
			// [2014/06/23] It may be possible to suppress this behavior of
			// the JVM by specifying -XXaggressive and -XX:-UseCallProfiling,
			// however it says here (http://docs.oracle.com/cd/E15289_01/doc.40/e15062/optionxx.htm)
			// that profiling is not enabled by default.
			
//			System.out.println( "[Episode] Burn-in" );
			final int t0 = 0;
			pi_.setState( sim_.state(), t0 );
			final JointAction<A> a = pi_.getAction();
		}
				
		for( int t = 0; t < T_; ++t ) {
//			System.out.println( "[Episode] Action selection" );
//			System.out.println( sim_.state() );
			pi_.setState( sim_.state(), t );
			firePreGetAction();
			final JointAction<A> a = pi_.getAction();
			firePostGetAction( a );
//			System.out.println( "!!! [t = " + t + "] a = " + a.toString() );
//			System.out.println( "[Episode] Execution" );
			sim_.takeAction( a );
			pi_.actionResult( sim_.state(), sim_.reward() );
			fireActionsTaken( sim_.state(), sim_.reward() );
			if( sim_.isTerminalState( ) ) {
				break;
			}
		}
		fireEndState( sim_.state() );
	}
	
	public void addListener( final EpisodeListener<S, A> listener )
	{
		listeners_.add( listener );
	}
	
	private void fireStartState( final S s, final double[] r )
	{
		for( final EpisodeListener<S, A> listener : listeners_ ) {
			listener.startState( s, r, pi_ );
		}
	}
	
	private void firePreGetAction()
	{
		for( final EpisodeListener<S, A> listener : listeners_ ) {
			listener.preGetAction();
		}
	}
	
	private void firePostGetAction( final JointAction<A> a )
	{
		for( final EpisodeListener<S, A> listener : listeners_ ) {
			listener.postGetAction( a );
		}
	}
	
	private void fireActionsTaken( final S sprime, final double[] r )
	{
		for( final EpisodeListener<S, A> listener : listeners_ ) {
			listener.onActionsTaken( sprime, r );
		}
	}
	
	private void fireEndState( final S s )
	{
		for( final EpisodeListener<S, A> listener : listeners_ ) {
			listener.endState( s );
		}
	}
}
