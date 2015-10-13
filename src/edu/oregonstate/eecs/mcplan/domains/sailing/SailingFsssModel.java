/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.sailing;

import java.util.Iterator;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssModel;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public class SailingFsssModel extends FsssModel<SailingState, SailingAction>
{
	private final RandomGenerator rng;
	private final SailingState.Factory state_factory;
	
	private int sample_count = 0;
	
	private final SailingGroundRepresenter base_repr = new SailingGroundRepresenter();
	private final SailingActionSetRepresenter action_repr = new SailingActionSetRepresenter();
	
	public SailingFsssModel( final RandomGenerator rng, final SailingState.Factory state_factory )
	{
		this.rng = rng;
		this.state_factory = state_factory;
	}
	
	@Override
	public FsssModel<SailingState, SailingAction> create( final RandomGenerator rng )
	{
		return new SailingFsssModel( rng, state_factory );
	}
	
	@Override
	public double Vmin( final SailingState s )
	{
		return (s.T - s.t) * SailingAction.obstacle_penalty;
	}

	@Override
	public double Vmax( final SailingState s )
	{
		return 0;
//		return heuristic( s );
	}

	@Override
	public double Vmin( final SailingState s, final SailingAction a )
	{
		return a.reward( s ) + (s.T - (s.t + 1))*SailingAction.obstacle_penalty;
	}

	@Override
	public double Vmax( final SailingState s, final SailingAction a )
	{
		// The heuristic is admissible, so we don't both with a.reward( s ).
		// We could obtain a *slightly* tighter bound.
//		return heuristic( s );
		return a.reward( s );
	}

	@Override
	public double discount()
	{
		return 1.0;
	}

	@Override
	public double heuristic( final SailingState s )
	{
		if( s.isTerminal() ) {
			return 0.0;
		}
		
		final int dx = s.width - 1 - s.x;
		final int dy = s.height - 1 - s.y;
		final int diag = Math.min( dx, dy );
		final int straight = Math.max( dx, dy ) - diag;
		final double distance = Math.sqrt( 2 ) * diag + straight;
		// Shortest path cost can be < Vmin if there are not enough time
		// steps left to complete the path.
		return Math.max( Vmin( s ), -distance / s.max_speed() );
	}

	@Override
	public RandomGenerator rng()
	{
		return rng;
	}

	@Override
	public FactoredRepresenter<SailingState, ? extends FactoredRepresentation<SailingState>> base_repr()
	{
		return base_repr;
	}

	@Override
	public Representer<SailingState, ? extends Representation<SailingState>> action_repr()
	{
		return action_repr;
	}

	@Override
	public SailingState initialState()
	{
		final SailingState s = state_factory.create( rng );
		return s;
	}
	
	/**
	 * Excludes actions that would cause a collision with an obstacle. This
	 * should not be used for the experiments, because you would have to have
	 * action_repr separate states with different legal actions, which doesn't
	 * leave much for refinement to do.
	 */
	private static class ObstacleActionGenerator extends Generator<SailingAction>
	{
		private final SailingState s;
		// -1 because: see doc for advance()
		private int i = -1;
		
		public ObstacleActionGenerator( final SailingState s )
		{
			this.s = s;
			advance();
			assert( i >= 0 );
			assert( i <= SailingState.Nwind_directions );
		}
		
		/**
		 * Sets 'i' to the *next* value that is not an obstacle
		 * (or to Nwind_directions if all remaining neighbors are obstacles).
		 */
		private void advance()
		{
			while( ++i < SailingState.Nwind_directions ) {
				if( !s.isNeighborObstacle( i ) ) {
					break;
				}
			}
		}
		
		@Override
		public boolean hasNext()
		{ return i < SailingState.Nwind_directions; }

		@Override
		public SailingAction next()
		{
			final SailingAction a = new SailingAction( i );
			advance();
			return a;
		}
	}
	
	private static class ObstacleActionIterable implements Iterable<SailingAction>
	{
		private final SailingState s;
		
		public ObstacleActionIterable( final SailingState s )
		{ this.s = s; }
		
		@Override
		public Iterator<SailingAction> iterator()
		{ return new ObstacleActionGenerator( s ); }
	}
	
	private static class AllActionGenerator extends Generator<SailingAction>
	{
		private int i = 0;
		
		@Override
		public boolean hasNext()
		{ return i < SailingState.Nwind_directions; }

		@Override
		public SailingAction next()
		{ return new SailingAction( i++ ); }
	}
	
	private static final Iterable<SailingAction> all_actions = new Iterable<SailingAction>() {
		@Override
		public Iterator<SailingAction> iterator()
		{ return new AllActionGenerator(); }
	};

	@Override
	public Iterable<SailingAction> actions( final SailingState s )
	{
//		return new ActionIterable( s );
		return all_actions;
	}

	@Override
	public SailingState sampleTransition( final SailingState s, final SailingAction a )
	{
		final SailingState sprime = new SailingState( s );
		a.doAction( rng, sprime );
		sprime.randomizeWind( rng );
		sprime.t += 1;
		sample_count += 1;
		return sprime;
	}

	@Override
	public double reward( final SailingState s )
	{
		return 0;
	}

	@Override
	public double reward( final SailingState s, final SailingAction a )
	{
		return a.reward( s );
	}

	@Override
	public int sampleCount()
	{
		return sample_count;
	}

	@Override
	public void resetSampleCount()
	{
		sample_count = 0;
	}
}
