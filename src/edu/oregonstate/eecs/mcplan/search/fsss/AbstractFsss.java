/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class AbstractFsss<S extends State, A extends VirtualConstructor<A>>
{
	public static interface Listener<S extends State, A extends VirtualConstructor<A>>
	{
		public abstract void onVisit( final FsssAbstractStateNode<S, A> asn );
		public abstract void onExpand( final FsssAbstractStateNode<S, A> asn );
		public abstract void onLeaf( final FsssAbstractStateNode<S, A> asn );
		
		public abstract void onTrajectoryStart();
		public abstract void onActionChoice( final FsssAbstractActionNode<S, A> aan );
		public abstract void onStateChoice( final FsssAbstractStateNode<S, A> asn );
		public abstract void onTrajectoryEnd();
	}
	
	// -----------------------------------------------------------------------
	
	private final FsssParameters parameters;
	private final FsssModel<S, A> model;
	private final FsssAbstractStateNode<S, A> root;
	
	private final ArrayList<Listener<S, A>> listeners = new ArrayList<Listener<S, A>>();
	
	private boolean use_logging = false;
	private boolean complete = false;
	private int min_depth = Integer.MAX_VALUE;
	
	public AbstractFsss( final FsssParameters parameters, final FsssModel<S, A> model,
						 final FsssAbstractStateNode<S, A> root )
	{
		this.parameters = parameters;
		this.model = model;
		this.root = root;
	}
	
	public void setLoggingEnabled( final boolean enabled )
	{
		use_logging = enabled;
	}
	
	public boolean isComplete()
	{
		return complete;
	}
	
	public void addListener( final Listener<S, A> listener )
	{
		listeners.add( listener );
	}
	
	public void run()
	{
		int iter = 0;
		while( true ) {
			if( parameters.budget.isExceeded() ) {
				break;
			}
			
			if( use_logging ) {
				System.out.println( "\tAFSSS: iter " + iter );
			}
			
			// Stop search if L(root, a*) >= U(root, a) forall a != a*
			final ArrayList<FsssAbstractActionNode<S, A>> glb = root.greatestLowerBound(); //root.astar();
			assert( glb != null );
			if( !glb.isEmpty() ) {
				final FsssAbstractActionNode<S, A> astar = glb.get( 0 );
				final double Lstar = astar.L();
				boolean done = true;
				for( final FsssAbstractActionNode<S, A> alt : root.successors() ) {
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
			
			// TODO: Debugging: This is too much output for "conventional" logging
//			if( use_logging ) {
//				System.out.println( " ===== FSSS Iteration " + iter + " ===== " );
//				FsssTest.printTree( root, System.out, 0 );
//
//				System.out.println( "\t==> Sample count = " + model.sampleCount() );
//
//				final ArrayList<String> errors = FsssTest.validateTree( root, model );
//				System.out.println( "\tvalidateTrees(): " + errors.size() + " errors" );
//				for( int i = 0; i < errors.size(); ++i ) {
//					System.out.println( "\t[" + i + "] " + errors.get( i ) );
//				}
//			}
		}
		
		if( use_logging ) {
			System.out.println( "\t=> FSSS: " + iter + " iterations to convergence" );
			System.out.println( "\t a* = " + root.astar() );
			System.out.println( "\t L* = " + root.greatestLowerBound() );
			System.out.println( "\t Sample count: " + model.sampleCount() );
		}
	}
	
	private void fsss( final FsssAbstractStateNode<S, A> asn, final int d )
	{
		System.out.println( "\tFSSS: d = " + d + ", asn = " + asn );
		if( !asn.isTerminal() ) {
			if( !asn.isExpanded() ) {
				if( parameters.budget.isExceeded() ) {
					System.out.println( "\t\tFSSS: Exceeded budget" );
					return;
				}
				asn.expand( model.actions( asn.exemplar().s() ), parameters.width, parameters.budget );
				fireExpand( asn );
			}
			
			asn.visit();
			fireVisit( asn );
			
//			final FsssAbstractActionNode<S, A> astar = sn.astar();
			final FsssAbstractActionNode<S, A> astar = asn.astar_random();
			fireActionChoice( astar );
			
//			if( astar == null ) {
//				assert( model.sampleCount() >= parameters.max_samples );
////				System.out.println( "astar == null, but it's cool!" );
//				return;
//			}
			
			// TODO: Debugging code
			if( astar == null ) {
				FsssTest.printTree( root, System.out, 1 );
				System.out.println( asn );
			}
			
//			final FsssAbstractStateNode<S, A> sstar = astar.sstar();
			final FsssAbstractStateNode<S, A> sstar = astar.sstar_random();
			fireStateChoice( sstar );
			
			// Note: Without randomization, sstar is never null because the
			// first an lexicographically will always have been sampled before
			// time runs out. With randomization, this is no longer true. If
			// we happen to sample a valid sstar, we'd still like to do a
			// backup, to be as consistent as possible with the earlier
			// non-randomized algorithm. sstar == null should still be an
			// error if it is not due to running out of budget.
			if( sstar == null ) {
				assert( parameters.budget.isExceeded() );
				return;
			}
			
			fsss( sstar, d - 1 );
			astar.backup();
			asn.backup();
		}
		else {
			// Note: 'leaf()' calls 'visit()'
			asn.leaf();
			fireLeaf( asn );
			
			min_depth = Math.min( min_depth, d );
		}
	}
	
	// -----------------------------------------------------------------------
	
	private void fireVisit( final FsssAbstractStateNode<S, A> asn )
	{
		for( final Listener<S, A> listener : listeners ) {
			listener.onVisit( asn );
		}
	}
	
	private void fireExpand( final FsssAbstractStateNode<S, A> asn )
	{
		for( final Listener<S, A> listener : listeners ) {
			listener.onExpand( asn );
		}
	}
	
	private void fireLeaf( final FsssAbstractStateNode<S, A> asn )
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
	
	private void fireActionChoice( final FsssAbstractActionNode<S, A> asn )
	{
		for( final Listener<S, A> listener : listeners ) {
			listener.onActionChoice( asn );
		}
	}
	
	private void fireStateChoice( final FsssAbstractStateNode<S, A> asn )
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
