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
public class FsssStaticAbstraction<S extends State, A extends VirtualConstructor<A>> extends FsssAbstraction<S, A>
{
	public static <S extends State, A extends VirtualConstructor<A>>
	FsssStaticAbstraction<S, A> create( final FsssModel<S, A> model )
	{
		return new FsssStaticAbstraction<S, A>( model );
	}
	
	// -----------------------------------------------------------------------
	
	private final FsssModel<S, A> model;
	
	public FsssStaticAbstraction( final FsssModel<S, A> model )
	{
		this.model = model;
	}
	
	@Override
	public String toString()
	{
		return "StaticAbstraction";
	}
	
	@Override
	public ClassifierRepresenter<S, A> createRepresenter()
	{
		return new RefineableRepresenterWrapper<S, A>( model, this );
	}

}
