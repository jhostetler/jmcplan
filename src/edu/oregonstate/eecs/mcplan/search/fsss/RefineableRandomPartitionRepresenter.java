/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.abstraction.IndexRepresentation;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public class RefineableRandomPartitionRepresenter<S extends State, A extends VirtualConstructor<A>>
	extends RefineablePartitionTreeRepresenter<S, A>
{
	public static class Abstraction<S extends State, A extends VirtualConstructor<A>>
		extends FsssAbstraction<S, A>
	{
		private final FsssModel<S, A> model;
	
		public Abstraction( final FsssModel<S, A> model )
		{
			this.model = model;
		}
		
		@Override
		public String toString()
		{
			return "RandomPartitionRefinement";
		}
		
		@Override
		public ClassifierRepresenter<S, A> createRepresenter()
		{
			return new RefineableRandomPartitionRepresenter<S, A>( model, this );
		}
	}
	
	private static class MapBinarySplitNode<S extends State, A extends VirtualConstructor<A>>
		extends SplitNode<S, A>
	{
		public final DataNode<S, A> left;
		public final DataNode<S, A> right;
		
		public Map<Representation<S>, DataNode<S, A>> assignments = new LinkedHashMap<Representation<S>, DataNode<S, A>>();
		
		public MapBinarySplitNode( final DataNodeFactory<S, A> f )
		{
			this.left = f.createDataNode();
			this.right = f.createDataNode();
		}
		
		@Override
		public String toString()
		{
			return "{MapSplit: " + assignments + "}";
		}
		
		/**
		 * FIXME: This function shouldn't be called "create", because you
		 * don't really want an empty one. You want a copy that doesn't contain
		 * the instances from the old one, but is behaviorally equivalent.
		 * @param f
		 * @return
		 */
		@Override
		public SplitNode<S, A> create( final DataNodeFactory<S, A> f )
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
				if( left.aggregate.states().size() < right.aggregate.states().size() ) {
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
	
	// -----------------------------------------------------------------------

	public RefineableRandomPartitionRepresenter( final FsssModel<S, A> model,
			final FsssAbstraction<S, A> abstraction )
	{
		super( model, abstraction, null /*split_chooser*/ );
	}
	
	@Override
	public RefineableRandomPartitionRepresenter<S, A> create()
	{
		return new RefineableRandomPartitionRepresenter<S, A>( model, abstraction );
	}
	
	@Override
	protected DataNode<S, A> createSplitNode( final FsssAbstractActionNode<S, A> aan )
	{
		final DataNode<S, A> choice;
		while( true ) {
			final DataNode<S, A> candidate = largestChild( aan );
			if( candidate == null ) {
				// No more refinements to do on 'aan'
				return null;
			}
//			else if( candidate.aggregate.states().size() == 1 ) {
//				// Candidate is fully refined
////				System.out.println( "\tRefine: closing (singleton): " + candidate );
//				candidate.close();
//			}
			else {
				final ArrayList<FsssStateNode<S, A>> states = candidate.aggregate.states();
				final FactoredRepresentation<S> ex = states.get( 0 ).x();
				boolean pure = true;
				for( int i = 1; i < states.size(); ++i ) {
					if( ! states.get( i ).x().equals( ex ) ) {
						pure = false;
						break;
					}
				}
				
				if( pure ) {
					// Candidate is fully refined
//					System.out.println( "\tRefine: closing (pure): " + candidate );
//					candidate.close();
				}
				else {
					choice = candidate;
					break;
				}
			}
		}
		
		assert( choice.split == null );
		choice.split = new MapBinarySplitNode<S, A>( dn_factory );
		
		for( final DataNode<S, A> dn_child : Fn.in( choice.split.children() ) ) {
			dn_child.aggregate = new FsssAbstractStateNode<S, A>(
				aan, model, abstraction, new IndexRepresentation<S>( dn_child.id ) );
		}
		
		final ArrayList<FsssStateNode<S, A>> shuffled = new ArrayList<FsssStateNode<S, A>>( choice.aggregate.states() );
		Fn.shuffle( model.rng(), shuffled );
		for( final FsssStateNode<S, A> gsn : shuffled ) {
			choice.split.addGroundStateNode( gsn );
		}
		
		return choice;
	}
}
