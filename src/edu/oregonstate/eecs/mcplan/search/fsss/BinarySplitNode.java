package edu.oregonstate.eecs.mcplan.search.fsss;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Generator;

public class BinarySplitNode<S extends State, A extends VirtualConstructor<A>> extends SplitNode<S, A>
{
	public final int attribute;
	public final double threshold;
	
	public final DataNode<S, A> left;
	public final DataNode<S, A> right;
	
	public BinarySplitNode( final DataNode.Factory<S, A> f, final int attribute, final double threshold )
	{
		this.left = f.createDataNode();
		this.right = f.createDataNode();
		this.attribute = attribute;
		this.threshold = threshold;
	}
	
	@Override
	public SplitNode<S, A> create( final DataNode.Factory<S, A> f )
	{
		return new BinarySplitNode<S, A>( f, attribute, threshold );
	}
	
	@Override
	public void addGroundStateNode( final FsssStateNode<S, A> gsn )
	{
		child( gsn.x() ).aggregate.addGroundStateNode( gsn );
	}
	
	@Override
	public DataNode<S, A> child( final FactoredRepresentation<S> x )
	{
		if( x.phi()[attribute] < threshold ) {
			return left;
		}
		else {
			return right;
		}
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
	
	@Override
	public String toString()
	{
		return attribute + "@" + threshold;
	}
}
