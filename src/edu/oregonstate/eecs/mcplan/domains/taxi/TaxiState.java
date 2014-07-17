/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.taxi;

import java.util.ArrayList;
import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class TaxiState implements State
{
	public static final int wall_right = 1<<0;
	public static final int wall_up = 1<<1;
	
	public final int[][] topology;
	public final ArrayList<int[]> locations;
	public final int Nother_taxis;
	
	public final int width;
	public final int height;
	
	public static final int IN_TAXI = -1;
	public static final int SELF_TAXI = -1;
	
	public int[] taxi = new int[2];
	public int passenger = 0;
	public int destination = 0;
	public final int[][] other_taxis;
	
	public int t = 0;
	public final int T = 100000;
	
	public boolean illegal_pickup_dropoff = false;
	public boolean goal = false;
	
	/**
	 * @param topology [x][y] order
	 * @param locations Special locations
	 * @param Nother_taxis Number of taxis not controlled by the agent
	 */
	public TaxiState( final int[][] topology, final ArrayList<int[]> locations, final int Nother_taxis )
	{
		this.topology = topology;
		this.locations = locations;
		this.Nother_taxis = Nother_taxis;
		
		width = this.topology.length;
		height = this.topology[0].length;
		
		other_taxis = new int[Nother_taxis][2];
	}
	
	public TaxiState( final TaxiState s )
	{
		topology = s.topology;
		locations = s.locations;
		Nother_taxis = s.Nother_taxis;
		width = s.width;
		height = s.height;
		
		taxi = Arrays.copyOf( s.taxi, s.taxi.length );
		other_taxis = Fn.copy( s.other_taxis );
		passenger = s.passenger;
		destination = s.destination;
		
		t = s.t;
		illegal_pickup_dropoff = s.illegal_pickup_dropoff;
		goal = s.goal;
	}
	
	public boolean isOccupiedByOther( final int[] pos )
	{
		return isOccupied( pos, -1 );
	}
	
	public boolean isOccupied( final int[] pos, final int exclude )
	{
		assert( exclude < Nother_taxis );
		for( int i = 0; i < Nother_taxis; ++i ) {
			if( i == exclude ) {
				continue;
			}
			if( Arrays.equals( pos, other_taxis[i] ) ) {
				return true;
			}
		}
		if( exclude != SELF_TAXI ) {
			if( Arrays.equals( pos, taxi ) ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isLegalMove( final int[] old_pos, final int[] new_pos )
	{
		return isLegalMove( -1, old_pos, new_pos );
	}
	
	public boolean isLegalMove( final int taxi, final int[] old_pos, final int[] new_pos )
	{
		if( new_pos[0] < 0 || new_pos[0] >= width || new_pos[1] < 0 || new_pos[1] >= height ) {
			return false;
		}
		
		if( isOccupied( new_pos, taxi ) ) {
			return false;
		}
		
		if( old_pos[0] != new_pos[0] ) {
			assert( old_pos[1] == new_pos[1] );
			final int min_x = Math.min( old_pos[0], new_pos[0] );
			if( (topology[min_x][old_pos[1]] & wall_right) != 0 ) {
				return false;
			}
		}
		else if( old_pos[1] != new_pos[1] ) {
			assert( old_pos[0] == new_pos[0] );
			final int min_y = Math.min( old_pos[1], new_pos[1] );
			if( (topology[old_pos[0]][min_y] & wall_up) != 0 ) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public boolean isTerminal()
	{
		return goal || t >= T;
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof TaxiState) ) {
			return false;
		}
		final TaxiState that = (TaxiState) obj;
		return new PrimitiveTaxiRepresentation( this ).equals( new PrimitiveTaxiRepresentation( that ) );
	}
	
	@Override
	public int hashCode()
	{
		return new PrimitiveTaxiRepresentation( this ).hashCode();
	}
	
	@Override
	public String toString()
	{
		return new PrimitiveTaxiRepresentation( this ).toString();
	}
}
