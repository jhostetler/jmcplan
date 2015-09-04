/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.LoggerManager;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * Right about here is where I give up. This class tries to force static
 * abstractions into the ClassifierRepresenter interface by any means
 * necessary. Proceed with great caution!
 * 
 * @author jhostetler
 */
public class StaticClassifierRepresenter<S extends State, A extends VirtualConstructor<A>>
	extends ClassifierRepresenter<S, A>
{
	private static final ch.qos.logback.classic.Logger Log = LoggerManager.getLogger( "log.search" );
	
	public StaticClassifierRepresenter( final FsssModel<S, A> model,
			final FsssAbstraction<S, A> abstraction )
	{
		super( model, abstraction );
	}

	@Override
	protected DataNode<S, A> novelInstance( final DataNode<S, A> dt_root, final FactoredRepresentation<S> x )
	{
		Log.trace( "\tnovelInstance(): {}", x );
		final DataNode<S, A> dn = ((MapSplitNode<S, A>) dt_root.split).createChild( x );
		// This is where all of the DNs that contain aggregates are created.
		// dt_leaves is never modified externally because we're not doing refinements.
		dt_leaves.add( dn );
		return dn;
	}

	@Override
	public ClassifierRepresenter<S, A> create()
	{
		return new StaticClassifierRepresenter<S, A>( model, abstraction );
	}
	
	@Override
	public ClassifierRepresenter<S, A> emptyInstance()
	{
		// In the static case the new instance is truly empty. It will be
		// created on the fly by the search algorithm.
		return create();
	}
	
	@Override
	protected DataNode<S, A> requireActionSet( final S s )
	{
		final Representation<S> arep = model.action_repr().encode( s );
		DataNode<S, A> dt_root = dt_roots.get( arep );
		if( dt_root == null ) {
			dt_root = dn_factory.createDataNode();
			dt_roots.put( arep, dt_root );
			
			// With a static abstraction, the root is always a split node
			// with one layer of children corresponding to the different
			// equivalence classes under the abstraction.
			dt_root.split = new MapSplitNode<S, A>( dn_factory );
			
//			dt_leaves.add( dt_root );
		}
		
		return dt_root;
	}
}
