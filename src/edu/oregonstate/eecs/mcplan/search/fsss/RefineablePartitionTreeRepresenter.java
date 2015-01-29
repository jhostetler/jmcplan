/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.abstraction.IndexRepresentation;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public class RefineablePartitionTreeRepresenter<S extends State, A extends VirtualConstructor<A>>
	implements Representer<S, Representation<S>>
{
	public static <S extends State, A extends VirtualConstructor<A>>
	void printDecisionTree( final RefineablePartitionTreeRepresenter<S, A>.DataNode dn, final int ws )
	{
		for( int i = 0; i < ws; ++i ) {
			System.out.print( "|-" );
		}
		System.out.print( "[id: " );
		System.out.print( dn.id );
		if( dn.split != null ) {
			System.out.print( ", " );
			System.out.print( dn.split.attribute );
			System.out.print( "@" );
			System.out.print( dn.split.threshold );
			System.out.println( "]" );
			printDecisionTree( dn.split.left, ws + 1 );
			printDecisionTree( dn.split.right, ws + 1 );
		}
		else {
			System.out.print( " (leaf): " );
			System.out.println( dn.aggregate );
		}
	}
	
	/**
	 * Returns the DataNode containing the non-terminal successor of 'aan'
	 * with the most constituent ground state nodes. Returns null if all
	 * successors are terminals or singletons.
	 * @param aan
	 * @return
	 */
	public static <S extends State, A extends VirtualConstructor<A>>
	RefineablePartitionTreeRepresenter<S, A>.DataNode largestChild( final FsssAbstractActionNode<S, A> aan )
	{
		final RefineablePartitionTreeRepresenter<S, A> repr = aan.repr;
		
		// Heuristic choice 1: Choose *largest* child node
		RefineablePartitionTreeRepresenter<S, A>.DataNode largest_child = null;
		for( final RefineablePartitionTreeRepresenter<S, A>.DataNode child : repr.dt_leaves ) {
			if( child.aggregate == null ) {
				// Note: We think this is okay. A dt leaf can have a null
				// aggregate if the node has been refined and all of the
				// GSNs that used to belong in that leaf ended up in the
				// other half of the tree. A null leaf will be initialized
				// the first time any sample trajectory ends up there.
				// @see RefineablePartitionTreeRepresenter.addTrainingSample()
				continue;
			}
			// Don't consider ASNs that are: closed, unvisisted by FSSS, are
			// terminal states, or are already fully refined.
			// FIXME: Wouldn't it be an error if "fully refined" did not imply "closed"?
			if( child.isClosed() || child.aggregate.nvisits() == 0
					|| child.aggregate.isTerminal() || child.aggregate.states().size() == 1 ) {
				continue;
			}
			if( largest_child == null || child.aggregate.states().size() > largest_child.aggregate.states().size() ) {
				largest_child = child;
			}
		}
		return largest_child;
	}
	
	// -----------------------------------------------------------------------
	
	private abstract class SplitNode
	{
		public abstract DataNode child( final double[] phi );
		public abstract Generator<? extends DataNode> children();
	}
	
	public class DataNode
	{
		public BinarySplitNode split = null;
		public FsssAbstractStateNode<S, A> aggregate = null;
		
		public final int id;
		private boolean closed = false;
		
		public DataNode( final int id )
		{
			this.id = id;
		}

		public boolean isClosed()
		{
			return closed;
		}
		
		public void close()
		{
			assert( !closed );
			closed = true;
		}
	}
	
	public class BinarySplitNode extends SplitNode
	{
		public final int attribute;
		public final double threshold;
		
		public DataNode left = null;
		public DataNode right = null;
		
		public BinarySplitNode( final int attribute, final double threshold )
		{
			this.attribute = attribute;
			this.threshold = threshold;
		}
		
		@Override
		public DataNode child( final double[] phi )
		{
			if( phi[attribute] < threshold ) {
				return left;
			}
			else {
				return right;
			}
		}

		@Override
		public Generator<? extends DataNode> children()
		{
			return new Generator<DataNode>() {
				int i = 0;
				
				@Override
				public boolean hasNext()
				{ return i < 2; }

				@Override
				public DataNode next()
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
	
	private class DataNodeFactory
	{
		private int next_id = 0;
		
		public DataNode createDataNode()
		{
			return new DataNode( next_id++ );
		}
	}
	
	// -----------------------------------------------------------------------
	
	public final FsssModel<S, A> model;
	public final FsssAbstraction<S, A> abstraction;
	public final FactoredRepresenter<S, ? extends FactoredRepresentation<S>> base_repr;
	public final Representer<S, ? extends Representation<S>> action_repr;
	
	private final DataNodeFactory dn_factory = new DataNodeFactory();
	public final Map<Representation<S>, DataNode> dt_roots = new HashMap<Representation<S>, DataNode>();
//	private final DataNode dt_root;
	
	public final ArrayList<DataNode> dt_leaves = new ArrayList<DataNode>();
	
	public RefineablePartitionTreeRepresenter( final FsssModel<S, A> model, final FsssAbstraction<S, A> abstraction,
											   final FactoredRepresenter<S, ? extends FactoredRepresentation<S>> repr,
											   final Representer<S, ? extends Representation<S>> action_repr )
	{
		this.base_repr = repr;
		this.action_repr = action_repr;
		this.model = model;
		this.abstraction = abstraction;
//		dt_root = dn_factory.createDataNode();
//		dt_leaves.add( dt_root );
	}
	
	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.search.fsss.RefineableRepresenter#create()
	 */
	@Override
	public RefineablePartitionTreeRepresenter<S, A> create()
	{
		return new RefineablePartitionTreeRepresenter<S, A>( model, abstraction, base_repr, action_repr );
	}
	
	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.search.fsss.RefineableRepresenter#emptyInstance()
	 */
	public RefineablePartitionTreeRepresenter<S, A> emptyInstance()
	{
		final RefineablePartitionTreeRepresenter<S, A> dup
			= new RefineablePartitionTreeRepresenter<S, A>( model, abstraction, base_repr, action_repr );
		
		for( final Representation<S> akey : dt_roots.keySet() ) {
			final DataNode dt_root = dt_roots.get( akey );
			final DataNode dup_dt_root = dup.dn_factory.createDataNode();
			dup.dt_roots.put( akey, dup_dt_root );
			// Perform a DFS on 'this' decision tree and copy the nodes into 'dup'.
			// Do not initialize any .aggregate members.
			final Deque<DataNode> stack = new ArrayDeque<DataNode>();
			final Deque<DataNode> dup_stack = new ArrayDeque<DataNode>();
			stack.push( dt_root );
			dup_stack.push( dup_dt_root );
			while( !stack.isEmpty() ) {
				final DataNode dn = stack.pop();
				final DataNode dup_dn = dup_stack.pop();
				if( dn.split != null ) {
					stack.push( dn.split.left );
					stack.push( dn.split.right );
					
					dup_dn.split = new BinarySplitNode( dn.split.attribute, dn.split.threshold );
					dup_dn.split.left = dup.dn_factory.createDataNode();
					dup_dn.split.right = dup.dn_factory.createDataNode();
					dup_stack.push( dup_dn.split.left );
					dup_stack.push( dup_dn.split.right );
				}
				else {
					dup.dt_leaves.add( dup_dn );
				}
			}
		}
		
		return dup;
	}
	
	private DataNode getActionSet( final S s )
	{
		final Representation<S> arep = action_repr.encode( s );
		final DataNode dt_root = dt_roots.get( arep );
		return dt_root;
	}
	
	private DataNode requireActionSet( final S s )
	{
		final Representation<S> arep = action_repr.encode( s );
		DataNode dt_root = dt_roots.get( arep );
		if( dt_root == null ) {
			dt_root = dn_factory.createDataNode();
			dt_roots.put( arep, dt_root );
			dt_leaves.add( dt_root );
		}
		
		return dt_root;
	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.search.fsss.RefineableRepresenter#encode(S)
	 */
	@Override
	public Representation<S> encode( final S s )
	{
		final DataNode dt_root = getActionSet( s );
		final FactoredRepresentation<S> x = base_repr.encode( s );
		final DataNode dn = classify( dt_root, x );
		assert( dn != null );
//		if( dn.aggregate == null ) {
////			dn.aggregate = new FsssAbstractStateNode<S, A>( an, model, abstraction, new IndexRepresentation<S>( dn.id ) );
////			System.out.println( "! No aggregate node in " + x );
//			for( final DataNode r : dt_roots.values() ) {
//				System.out.println( "******" );
//				RefineablePartitionTreeRepresenter.printDecisionTree( r, 1 );
//			}
//			error = "! No aggregate node in " + x + " (dn.id = " + dn.id + ")";
//		}
		return dn.aggregate.x();
	}
	
	public String error = "";

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.search.fsss.RefineableRepresenter#refine(edu.oregonstate.eecs.mcplan.search.fsss.FsssAbstractActionNode, edu.oregonstate.eecs.mcplan.search.fsss.RefineablePartitionTreeRepresenter, int, double)
	 */
	public ArrayList<FsssAbstractStateNode<S, A>> refine( final FsssAbstractActionNode<S, A> an,
								   final DataNode dn, final int attribute, final double split )
	{
		dt_leaves.remove( dn );
		dn.split = new BinarySplitNode( attribute, split );
		dn.split.left = dn_factory.createDataNode();
		dt_leaves.add( dn.split.left );
		dn.split.left.aggregate
			= new FsssAbstractStateNode<S, A>( an, model, abstraction, new IndexRepresentation<S>( dn.split.left.id ) );
		dn.split.left.aggregate.visit();
		dn.split.right = dn_factory.createDataNode();
		dt_leaves.add( dn.split.right );
		dn.split.right.aggregate
			= new FsssAbstractStateNode<S, A>( an, model, abstraction, new IndexRepresentation<S>( dn.split.right.id ) );
		dn.split.right.aggregate.visit();
		for( final FsssStateNode<S, A> gsn : dn.aggregate.states() ) {
			if( gsn.x().phi()[attribute] < split ) {
				dn.split.left.aggregate.addGroundStateNode( gsn );
			}
			else {
				dn.split.right.aggregate.addGroundStateNode( gsn );
			}
		}
		
		final ArrayList<FsssAbstractStateNode<S, A>> parts = new ArrayList<FsssAbstractStateNode<S, A>>();
		for( final RefineablePartitionTreeRepresenter<S, A>.DataNode d : Fn.in( dn.split.children() ) ) {
			parts.add( d.aggregate );
		}
		
		return parts;
	}
	
	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.search.fsss.RefineableRepresenter#classify(edu.oregonstate.eecs.mcplan.search.fsss.RefineablePartitionTreeRepresenter, edu.oregonstate.eecs.mcplan.FactoredRepresentation)
	 */
	public DataNode classify( final DataNode dt_root, final FactoredRepresentation<S> x )
	{
		final double[] phi = x.phi();
		DataNode dn = dt_root;
		while( dn.split != null ) {
			dn = dn.split.child( phi );
		}
		return dn;
	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.search.fsss.RefineableRepresenter#addTrainingSample(edu.oregonstate.eecs.mcplan.search.fsss.FsssAbstractActionNode, S, edu.oregonstate.eecs.mcplan.FactoredRepresentation)
	 */
	public FsssAbstractStateNode<S, A> addTrainingSample( final FsssAbstractActionNode<S, A> an,
									   final S s, final FactoredRepresentation<S> x )
	{
		final DataNode dt_root = requireActionSet( s );
		final DataNode dn = classify( dt_root, x );
		
		if( dn.aggregate == null ) {
			dn.aggregate = new FsssAbstractStateNode<S, A>( an, model, abstraction, new IndexRepresentation<S>( dn.id ) );
		}
//		dn.aggregate.addGroundStateNode( new FsssStateNode<S, A>( model, s ) );
		
		for( final FsssStateNode<S, A> gsn : dn.aggregate.states() ) {
			assert( model.action_repr().encode( gsn.s() ).equals( model.action_repr().encode( s ) ) );
		}
		
		return dn.aggregate;
	}
	
	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.search.fsss.RefineableRepresenter#addTrainingSampleAsExistingNode(edu.oregonstate.eecs.mcplan.search.fsss.FsssAbstractActionNode, edu.oregonstate.eecs.mcplan.search.fsss.FsssStateNode)
	 */
	public FsssAbstractStateNode<S, A> addTrainingSampleAsExistingNode( final FsssAbstractActionNode<S, A> an,
													 final FsssStateNode<S, A> sn )
	{
		final DataNode dt_root = requireActionSet( sn.s() );
		final DataNode dn = classify( dt_root, sn.x() );
		
		if( dn.aggregate == null ) {
			dn.aggregate = new FsssAbstractStateNode<S, A>( an, model, abstraction, new IndexRepresentation<S>( dn.id ) );
		}
//		dn.aggregate.addGroundStateNode( sn );
		
		return dn.aggregate;
	}

}
