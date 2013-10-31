package edu.oregonstate.eecs.mcplan.abstraction;

import java.util.ArrayList;
import java.util.List;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.ml.GameTreeStateSimilarityDataset;
import edu.oregonstate.eecs.mcplan.search.ActionNode;
import edu.oregonstate.eecs.mcplan.search.GameTree;
import edu.oregonstate.eecs.mcplan.search.StateNode;
import edu.oregonstate.eecs.mcplan.util.Tuple.Tuple2;

public abstract class AbstractionAStar<X extends FactoredRepresentation<?>, A extends VirtualConstructor<A>>
	extends GameTreeStateSimilarityDataset<X, A>
{
	public final double false_positive_weight;
	public final double q_tolerance;
	
	public AbstractionAStar( final GameTree<X, A> tree,
							 final ArrayList<Attribute> attributes,
							 final int player, final double false_positive_weight,
							 final double q_tolerance )
	{
		// TODO: It appears that context = true is the same as context = false ???
		super( tree, attributes, player, 1 /* min_samples to consider a state node */,
			   4 /* max instances of each class */, true /* Use context */ );
		this.false_positive_weight = false_positive_weight;
		this.q_tolerance = q_tolerance;
	}
	
	public abstract double computeInstanceWeight( final StateNode<X, A> s1, final ActionNode<X, A> a1,
												  final StateNode<X, A> s2, final ActionNode<X, A> a2,
												  final int label, final double fp_weight );
	
	public abstract ActionNode<X, A> getAction( final StateNode<X, A> s );

	@Override
	public Tuple2<Integer, Double> label( final List<ActionNode<X, A>> path,
										  final int player,
										  final StateNode<X, A> s1,
										  final StateNode<X, A> s2 )
	{
		final ActionNode<X, A> a1 = getAction( s1 );
		final ActionNode<X, A> a2 = getAction( s2 );
		final int label;
		if( a1 != null && a2 != null
				&& a1.a( player ).equals( a2.a( player ) )
				&& Math.abs( a1.q( player ) - a2.q( player ) ) < q_tolerance ) {
			label = 1;
		}
		else {
			label = 0;
		}
		final double weight = computeInstanceWeight( s1, a1, s2, a2, label, false_positive_weight );
		return Tuple2.of( label, weight );
	}
}