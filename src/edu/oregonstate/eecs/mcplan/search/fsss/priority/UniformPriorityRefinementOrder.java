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
public class UniformPriorityRefinementOrder<S extends State, A extends VirtualConstructor<A>>
	extends PriorityRefinementOrder<S, A>
{
	public static class Factory<S extends State, A extends VirtualConstructor<A>>
		implements PriorityRefinementOrder.Factory<S, A>
	{
		@Override
		public PriorityRefinementOrder<S, A> create( final FsssParameters parameters, final FsssModel<S, A> model,
													 final FsssAbstractStateNode<S, A> root )
		{
			return new UniformPriorityRefinementOrder<S, A>( parameters, model, root );
		}
		
		@Override
		public String toString()
		{ return "priority.uniform"; }
	}
	
	// -----------------------------------------------------------------------
	
	public UniformPriorityRefinementOrder( final FsssParameters parameters,
			final FsssModel<S, A> model, final FsssAbstractStateNode<S, A> root )
	{
		super( parameters, model, root );
	}

	@Override
	protected double calculatePriority( final FsssAbstractStateNode<S, A> asn )
	{
		return 1.0;
	}
}
