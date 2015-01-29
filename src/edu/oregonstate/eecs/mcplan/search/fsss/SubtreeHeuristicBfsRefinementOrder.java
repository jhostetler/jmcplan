/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * Chooses splits based on an evaluation function. The evaluation is
 * 		score = D + \lambda*R
 * where D is the L1 distance between the Q-functions of the two resulting
 * abstract states, R measures the size balance of the two states, and
 * \lambda is a regularization parameter.
 */
public class SubtreeHeuristicBfsRefinementOrder<S extends State, A extends VirtualConstructor<A>>
	extends SubtreeBreadthFirstRefinementOrder<S, A>
{
	public static class Factory<S extends State, A extends VirtualConstructor<A>>
		implements SubtreeRefinementOrder.Factory<S, A>
	{
		private final SplitEvaluator<S, A> evaluator;
		
		public Factory( final SplitEvaluator<S, A> evaluator )
		{
			this.evaluator = evaluator;
		}
		
		@Override
		public SubtreeRefinementOrder<S, A> create( final FsssParameters parameters,
				final FsssModel<S, A> model, final FsssAbstractActionNode<S, A> root )
		{
			return new SubtreeHeuristicBfsRefinementOrder<S, A>( parameters, model, root, evaluator );
		}
		
		@Override
		public String toString()
		{
			return "SubtreeHeuristicBfs(" + evaluator + ")";
		}
	}
	
	// -----------------------------------------------------------------------
	
	private final SplitEvaluator<S, A> evaluator;
	
	public SubtreeHeuristicBfsRefinementOrder( final FsssParameters parameters,
											   final FsssModel<S, A> model,
											   final FsssAbstractActionNode<S, A> root_action,
											   final SplitEvaluator<S, A> evaluator )
	{
		super( parameters, model, root_action );
		this.evaluator = evaluator;
	}
	
	/**
	 * Choose the largest child of 'aan' as 'dn. Chooses an attribute and value
	 * to split on that maximizes evaluateSplit(). Returns "no split" if there
	 * is no split that separates the ground state members of 'largest_child'
	 * into two non-empty sets.
	 * @param aan
	 * @return
	 */
	@Override
	protected SplitChoice<S, A> chooseSplit( final FsssAbstractActionNode<S, A> aan )
	{
		final RefineablePartitionTreeRepresenter<S, A>.DataNode largest_child
			= RefineablePartitionTreeRepresenter.largestChild( aan );
		if( largest_child == null ) {
			return null;
		}
		
		// Test all attributes for split quality
		int best_attribute = -1;
		double best_value = Double.NaN;
		double max_score = -Double.MAX_VALUE;
		for( int i = 0; i < model.base_repr().attributes().size(); ++i ) {
			// Sort by current attribute
			final int ii = i;
			Collections.sort( largest_child.aggregate.states(), new Comparator<FsssStateNode<S, A>>() {
				@Override
				public int compare( final FsssStateNode<S, A> a, final FsssStateNode<S, A> b )
				{ return (int) Math.signum( a.x().phi()[ii] - b.x().phi()[ii] ); }
			} );
			
			// Test all split points for quality
			final int start = 0;
			final int end = largest_child.aggregate.states().size();
			double v0 = largest_child.aggregate.states().get( start ).x().phi()[i];
			
			for( int j = start + 1; j < end; ++j ) {
				final double v1 = largest_child.aggregate.states().get( j ).x().phi()[i];
				if( v1 > v0 ) { // Value changes between j and j-1
					final ArrayList<FsssStateNode<S, A>> Left = new ArrayList<FsssStateNode<S, A>>();
					final ArrayList<FsssStateNode<S, A>> Right = new ArrayList<FsssStateNode<S, A>>();
					final double split = (v1 + v0) / 2;
					
					for( int k = 0; k < j; ++k ) {
						Left.add( largest_child.aggregate.states().get( k ) );
					}
					for( int k = j; k < largest_child.aggregate.states().size(); ++k ) {
						Right.add( largest_child.aggregate.states().get( k ) );
					}
					final double score = evaluator.evaluateSplit( largest_child.aggregate, Left, Right );
					
					// TODO: Debugging code
					if( !(score >= 0) ) {
//							System.out.println( largest_child.aggregate );
//							for( final FsssAbstractActionNode<S, A> succ : largest_child.aggregate.successors() ) {
//								System.out.println( "\t" + succ );
//								for( final FsssAbstractStateNode<S, A> ssucc : succ.successors() ) {
//									System.out.println( "\t\t" + ssucc );
//								}
//							}
//							System.out.println( "! score = " + score );
						
						FsssTest.printTree( largest_child.aggregate, System.out, 1 );
					}
					
					assert( score >= 0.0 );
					if( score > max_score ) {
						best_attribute = i;
						best_value = split;
						max_score = score;
					}
					
					v0 = v1;
				}
			}
		}
		
		if( best_attribute == -1 ) {
			// Couldn't find a split that discriminates any states
//				System.out.println( "!\tCouldn't find a split in " + largest_child.aggregate );
			return new SplitChoice<S, A>( largest_child, null );
		}
		else {
			return new SplitChoice<S, A>( largest_child, new Split( best_attribute, best_value ) );
		}
	}
}
