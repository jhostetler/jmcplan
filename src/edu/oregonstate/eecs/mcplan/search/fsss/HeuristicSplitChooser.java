/* LICENSE
Copyright (c) 2013-2016, Jesse Hostetler (jessehostetler@gmail.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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
 * @author jhostetler
 *
 * @param <S>
 * @param <A>
 */
public class HeuristicSplitChooser<S extends State, A extends VirtualConstructor<A>>
	implements SplitChooser<S, A>
{
	public static class Factory<S extends State, A extends VirtualConstructor<A>>
		implements SplitChooser.Factory<S, A>
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
			return new HeuristicSplitChooser<>( parameters, model, evaluator );
		}
	}
	
	// -----------------------------------------------------------------------
	
	private final FsssParameters parameters;
	private final FsssModel<S, A> model;
	private final SplitEvaluator<S, A> evaluator;
	
	public HeuristicSplitChooser( final FsssParameters parameters,
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
