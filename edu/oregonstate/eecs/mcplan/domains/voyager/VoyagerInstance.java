/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.awt.Point;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.experiments.Instance;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveSimulator;
import edu.oregonstate.eecs.mcplan.util.F;
import edu.oregonstate.eecs.mcplan.util.ListUtil;

/**
 * @author jhostetler
 *
 */
public class VoyagerInstance extends Instance<VoyagerInstance, VoyagerState, VoyagerEvent>
{
	public final long seed;
	private final VoyagerState state_;
	private final VoyagerSimulator simulator_;
	
	public VoyagerInstance( final VoyagerParameters params, final int seed )
	{
		this.seed = seed;
		state_ = createDefaultProblem( params, new int[] { 1, 0 }, Arrays.asList( EntityType.Worker ), seed );
		simulator_ = new VoyagerSimulator( state_, seed, params.horizon, params.primitive_epoch );
	}
	
	public VoyagerInstance( final VoyagerInstance that )
	{
		seed = that.seed;
		state_ = that.state_.copy();
		simulator_ = that.simulator_;
	}
	
	private static double[] sampleDirichlet( final RandomGenerator rng, final double[] alpha )
	{
		final double[] result = new double[alpha.length];
		for( int i = 0; i < alpha.length; ++i ) {
			final GammaDistribution gamma = new GammaDistribution(
				rng, alpha[i], 1, GammaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY );
			result[i] = gamma.sample();
		}
		return F.normalize( result );
	}
	
	private static VoyagerState createDefaultProblem(
		final VoyagerParameters params, final int[] initalPopulation,
		final List<EntityType> initial_production, final int seed )
	{
		assert( params.Nsites*params.Nsites >= 2 * params.Nplanets );
		
		final RandomGenerator rng = new MersenneTwister( seed );
		final ArrayList<Integer> capacity_list = new ArrayList<Integer>();
		final double[] dirichlet_alpha = new double[params.planet_capacity_steps];
		// Symmetric Dirichlet distribution on proportion of planet sizes.
		// TODO: Generalize
		Arrays.fill( dirichlet_alpha, 1.0 / params.planet_capacity_steps );
		final double[] proportions = sampleDirichlet( rng, dirichlet_alpha );
		int start = 0;
		double s = 0;
		for( int i = 0; i < params.planet_capacity_steps; ++i ) {
			final double p = proportions[i];
			s += p;
			final int end = (int) Math.ceil( s * params.Nplanets );
			for( int j = start; j < end; ++j ) {
				capacity_list.add( params.min_planet_capacity + i );
			}
			start = end;
		}
		assert( capacity_list.size() == params.Nplanets );
		ListUtil.randomShuffle( rng, capacity_list );

		final ArrayList<Point> grid = new ArrayList<Point>();
		for (int x = 0; x < params.Nsites; ++x) {
			for (int y = 0; y < params.Nsites; ++y) {
				final Point pos1 = new Point(x, y);
				final Point pos2 = new Point(x, -y);
				grid.add(pos1);
				grid.add(pos2);
			}
		}

		final ArrayList<Point> planetPos = new ArrayList<Point>();
		for (int i = 0; i < params.Nplanets; ++i) {
			final int index = rng.nextInt(grid.size());
			planetPos.add(grid.get(index));
			grid.remove(index);
		}

		final int playerBasePlanetIndex = rng.nextInt( params.Nplanets );
		final ArrayList<Planet> planets = new ArrayList<Planet>();
		for (int i = 0; i < params.Nplanets; ++i) {
			final int capacity = capacity_list.get( i );
			final Point point = planetPos.get(i);
			final int x = point.x;
			final int y = point.y;
			final ArrayList<Planet> ps = new ArrayList<Planet>();
			ps.add( new Planet.Builder().capacity( capacity ).x( x ).y( y ).owner( Player.Neutral ).finish() );
			ps.add( new Planet.Builder().capacity( capacity ).x( -x ).y( -y ).owner( Player.Neutral ).finish() );
			if (playerBasePlanetIndex == i) {
				for (int p = 0; p < ps.size(); ++p) {
					ps.get( p ).setOwner( Player.values()[p] )
					  .setPopulation( initalPopulation ).setProductionSchedule( initial_production )
					  .productionForward(); // Need productionForward() to set next_produced_.
				}
			}
			planets.addAll(ps);
		}
		
		final VoyagerState initState = new VoyagerState(
			planets.toArray( new Planet[] { } ), Player.values(), params.Nsites, params.Nsites );
		return initState;
	}

	@Override
	public VoyagerInstance copy()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeCsv( final PrintStream out )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public SimultaneousMoveSimulator<VoyagerState, VoyagerEvent> simulator()
	{
		return simulator_;
	}

	@Override
	public VoyagerState state()
	{
		return state_;
	}
}
