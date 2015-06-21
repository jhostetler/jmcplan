package edu.oregonstate.eecs.mcplan.search.fsss;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

public interface RefinementOrder<S extends State, A extends VirtualConstructor<A>>
{
	public static interface Factory<S extends State, A extends VirtualConstructor<A>>
	{
		public abstract RefinementOrder<S, A> create(
			final FsssParameters parameters, final FsssModel<S, A> model, final FsssAbstractStateNode<S, A> root );
	}
	
	public abstract boolean isClosed();
	public abstract void refine();
}
