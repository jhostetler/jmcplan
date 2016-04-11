/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mathworks.toolbox.javabuilder.MWCharArray;
import com.mathworks.toolbox.javabuilder.MWLogicalArray;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.mathworks.toolbox.javabuilder.MWStructArray;

import edu.oregonstate.eecs.mcplan.State;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * Represents a Cosmic state.
 * <p>
 * CosmicState owns all Matlab objects passed to its constructor.
 */
public class CosmicState implements State
{
	public static class GsonSerializer implements TypeAdapterFactory
	{
		@SuppressWarnings( "unchecked" )
		@Override
		public <T> TypeAdapter<T> create( final Gson gson, final TypeToken<T> token )
		{
			if( token.getRawType() != CosmicState.class ) {
				return null;
			}
			
			final TypeAdapter<?> mw = gson.getAdapter( TypeToken.get( MWNumericArray.class ) );
			return (TypeAdapter<T>) new GsonTypeAdapter( (TypeAdapter<MWNumericArray>) mw );
		}
	}

	public static class GsonTypeAdapter extends TypeAdapter<CosmicState>
	{
		private final TypeAdapter<MWNumericArray> mw;
		
		public GsonTypeAdapter( final TypeAdapter<MWNumericArray> mw )
		{
			this.mw = mw;
		}
		
		@Override
		public CosmicState read( final JsonReader reader ) throws IOException
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void write( final JsonWriter writer, final CosmicState s )
				throws IOException
		{
			writer.beginObject();
			
			writer.name( "t" ).value( s.t );
			
			writer.name( "ps" ).beginObject();
			writer.name( "casename" ).value( ((MWCharArray) s.ps.getField( "casename", 1 )).toString() );
			writer.name( "blackout" ).value( ((MWLogicalArray) s.ps.getField( "blackout", 1 )).getBoolean( 1 ) );
			final String[] psfields = new String[] {
				"baseMVA",
				"bus", "branch", "gen", "mac", "shunt", "exc", "gov",
				"t_delay", "t_prev_check", "dist2threshold", "state_a"
			};
			for( final String field : psfields ) {
				writer.name( field );
				mw.write( writer, (MWNumericArray) s.ps.getField( field, 1 ) );
			}
			writer.endObject();
			
			writer.name( "x" );
			mw.write( writer, s.mx );
			
			writer.name( "y" );
			mw.write( writer, s.my );
			
			writer.name( "event" );
			mw.write( writer, s.event );
			
			writer.endObject();
		}
	}
	
	// -----------------------------------------------------------------------
	
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
	
	public final class X
	{
		public MWNumericArray asNumericArray()
		{
			return mx;
		}
		
		public double omega_pu( final Generator g )
		{
			final MWStructArray subindex = (MWStructArray) params.index.getField( "x", 1 );
			final int idx = ((MWNumericArray) subindex.getField( "omega_pu", 1 )).getInt();
			return mx.getDouble( idx );
		}
	}
	
	public final CosmicParameters params;
	public final MWStructArray ps;
	public final double t;
	public final MWNumericArray mx;
	public final MWNumericArray my;
	public final MWNumericArray event;
	
	public final X x = new X();
	
	// Non-Cosmic State ------------------------------------------------------
	
	private int error = CosmicError.None.code;
	/*internal*/ final TIntList islands = new TIntArrayList();
	
	// -----------------------------------------------------------------------
	
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
		this.mx = x;
		this.my = y;
		this.event = event;
	}
	
	public CosmicState createSuccessor( final CosmicParameters params,
										final CosmicAction a,
										final MWStructArray ps, final double t,
										final MWNumericArray x, final MWNumericArray y, final MWNumericArray event )
	{
		final CosmicState sprime = new CosmicState( params, ps, t, x, y, event );
		copyNonCosmicStateInto( sprime );
		a.applyNonCosmicChanges( sprime );
		return sprime;
	}
	
	private void copyNonCosmicStateInto( final CosmicState sprime )
	{
		sprime.error = this.error;
		
		assert( sprime.islands.isEmpty() );
		sprime.islands.addAll( this.islands );
	}
	
	public CosmicState copy()
	{
		try {
			final CosmicState sprime = new CosmicState( params, (MWStructArray) ps.clone(), t,
									(MWNumericArray) mx.clone(), (MWNumericArray) my.clone(),
									(MWNumericArray) event.clone() );
			copyNonCosmicStateInto( sprime );
			return sprime;
		}
		catch( final CloneNotSupportedException ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	@Override
	public void close()
	{
		ps.dispose();
		mx.dispose();
		my.dispose();
		event.dispose();
	}
	
	@Override
	public boolean isTerminal()
	{
		return error != CosmicError.None.code || t >= params.T;
	}
	
	public void setError( final CosmicError err )
	{
		error |= err.code;
	}
	
	public boolean testError()
	{
		return error != CosmicError.None.code;
	}
	
	public boolean testError( final CosmicError err )
	{
		return (error & err.code) != 0;
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
	
	// -----------------------------------------------------------------------
	
	public Branch branch( final int id )
	{
		return new Branch( id, params, (MWNumericArray) ps.getField( "branch", 1 ) );
	}
	
	public Iterable<Branch> branches()
	{
		return new Iterable<Branch>() {
			@Override
			public Iterator<Branch> iterator()
			{
				return new Iterator<Branch>() {
					int i = 1;
					@Override
					public boolean hasNext()
					{ return i <= params.Nbranch; }

					@Override
					public Branch next()
					{ return branch( i++ ); }

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
	
	public Generator generator( final int id )
	{
		return new Generator( id, params, (MWNumericArray) ps.getField( "gen", 1 ) );
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
	
	// -----------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "[CosmicState]\n" );
		
//		sb.append( "ps:\n" ).append( ps ).append( "\n" );
		sb.append( "t: " ).append( t ).append( "\n" );
//		sb.append( "mx:\n" ).append( mx ).append( "\n" );
//		sb.append( "my:\n" ).append( my ).append( "\n" );
//		sb.append( "event:\n" ).append( event ).append( "\n" );
		
		sb.append( "***** Buses:" ).append( "\n" );
		for( final Bus bus : buses() ) {
			sb.append( "\t" ).append( bus ).append( "\n" );
		}
		
		sb.append( "***** Branches:" ).append( "\n" );
		for( final Branch branch : branches() ) {
			sb.append( "\t" ).append( branch ).append( "\n" );
		}
		
		sb.append( "***** Shunts:" ).append( "\n" );
		for( final Shunt shunt : shunts() ) {
			sb.append( "\t" ).append( shunt ).append( "\n" );
		}
		return sb.toString();
	}
	
}
