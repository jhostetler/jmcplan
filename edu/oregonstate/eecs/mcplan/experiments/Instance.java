/**
 * 
 */
package edu.oregonstate.eecs.mcplan.experiments;

import java.awt.Point;
import java.io.PrintStream;
import java.util.ArrayList;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.agents.galcon.CoarseSimulation;
import edu.oregonstate.eecs.mcplan.domains.galcon.FastGalconEvent;
import edu.oregonstate.eecs.mcplan.domains.galcon.FastGalconState;
import edu.oregonstate.eecs.mcplan.domains.galcon.GalconAction;
import edu.oregonstate.eecs.mcplan.domains.galcon.GalconSimulator;
import edu.oregonstate.eecs.mcplan.domains.galcon.GalconState;
import edu.oregonstate.eecs.mcplan.domains.galcon.Planet;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveSimulator;

/**
 * @author jhostetler
 *
 */
public class Instance implements CsvWriter, Copyable<Instance>
{
	public final GalconParameters params;
	public final int instance_seed;
	public final GalconSimulator sim;
	public final FastGalconState fast_state;
	public final SimultaneousMoveSimulator<FastGalconState, FastGalconEvent> fast_sim;
	public final CoarseSimulation<FastGalconState, FastGalconEvent> durative_sim;
	
	private static GalconState createDefaultProblem( final GalconParameters params,
			final int Nplanets, final int Nsites,
			final int initalPopulation, final long seed) throws Exception
	{
		assert( Nsites*Nsites >= 2 * Nplanets );
		final int planetMargin = 2;
		final int neutral_pop_cap = 100;
		
		final RandomGenerator rng = new MersenneTwister( seed );
		
		final ArrayList<Integer> planetRadiusList = new ArrayList<Integer>();
		for (int r = params.min_planet_radius; r <= params.max_planet_radius; ++r) {
			planetRadiusList.add(r);
		}

		final ArrayList<Point> grid = new ArrayList<Point>();
		for (int x = 1; x <= Nsites; ++x) {
			for (int y = 1; y <= Nsites; ++y) {
				final Point pos1 = new Point(x, y);
				final Point pos2 = new Point(x, -y);
				grid.add(pos1);
				grid.add(pos2);
			}
		}

		final ArrayList<Point> planetPos = new ArrayList<Point>();
		for (int i = 0; i < Nplanets; ++i) {
			final int index = rng.nextInt(grid.size());
			planetPos.add(grid.get(index));
			grid.remove(index);
		}

		final int playerBasePlanetIndex = rng.nextInt( Nplanets );
		final ArrayList<Planet> planets = new ArrayList<Planet>();
		for (int i = 0; i < planetPos.size(); ++i) {
			final Point point = planetPos.get(i);
			final int radius = planetRadiusList.get(rng.nextInt(planetRadiusList.size()));
			final int x = point.x * (params.max_planet_radius + planetMargin) * 2;
			final int y = point.y * (params.max_planet_radius + planetMargin) * 2;
			final ArrayList<Planet> ps = new ArrayList<Planet>();
			final int population = rng.nextInt( neutral_pop_cap );
			ps.add(new Planet(planets.size(), radius, population, x, y, -1));
			ps.add(new Planet(planets.size() + 1, radius, population, -x, -y,
					-1));
			if (playerBasePlanetIndex == i) {
				for (int p = 0; p < ps.size(); ++p) {
					ps.get(p).setOwnerID(p).setPopulation(initalPopulation);
				}
			}
			planets.addAll(ps);
		}

		final int mapSize = (params.max_planet_radius + planetMargin) * 2 * Nsites;
		final GalconState initState = new GalconState(planets, null, mapSize, mapSize, 0, 0);
		initState.createPlayers( params.Nplayers );
		return initState;
	}
	
