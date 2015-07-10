package edu.oregonstate.eecs.mcplan.search.fsss;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

public class DataNode<S extends State, A extends VirtualConstructor<A>>
{
	public static abstract class Factory<S extends State, A extends VirtualConstructor<A>>
	{
		public abstract DataNode<S, A> createDataNode();
	}

	public static class DefaultFactory<S extends State, A extends VirtualConstructor<A>>
		extends DataNode.Factory<S, A>
	{
		private int next_id = 0;
		
		@Override
		public DataNode<S, A> createDataNode()
		{
			return new DataNode<S, A>( next_id++ );
		}
	}

	public SplitNode<S, A> split = null;
	
//	private final FsssAbstractStateNode<S, A> sentinel = new FsssAbstractStateNode<S, A>(
//		-1, null, null, null, new ArrayList<FsssStateNode<S, A>>() );
	
	public FsssAbstractStateNode<S, A> aggregate = null;
	
	public final int id;
	
	public DataNode( final int id )
	{
		this.id = id;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "DataNode@" );
		sb.append( Integer.toHexString( System.identityHashCode( this ) ) );
		sb.append( " -> " );
		if( split != null ) {
			sb.append( split );
		}
		else if( aggregate != null ) {
			sb.append( aggregate );
		}
		else {
			sb.append( "null" );
		}
		return sb.toString();
	}
}