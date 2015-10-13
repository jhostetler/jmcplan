package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * Implementation of SplitNode using a map to split instances into arbitrary
 * sets.
 * @param <S>
 * @param <A>
 */
public class MapBinarySplitNode<S extends State, A extends VirtualConstructor<A>>
	extends SplitNode<S, A>
{
	public final DataNode<S, A> left;
	public final DataNode<S, A> right;
	
	public Map<Representation<S>, DataNode<S, A>> assignments = new LinkedHashMap<Representation<S>, DataNode<S, A>>();
	
	public MapBinarySplitNode( final DataNode.Factory<S, A> f )
	{
		this.left = f.createDataNode();
		this.right = f.createDataNode();
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
		for( final Map.Entry<Representation<S>, DataNode<S, A>> e : assignments.entrySet() ) {
			if( e.getValue() == left ) {
				copy.assignments.put( e.getKey(), copy.left );
			}
			else {
				assert( e.getValue() == right );
				copy.assignments.put( e.getKey(), copy.right );
			}
		}
		return copy;
	}
	
	@Override
	public void addGroundStateNode( final FsssStateNode<S, A> gsn )
	{
		DataNode<S, A> dn = child( gsn.x() );
		if( dn == null ) {
			if( left.aggregate.n() < right.aggregate.n() ) {
				dn = left;
			}
			else {
				dn = right;
			}
			assignments.put( gsn.x(), dn );
		}
		dn.aggregate.addGroundStateNode( gsn );
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
			int i = 0;
			
			@Override
			public boolean hasNext()
			{ return i < 2; }

			@Override
			public DataNode<S, A> next()
			{
				switch( i++ ) {
				case 0: return left;
				case 1: return right;
				default: throw new IllegalStateException( "hasNext() == false" );
				}
			}
		};
	}
}