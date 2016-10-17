/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.pats;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.LoggerManager;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssParameters;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssTest;
import edu.oregonstate.eecs.mcplan.sim.TransitionSimulator;

/**
 * @author jhostetler
 *
 */
public class ForwardSearchSparseSampling<S extends State, A>
{
	public static interface Listener<S extends State, A>
	{
		/**
		 * Before interacting with node.
		 * @param asn
		 */
		public abstract void onVisit( final PatsStateNode<S, A> asn );
		
		/**
		 * After non-leaf node expanded for the first time.
		 * @param asn
		 */
		public abstract void onExpand( final PatsStateNode<S, A> asn );
		
		/**
		 * Leaf node expanded for the first time.
		 * @param asn
		 */
		public abstract void onLeaf( final PatsStateNode<S, A> asn );
		
		/**
		 * Before sampling trajectory begins.
		 */
		public abstract void onTrajectoryStart();
		
		/**
		 * Action successor selected.
		 * @param aan
		 */
		public abstract void onActionChoice( final PatsActionNode<S, A> aan );
		
		/**
		 * State successor selected.
		 * @param asn
		 */
		public abstract void onStateChoice( final PatsStateNode<S, A> asn );
		
		/**
		 * After sampling trajectory ends.
		 */
		public abstract void onTrajectoryEnd();
	}
	
	// -----------------------------------------------------------------------
	
	private final ch.qos.logback.classic.Logger Log = LoggerManager.getLogger( "log.search" );
	
	private final FsssParameters parameters;
	private final TransitionSimulator<S, A> sim;
	private final PatsStateNode<S, A> root;
	
	private final ArrayList<Listener<S, A>> listeners = new ArrayList<Listener<S, A>>();
	private boolean complete = false;
	private int min_depth = Integer.MAX_VALUE;
	
	/**
	 * @param parameters
	 * @param model
	 * @param root Root state node of partial AFSSS tree
	 */
	public ForwardSearchSparseSampling( final FsssParameters parameters, final TransitionSimulator<S, A> sim,
						 				final PatsStateNode<S, A> root )
	{
		this.parameters = parameters;
		this.sim = sim;
		this.root = root;
	}
	
	/**
	 * True if run() has returned.
	 * @return
	 */
	public boolean isComplete()
	{
		return complete;
	}
	
	/**
	 * Subscribe to algorithm hooks.
	 * @param listener
	 */
	public void addListener( final Listener<S, A> listener )
	{
		listeners.add( listener );
	}
	
	/**
	 * Run search until convergence or budget exhausted.
	 */
	public void run()
	{
		int iter = 0;
		while( true ) {
			if( parameters.budget.isExceeded() ) {
				break;
			}
			
			Log.trace( "\tAFSSS: iter {}", iter );
			
			// Stop search if L(root, a*) >= U(root, a) forall a != a*
			final ArrayList<PatsActionNode<S, A>> glb = root.greatestLowerBound(); //root.astar();
			assert( glb != null );
			if( !glb.isEmpty() ) {
				final PatsActionNode<S, A> astar = glb.get( 0 );
				final double Lstar = astar.L();
				boolean done = true;
				for( final PatsActionNode<S, A> alt : root.successors() ) {
					if( alt != astar && alt.U() > Lstar ) {
						done = false;
						break;
					}
				}
				if( done ) {
					complete = true;
					break;
				}
			}
			
			iter += 1;
			
			fireTrajectoryStart();
			fsss( root, parameters.depth );
			fireTrajectoryEnd();
		}
		
		Log.debug( "\t=> FSSS: {} iterations to convergence", iter );
		Log.debug( "\t a* = {}", root.astar() );
		Log.debug( "\t L* = {}", root.greatestLowerBound() );
		Log.debug( "\t Sample count: {}", model.sampleCount() );
	}
	
	private void fsss( final PatsStateNode<S, A> asn, final int d )
	{
		Log.trace( "\tFSSS: d = {}, asn = {}", d, asn );
		fireVisit( asn );
		
		if( !asn.isTerminal() ) {
			if( !asn.isExpanded() ) {
				if( parameters.budget.isExceeded() ) {
					Log.info( "\t\tFSSS: Exceeded budget" );
					return;
				}
				asn.expand( model.actions( asn ), parameters.width, parameters.budget );
				fireExpand( asn );
				
				if( parameters.use_close
						&& (asn.predecessor == null || asn.predecessor.predecessor.isClosed())
						&& asn.isReadyToClose() ) {
					Log.debug( "close() [AbstractFsss]" );
					asn.close();
				}
			}
			
			final PatsActionNode<S, A> astar = asn.astar_random();
			fireActionChoice( astar );
			
			if( Log.isDebugEnabled() && astar == null ) {
				FsssTest.printTree( root, Log, 1 );
				Log.debug( "{}", asn );
			}
			
			final PatsStateNode<S, A> sstar = astar.sstar_random();
			fireStateChoice( sstar );
			
			// Note: Without randomization, sstar is never null because the
			// first asn lexicographically will always have been sampled before
			// time runs out. With randomization, this is no longer true. If
			// we happen to sample a valid sstar, we'd still like to do a
			// backup, to be as consistent as possible with the earlier
			// non-randomized algorithm. sstar == null should still be an
			// error if it is not due to running out of budget.
			if( sstar == null ) {
				if( !parameters.budget.isExceeded() ) {
					FsssTest.printTree( root, System.out, 1 );
				}
				
				assert( parameters.budget.isExceeded() );
				return;
			}
			
			fsss( sstar, d - 1 );
			astar.backup();
			asn.backup();
		}
		else { // Leaf node
			asn.leaf();
			fireLeaf( asn );
			
			min_depth = Math.min( min_depth, d );
		}
	}
	
	// -----------------------------------------------------------------------
	
	private void fireVisit( final PatsStateNode<S, A> asn )
	{
		for( final Listener<S, A> listener : listeners ) {
			listener.onVisit( asn );
		}
	}
	
	private void fireExpand( final PatsStateNode<S, A> asn )
	{
		for( final Listener<S, A> listener : listeners ) {
			listener.onExpand( asn );
		}
	}
	
	private void fireLeaf( final PatsStateNode<S, A> asn )
	{
		for( final Listener<S, A> listener : listeners ) {
			listener.onLeaf( asn );
		}
	}
	
	private void fireTrajectoryStart()
	{
		for( final Listener<S, A> listener : listeners ) {
			listener.onTrajectoryStart();
		}
	}
	
	private void fireActionChoice( final PatsActionNode<S, A> asn )
	{
		for( final Listener<S, A> listener : listeners ) {
			listener.onActionChoice( asn );
		}
	}
	
	private void fireStateChoice( final PatsStateNode<S, A> asn )
	{
		for( final Listener<S, A> listener : listeners ) {
			listener.onStateChoice( asn );
		}
	}
	
	private void fireTrajectoryEnd()
	{
		for( final Listener<S, A> listener : listeners ) {
			listener.onTrajectoryEnd();
		}
	}
}