package edu.oregonstate.eecs.mcplan.search.fsss;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

public interface SubtreeRefinementOrder<S extends State, A extends VirtualConstructor<A>>
	extends RefinementOrder<S, A>
{
	public static interface Factory<S extends State, A extends VirtualConstructor<A>>
	{
		public abstract SubtreeRefinementOrder<S, A> create(
			final FsssParameters parameters, final FsssModel<S, A> model, final FsssAbstractActionNode<S, A> root );
	}
	
	public abstract FsssAbstractActionNode<S, A> rootAction();
}
