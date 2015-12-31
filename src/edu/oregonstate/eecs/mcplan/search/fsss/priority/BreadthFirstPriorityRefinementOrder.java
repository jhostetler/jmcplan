/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss.priority;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssAbstractStateNode;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssModel;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssParameters;
import edu.oregonstate.eecs.mcplan.search.fsss.PriorityRefinementOrder;

/**
 * @author jhostetler
 *
 */
public class BreadthFirstPriorityRefinementOrder<S extends State, A extends VirtualConstructor<A>>
	extends PriorityRefinementOrder<S, A>
{
	public static class Factory<S extends State, A extends VirtualConstructor<A>>
		implements PriorityRefinementOrder.Factory<S, A>
	{
		@Override
		public PriorityRefinementOrder<S, A> create( final FsssParameters parameters, final FsssModel<S, A> model,
													 final FsssAbstractStateNode<S, A> root )
		{
			return new BreadthFirstPriorityRefinementOrder<S, A>( parameters, model, root );
		}
		
		@Override
		public String toString()
		{ return "priority.bf"; }
	}
	
	// -----------------------------------------------------------------------
	
	public BreadthFirstPriorityRefinementOrder( final FsssParameters parameters,
			final FsssModel<S, A> model, final FsssAbstractStateNode<S, A> root )
	{
		super( parameters, model, root );
	}

	@Override
	protected double calculatePriority( final FsssAbstractStateNode<S, A> asn )
	{
		// Nodes near the root have larger depth, so we take the inverse
		return 1.0 / asn.depth;
	}
}
