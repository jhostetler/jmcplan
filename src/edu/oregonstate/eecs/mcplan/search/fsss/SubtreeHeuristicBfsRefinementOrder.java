/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.search.fsss.SubtreeRefinementOrder.Split;
import edu.oregonstate.eecs.mcplan.search.fsss.SubtreeRefinementOrder.SplitChoice;
import edu.oregonstate.eecs.mcplan.search.fsss.SubtreeRefinementOrder.SplitChooser;

/**
 * Chooses splits based on an evaluation function.
 */
public class SubtreeHeuristicBfsRefinementOrder<S extends State, A extends VirtualConstructor<A>>
	implements SubtreeRefinementOrder.SplitChooser<S, A>
{
	public static class Factory<S extends State, A extends VirtualConstructor<A>>
		implements SubtreeRefinementOrder.SplitChooser.Factory<S, A>
	{
		private final SplitEvaluator<S, A> evaluator;
		
		public Factory( final SplitEvaluator<S, A> evaluator )
		{
			this.evaluator = evaluator;
		}
		
		@Override
		public String toString()
		{
			return "Heuristic(" + evaluator + ")";
		}

		@Override
		public SplitChooser<S, A> createSplitChooser(
				final FsssParameters parameters, final FsssModel<S, A> model )
		{
			return new SubtreeHeuristicBfsRefinementOrder<S, A>( parameters, model, evaluator );
		}
	}
	
	// -----------------------------------------------------------------------
	
	private final FsssParameters parameters;
	private final FsssModel<S, A> model;
	private final SplitEvaluator<S, A> evaluator;
	
	public SubtreeHeuristicBfsRefinementOrder( final FsssParameters parameters,
											   final FsssModel<S, A> model,
											   final SplitEvaluator<S, A> evaluator )
	{
		this.parameters = parameters;
		this.model = model;
		this.evaluator = evaluator;
	}
	
	@Override
	public SplitChoice<S, A> chooseSplit( final FsssAbstractActionNode<S, A> aan )
	{
		final DataNode<S, A> largest_child = ClassifierRepresenter.largestChild( aan );
		if( largest_child == null ) {
			return null;
		}
		
		final Split split = chooseSplit( largest_child );
		return new SplitChoice<S, A>( largest_child, split );
	}

	@Override
	public Split chooseSplit( final DataNode<S, A> dn )
	{
		// Test all attributes for split quality
		int best_attribute = -1;
		double best_value = Double.NaN;
		double max_score = -Double.MAX_VALUE;
		for( int i = 0; i < model.base_repr().attributes().size(); ++i ) {
			// Sort by current attribute
			final int ii = i;
			Collections.sort( dn.aggregate.states(), new Comparator<FsssStateNode<S, A>>() {
				@Override
				public int compare( final FsssStateNode<S, A> a, final FsssStateNode<S, A> b )
				{ return (int) Math.signum( a.x().phi()[ii] - b.x().phi()[ii] ); }
			} );
			
			// Test all split points for quality
			final int start = 0;
			final int end = dn.aggregate.states().size();
			double v0 = dn.aggregate.states().get( start ).x().phi()[i];
			
			for( int j = start + 1; j < end; ++j ) {
				final double v1 = dn.aggregate.states().get( j ).x().phi()[i];
				if( v1 > v0 ) { // Value changes between j and j-1
					final ArrayList<FsssStateNode<S, A>> Left = new ArrayList<FsssStateNode<S, A>>();
					final ArrayList<FsssStateNode<S, A>> Right = new ArrayList<FsssStateNode<S, A>>();
					final double split = (v1 + v0) / 2;
					
					for( int k = 0; k < j; ++k ) {
						Left.add( dn.aggregate.states().get( k ) );
					}
					for( int k = j; k < dn.aggregate.states().size(); ++k ) {
						Right.add( dn.aggregate.states().get( k ) );
					}
					final double score = evaluator.evaluateSplit( dn.aggregate, Left, Right );
					
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
						
						FsssTest.printTree( dn.aggregate, System.out, 1 );
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
			return null;
		}
		else {
			return new Split( best_attribute, best_value );
		}
	}
}
