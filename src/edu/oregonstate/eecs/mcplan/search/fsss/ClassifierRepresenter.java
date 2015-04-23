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
public abstract class ClassifierRepresenter<S extends State, A extends VirtualConstructor<A>>
	implements Representer<S, Representation<S>>
{
	public static <S extends State, A extends VirtualConstructor<A>>
	void printDecisionTree( final DataNode<S, A> dn, final int ws )
	{
		for( int i = 0; i < ws; ++i ) {
			System.out.print( "|-" );
		}
		System.out.print( "[id: " );
		System.out.print( dn.id );
		if( dn.split != null ) {
			System.out.print( ", " );
//			System.out.print( dn.split.attribute );
//			System.out.print( "@" );
//			System.out.print( dn.split.threshold );
			System.out.print( dn.split );
			System.out.println( "]" );
			for( final DataNode<S, A> child : Fn.in( dn.split.children() ) ) {
				printDecisionTree( child, ws + 1 );
			}
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
	DataNode<S, A> largestChild( final FsssAbstractActionNode<S, A> aan )
	{
		final ClassifierRepresenter<S, A> repr = aan.repr;
		
		// Heuristic choice 1: Choose *largest* child node
		DataNode<S, A> largest_child = null;
		for( final DataNode<S, A> child : repr.dt_leaves ) {
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
			//child.isClosed()
			if( child.aggregate.isPure() || child.aggregate.nvisits() == 0
					|| child.aggregate.isTerminal() || child.aggregate.states().size() == 1 ) {
				continue;
			}
			if( largest_child == null || child.aggregate.states().size() > largest_child.aggregate.states().size() ) {
				largest_child = child;
			}
		}
		return largest_child;
	}
	
	protected abstract static class SplitNode<S extends State, A extends VirtualConstructor<A>>
	{
		public abstract DataNode<S, A> child( final FactoredRepresentation<S> phi );
		public abstract Generator<? extends DataNode<S, A>> children();
		public abstract void addGroundStateNode( final FsssStateNode<S, A> gsn );
		
		public abstract SplitNode<S, A> create( final DataNodeFactory<S, A> f );
	}
	
	public static class DataNode<S extends State, A extends VirtualConstructor<A>>
	{
		public SplitNode<S, A> split = null;
		public FsssAbstractStateNode<S, A> aggregate = null;
		
		public final int id;
		private final boolean closed = false;
		
		public DataNode( final int id )
		{
			this.id = id;
		}

//		public boolean isClosed()
//		{
//			return closed;
//		}
//
//		public void close()
//		{
//			assert( !closed );
//			closed = true;
//		}
	}
	
	public static class BinarySplitNode<S extends State, A extends VirtualConstructor<A>> extends SplitNode<S, A>
	{
		public final int attribute;
		public final double threshold;
		
		public final DataNode<S, A> left;
		public final DataNode<S, A> right;
		
		public BinarySplitNode( final DataNodeFactory<S, A> f, final int attribute, final double threshold )
		{
			this.left = f.createDataNode();
			this.right = f.createDataNode();
			this.attribute = attribute;
			this.threshold = threshold;
		}
		
		@Override
		public SplitNode<S, A> create( final DataNodeFactory<S, A> f )
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
	
	protected static abstract class DataNodeFactory<S extends State, A extends VirtualConstructor<A>>
	{
		public abstract DataNode<S, A> createDataNode();
	}
	
	protected static class DefaultDataNodeFactory<S extends State, A extends VirtualConstructor<A>>
		extends DataNodeFactory<S, A>
	{
		private int next_id = 0;
		
		@Override
		public DataNode<S, A> createDataNode()
		{
			return new DataNode<S, A>( next_id++ );
		}
	}
	
	// -----------------------------------------------------------------------
	
	public final FsssModel<S, A> model;
	public final FsssAbstraction<S, A> abstraction;
	
	protected final DataNodeFactory<S, A> dn_factory;
	public final Map<Representation<S>, DataNode<S, A>> dt_roots = new HashMap<Representation<S>, DataNode<S, A>>();
	
	public final ArrayList<DataNode<S, A>> dt_leaves = new ArrayList<DataNode<S, A>>();
	
	public ClassifierRepresenter( final FsssModel<S, A> model, final FsssAbstraction<S, A> abstraction )
	{
		this( model, abstraction, new DefaultDataNodeFactory<S, A>() );
	}
	
	public ClassifierRepresenter( final FsssModel<S, A> model, final FsssAbstraction<S, A> abstraction,
								  final DataNodeFactory<S, A> dn_factory )
	{
		this.model = model;
		this.abstraction = abstraction;
		this.dn_factory = dn_factory;
	}
	
	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.search.fsss.RefineableRepresenter#create()
	 */
	@Override
	public abstract ClassifierRepresenter<S, A> create();

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.search.fsss.RefineableRepresenter#emptyInstance()
	 */
	public ClassifierRepresenter<S, A> emptyInstance()
	{
		final ClassifierRepresenter<S, A> dup = create();
		
		for( final Representation<S> akey : dt_roots.keySet() ) {
			final DataNode<S, A> dt_root = dt_roots.get( akey );
			final DataNode<S, A> dup_dt_root = dup.dn_factory.createDataNode();
			dup.dt_roots.put( akey, dup_dt_root );
			// Perform a DFS on 'this' decision tree and copy the nodes into 'dup'.
			// Do not initialize any .aggregate members.
			final Deque<DataNode<S, A>> stack = new ArrayDeque<DataNode<S, A>>();
			final Deque<DataNode<S, A>> dup_stack = new ArrayDeque<DataNode<S, A>>();
			stack.push( dt_root );
			dup_stack.push( dup_dt_root );
			while( !stack.isEmpty() ) {
				final DataNode<S, A> dn = stack.pop();
				final DataNode<S, A> dup_dn = dup_stack.pop();
				if( dn.split != null ) {
//					stack.push( dn.split.left );
//					stack.push( dn.split.right );
					for( final DataNode<S, A> child : Fn.in( dn.split.children() ) ) {
						stack.push( child );
					}
					
					dup_dn.split = dn.split.create( dup.dn_factory );
//					dup_dn.split = createSplitNode( dn.split.attribute, dn.split.threshold );
//					dup_dn.split.left = dup.dn_factory.createDataNode();
//					dup_dn.split.right = dup.dn_factory.createDataNode();
					
//					dup_stack.push( dup_dn.split.left );
//					dup_stack.push( dup_dn.split.right );
					for( final DataNode<S, A> child : Fn.in( dup_dn.split.children() ) ) {
						dup_stack.push( child );
					}
				}
				else {
					dup.dt_leaves.add( dup_dn );
				}
			}
		}
		
		return dup;
	}
	
	private DataNode<S, A> getActionSet( final S s )
	{
		final Representation<S> arep = model.action_repr().encode( s );
		final DataNode<S, A> dt_root = dt_roots.get( arep );
		return dt_root;
	}
	
	private DataNode<S, A> requireActionSet( final S s )
	{
		final Representation<S> arep = model.action_repr().encode( s );
		DataNode<S, A> dt_root = dt_roots.get( arep );
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
		final DataNode<S, A> dt_root = getActionSet( s );
		final FactoredRepresentation<S> x = model.base_repr().encode( s );
		final DataNode<S, A> dn = classify( dt_root, x );
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
	
	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.search.fsss.RefineableRepresenter#classify(edu.oregonstate.eecs.mcplan.search.fsss.RefineablePartitionTreeRepresenter, edu.oregonstate.eecs.mcplan.FactoredRepresentation)
	 */
	protected DataNode<S, A> classify( final DataNode<S, A> dt_root, final FactoredRepresentation<S> x )
	{
		DataNode<S, A> dn = dt_root;
		while( dn.split != null ) {
			dn = dn.split.child( x );
			
			if( dn == null ) {
				System.out.println( "\t! No DT path for " + x );
				printDecisionTree( dt_root, 2 );
			}
			
			assert( dn != null );
		}
		return dn;
	}
	
	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.search.fsss.RefineableRepresenter#addTrainingSample(edu.oregonstate.eecs.mcplan.search.fsss.FsssAbstractActionNode, S, edu.oregonstate.eecs.mcplan.FactoredRepresentation)
	 */
	public FsssAbstractStateNode<S, A> addTrainingSample( final FsssAbstractActionNode<S, A> an, final S s )
	{
		final FactoredRepresentation<S> x = model.base_repr().encode( s );
		final DataNode<S, A> dt_root = requireActionSet( s );
		final DataNode<S, A> dn = classify( dt_root, x );
		
		if( dn.aggregate == null ) {
			dn.aggregate = new FsssAbstractStateNode<S, A>( an, model, abstraction, new IndexRepresentation<S>( dn.id ) );
		}
//		dn.aggregate.addGroundStateNode( new FsssStateNode<S, A>( model, s ) );
		
		// TODO: Debugging code
//		for( final FsssStateNode<S, A> gsn : dn.aggregate.states() ) {
//			assert( model.action_repr().encode( gsn.s() ).equals( model.action_repr().encode( s ) ) );
//		}
		
		return dn.aggregate;
	}
	
	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.search.fsss.RefineableRepresenter#addTrainingSampleAsExistingNode(edu.oregonstate.eecs.mcplan.search.fsss.FsssAbstractActionNode, edu.oregonstate.eecs.mcplan.search.fsss.FsssStateNode)
	 */
	public FsssAbstractStateNode<S, A> addTrainingSampleAsExistingNode( final FsssAbstractActionNode<S, A> an,
													 final FsssStateNode<S, A> sn )
	{
		final DataNode<S, A> dt_root = requireActionSet( sn.s() );
		final DataNode<S, A> dn = classify( dt_root, sn.x() );
		
		if( dn.aggregate == null ) {
			dn.aggregate = new FsssAbstractStateNode<S, A>( an, model, abstraction, new IndexRepresentation<S>( dn.id ) );
		}
//		dn.aggregate.addGroundStateNode( sn );
		
		return dn.aggregate;
	}


}
