/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public class MapSplitNode<S extends State, A extends VirtualConstructor<A>>
	extends SplitNode<S, A>
{
	// Needs to be a LinkedHashMap to ensure consistent iteration order over different runs.
	public Map<Representation<S>, DataNode<S, A>> assignments = new LinkedHashMap<Representation<S>, DataNode<S, A>>();
	
	private final DataNode.Factory<S, A> dn_factory;
	
	public MapSplitNode( final DataNode.Factory<S, A> f )
	{
		dn_factory = f;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "{MapSplit: " );
		for( final Map.Entry<Representation<S>, DataNode<S, A>> e : assignments.entrySet() ) {
			sb.append( e.getKey() ).append( " -> " )
			  .append( "@" ).append( Integer.toHexString( System.identityHashCode( e.getValue() ) ) );
		}
		sb.append( "}" );
		return sb.toString();
//		return "{MapSplit: " + assignments + "}";
	}
	
	/**
	 * FIXME: This function shouldn't be called "create", because you
	 * don't really want an empty one. You want a copy that doesn't contain
	 * the instances from the old one, but is behaviorally equivalent.
	 * @param f
	 * @return
	 */
	@Override
	public SplitNode<S, A> create( final DataNode.Factory<S, A> f )
	{
		final MapBinarySplitNode<S, A> copy = new MapBinarySplitNode<S, A>( f );
		return copy;
	}
	
	@Override
	public void addGroundStateNode( final FsssStateNode<S, A> gsn )
	{
		final DataNode<S, A> dn = child( gsn.x() );
		assert( dn != null );
//		if( dn == null ) {
//			dn = dn_factory.createDataNode();
//			assignments.put( gsn.x(), dn );
//		}
		dn.aggregate.addGroundStateNode( gsn );
	}
	
	public DataNode<S, A> createChild( final FactoredRepresentation<S> x )
	{
		final DataNode<S, A> dn = dn_factory.createDataNode();
		final DataNode<S, A> check = assignments.put( x, dn );
		assert( check == null );
		return dn;
	}
	
	@Override
	public DataNode<S, A> child( final FactoredRepresentation<S> x )
	{
		return assignments.get( x );
	}

	@Override
	public Generator<? extends DataNode<S, A>> children()
	{
		return new Generator<DataNode<S, A>>() {
			final Iterator<DataNode<S, A>> itr = assignments.values().iterator();
			
			@Override
			public boolean hasNext()
			{ return itr.hasNext(); }

			@Override
			public DataNode<S, A> next()
			{ return itr.next(); }
		};
	}
}
