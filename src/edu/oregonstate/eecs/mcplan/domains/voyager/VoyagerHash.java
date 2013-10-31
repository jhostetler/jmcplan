package edu.oregonstate.eecs.mcplan.domains.voyager;

import org.apache.commons.math3.random.MersenneTwister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores all of the random numbers needed for Zobrist hashing.
 */
public class VoyagerHash
{
	private static final Logger log = LoggerFactory.getLogger( VoyagerHash.class );
	
	public final int planet_numbers;
	public final int planet_pair_numbers;
	public final int Nhashes;
	
	public final int Nplanets;
	
	/**
	 * For each planet			8
			Owner 					3
			Production				2
			Stored					40
			Population				200
									245
								1960
		For each planet pair	64
			For each ETA			29
				For each player			2
					Population				200
										400
									11600
								742400
							744360
			
	 * @param params
	 * @param instance
	 */
	public VoyagerHash( final VoyagerParameters params )
	{
		final MersenneTwister rng = new MersenneTwister( params.master_seed );
		
		// FIXME: Hardcoded some values to test "designed instance"
		Nplanets = 9; // FIXME: params.Nplanets * Player.competitors;
		final int Nplayers = Player.values().length;
		final int Nunit_types = Unit.values().length;
		final int population = Nunit_types * params.max_population;
		int type_costs = 0;
		for( final Unit type : Unit.values() ) {
			type_costs += type.cost();
		}
		
		// Planet stuff
		planet_numbers = Nplanets * (Nplayers + Nunit_types + population + type_costs);
		planet_owner = new long[Nplanets][Nplayers];
		planet_population = new long[Nplanets][Nunit_types][params.max_population + 1]; // Need one for zero
		planet_production = new long[Nplanets][Nunit_types + 1]; // Need one for 'null'
		planet_stored_production = new long[Nplanets][Nunit_types][];
		for( int p = 0; p < Nplanets; ++p ) {
			for( int t = 0; t < Unit.values().length; ++t ) {
				// FIXME: Didn't expect to need '+1' here; why doesn't
				// production happen immediately?
				planet_stored_production[p][t] = new long[Unit.values()[t].cost() + 1];
			}
		}
		
		// Spaceship stuff
		max_eta = params.max_eta; //(int) Math.ceil( Math.sqrt( 2 * 4*params.Nsites*params.Nsites ) / params.ship_speed );
		planet_pair_numbers = Nplanets * Nplanets
							* max_eta * Player.competitors * Nunit_types * params.max_population;
		ships = new long[Nplanets][Nplanets]
					    [max_eta]
						[Player.competitors]
						[Nunit_types]
						[params.max_population];
		
		Nhashes = planet_numbers + planet_pair_numbers;
//		log.info( "Nhashes = {}", Nhashes );
		
		// Initialize Planet hash arrays
		int total = 0;
		for( int p = 0; p < Nplanets; ++p ) {
			for( int o = 0; o < Player.values().length; ++o ) {
				planet_owner[p][o] = rng.nextLong();
				total += 1;
			}
			
			for( int t = 0; t < Unit.values().length; ++t ) {
				planet_production[p][t] = rng.nextLong();
				total += 1;
				
				for( int pop = 0; pop < params.max_population; ++pop ) {
					planet_population[p][t][pop] = rng.nextLong();
					total += 1;
				}
				
				for( int prod = 0; prod < Unit.values()[t].cost(); ++prod ) {
					planet_stored_production[p][t][prod] = rng.nextLong();
					total += 1;
				}
			}
			
			// Need one extra for 'null'
			planet_production[p][Unit.values().length] = rng.nextLong();
		}
		assert( total == planet_numbers );
		
		// Initialize Spaceship hash arrays
		total = 0;
		for( int p1 = 0; p1 < Nplanets; ++p1 ) {
			for( int p2 = 0; p2 < Nplanets; ++p2 ) {
				for( int eta = 0; eta < max_eta; ++eta ) {
					for( int player = 0; player < Player.competitors; ++player ) {
						for( int t = 0; t < Unit.values().length; ++t ) {
							for( int pop = 0; pop < params.max_population; ++pop ) {
								ships[p1][p2][eta][player][t][pop] = rng.nextLong();
								total += 1;
							}
						}
					}
				}
			}
		}
		assert( total == planet_pair_numbers );
	}
	
	public final int max_eta;
	private final long[][] planet_owner;
	private final long[][][] planet_population;
	private final long[][] planet_production;
	private final long[][][] planet_stored_production;
	private final long[][][][][][] ships;
	
	public long hashOwner( final Planet p, final Player owner )
	{
		return planet_owner[p.id][owner.id];
	}
	
	public long hashPopulation( final Planet p, final Unit type, final int population )
	{
		return planet_population[p.id][type.ordinal()][population];
	}
	
	public long hashProduction( final Planet p, final Unit type )
	{
		final int idx;
		if( type == null ) {
			idx = Unit.values().length;
		}
		else {
			idx = type.ordinal();
		}
		return planet_production[p.id][idx];
	}
	
	public long hashStoredProduction( final Planet p, final Unit type, final int stored )
	{
		return planet_stored_production[p.id][type.ordinal()][stored];
	}
	
	public long hashSpaceship( final Planet src, final Planet dest, final int eta,
							   final Player owner, final Unit type, final int population )
	{
//		System.out.println( src.id );
//		System.out.println( dest.id );
		return ships[src.id][dest.id][eta][owner.id][type.ordinal()][population];
	}
}
