/* LICENSE
Copyright (c) 2013-2016, Jesse Hostetler (jessehostetler@gmail.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

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
 * The state is considered *terminal* if current_P_total() == 0 at construction
 * time.
 * <p>
 * CosmicState instances are intended to be *immutable*. They are not
 * necessarily *actually immutable*, but don't try to change them!
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
			{
				writer.name( "t" ).value( s.t );
				
				writer.name( "ps" ).beginObject();
				{
					MWCharArray casename = null;
					MWLogicalArray blackout = null;
					try {
						casename = (MWCharArray) s.ps.getField( "casename", 1 );
						writer.name( "casename" ).value( casename.toString() );
						blackout = (MWLogicalArray) s.ps.getField( "blackout", 1 );
						writer.name( "blackout" ).value( blackout.getBoolean( 1 ) );
					}
					finally {
						casename.dispose();
						blackout.dispose();
					}
					
					final String[] psfields = new String[] {
						"baseMVA",
						"bus", "branch", "gen", "mac", "shunt", "exc", "gov",
						"t_delay", "t_prev_check", "dist2threshold", "state_a",
						"x", "y"
					};
					for( final String field : psfields ) {
						writer.name( field );
						MWNumericArray f = null;
						try {
							f = (MWNumericArray) s.ps.getField( field, 1 );
							mw.write( writer, f );
						}
						finally {
							f.dispose();
						}
					}
				}
				writer.endObject();
				
	//			writer.name( "x" );
	//			mw.write( writer, s.mx );
	//
	//			writer.name( "y" );
	//			mw.write( writer, s.my );
	//
	//			writer.name( "event" );
	//			mw.write( writer, s.event );
			}
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
			MWStructArray subindex = null;
			try {
				subindex = (MWStructArray) params.index.getField( "x", 1 );
				final int idx = (int) subindex.get( "omega_pu", 1 );
				return mx.getDouble( idx );
			}
			finally {
				subindex.dispose();
			}
//			final int idx = ((MWNumericArray) subindex.getField( "omega_pu", 1 )).getInt();
//			return mx.getDouble( idx );
		}
	}
	
	public final CosmicParameters params;
	public final MWStructArray ps;
	public final double t;
	public final MWNumericArray mx;
	public final MWNumericArray my;
	public final MWNumericArray event;
	
	public final X x = new X();
	
	public final boolean blackout;
	
	// Non-Cosmic State ------------------------------------------------------
	
	private int error = CosmicError.None.code;
	public final TIntList islands = new TIntArrayList();
	
	// -----------------------------------------------------------------------
	
	/**
	 * CosmicState owns all Matlab objects passed to this constructor.
	 * @param ps
	 * @param t
	 * @param x
	 * @param y
	 * @param event
	 */
	public CosmicState( final CosmicParameters params, final MWStructArray ps, final double t )
	{
		// FIXME: We are retaining the 'event' field so that we can switch
		// between take_action() and take_action2(). Eventually we should
		// remove it.
		this( params, ps, t, (MWNumericArray) ps.getField( "x", 1 ),
		      (MWNumericArray) ps.getField( "y", 1 ), new MWNumericArray() );
	}
	
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
		
		this.blackout = (current_P_total() == 0);
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
	
	public CosmicState createSuccessor( final CosmicParameters params,
										final CosmicAction a,
										final MWStructArray ps, final double t )
	{
		final CosmicState sprime = new CosmicState( params, ps, t );
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
		if( event != null ) {
			event.dispose();
		}
	}
	
	public double current_P_total()
	{
		double P = 0;
		for( final Shunt sh : shunts() ) {
			final double cur_p = sh.current_P();
			P += Math.max( cur_p, 0 );
		}
		return P;
	}
	
	@Override
	public boolean isTerminal()
	{
		// FIXME: The 't > 0' condition is a hack to work around the problem
		// that current_P = 0 at t = 0 for some reason.
		return error != CosmicError.None.code || (t > 0 && blackout) || t >= params.T;
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
	public DistanceBusPair[] nearestBusesByRowElectricalDistance( final CosmicParameters params, final int bus_id )
	{
		MWNumericArray E = null;
		try {
			E = (MWNumericArray) ps.getField( "Ebus", 1 );
			final int[] idx = new int[] { params.matlabIndex( bus( bus_id ) ), 1 };
			final DistanceBusPair[] result = new DistanceBusPair[params.Nbus];
			for( int i = 0; i < params.Nbus; ++i ) {
				idx[1] = i + 1;
				final double d = E.getDouble( idx );
				result[i] = new DistanceBusPair( d, params.bus_id_at(i + 1) );
			}
			Arrays.sort( result );
			return result;
		}
		finally {
			E.dispose();
		}
	}
	
	// -----------------------------------------------------------------------
	
	public Branch branch( final int id )
	{
		return new Branch( id, params, ps );
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
	 * Returns a Bus instance for the bus with bus.id == 'id'.
	 * @param id
	 * @return
	 */
	public Bus bus( final int id )
	{
		return new Bus( id, params, ps );
	}
	
	/**
	 * Returns a Bus instance for the bus at Matlab index 'mi'.
	 * @param mi
	 * @return
	 */
	public Bus busAt( final int mi )
	{
		return new Bus( params.bus_id_at( mi ), params, ps );
	}
	
	public Iterable<Bus> buses()
	{
		return new Iterable<Bus>() {
			@Override
			public Iterator<Bus> iterator()
			{
				return new Iterator<Bus>() {
					Iterator<Map.Entry<Integer, Integer>> itr = params.bus_id_to_matlab_index.entrySet().iterator();
					
					@Override
					public boolean hasNext()
					{ return itr.hasNext(); }

					@Override
					public Bus next()
					{ return bus( itr.next().getKey() ); }

					@Override
					public void remove()
					{ throw new UnsupportedOperationException(); }
				};
			}
		};
	}
	
	public Generator generator( final int id )
	{
		return new Generator( id, params, ps );
	}
	
	public Machine machine( final int id )
	{
		return new Machine( id, params, ps );
	}
	
	public Iterable<Machine> machines()
	{
		return new Iterable<Machine>() {
			@Override
			public Iterator<Machine> iterator()
			{
				return new Iterator<Machine>() {
					int i = 1;
					@Override
					public boolean hasNext()
					{ return i <= params.Nmachine; }

					@Override
					public Machine next()
					{ return machine( i++ ); }

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
		return new Shunt( id, params, ps );
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
	
	public int[] liveShunts()
	{
		final TIntList live_shunts = new TIntArrayList();
		for( final Shunt sh : shunts() ) {
			// Exclude shunts that are already tripped, and shunts that
			// generate negative power.
			if( sh.factor() > 0 && sh.P() > 0 ) {
				live_shunts.add( sh.id() );
			}
		}
		return live_shunts.toArray();
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
