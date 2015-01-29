package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.GibbsDistribution;

/**
 * Chooses a subtree to refine based on a Gibbs distribution over the
 * non-optimal subtrees, using -U as the energy function.
 *
 * @param <S>
 * @param <A>
 */
public class GibbsRandomRefinementOrder<S extends State, A extends VirtualConstructor<A>>
	extends RefinementOrderBase<S, A>
{
	public static class Factory<S extends State, A extends VirtualConstructor<A>>
		implements RefinementOrder.Factory<S, A>
	{
		private final RandomGenerator rng;
		private final double temperature;
		private final SubtreeRefinementOrder.Factory<S, A> subtree_factory;
		
		public Factory( final RandomGenerator rng, final double temperature,
						final SubtreeRefinementOrder.Factory<S, A> subtree_factory )
		{
			this.rng = rng;
			this.temperature = temperature;
			this.subtree_factory = subtree_factory;
		}
		
		@Override
		public String toString()
		{
			return "GibbsBfs(" + temperature + "; " + subtree_factory + ")";
		}
	
		@Override
		public RefinementOrder<S, A> create( final FsssParameters parameters, final FsssModel<S, A> model,
									   final FsssAbstractStateNode<S, A> root )
		{
			final ArrayList<SubtreeRefinementOrder<S, A>> subtrees
				= new ArrayList<SubtreeRefinementOrder<S, A>>();
			for( final FsssAbstractActionNode<S, A> aan : root.successors() ) {
				subtrees.add( subtree_factory.create( parameters, model, aan ) );
			}
			assert( subtrees.size() > 1 );
			return new GibbsRandomRefinementOrder<S, A>( rng, temperature, root, subtrees );
		}
	}
	
	// -----------------------------------------------------------------------
	
	private final RandomGenerator rng;
	private final double temperature;
	
	public GibbsRandomRefinementOrder( final RandomGenerator rng, final double temperature,
									   final FsssAbstractStateNode<S, A> root,
									   final ArrayList<SubtreeRefinementOrder<S, A>> subtrees )
	{
		super( root, subtrees );
		this.rng = rng;
		this.temperature = temperature;
	}
	
	@Override
	public boolean isClosed()
	{
		return closed;
	}

	@Override
	protected SubtreeRefinementOrder<S, A> chooseSubtree()
	{
		final FsssAbstractActionNode<S, A> astar = root.astar();
		// Choose a non-optimal subtree at random with probability
		// determined by a Gibbs distribution with -U as the energy.
		int astar_idx = Integer.MAX_VALUE; // This value ensures astar_idx > tree_idx if astar subtree is closed
		final GibbsDistribution gibbs = new GibbsDistribution( rng, 1.0 / temperature );
		for( int i = 0; i < subtrees.size(); ++i ) {
			final SubtreeRefinementOrder<S, A> t = subtrees.get( i );
			if( t.rootAction().a().equals( astar.a() ) ) {
				astar_idx = i;
				continue;
			}
			else {
				assert( !t.isClosed() );
				final double Ui = t.rootAction().U();
				gibbs.add( -Ui );
			}
		}
		final int tree_idx = gibbs.sample();
		// Adjust index to skip optimal subtree
		final SubtreeRefinementOrder<S, A> selected_subtree
			= subtrees.get( (tree_idx < astar_idx ? tree_idx : tree_idx + 1) );
		return selected_subtree;
	}
}
