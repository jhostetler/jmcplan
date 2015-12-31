package edu.oregonstate.eecs.mcplan.search.fsss;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

public class SplitChoice<S extends State, A extends VirtualConstructor<A>>
{
	public final DataNode<S, A> dn;
	public final Split split;
	
	public SplitChoice( final DataNode<S, A> dn, final Split split )
	{
		this.dn = dn;
		this.split = split;
	}
}