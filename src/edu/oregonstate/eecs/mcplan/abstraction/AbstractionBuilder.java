package edu.oregonstate.eecs.mcplan.abstraction;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.core.Attribute;
import weka.core.Instances;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.ml.GameTreeStateSimilarityDataset;
import edu.oregonstate.eecs.mcplan.search.ActionNode;
import edu.oregonstate.eecs.mcplan.search.GameTree;
import edu.oregonstate.eecs.mcplan.search.GameTreeFactory;
import edu.oregonstate.eecs.mcplan.search.MctsVisitor;
import edu.oregonstate.eecs.mcplan.search.SearchPolicy;
import edu.oregonstate.eecs.mcplan.search.StateNode;

public abstract class AbstractionBuilder<S, X extends FactoredRepresentation<S>, A extends VirtualConstructor<A>>
	extends SearchPolicy<S, X, A>
{
	private final ArrayList<Attribute> attributes_;
	private final int player_;
	private final int min_samples_;
	private final int max_instances_;
	private final double false_positive_weight_;
	private final double q_tolerance_;
	private final boolean use_action_context_;
	
	private final HashMap<List<ActionNode<X, A>>, Instances>
		instances_ = new HashMap<List<ActionNode<X, A>>, Instances>();
	
	public AbstractionBuilder(
			final GameTreeFactory<S, X, A> factory,
			final MctsVisitor<S, X, A> visitor,
			final ArrayList<Attribute> attributes,
		    final int player, final int min_samples, final int max_instances,
		    final double false_positive_weight, final double q_tolerance,
		    final boolean use_action_context,
			final PrintStream log_stream )
	{
		super( factory, visitor, log_stream );
		attributes_ = attributes;
		player_ = player;
		min_samples_ = min_samples;
		max_instances_ = max_instances;
		false_positive_weight_ = false_positive_weight;
		q_tolerance_ = q_tolerance;
		use_action_context_ = use_action_context;
	}
	
	public abstract double computeInstanceWeight( final StateNode<X, A> s1, final ActionNode<X, A> a1,
												  final StateNode<X, A> s2, final ActionNode<X, A> a2,
												  final int label, final double fp_weight );
	
	public abstract ActionNode<X, A> getAction( final StateNode<X, A> s );
	
	public HashMap<List<ActionNode<X, A>>, Instances> instances()
	{
		return instances_;
	}
	
	private void mergeInstances( final HashMap<List<ActionNode<X, A>>, Instances> novel )
	{
		for( final Map.Entry<List<ActionNode<X, A>>, Instances> e : novel.entrySet() ) {
			Instances local = instances_.get( e.getKey() );
			if( local == null ) {
				local = new Instances( "foobar", attributes_, 0 ); // TODO: "foobar"
				instances_.put( e.getKey(), local );
			}
			local.addAll( e.getValue() );
		}
	}

	@Override
	protected JointAction<A> selectAction( final GameTree<X, A> tree )
	{
		// TODO: Building the dataset adds significant computation to
		// getAction(), although the 'control' value still applies only
		// to time spent constructing the tree.
		final AbstractionBuilder<S, X, A> outer = this;
		final GameTreeStateSimilarityDataset<X, A> dataset
			= new AbstractionAStar<X, A>( tree, attributes_, player_, false_positive_weight_, q_tolerance_ ) {
				@Override
				public double computeInstanceWeight( final StateNode<X, A> s1,
						final ActionNode<X, A> a1, final StateNode<X, A> s2,
						final ActionNode<X, A> a2, final int label, final double fp_weight )
				{
					return outer.computeInstanceWeight( s1, a1, s2, a2, label, fp_weight );
				}

				@Override
				public ActionNode<X, A> getAction( final StateNode<X, A> s )
				{
					return outer.getAction( s );
				}
		};
		dataset.run();
		mergeInstances( dataset.getInstances() );
		
		// TODO: It should be possible to get the backup rule from StateNode
		return getAction( tree.root() ).a();
	}

	@Override
	public int hashCode()
	{
		return System.identityHashCode( this );
	}

	@Override
	public boolean equals( final Object that )
	{
		return this == that;
	}
	
}