package edu.oregonstate.eecs.mcplan.search.fsss;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Generator;

public abstract class SplitNode<S extends State, A extends VirtualConstructor<A>>
{
	public abstract DataNode<S, A> child( final FactoredRepresentation<S> phi );
	public abstract Generator<? extends DataNode<S, A>> children();
	public abstract void addGroundStateNode( final FsssStateNode<S, A> gsn );
	
	public abstract SplitNode<S, A> create( final DataNode.Factory<S, A> f );
}