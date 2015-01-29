/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
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
	FsssPartitionTreeRefinementAbstraction<S, A> create( final FsssModel<S, A> model )
	{
		return new FsssPartitionTreeRefinementAbstraction<S, A>( model );
	}
	
	// -----------------------------------------------------------------------
	
	private final FsssModel<S, A> model;
	
	public FsssPartitionTreeRefinementAbstraction( final FsssModel<S, A> model )
	{
		this.model = model;
	}
	
	@Override
	public Representer<S, ? extends Representation<S>> createRepresenter()
	{
		return new RefineablePartitionTreeRepresenter<S, A>(
			model, this, model.base_repr().create(), model.action_repr().create() );
	}

}
