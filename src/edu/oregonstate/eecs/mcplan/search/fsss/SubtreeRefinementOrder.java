package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;
import java.util.Map;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.search.fsss.ClassifierRepresenter.DataNode;

public abstract class SubtreeRefinementOrder<S extends State, A extends VirtualConstructor<A>>
	implements RefinementOrder<S, A>
{
	public static interface Factory<S extends State, A extends VirtualConstructor<A>>
	{
		public abstract SubtreeRefinementOrder<S, A> create(
			final FsssParameters parameters, final FsssModel<S, A> model, final FsssAbstractActionNode<S, A> root );
	}
	
	public static class Split
	{
		public final int attribute;
		public final double value;
		
		public Split( final int attribute, final double value )
		{
			this.attribute = attribute;
			this.value = value;
		}
	}
	
	public static class SplitChoice<S extends State, A extends VirtualConstructor<A>>
	{
		public final DataNode<S, A> dn;
		public final Split split;
		
		public SplitChoice( final DataNode<S, A> dn, final Split split )
		{
			this.dn = dn;
			this.split = split;
		}
	}
	
	public static interface SplitChooser<S extends State, A extends VirtualConstructor<A>>
	{
		public static interface Factory<S extends State, A extends VirtualConstructor<A>>
		{
			public abstract SplitChooser<S, A> createSplitChooser(
				final FsssParameters parameters, final FsssModel<S, A> model );
		}
		
		/**
		 * Chooses an attribute and value to split on. Must return 'null' if no
		 * more refinements should be attempted on 'aan'. Must return a non-null
		 * SplitChoice with non-null .dn and null .split member to indicate that no
		 * more refinements should be attempted for the .dn member.
		 * 
		 * @param aan
		 * @return
		 */
		public abstract SplitChoice<S, A> chooseSplit( final FsssAbstractActionNode<S, A> aan );
	}
	
	public abstract FsssAbstractActionNode<S, A> rootAction();
	
		/**
	 * Calls backup() along the path from 'aan' to the root node.
	 * @param aan
	 */
	protected void backupToRoot( final FsssAbstractActionNode<S, A> aan )
	{
		final FsssAbstractStateNode<S, A> s = aan.predecessor;
		s.backup();
		if( s.predecessor != null ) {
			s.predecessor.backup();
			backupToRoot( s.predecessor );
		}
	}
	
	protected void upSample( final FsssAbstractActionNode<S, A> aan, final FsssParameters parameters )
	{
		// Don't sample if we're at the limit, but we still need to do
		// backups because buildSubtree2() does not do them.
//		final Map<FsssAbstractStateNode<S, A>, ArrayList<FsssStateNode<S, A>>> added;
//		if( model.sampleCount() < parameters.max_samples ) {
//			added = aan.upSample( parameters.width, parameters.max_samples );
//		}
//		else {
//			added = new HashMap<FsssAbstractStateNode<S, A>, ArrayList<FsssStateNode<S, A>>>();
//		}
		
		final Map<FsssAbstractStateNode<S, A>, ArrayList<FsssStateNode<S, A>>> added
//			= aan.upSample( parameters.width, parameters.max_samples );
			= aan.upSample( parameters.width, parameters.budget );
		
		for( final FsssAbstractStateNode<S, A> sn : aan.successors() ) {
			// If the node has not been expanded yet, do not upSample
			if( sn.nvisits() == 0 ) {
//					System.out.println( "!!\t Not recursively upSampling " + sn );
//					System.out.println( "!!\t nsuccessors = " + sn.nsuccessors() );
				continue;
			}
			
			final ArrayList<FsssStateNode<S, A>> sn_added = added.get( sn );
			if( sn_added != null ) {
				sn.addActionNodes( sn_added );
			}
			
			if( sn.isTerminal() ) {
				sn.leaf();
//					for( final FsssAbstractActionNode<S, A> aan_prime : sn.successors() ) {
//						aan_prime.leaf();
//					}
			}
			else {
				for( final FsssAbstractActionNode<S, A> aan_prime : sn.successors() ) {
					upSample( aan_prime, parameters );
				}
				sn.backup();
			}
			
		}
		if( aan.nsuccessors() > 0 ) {
			aan.backup();
		}
//		else {
//			FsssTest.printTree( FsssTest.findRoot( aan ), System.out, 1 );
//			System.out.println( "! " + aan );
//			System.exit( 0 );
//		}
	}
}
