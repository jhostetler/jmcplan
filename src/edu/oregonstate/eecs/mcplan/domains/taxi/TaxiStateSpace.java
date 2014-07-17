/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.taxi;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public class TaxiStateSpace extends edu.oregonstate.eecs.mcplan.StateSpace<TaxiState>
{
	private class G extends Generator<TaxiState>
	{
		public final int Nsequence = 2 + 1 + 1 + 2*Nother_taxis;
		public final int[] ranges = new int[Nsequence];
		public final int[] sequence = new int[Nsequence];
		public int count = 0;
		
		public G()
		{
			int idx = 0;
			ranges[idx++] = width;
			ranges[idx++] = height;
			ranges[idx++] = Nlocations + 1;
			ranges[idx++] = Nlocations;
			for( int i = 0; i < Nother_taxis; ++i ) {
				ranges[idx++] = width;
				ranges[idx++] = height;
			}
		}
		
		@Override
		public boolean hasNext()
		{
			return count < Nstates;
		}

		@Override
		public TaxiState next()
		{
			final TaxiState s = new TaxiState( topology, locations, Nother_taxis );
			
			int idx = 0;
			s.taxi[0] = sequence[idx++];
			s.taxi[1] = sequence[idx++];
			s.passenger = sequence[idx++] - 1;
			s.destination = sequence[idx++];
			for( int i = 0; i < Nother_taxis; ++i ) {
				s.other_taxis[i][0] = sequence[idx++];
				s.other_taxis[i][1] = sequence[idx++];
			}
			
			for( int i = 0; i < Nsequence; ++i ) {
				sequence[i] += 1;
				if( sequence[i] == ranges[i] ) {
					sequence[i] = 0;
				}
				else {
					break;
				}
			}
			
			// We now check if two taxis are in the same (x, y) coordinate,
			// and reject the current state if they are.
			boolean bad_state = s.isOccupiedByOther( s.taxi );
			for( int i = 0; i < Nother_taxis; ++i ) {
				if( bad_state ) {
					break;
				}
				bad_state = bad_state || s.isOccupied( s.other_taxis[i], i );
			}
			if( bad_state ) {
				return next();
			}
			
			// This state is good; increment count and return
			count += 1;
			return s;
		}
	}
	
	// -----------------------------------------------------------------------
	
	public final int[][] topology;
	public final ArrayList<int[]> locations;
	
	public final int width;
	public final int height;
	public final int Nlocations;
	public final int Nother_taxis;
	
	private final int Nstates;
	
	public TaxiStateSpace( final TaxiState s )
	{
		topology = s.topology;
		locations = s.locations;
		width = s.width;
		height = s.height;
		Nlocations = s.locations.size();
		Nother_taxis = s.Nother_taxis;
		
		int ns = width*height * Nlocations * (Nlocations + 1);
		for( int i = 0; i < Nother_taxis; ++i ) {
			ns *= width*height - i - 1;
		}
		Nstates = ns;
	}
	
	@Override
	public int cardinality()
	{
		return Nstates;
	}

	@Override
	public boolean isFinite()
	{
		return true;
	}

	@Override
	public boolean isCountable()
	{
		return true;
	}

	@Override
	public Generator<TaxiState> generator()
	{
		return new G();
	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] argv )
	{
		final TaxiState s = TaxiWorlds.dietterich2000( 2 );
		final TaxiStateSpace ss = new TaxiStateSpace( s );
		final Generator<TaxiState> g = ss.generator();
		
		int count = 0;
		while( g.hasNext() ) {
			final TaxiState si = g.next();
			if( si.toString().equals( "[0.0, 0.0, 4.0, 4.0, 3.0, 4.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0]" ) ) {
				System.out.println( si );
			}
//			System.out.println( si );
			count += 1;
		}
		System.out.println( count );
		assert( count == ss.cardinality() );
	}
}
