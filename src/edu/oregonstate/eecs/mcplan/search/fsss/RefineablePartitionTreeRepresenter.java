/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.abstraction.IndexRepresentation;
import edu.oregonstate.eecs.mcplan.search.fsss.SubtreeRefinementOrder.Split;
import edu.oregonstate.eecs.mcplan.search.fsss.SubtreeRefinementOrder.SplitChoice;
import edu.oregonstate.eecs.mcplan.search.fsss.SubtreeRefinementOrder.SplitChooser;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class RefineablePartitionTreeRepresenter<S extends State, A extends VirtualConstructor<A>>
	extends RefineableClassifierRepresenter<S, A>
{
	private final SplitChooser<S, A> split_chooser;
	
	public RefineablePartitionTreeRepresenter( final FsssModel<S, A> model, final FsssAbstraction<S, A> abstraction,
											   final SplitChooser<S, A> split_chooser )
	{
		super( model, abstraction );
		this.split_chooser = split_chooser;
	}
	
	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.search.fsss.RefineableRepresenter#create()
	 */
	@Override
	public RefineablePartitionTreeRepresenter<S, A> create()
	{
		return new RefineablePartitionTreeRepresenter<S, A>( model, abstraction, split_chooser );
	}
	
	protected DataNode<S, A> createSplitNode( final FsssAbstractActionNode<S, A> aan, final Object proposal )
	{
		@SuppressWarnings( "unchecked" )
		final SplitChoice<S, A> choice = (SplitChoice<S, A>) proposal;
		createSplitNode( choice.dn, choice.split );
		return choice.dn;
//		assert( choice.dn.split == null );
//		choice.dn.split = new BinarySplitNode<S, A>( dn_factory, choice.split.attribute, choice.split.value );
//
//		for( final DataNode<S, A> dn_child : Fn.in( choice.dn.split.children() ) ) {
//			dn_child.aggregate = new FsssAbstractStateNode<S, A>(
//				aan, model, abstraction, new IndexRepresentation<S>( dn_child.id ) );
//		}
//
//		for( final FsssStateNode<S, A> gsn : choice.dn.aggregate.states() ) {
//			choice.dn.split.addGroundStateNode( gsn );
//		}
//
//		return choice.dn;
	}
	
	protected void createSplitNode( final DataNode<S, A> dn, final Split split )
	{
		assert( dn.aggregate != null );
		assert( dn.split == null );
		dn.split = new BinarySplitNode<S, A>( dn_factory, split.attribute, split.value );
		
		for( final DataNode<S, A> dn_child : Fn.in( dn.split.children() ) ) {
			dn_child.aggregate = new FsssAbstractStateNode<S, A>(
				dn.aggregate.predecessor, model, abstraction, new IndexRepresentation<S>( dn_child.id ) );
		}
		
		for( final FsssStateNode<S, A> gsn : dn.aggregate.states() ) {
			dn.split.addGroundStateNode( gsn );
		}
		
//		return dn;
	}
	
	@Override
	public Object proposeRefinement( final FsssAbstractActionNode<S, A> aan )
	{
		return split_chooser.chooseSplit( aan );
	}
	
	protected void doSplit( final DataNode<S, A> dn )
	{
		assert( dn.aggregate != null );
		assert( dn.split != null );
		final boolean check = dt_leaves.remove( dn );
		assert( check );
//		System.out.println( "\tRefining " + dn.aggregate );
		
		for( final DataNode<S, A> dn_child : Fn.in( dn.split.children() ) ) {
			dt_leaves.add( dn_child );
			dn_child.aggregate.visit();
		}
		
		final ArrayList<FsssAbstractStateNode<S, A>> parts = new ArrayList<FsssAbstractStateNode<S, A>>();
		for( final DataNode<S, A> dn_child : Fn.in( dn.split.children() ) ) {
			parts.add( dn_child.aggregate );
		}
		
		dn.aggregate.predecessor.splitSuccessor( dn.aggregate, parts );
		System.out.println( "\tdoSplit(): Setting aggregate to null: " + dn );
		dn.aggregate = null; // Allow GC of the old ASN
	}
	
	@Override
	public void refine( final FsssAbstractActionNode<S, A> aan, final Object proposal )
	{
		assert( proposal != null );
		final DataNode<S, A> dn = createSplitNode( aan, proposal );
		doSplit( dn );
		
		/*
		final boolean check = dt_leaves.remove( dn );
		assert( check );
//		System.out.println( "\tRefining " + dn.aggregate );
		
		for( final DataNode<S, A> dn_child : Fn.in( dn.split.children() ) ) {
			dt_leaves.add( dn_child );
			dn_child.aggregate.visit();
		}
		
		final ArrayList<FsssAbstractStateNode<S, A>> parts = new ArrayList<FsssAbstractStateNode<S, A>>();
		for( final DataNode<S, A> dn_child : Fn.in( dn.split.children() ) ) {
			parts.add( dn_child.aggregate );
		}
		
		aan.splitSuccessor( dn.aggregate, parts );
		dn.aggregate = null; // Allow GC of the old ASN
		*/
	}

	@Override
	public void refine( final DataNode<S, A> dn )
	{
		final Split split = split_chooser.chooseSplit( dn );
		createSplitNode( dn, split );
		doSplit( dn );
	}
}
