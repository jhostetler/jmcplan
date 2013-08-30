/**
 * 
 */
package edu.oregonstate.eecs.mcplan.ml;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.search.ActionNode;
import edu.oregonstate.eecs.mcplan.search.GameTree;
import edu.oregonstate.eecs.mcplan.search.GameTreeVisitor;
import edu.oregonstate.eecs.mcplan.search.StateNode;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.Tuple.Tuple2;

/**
 * @author jhostetler
 *
 */
public abstract class GameTreeStateSimilarityDataset<S extends FactoredRepresentation<?, ?>, A extends VirtualConstructor<A>>
{
	private class Visitor implements GameTreeVisitor<S, A>
	{
		public final HashMap<List<ActionNode<S, A>>, List<StateNode<S, A>>> xs
			= new HashMap<List<ActionNode<S, A>>, List<StateNode<S, A>>>();
		
		private final Deque<ActionNode<S, A>> ahist_ = new ArrayDeque<ActionNode<S, A>>();
		
		@Override
		public void visit( final StateNode<S, A> s )
		{
			for( final ActionNode<S, A> a : Fn.in( s.successors() ) ) {
				ahist_.push( a );
				a.accept( this );
				ahist_.pop();
			}
		}

		@Override
		public void visit( final ActionNode<S, A> a )
		{
			final List<ActionNode<S, A>> key;
			if( use_action_context_ ) {
				key = new ArrayList<ActionNode<S, A>>( ahist_ );
			}
			else {
				key = null;
			}
			
			List<StateNode<S, A>> values = xs.get( key );
			if( values == null ) {
				values = new ArrayList<StateNode<S, A>>();
				xs.put( key, values );
			}
			
			for( final StateNode<S, A> s : Fn.in( a.successors() ) ) {
				values.add( s );
			}
		}
	}
	
	public final int label_index;
	
	private final HashMap<List<ActionNode<S, A>>, Instances> xs_
		= new HashMap<List<ActionNode<S, A>>, Instances>();
	private final boolean use_action_context_;
	
	public GameTreeStateSimilarityDataset( final GameTree<S, A> tree, final ArrayList<Attribute> attributes,
										   final boolean use_action_context )
	{
		use_action_context_ = use_action_context;
		final Visitor visitor = new Visitor();
		tree.root().accept( visitor );
		
		label_index = attributes.size();
		for( final Map.Entry<List<ActionNode<S, A>>, List<StateNode<S, A>>> e : visitor.xs.entrySet() ) {
			final String name = (e.getKey() != null ? e.getKey().toString() : "null");
			final Instances x = new Instances( name, attributes, 0 );
			final List<StateNode<S, A>> values = e.getValue();
			for( int i = 0; i < values.size(); ++i ) {
				for( int j = i + 1; j < values.size(); ++j ) {
					final StateNode<S, A> s_i = values.get( i );
					final StateNode<S, A> s_j = values.get( j );
					final double[] phi_i = s_i.token.phi();
					final double[] phi_j = s_j.token.phi();
					final double[] phi_diff = Fn.vminus( phi_i, phi_j );
					final double[] labeled = new double[phi_diff.length + 1];
					for( int k = 0; k < phi_diff.length; ++k ) {
						labeled[k] = phi_diff[k];
					}
					final Tuple2<Integer, Double> label = label( e.getKey(), s_i, s_j );
					final String label_string = Integer.toString( label._1 );
					labeled[label_index] = attributes.get( label_index ).indexOfValue( label_string );
					x.add( new DenseInstance( label._2, labeled ) );
				}
			}
			xs_.put( e.getKey(), x );
		}
	}
	
	public abstract Tuple2<Integer, Double> label(
		final List<ActionNode<S, A>> path, final StateNode<S, A> s1, final StateNode<S, A> s2 );
}
