/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import edu.oregonstate.eecs.mcplan.LoggerManager;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

import ch.qos.logback.classic.Logger;

import com.mathworks.toolbox.javabuilder.MWCellArray;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.mathworks.toolbox.javabuilder.MWStructArray;

/**
 * @author jhostetler
 *
 */
public final class CosmicParameters implements AutoCloseable
{
	public final double delta_t = 1.0;
	
	public final CosmicMatlabInterface cosmic;
	public final MWStructArray C;
	public final MWStructArray index;
	public final double T;
	
	public final int Nbus;
	public final int Nbranch;
	public final int Ngenerator;
	public final int Nmachine;
	public final int Nexciter;
	public final int Ngovernor;
	public final int Nshunt;
	public final LinkedHashMap<String, Integer> bu_col_names = new LinkedHashMap<>();
	public final LinkedHashMap<String, Integer> br_col_names = new LinkedHashMap<>();
	public final LinkedHashMap<String, Integer> ge_col_names = new LinkedHashMap<>();
	public final LinkedHashMap<String, Integer> ma_col_names = new LinkedHashMap<>();
	public final LinkedHashMap<String, Integer> ex_col_names = new LinkedHashMap<>();
	public final LinkedHashMap<String, Integer> go_col_names = new LinkedHashMap<>();
	public final LinkedHashMap<String, Integer> sh_col_names = new LinkedHashMap<>();
	public final LinkedHashMap<String, Integer> ev_col_names = new LinkedHashMap<>();
	
	public final int ev_cols;
	public final int ev_time;
	public final int ev_type;
	public final int ev_trip_branch;
	public final int ev_branch_loc;
	public final int ev_trip_bus;
	public final int ev_bus_loc;
	public final int ev_shed_load;
	public final int ev_shunt_loc;
	public final int ev_trip_shunt;
	public final int ev_change_by;
	public final int ev_quantity;
	
	public static final int ev_change_by_amount		= 0;
	public static final int ev_change_by_percent	= 1;
	
	private final TIntObjectMap<TIntList> bus_to_shunts = new TIntObjectHashMap<>();
	
	public final int Nzones;
	/**
	 * Map from zone ID to IDs of buses in that zone.
	 */
	private final TIntObjectMap<TIntList> zone_to_buses = new TIntObjectHashMap<>();
	
	private final TIntObjectMap<TIntList> zone_to_shunts = new TIntObjectHashMap<>();
	
	/**
	 * Maps a zone ID to the IDs of the branches that need to be cut in order
	 * to isolate that zone from the rest of the grid.
	 */
	private final TIntObjectMap<TIntSet> zone_cutsets = new TIntObjectHashMap<>();
	
