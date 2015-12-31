/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import java.util.Arrays;
import java.util.Iterator;

import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.mathworks.toolbox.javabuilder.MWStructArray;

import edu.oregonstate.eecs.mcplan.State;

/**
 * Represents a Cosmic state.
 * <p>
 * CosmicState owns all Matlab objects passed to its constructor.
 */
public class CosmicState implements State
{
	public static final class DistanceBusPair implements Comparable<DistanceBusPair>
	{
		public final double d;
		public final int bus;
		
		public DistanceBusPair( final double d, final int bus )
		{
			this.d = d;
			this.bus = bus;
		}

		@Override
		public int compareTo( final DistanceBusPair that )
		{
			final double dd = d - that.d;
			if( dd != 0 ) {
				return (int) Math.signum( dd );
			}
			else {
				return (int) Math.signum( bus - that.bus );
			}
		}
		
		@Override
		public String toString()
		{
			return "(" + d + ", " + bus + ")";
		}
	}
	
	public final CosmicParameters params;
	public final MWStructArray ps;
	public final double t;
	public final MWNumericArray x;
	public final MWNumericArray y;
	public final MWNumericArray event;
	
	/**
	 * CosmicState owns all Matlab objects passed to this constructor.
	 * @param ps
	 * @param t
	 * @param x
	 * @param y
	 * @param event
	 */
	public CosmicState( final CosmicParameters params, final MWStructArray ps, final double t,
						final MWNumericArray x, final MWNumericArray y, final MWNumericArray event )
	{
		this.params = params;
		this.ps = ps;
		this.t = t;
		this.x = x;
		this.y = y;
		this.event = event;
	}
	
	public CosmicState copy()
	{
		try {
			return new CosmicState( params, (MWStructArray) ps.clone(), t,
									(MWNumericArray) x.clone(), (MWNumericArray) y.clone(),
									(MWNumericArray) event.clone() );
		}
		catch( final CloneNotSupportedException ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	@Override
	public void close()
	{
		ps.dispose();
		x.dispose();
		y.dispose();
		event.dispose();
	}
	
	@Override
	public boolean isTerminal()
	{
		return t >= params.T;
	}
	
	/**
	 * Returns the buses ordered by electrical distance from the reference
	 * bus. The list *includes* the self-distance of bus to itself, which
	 * presumably will be 0. We consider the *row* of the distance matrix
	 * corresponding to 'bus', which might matter if E is not symmetric.
	 * @param params
	 * @param bus
	 * @return
	 */
	public DistanceBusPair[] nearestBusesByRowElectricalDistance( final CosmicParameters params, final int bus )
	{
		final MWNumericArray E = (MWNumericArray) ps.getField( "Ebus", 1 );
		final int[] idx = new int[] { bus, 1 };
		final DistanceBusPair[] result = new DistanceBusPair[params.Nbus];
		for( int i = 0; i < params.Nbus; ++i ) {
			idx[1] = i + 1;
			final double d = E.getDouble( idx );
			result[i] = new DistanceBusPair( d, i + 1 );
		}
		Arrays.sort( result );
		return result;
	}
	
	/**
	 * Returns a Shunt instance for the given shunt id.
	 * @param id
	 * @return
	 */
	public Bus bus( final int id )
	{
		return new Bus( id, params, (MWNumericArray) ps.getField( "bus", 1 ) );
	}
	
	public Iterable<Bus> buses()
	{
		return new Iterable<Bus>() {
			@Override
			public Iterator<Bus> iterator()
			{
				return new Iterator<Bus>() {
					int i = 1;
					@Override
					public boolean hasNext()
					{ return i <= params.Nbus; }

					@Override
					public Bus next()
					{ return bus( i++ ); }

					@Override
					public void remove()
					{ throw new UnsupportedOperationException(); }
				};
			}
		};
	}
	
	/**
	 * Returns a Shunt instance for the given shunt id.
	 * @param id
	 * @return
	 */
	public Shunt shunt( final int id )
	{
		return new Shunt( id, params, (MWNumericArray) ps.getField( "shunt", 1 ) );
	}
	
	public Iterable<Shunt> shunts()
	{
		return new Iterable<Shunt>() {
			@Override
			public Iterator<Shunt> iterator()
			{
				return new Iterator<Shunt>() {
					int i = 1;
					@Override
					public boolean hasNext()
					{ return i <= params.Nshunt; }

					@Override
					public Shunt next()
					{ return shunt( i++ ); }

					@Override
					public void remove()
					{ throw new UnsupportedOperationException(); }
				};
			}
		};
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "ps:\n" ).append( ps ).append( "\n" );
		sb.append( "t: " ).append( t ).append( "\n" );
		sb.append( "x:\n" ).append( x ).append( "\n" );
		sb.append( "y:\n" ).append( y ).append( "\n" );
		sb.append( "event:\n" ).append( event ).append( "\n" );
		return sb.toString();
	}
	
}
