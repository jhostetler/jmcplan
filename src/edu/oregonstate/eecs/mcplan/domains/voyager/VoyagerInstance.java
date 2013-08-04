/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.awt.Point;
import java.io.PrintStream;
import java.util.ArrayList;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.experiments.Instance;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.ListUtil;

/**
 * @author jhostetler
 *
 */
public class VoyagerInstance extends Instance<VoyagerInstance, VoyagerState, UndoableAction<VoyagerState>>
{
	public final VoyagerParameters params;
	public final long seed;
	public final VoyagerHash hash;
	private final VoyagerState state_;
	private final VoyagerSimulator<UndoableAction<VoyagerState>> simulator_;
	private final MersenneTwister rng_;
	
	public VoyagerInstance( final VoyagerParameters params, final long seed )
	{
		this.params = params;
		this.seed = seed;
		rng_ = new MersenneTwister( seed );
		hash = new VoyagerHash( params );
		state_ = createDefaultProblem( params, new int[] { 1, 0 }, EntityType.Worker, seed, hash );
		simulator_ = new VoyagerSimulator<UndoableAction<VoyagerState>>(
			state_, seed, params.max_population, params.horizon, params.primitive_epoch );
	}
	
	private static double[] sampleDirichlet( final RandomGenerator rng, final double[] alpha )
	{
		final double[] result = new double[alpha.length];
		for( int i = 0; i < alpha.length; ++i ) {
			final GammaDistribution gamma = new GammaDistribution(
				rng, alpha[i], 1, GammaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY );
			result[i] = gamma.sample();
		}
		return Fn.normalize_inplace( result );
	}
	
	private static VoyagerState createDefaultProblem(
		final VoyagerParameters params, final int[] initalPopulation,
		final EntityType initial_production, final long seed, final VoyagerHash hash )
	{
		assert( params.Nsites*params.Nsites >= 2 * params.Nplanets );
		
		final RandomGenerator rng = new MersenneTwister( seed );
		final ArrayList<Integer> capacity_list = new ArrayList<Integer>();
		// Symmetric Dirichlet distribution on proportion of planet sizes.
		// TODO: Generalize
		final double[] dirichlet_alpha = Fn.repeat( 1.0 / params.planet_capacity_steps, params.planet_capacity_steps );
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
		for (int x = 1; x < params.Nsites; ++x) {
			for (int y = 1; y < params.Nsites; ++y) {
				// We only use non-negative 'x' values because we're going
				// to reflect over 'y = -x' later to get a symmetrical map.
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
		int planet_id = 0;
		for (int i = 0; i < params.Nplanets; ++i) {
			final int capacity = capacity_list.get( i );
			final Point point = planetPos.get(i);
			final int x = point.x;
			final int y = point.y;
			final ArrayList<Planet> ps = new ArrayList<Planet>();
			ps.add( new Planet.Builder().id( planet_id++ ).hash( hash ).capacity( capacity )
										.x( x ).y( y ).owner( Player.Neutral ).finish() );
			ps.add( new Planet.Builder().id( planet_id++ ).hash( hash ).capacity( capacity )
										.x( -x ).y( -y ).owner( Player.Neutral ).finish() );
			if (playerBasePlanetIndex == i) {
				for (int p = 0; p < ps.size(); ++p) {
					ps.get( p ).setOwner( Player.values()[p] )
					  .setPopulation( initalPopulation ).setProduction( initial_production );
				}
			}
			planets.addAll(ps);
		}
		
		final VoyagerState initState = new VoyagerState(
			planets.toArray( new Planet[] { } ), Player.values(), params.Nsites, params.Nsites, hash );
		return initState;
	}

	@Override
	public void writeCsv( final PrintStream out )
	{
		
	}

	@Override
	public VoyagerSimulator<UndoableAction<VoyagerState>> simulator()
	{
		return simulator_;
	}

	@Override
	public VoyagerState state()
	{
		return state_;
	}
	
	@Override
	public long nextSeed()
	{
		return rng_.nextLong();
	}

	@Override
	public VoyagerInstance copy()
	{
		return new VoyagerInstance( params, seed );
	}
}