	/**
	 * @param cosmic
	 * @param C Owned by CosmicParameters
	 * @param ps NOT owned by CosmicParameters
	 * @param nx
	 * @param ny
	 */
	public CosmicParameters( final CosmicMatlabInterface cosmic,
							 final MWStructArray C, final MWStructArray ps, final MWStructArray index,
							 final double T )
	{
		this.cosmic = cosmic;
		this.C = C;
		this.index = index;
		this.T = T;
		
		final Logger log = LoggerManager.getLogger( "log.domain" );
		
		final MWStructArray ev = (MWStructArray) C.getField( "ev", 1 );
		ev_cols = ((MWNumericArray) ev.getField( "cols", 1 )).getInt();
		ev_time = ((MWNumericArray) ev.getField( "time", 1 )).getInt();
		ev_type = ((MWNumericArray) ev.getField( "type", 1 )).getInt();
		ev_trip_branch = ((MWNumericArray) ev.getField( "trip_branch", 1 )).getInt();
		ev_branch_loc = ((MWNumericArray) ev.getField( "branch_loc", 1 )).getInt();
		ev_trip_bus = ((MWNumericArray) ev.getField( "trip_bus", 1 )).getInt();
		ev_bus_loc = ((MWNumericArray) ev.getField( "bus_loc", 1 )).getInt();
		ev_shed_load = ((MWNumericArray) ev.getField( "shed_load", 1 )).getInt();
		ev_shunt_loc = ((MWNumericArray) ev.getField( "shunt_loc", 1 )).getInt();
		ev_trip_shunt = ((MWNumericArray) ev.getField( "trip_shunt", 1 )).getInt();
		ev_change_by = ((MWNumericArray) ev.getField( "change_by", 1 )).getInt();
		ev_quantity = ((MWNumericArray) ev.getField( "quantity", 1 )).getInt();
		
		fillColumnNames( bu_col_names, "bu" );
		fillColumnNames( br_col_names, "br" );
		fillColumnNames( ge_col_names, "ge" );
		fillColumnNames( ma_col_names, "ma" );
		fillColumnNames( ex_col_names, "ex" );
		fillColumnNames( go_col_names, "go" );
		fillColumnNames( sh_col_names, "sh" );
		fillColumnNames( ev_col_names, "ev" );
		
		Nbus = ps.getField( "bus", 1 ).getDimensions()[0];
		if( ps.getField( "bus", 1 ).getDimensions()[1] != bu_col_names.size() ) {
			throw new IllegalArgumentException( "ps.bus dimension != col_names" );
		}
		
		Nbranch = ps.getField( "branch", 1 ).getDimensions()[0];
		if( ps.getField( "branch", 1 ).getDimensions()[1] != br_col_names.size() ) {
			throw new IllegalArgumentException( "ps.branch dimension != col_names" );
		}
		
		Ngenerator = ps.getField( "gen", 1 ).getDimensions()[0];
		if( ps.getField( "gen", 1 ).getDimensions()[1] < ge_col_names.size() ) {
			throw new IllegalArgumentException( "ps.gen dimension != col_names" );
		}
		else if( ps.getField( "gen", 1 ).getDimensions()[1] > ge_col_names.size() ) {
			log.warn( "ps.gen dimension {} > col_names.size() {}",
					  ps.getField( "gen", 1 ).getDimensions()[1], ge_col_names.size() );
		}
		
		Nmachine = ps.getField( "mac", 1 ).getDimensions()[0];
		if( ps.getField( "mac", 1 ).getDimensions()[1] != ma_col_names.size() ) {
			throw new IllegalArgumentException( "ps.mac dimension != col_names" );
		}
		
		Nexciter = ps.getField( "exc", 1 ).getDimensions()[0];
		if( ps.getField( "exc", 1 ).getDimensions()[1] != ex_col_names.size() ) {
			throw new IllegalArgumentException( "ps.exc dimension != col_names" );
		}
		
		Ngovernor = ps.getField( "gov", 1 ).getDimensions()[0];
		if( ps.getField( "gov", 1 ).getDimensions()[1] != go_col_names.size() ) {
			throw new IllegalArgumentException( "ps.gov dimension != col_names" );
		}
		
		Nshunt = ps.getField( "shunt", 1 ).getDimensions()[0];
		// FIXME: This error check is desirable, but the current Cosmic
		// code violates it and will need to be fixed first.
//		if( ps.getField( "shunt", 1 ).getDimensions()[1] != sh_col_names.size() ) {
//			throw new IllegalArgumentException( "ps.shunt dimension != col_names" );
//		}
		
		// Initialize bus_to_shunts map
		for( int mi = 1; mi <= Nbus; ++mi ) {
			bus_to_shunts.put( mi, new TIntArrayList() );
		}
		final MWNumericArray mshunts = (MWNumericArray) ps.getField( "shunt", 1 );
		for( int mi = 1; mi <= Nshunt; ++mi ) {
			final int shunt_id	= mshunts.getInt( new int[] { mi, sh_col_names.get( "id" ) } );
			final int bus_id	= mshunts.getInt( new int[] { mi, sh_col_names.get( "bus" ) } );
			bus_to_shunts.get( bus_id ).add( shunt_id );
		}
		
		// Initialize zone_to_buses map
		final MWNumericArray mbuses = (MWNumericArray) ps.getField( "bus", 1 );
		for( int mi = 1; mi <= Nbus; ++mi ) {
			final Bus bus = new Bus( mi, this, mbuses );
			final int zone = bus.zone();
			TIntList zone_buses = zone_to_buses.get( zone );
			if( zone_buses == null ) {
				zone_buses = new TIntArrayList();
				zone_to_buses.put( zone, zone_buses );
			}
			zone_buses.add( bus.id() );
		}
		Nzones = zone_to_buses.size();
		
		// Initialize zone_to_shunts map
		for( final int zone : zone_to_buses.keys() ) {
			final TIntList zone_shunts = new TIntArrayList();
			
			final TIntList buses = zone_to_buses.get( zone );
			final TIntIterator bus_itr = buses.iterator();
			while( bus_itr.hasNext() ) {
				final TIntList bus_shunts = bus_to_shunts.get( bus_itr.next() );
				final TIntIterator sh_itr = bus_shunts.iterator();
				while( sh_itr.hasNext() ) {
					zone_shunts.add( sh_itr.next() );
				}
			}
			
			zone_to_shunts.put( zone, zone_shunts );
		}
		
		// Initialize zone_cutsets map
		for( int mi = 1; mi <= Nzones; ++mi ) {
			zone_cutsets.put( mi, new TIntHashSet() );
		}
		final MWNumericArray mbranches = (MWNumericArray) ps.getField( "branch", 1 );
		for( int mi = 1; mi <= Nbranch; ++mi ) {
			final Branch br = new Branch( mi, this, mbranches );
			final int from = br.from();
			final int to = br.to();
			final int from_zone = new Bus( from, this, mbuses ).zone();
			final int to_zone = new Bus( to, this, mbuses ).zone();
			if( from_zone != to_zone ) {
				zone_cutsets.get( from_zone ).add( br.id() );
				zone_cutsets.get( to_zone ).add( br.id() );
			}
		}
		
		if( log.isInfoEnabled() ) {
			for( int mi = 1; mi <= Nzones; ++mi ) {
				final TIntSet cutset = zone_cutsets.get( mi );
				log.info( "Zone " + mi + ": cutset " + cutset );
			}
		}
	}
	
	private void fillColumnNames( final LinkedHashMap<String, Integer> names, final String category )
	{
		final MWStructArray cat = (MWStructArray) C.getField( category, 1 );
		final MWCellArray col_names = (MWCellArray) cat.getField( "col_names", 1 );
		for( int i = 1; i <= col_names.numberOfElements(); ++i ) {
			names.put( new String( (char[]) col_names.getCell( i ).getData() ), i );
		}
	}
	
	public TIntIterator shuntsForBus( final int bus_id )
	{
		return bus_to_shunts.get( bus_id ).iterator();
	}
	
	public TIntList shuntsForZone( final int zone )
	{
		return zone_to_shunts.get( zone );
	}
	
	public TIntSet zoneCutset( final int zone_id )
	{
		return zone_cutsets.get( zone_id );
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		try {
			for( final Field f : CosmicParameters.class.getFields() ) {
				sb.append( f.getName() ).append( ": " ).append( f.get( this ) ).append( "\n" );
			}
		}
		catch( IllegalArgumentException | IllegalAccessException ex ) {
			throw new RuntimeException( ex );
		}
		return sb.toString();
	}

	@Override
	public void close()
	{
		C.dispose();
	}
}
