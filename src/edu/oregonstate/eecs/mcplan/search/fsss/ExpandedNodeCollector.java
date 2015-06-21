package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * Collects all ASNs that were Expanded during a complete run of FSSS.
 * 
 * @author jhostetler
 *
 * @param <S>
 * @param <A>
 */
public final class ExpandedNodeCollector<S extends State, A extends VirtualConstructor<A>>
		implements AbstractFsss.Listener<S, A>
{
	private FsssAbstractActionNode<S, A> root_action = null;
	public final Map<FsssAbstractActionNode<S, A>, ArrayList<FsssAbstractStateNode<S, A>>>
		expanded = new HashMap<FsssAbstractActionNode<S, A>, ArrayList<FsssAbstractStateNode<S, A>>>();
	
	@Override
	public void onVisit( final FsssAbstractStateNode<S, A> asn )
	{ }

	@Override
	public void onExpand( final FsssAbstractStateNode<S, A> asn )
	{
		System.out.println( "\tExpandedNodeCollector.onExpand()" );
		assert( root_action != null );
		ArrayList<FsssAbstractStateNode<S, A>> subtree = expanded.get( root_action );
		if( subtree == null ) {
			subtree = new ArrayList<FsssAbstractStateNode<S, A>>();
			expanded.put( root_action, subtree );
		}
		subtree.add( asn );
	}

	@Override
	public void onLeaf( final FsssAbstractStateNode<S, A> asn )
	{ }

	@Override
	public void onTrajectoryStart()
	{
		root_action = null;
	}

	@Override
	public void onActionChoice( final FsssAbstractActionNode<S, A> aan )
	{
		if( root_action == null ) {
			root_action = aan;
		}
	}

	@Override
	public void onStateChoice( final FsssAbstractStateNode<S, A> asn )
	{ }

	@Override
	public void onTrajectoryEnd()
	{ }
}
