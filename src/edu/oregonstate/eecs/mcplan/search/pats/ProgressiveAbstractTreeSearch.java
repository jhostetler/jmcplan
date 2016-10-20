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
package edu.oregonstate.eecs.mcplan.search.pats;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.LoggerManager;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssAbstractActionNode;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssParameters;
import edu.oregonstate.eecs.mcplan.search.fsss.PriorityRefinementOrder;
import edu.oregonstate.eecs.mcplan.search.pats.ForwardSearchSparseSampling.Listener;
import edu.oregonstate.eecs.mcplan.sim.TransitionSimulator;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class ProgressiveAbstractTreeSearch<S extends State, A> implements Runnable
{
	private final ch.qos.logback.classic.Logger Log = LoggerManager.getLogger( "log.search" );
	
	private final FsssParameters parameters;
	private final TransitionSimulator<S, A> sim;
	private final PatsStateNode<S, A> root;
	
	private final ArrayList<Listener<S, A>> listeners = new ArrayList<Listener<S, A>>();
	private boolean complete = false;
	private final int min_depth = Integer.MAX_VALUE;
	
	/**
	 * @param parameters
	 * @param model
	 * @param root Root state node of partial AFSSS tree
	 */
	public ProgressiveAbstractTreeSearch( final FsssParameters parameters, final TransitionSimulator<S, A> sim,
						 				final PatsStateNode<S, A> root, final AbstractionStrategy<S, A> ab )
	{
		this.parameters = parameters;
		this.sim = sim;
		this.root = root;
	}
	
	@Override
	public void run()
	{
		parameters.budget.reset();
		num_refinements = 0;
		complete = false;
		
		// AB-FSSS
		final ForwardSearchSparseSampling<S, A> fsss = new ForwardSearchSparseSampling<S, A>( parameters, sim, root );
		fsss.run();
		
		ArrayList<PatsActionNode<S, A>> lstar = Fn.copy( root.greatestLowerBound() );
		
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
}
