/**
 * 
 */
package edu.oregonstate.eecs.mcplan.ml;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
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
public abstract class GameTreeStateSimilarityDataset<S extends FactoredRepresentation<?>, A extends VirtualConstructor<A>>
	implements Runnable
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
				if( s.successors().hasNext() ) {
					// If not leaf node
					values.add( s );
				}
			}
//			System.out.println( "***** values.size() = " + values.size() );
			for( final StateNode<S, A> s : Fn.in( a.successors() ) ) {
				s.accept( this );
			}
		}
	}
	
	public final int label_index;
	
	private final GameTree<S, A> tree_;
	private final ArrayList<Attribute> attributes_;
	private final int player_;
	private final int min_samples_;
	private final int max_instances_;
	private final boolean use_action_context_;
	
	private final HashMap<List<ActionNode<S, A>>, Instances> xs_
		= new HashMap<List<ActionNode<S, A>>, Instances>();
	
	public GameTreeStateSimilarityDataset( final GameTree<S, A> tree, final ArrayList<Attribute> attributes,
										   final int player, final int min_samples, final int max_instances,
										   final boolean use_action_context )
	{
		tree_ = tree;
		attributes_ = attributes;
		player_ = player;
		min_samples_ = min_samples;
		max_instances_ = max_instances;
		label_index = attributes.size() - 1;
		use_action_context_ = use_action_context;
	}
	
	@Override
	public void run()
	{
		System.out.println( "*** Extracting state nodes" );
		final Visitor visitor = new Visitor();
		tree_.root().accept( visitor );
		
		// This extracts only the level-1 nodes.
		// TODO: Do this somewhere better.
		final HashMap<List<ActionNode<S, A>>, List<StateNode<S, A>>> tx
			= new HashMap<List<ActionNode<S, A>>, List<StateNode<S, A>>>();
		final ArrayList<StateNode<S, A>> depth_1 = new ArrayList<StateNode<S, A>>();
		for( final Map.Entry<List<ActionNode<S, A>>, List<StateNode<S, A>>> e : visitor.xs.entrySet() ) {
			if( e.getKey() == null || e.getKey().size() != 1 ) {
				continue;
			}
			else {
				depth_1.addAll( e.getValue() );
			}
		}
		tx.put( null, depth_1 );
		
		final Comparator<Instance> weight_comp = new Comparator<Instance>() {
			@Override
			public int compare( final Instance a, final Instance b )
			{ return (int) Math.signum( a.weight() - b.weight() ); }
		};
		final int max_cap = max_instances_ + 1;
		final PriorityQueue<Instance> positive = new PriorityQueue<Instance>( max_cap, weight_comp );
		final PriorityQueue<Instance> negative = new PriorityQueue<Instance>( max_cap, weight_comp );
		System.out.println( "*** Building Instances" );
		for( final Map.Entry<List<ActionNode<S, A>>, List<StateNode<S, A>>> e : tx.entrySet() ) {
			System.out.println( "***** key = " + e.getKey() + ", value.size() = " + e.getValue().size() );
			
			final String name = (e.getKey() != null ? e.getKey().toString() : "null");
			final List<StateNode<S, A>> values = e.getValue();
			final int[] num_instances = { 0, 0 };
			int count = 0;
			for( int i = 0; i < values.size(); ++i ) {
				for( int j = i + 1; j < values.size(); ++j ) {
					if( count++ % 100 == 0 ) {
						System.out.println( "***** instance " + (count - 1) );
					}
					
					final StateNode<S, A> s_i = values.get( i );
					final StateNode<S, A> s_j = values.get( j );
					if( s_i.n() < min_samples_ || s_j.n() < min_samples_ ) {
						System.out.println( "! skipping under-sampled state pair" );
						continue;
					}
					final double[] phi_i = s_i.token.phi();
					final double[] phi_j = s_j.token.phi();
					assert( phi_i.length == phi_j.length );
					if( phi_i.length != attributes_.size() - 1 ) {
						System.out.println( "! phi_i.length = " + phi_i.length );
						System.out.println( "! attributes_.size() = " + attributes_.size() );
					}
					assert( phi_i.length == attributes_.size() - 1 );
					// Feature vector is absolute difference of the two state
					// feature vectors.
					final double[] phi_labeled = new double[phi_i.length + 1];
					for( int k = 0; k < phi_i.length; ++k ) {
						phi_labeled[k] = Math.abs( phi_i[k] - phi_j[k] );
					}
					final Tuple2<Integer, Double> labeled = label( e.getKey(), player_, s_i, s_j );
					final int label = labeled._1;
					final double weight = labeled._2;
					final String label_string = Integer.toString( label );
					phi_labeled[label_index] = label; //attributes.get( label_index ).indexOfValue( label_string );
					
					num_instances[label] += 1;
					
					final Instance instance = new DenseInstance( weight, phi_labeled );
					if( label == 0 ) {
						negative.add( instance );
						if( negative.size() >= max_cap ) {
							negative.poll();
						}
					}
					else {
						positive.add( instance );
						if( positive.size() >= max_cap ) {
							positive.poll();
						}
					}
				} // for j
			} // for i
			System.out.println( "num_instances = " + Arrays.toString( num_instances ) );
			final Instances x = new Instances( name, attributes_, negative.size() + positive.size() );
			x.setClassIndex( label_index );
			x.addAll( negative );
			x.addAll( positive );
			xs_.put( e.getKey(), x );
		}
	}
	
	public HashMap<List<ActionNode<S, A>>, Instances> getInstances()
	{
		return xs_;
	}
	
	public Instances getInstances( final List<ActionNode<S, A>> path )
	{
		return xs_.get( path );
	}
	
	public abstract Tuple2<Integer, Double> label(
		final List<ActionNode<S, A>> path, final int player, final StateNode<S, A> s1, final StateNode<S, A> s2 );
}
