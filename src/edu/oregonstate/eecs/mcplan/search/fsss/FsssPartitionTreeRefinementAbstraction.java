/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class FsssPartitionTreeRefinementAbstraction<S extends State, A extends VirtualConstructor<A>>
	extends FsssAbstraction<S, A>
{
	public static <S extends State, A extends VirtualConstructor<A>>
	FsssPartitionTreeRefinementAbstraction<S, A> create( final FsssModel<S, A> model,
														 final SplitChooser<S, A> split_chooser )
	{
		return new FsssPartitionTreeRefinementAbstraction<S, A>( model, split_chooser );
	}
	
	// -----------------------------------------------------------------------
	
	private final FsssModel<S, A> model;
	private final SplitChooser<S, A> split_chooser;
	
	public FsssPartitionTreeRefinementAbstraction( final FsssModel<S, A> model,
												   final SplitChooser<S, A> split_chooser )
	{
		this.model = model;
		this.split_chooser = split_chooser;
	}
	
	@Override
	public String toString()
	{
		return "PartitionTreeRefinement";
	}
	
	@Override
	public RefineablePartitionTreeRepresenter<S, A> createRepresenter()
	{
		return new RefineablePartitionTreeRepresenter<S, A>( model, this, split_chooser );
	}

}
