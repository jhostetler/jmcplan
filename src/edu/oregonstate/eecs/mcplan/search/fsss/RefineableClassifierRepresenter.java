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
public abstract class RefineableClassifierRepresenter<S extends State, A extends VirtualConstructor<A>>
	extends ClassifierRepresenter<S, A>
{

	public RefineableClassifierRepresenter( final FsssModel<S, A> model, final FsssAbstraction<S, A> abstraction )
	{
		super( model, abstraction );
	}
	
	@Override
	public abstract RefineableClassifierRepresenter<S, A> create();
	
	public abstract boolean refine( final FsssAbstractActionNode<S, A> aan );
}