	private static GalconState createGapProblem( final GalconParameters params, final int seed,
			final int Nsites, final int gap)
	{
		assert( Nsites*Nsites >= 2 * params.Nplanets );
		final int planetMargin = 2;
		final int Nsites_away = (int) Math.floor( Nsites / 2.0 );
		final int Nsites_home = (int) Math.ceil( Nsites / 2.0 );
		final int Nplanets_away = (int) Math.floor( params.Nplanets / 2.0 );
		final int Nplanets_home = (int) Math.ceil( params.Nplanets / 2.0 );
		
		final RandomGenerator rng = new MersenneTwister( seed );
		
		final ArrayList<Integer> planetRadiusList = new ArrayList<Integer>();
		for (int r = params.min_planet_radius; r <= params.max_planet_radius; ++r) {
			planetRadiusList.add(r);
		}

		final ArrayList<Point> grid_away = new ArrayList<Point>();
		for (int x = 1; x <= Nsites_away; ++x) {
			for (int y = 1; y <= Nsites_away; ++y) {
				final Point pos1 = new Point(x + gap, y + gap);
				grid_away.add(pos1);
			}
		}
		final ArrayList<Point> grid_home = new ArrayList<Point>();
		for (int x = 1; x <= Nsites_home; ++x) {
			for (int y = 1; y <= Nsites_home; ++y) {
				final Point pos2 = new Point(x + gap, -y - gap);
				grid_home.add(pos2);
			}
		}

		final ArrayList<Point> planetPos_away = new ArrayList<Point>();
		for (int i = 0; i < Nplanets_away; ++i) {
			final int index = rng.nextInt(grid_away.size());
			planetPos_away.add(grid_away.get(index));
			grid_away.remove(index);
		}
		final ArrayList<Point> planetPos_home = new ArrayList<Point>();
		for (int i = 0; i < Nplanets_home; ++i) {
			final int index = rng.nextInt(grid_home.size());
			planetPos_home.add(grid_home.get(index));
			grid_home.remove(index);
		}

		
		final ArrayList<Planet> planets = new ArrayList<Planet>();
		for (int i = 0; i < planetPos_away.size(); ++i) {
			final Point point = planetPos_away.get(i);
			final int radius = planetRadiusList.get(rng.nextInt(planetRadiusList.size()));
			final int x = point.x * (params.max_planet_radius + planetMargin) * 2;
			final int y = point.y * (params.max_planet_radius + planetMargin) * 2;
			final ArrayList<Planet> ps = new ArrayList<Planet>();
			final int population = rng.nextInt( params.max_neutral_population );
			ps.add(new Planet(planets.size(), radius, population, x, y, -1));
			ps.add(new Planet(planets.size() + 1, radius, population, -x, -y, -1));
			planets.addAll(ps);
		}
		final int playerBasePlanetIndex = rng.nextInt( Nplanets_home );
		for (int i = 0; i < planetPos_home.size(); ++i) {
			final Point point = planetPos_home.get(i);
			final int radius = planetRadiusList.get(rng.nextInt(planetRadiusList.size()));
			final int x = point.x * (params.max_planet_radius + planetMargin) * 2;
			final int y = point.y * (params.max_planet_radius + planetMargin) * 2;
			final ArrayList<Planet> ps = new ArrayList<Planet>();
			final int population = rng.nextInt( params.max_neutral_population );
			ps.add(new Planet(planets.size(), radius, population, x, y, -1));
			ps.add(new Planet(planets.size() + 1, radius, population, -x, -y, -1));
			if (playerBasePlanetIndex == i) {
				for (int p = 0; p < ps.size(); ++p) {
					ps.get(p).setOwnerID(p).setPopulation(params.initial_population);
				}
			}
			planets.addAll(ps);
		}

		final int mapSize = (params.max_planet_radius + planetMargin) * 2 * (Nsites + gap);
		final GalconState initState = new GalconState(planets, null, mapSize, mapSize, 0, 0);
		initState.createPlayers( params.Nplayers );
		return initState;
	}
	
	public static Instance createBasic( final GalconParameters params, final int instance_seed )
	{
		final GalconSimulator sim = new GalconSimulator(
			params.horizon, params.primitive_epoch, false, false,
			instance_seed, params.Nplanets, params.min_launch_percentage, params.launch_size_steps );
		return new Instance( params, instance_seed, sim );
	}
	
	public static Instance createGap( final GalconParameters params, final int instance_seed, final int gap )
	{
		final GalconSimulator sim = new GalconSimulator(
			params.horizon, params.primitive_epoch, false, false,
			createGapProblem( params, instance_seed, params.Nplanets, gap ),
			new ArrayList<GalconAction>(), new int[] { } );
		return new Instance( params, instance_seed, sim );
	}
	
	private Instance( final GalconParameters params, final int instance_seed, final GalconSimulator sim )
	{
		this.params = params;
		this.instance_seed = instance_seed;
		this.sim = sim;
		fast_state = new FastGalconState(
			sim, params.primitive_epoch, params.horizon,
			params.min_launch_percentage, params.launch_size_steps );
		fast_sim = fast_state;
		durative_sim = new CoarseSimulation<FastGalconState, FastGalconEvent>( fast_sim, params.policy_epoch );
	}
	
	public Instance( final Instance that )
	{
		params = that.params;
		instance_seed = that.instance_seed;
		sim = that.sim;
		fast_state = that.fast_state.copy();
		fast_sim = fast_state;
		durative_sim = new CoarseSimulation<FastGalconState, FastGalconEvent>( fast_sim, params.policy_epoch );
	}
	
	@Override
	public Instance copy()
	{
		return new Instance( this );
	}

	@Override
	public void writeCsv( final PrintStream out )
	{
		out.println( "key,value" );
		out.println( "instance_seed," + instance_seed );
		params.writeCsv( out );
	}
}
