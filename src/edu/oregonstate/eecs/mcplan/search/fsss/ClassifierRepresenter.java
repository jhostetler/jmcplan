/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.LoggerManager;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.abstraction.IndexRepresentation;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public abstract class ClassifierRepresenter<S extends State, A extends VirtualConstructor<A>>
	implements Representer<S, Representation<S>>
{
	private static final ch.qos.logback.classic.Logger Log = LoggerManager.getLogger( "log.search" );
	
	public static <S extends State, A extends VirtualConstructor<A>>
	void printDecisionTree( final DataNode<S, A> dn, final int ws )
	{
		for( int i = 0; i < ws; ++i ) {
			System.out.print( "|-" );
		}
		System.out.print( "[id: " );
		System.out.print( dn.id );
		System.out.print( " @" + Integer.toHexString( System.identityHashCode( dn ) ) );
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
			// Don't consider ASNs that are:
			// 1. unvisisted by FSSS, 2. terminal states, 3. pure.
			if( child.aggregate.isPure() || child.aggregate.nvisits() == 0 || child.aggregate.isTerminal() ) {
				continue;
			}
			if( largest_child == null || child.aggregate.states().size() > largest_child.aggregate.states().size() ) {
				largest_child = child;
			}
		}
		return largest_child;
	}
	
	// -----------------------------------------------------------------------
	
	public final FsssModel<S, A> model;
	public final FsssAbstraction<S, A> abstraction;
	
	protected final DataNode.Factory<S, A> dn_factory;
	public final Map<Representation<S>, DataNode<S, A>> dt_roots = new HashMap<Representation<S>, DataNode<S, A>>();
	
	public final ArrayList<DataNode<S, A>> dt_leaves = new ArrayList<DataNode<S, A>>();
	
	public ClassifierRepresenter( final FsssModel<S, A> model, final FsssAbstraction<S, A> abstraction )
	{
		this( model, abstraction, new DataNode.DefaultFactory<S, A>() );
	}
	
	public ClassifierRepresenter( final FsssModel<S, A> model, final FsssAbstraction<S, A> abstraction,
								  final DataNode.Factory<S, A> dn_factory )
	{
		this.model = model;
		this.abstraction = abstraction;
		this.dn_factory = dn_factory;
	}
	
	private static final class ClassIterator<S extends State, A extends VirtualConstructor<A>>
		implements Iterator<FsssAbstractStateNode<S, A>>
	{
		private final Iterator<DataNode<S, A>> itr;
		
		public ClassIterator( final ArrayList<DataNode<S, A>> dt_leaves )
		{
			itr = dt_leaves.iterator();
		}
		
		@Override
		public boolean hasNext()
		{
			return itr.hasNext();
		}

		@Override
		public FsssAbstractStateNode<S, A> next()
		{
			return itr.next().aggregate;
		}

		@Override
		public void remove()
		{ throw new UnsupportedOperationException(); }
	}
	
	public Iterator<FsssAbstractStateNode<S, A>> classes()
	{
		return new ClassIterator<S, A>( dt_leaves );
	}
	
	public int nclasses()
	{
		return dt_leaves.size();
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
	
	protected DataNode<S, A> requireActionSet( final S s )
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
	
	/**
	 * Given a ground representation, returns the corresponding DataNode. May
	 * return null if the classifier is not exhaustive and 'x' has not yet
	 * been added.
	 * 
	 * FIXME: Consolidate / minimize all of these "lookup" methods.
	 * 
	 * @param x
	 * @return
	 */
	public DataNode<S, A> classify( final S s )
	{
		final DataNode<S, A> dt_root = getActionSet( s );
		final FactoredRepresentation<S> x = model.base_repr().encode( s );
		final DataNode<S, A> dn = classify( dt_root, x );
		return dn;
	}
	
	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.search.fsss.RefineableRepresenter#classify(edu.oregonstate.eecs.mcplan.search.fsss.RefineablePartitionTreeRepresenter, edu.oregonstate.eecs.mcplan.FactoredRepresentation)
	 */
	protected DataNode<S, A> classify( final DataNode<S, A> dt_root, final FactoredRepresentation<S> x )
	{
		DataNode<S, A> dn = dt_root;
		while( dn != null && dn.split != null ) {
			dn = dn.split.child( x );
			
//			if( dn == null ) {
//				System.out.println( "\t! No DT path for " + x );
//				printDecisionTree( dt_root, 2 );
//			}
			
//			assert( dn != null );
		}
		return dn;
	}
	
	/**
	 * Called when encountering an instance that is not currently assigned to
	 * any class by this classifier. Default implementation throws an
	 * exception, because the classifier is assumed to be exhaustive.
	 * @param dt_root
	 * @param x
	 * @return
	 */
	protected DataNode<S, A> novelInstance( final DataNode<S, A> dt_root, final FactoredRepresentation<S> x )
	{
		throw new IllegalStateException( "Classifier should be exhaustive" );
	}
	
	/**
	 * Called after upSample() to allow the classifier to perform any
	 * necessary modifications to account for the new abstraction.
	 * 
	 * Default implementation does nothing.
	 */
	public void prune()
	{ }
	
	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.search.fsss.RefineableRepresenter#addTrainingSample(edu.oregonstate.eecs.mcplan.search.fsss.FsssAbstractActionNode, S, edu.oregonstate.eecs.mcplan.FactoredRepresentation)
	 */
	public FsssAbstractStateNode<S, A> addTrainingSample( final FsssAbstractActionNode<S, A> an, final S s )
	{
		Log.trace( "addTrainingSample(): {}", s );
		
		final FactoredRepresentation<S> x = model.base_repr().encode( s );
		final DataNode<S, A> dt_root = requireActionSet( s );
		DataNode<S, A> dn = classify( dt_root, x );
		
		if( dn == null ) {
			dn = novelInstance( dt_root, x );
		}
		
		if( dn.aggregate == null ) {
			dn.aggregate = new FsssAbstractStateNode<S, A>( an, model, abstraction, new IndexRepresentation<S>( dn.id ) );
		}
		
		return dn.aggregate;
	}
	
	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.search.fsss.RefineableRepresenter#addTrainingSampleAsExistingNode(edu.oregonstate.eecs.mcplan.search.fsss.FsssAbstractActionNode, edu.oregonstate.eecs.mcplan.search.fsss.FsssStateNode)
	 */
	public FsssAbstractStateNode<S, A> addTrainingSampleAsExistingNode( final FsssAbstractActionNode<S, A> an,
													 final FsssStateNode<S, A> sn )
	{
		Log.trace( "addExistingNode(): {}", sn );
		
		final DataNode<S, A> dt_root = requireActionSet( sn.s() );
		DataNode<S, A> dn = classify( dt_root, sn.x() );
		
		if( dn == null ) {
			dn = novelInstance( dt_root, sn.x() );
		}
		
		if( dn.aggregate == null ) {
			dn.aggregate = new FsssAbstractStateNode<S, A>( an, model, abstraction, new IndexRepresentation<S>( dn.id ) );
		}
		
		return dn.aggregate;
	}
	
	/**
	 * Given an ASN, returns the DataNode dn in dt_leaves such that
	 * dn.aggregate == asn, or null if no such DN exists.
	 * @param asn
	 * @return
	 */
	public DataNode<S, A> getDataNode( final FsssAbstractStateNode<S, A> asn )
	{
		for( final DataNode<S, A> dn : dt_leaves ) {
			if( dn.aggregate == asn ) {
				return dn;
			}
		}
		return null;
	}
}
