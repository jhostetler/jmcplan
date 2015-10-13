/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.IdentityRepresentation;
import edu.oregonstate.eecs.mcplan.LoggerManager;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * The PARSS algorithm.
 */
public class ParssTreeBuilder<S extends State, A extends VirtualConstructor<A>>
	implements SearchAlgorithm<S, A>
{
	private final ch.qos.logback.classic.Logger Log = LoggerManager.getLogger( "log.search" );
	
	private final FsssParameters parameters;
	private final FsssModel<S, A> model;
	private final FsssAbstractStateNode<S, A> root;
	
	private final PriorityRefinementOrder.Factory<S, A> refinement_order_factory;
	
	private boolean complete = false;
	private final int min_depth = Integer.MAX_VALUE;
	
	private int num_refinements = 0;
	private int num_lead_changes = 0;
	
	public ParssTreeBuilder( final FsssParameters parameters,
							   final FsssModel<S, A> model, final FsssAbstraction<S, A> abstraction,
							   final S initial_state,
							   final PriorityRefinementOrder.Factory<S, A> refinement_order_factory )
	{
		this.parameters = parameters;
		this.model = model;
		
		this.refinement_order_factory = refinement_order_factory;
		
		final FsssStateNode<S, A> groot = new FsssStateNode<S, A>( parameters.depth, model, initial_state );
		final ArrayList<FsssStateNode<S, A>> root_states = new ArrayList<FsssStateNode<S, A>>();
		root_states.add( groot );
		root = new FsssAbstractStateNode<S, A>(
			parameters.depth, model, abstraction, new IdentityRepresentation<S>(), root_states );
	}
	
	@Override
	public FsssAbstractStateNode<S, A> root()
	{
		return root;
	}
	
	/**
	 * Returns true if the *initial* (ie. pre-refinement) run of FSSS ran
	 * until convergence (rather than running out of sample budget).
	 * 
	 * Note that if you are using refinement, and refinements were actually
	 * performed, this by definition means that the first FSSS run converged,
	 * and thus isComplete() will return true. It doesn't tell you anything
	 * about whether the last refinement was interrupted.
	 * @return
	 */
	public boolean isComplete()
	{
		return complete;
	}
	
	public boolean isFullDepth()
	{
		return min_depth == 1;
	}
	
	@Override
	public int numRefinements()
	{
		return num_refinements;
	}
	
	@Override
	public int numLeadChanges()
	{
		return num_lead_changes;
	}
	
	@Override
	public void run()
	{
		parameters.budget.reset();
		num_refinements = 0;
		complete = false;
		
		// AB-FSSS
		final AbstractFsss<S, A> fsss = new AbstractFsss<S, A>( parameters, model, root );
		fsss.run();
		
		ArrayList<FsssAbstractActionNode<S, A>> lstar = Fn.copy( root.greatestLowerBound() );
		
		if( refinement_order_factory != null ) {
			final PriorityRefinementOrder<S, A> refinement_order
				= refinement_order_factory.create( parameters, model, root );
			
			if( Log.isDebugEnabled() ) {
				Log.debug( " ===== Before refinement ===== " );
				logDiagnostics();
			}
			
			while( !parameters.budget.isExceeded() ) {
				if( refinement_order.isClosed() ) {
					// Tree is fully refined.
					Log.info( "\t+++++ Tree is fully refined +++++" );
					break;
				}
				
				// Refine one state
				refinement_order.refine();
				num_refinements += 1;
				
//				final FsssNodeCloser<S, A> closer = new FsssNodeCloser<S, A>();
//				closer.traverse( root );
				
				// If the intersection of the old optimal action set and the
				// new optimal action set is empty, the best action has changed
				final ArrayList<FsssAbstractActionNode<S, A>> lprime = Fn.copy( root.greatestLowerBound() );
				lstar.retainAll( lprime );
				if( lstar.isEmpty() ) {
					Log.debug( "\t\tLead change!" );
					num_lead_changes += 1;
				}
				lstar = lprime;
				
				if( Log.isDebugEnabled() ) {
					Log.debug( " ===== After refinement ===== " );
					logDiagnostics();
				}
			}
		}
		
		if( Log.isDebugEnabled() ) {
			Log.debug( " ===== Final ===== " );
			logDiagnostics();
		}
	}
	
	private void logDiagnostics()
	{
		FsssTest.printTree( root, Log, 0 );
	
		Log.debug( "\t==> Sample count = {}", model.sampleCount() );
		
		final ArrayList<String> errors = FsssTest.validateTree( parameters, root(), model );
		Log.debug( "\tvalidateTrees(): {} errors", errors.size() );
		for( int i = 0; i < errors.size(); ++i ) {
			Log.debug( "\t\t[{}] {}", i, errors.get( i ) );
		}
		
		final ArrayList<String> dt_errors = FsssTest.validateAllDecisionTrees( root() );
		Log.debug( "\tvalidateAllDecisionTrees(): {} errors", dt_errors.size() );
		for( int i = 0; i < dt_errors.size(); ++i ) {
			Log.debug( "\t\t[{}] {}", i, dt_errors.get( i ) );
		}
	}
}
