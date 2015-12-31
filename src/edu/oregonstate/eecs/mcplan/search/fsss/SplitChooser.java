package edu.oregonstate.eecs.mcplan.search.fsss;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

public interface SplitChooser<S extends State, A extends VirtualConstructor<A>>
{
	public static interface Factory<S extends State, A extends VirtualConstructor<A>>
	{
		public abstract SplitChooser<S, A> createSplitChooser(
			final FsssParameters parameters, final FsssModel<S, A> model );
	}
	
	/**
	 * Chooses an attribute and value to split on. Must return 'null' if no
	 * more refinements should be attempted on 'aan'. Must return a non-null
	 * SplitChoice with non-null .dn and null .split member to indicate that no
	 * more refinements should be attempted for the .dn member.
	 * 
	 * @param aan
	 * @return
	 */
	public abstract SplitChoice<S, A> chooseSplit( final FsssAbstractActionNode<S, A> aan );
	
	/**
	 * Returns an attribute and value to split on. Returns 'null' if no
	 * attribute-value pair partitions the state into two non-empty sets.
	 * @param asn
	 * @return
	 */
	public abstract Split chooseSplit( final DataNode<S, A> dn );
}