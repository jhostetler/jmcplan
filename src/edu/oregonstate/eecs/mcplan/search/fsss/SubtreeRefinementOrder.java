package edu.oregonstate.eecs.mcplan.search.fsss;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.search.fsss.ClassifierRepresenter.DataNode;

public interface SubtreeRefinementOrder<S extends State, A extends VirtualConstructor<A>>
	extends RefinementOrder<S, A>
{
	public static interface Factory<S extends State, A extends VirtualConstructor<A>>
	{
		public abstract SubtreeRefinementOrder<S, A> create(
			final FsssParameters parameters, final FsssModel<S, A> model, final FsssAbstractActionNode<S, A> root );
	}
	
	public static class Split
	{
		public final int attribute;
		public final double value;
		
		public Split( final int attribute, final double value )
		{
			this.attribute = attribute;
			this.value = value;
		}
	}
	
	public static class SplitChoice<S extends State, A extends VirtualConstructor<A>>
	{
		public final DataNode<S, A> dn;
		public final Split split;
		
		public SplitChoice( final DataNode<S, A> dn, final Split split )
		{
			this.dn = dn;
			this.split = split;
		}
	}
	
	public static interface SplitChooser<S extends State, A extends VirtualConstructor<A>>
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
	}
	
	public abstract FsssAbstractActionNode<S, A> rootAction();
}
