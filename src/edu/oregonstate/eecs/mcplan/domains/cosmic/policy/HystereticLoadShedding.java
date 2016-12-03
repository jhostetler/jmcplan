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
package edu.oregonstate.eecs.mcplan.domains.cosmic.policy;

import java.util.Arrays;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.LoggerManager;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicAction;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicNothingAction;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicParameters;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicState;
import edu.oregonstate.eecs.mcplan.domains.cosmic.ShedLoadAction;
import edu.oregonstate.eecs.mcplan.domains.cosmic.Shunt;
import edu.oregonstate.eecs.mcplan.util.Fn;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;


/**
 * Under-voltage load shedding with response hysteresis.
 */
public class HystereticLoadShedding extends AnytimePolicy<CosmicState, CosmicAction>
{
	public static abstract class Feature
	{
		public abstract double[] forState( final CosmicState s );
		
		public abstract Shunt shunt( final CosmicState s, final int fault_idx );
		
		@Override
		public abstract String toString();
	}
	
	// -----------------------------------------------------------------------
	
	private final ch.qos.logback.classic.Logger LogAgent = LoggerManager.getLogger( "log.agent" );
	
	private final Feature feature;
	private final double amount;
	private final double fault_threshold;
	private final double clear_threshold;
	private final double delay;
	private final boolean reset_on_action;
	
	private final boolean[] fault;
	private final double[] tref;
	private final double[] time;
	
	private final TIntIntMap tripped;
//	private double t = 0;
	
	/**
	 * @param rng
	 * @param feature
	 * @param params
	 * @param amount Amount (proportion of 1pu demand) to shed
	 * @param fault_threshold
	 * @param clear_threshold
	 * @param delay
	 */
	public HystereticLoadShedding( final Feature feature,
								   final CosmicParameters params, final double amount,
								   final double fault_threshold, final double clear_threshold, final double delay,
								   final boolean reset_on_action )
	{
		this( feature, amount,
		      fault_threshold, clear_threshold, delay, reset_on_action,
		      new boolean[params.Nbus],
		      new double[params.Nbus],
			  new double[params.Nbus], new TIntIntHashMap() );
	}
	
	private HystereticLoadShedding( final Feature feature, final double amount,
								   final double fault_threshold, final double clear_threshold, final double delay,
								   final boolean reset_on_action,
								   final boolean[] fault, final double[] tref, final double[] time,
								   final TIntIntMap tripped )
	{
		assert( amount > 0.0 );
		assert( amount <= 1.0 );
		assert( fault_threshold < clear_threshold );
		assert( delay >= 0.0 );
		
		this.feature = feature;
		this.amount = amount;
		this.fault_threshold = fault_threshold;
		this.clear_threshold = clear_threshold;
		this.delay = delay;
		this.reset_on_action = reset_on_action;
		
		this.fault = fault;
		this.tref = tref;
		this.time = time;
		this.tripped = tripped;
	}
	
	@Override
	public HystereticLoadShedding copy()
	{
		return new HystereticLoadShedding(
			feature, amount, fault_threshold, clear_threshold,
			delay, reset_on_action, Fn.copy( fault ), Fn.copy( tref ), Fn.copy( time ),
			new TIntIntHashMap( tripped ) );
										
	}
	
	@Override
	public final void reset()
	{
		Arrays.fill( fault, false );
		Arrays.fill( time, 0 );
		Arrays.fill( tref, 0 );
		tripped.clear();
	}
	
	@Override
	public final boolean isStationary()
	{
		return false;
	}
	
	@Override
	public final void setState( final CosmicState s, final long _t )
	{
//		tripped.clear();
//		this.t = s.t;
		final double[] f = feature.forState( s );
		for( int idx = 0; idx < f.length; ++idx ) {
			if( !fault[idx] && f[idx] < fault_threshold ) { // Trigger under-voltage condition
				LogAgent.debug( "{}: t = {}: Exceeded fault_threshold at {}", getName(), s.t, idx );
				fault[idx] = true;
				time[idx] = tref[idx] = s.t;
			}
			
			if( fault[idx] ) { // Under-voltage condition ongoing
				if( f[idx] < clear_threshold ) { // Not recovered
					time[idx] = s.t;
					if( time[idx] - tref[idx] >= delay ) {
						final int mi = idx + 1;
						final int bus_id = s.params.bus_id_at( mi );
						LogAgent.debug( "{}: t = {}: Fault at {} (bus {})", getName(), s.t, idx, bus_id );
						final Shunt sh = feature.shunt( s, idx );
						if( sh == null ) {
							tripped.remove( idx );
						}
						else {
							tripped.put( idx, sh.id() );
						}
//						tref[idx] = s.t; // Reset trip clock
					}
				}
				else {
					LogAgent.debug( "{}: t = {}: Cleared fault at {}", getName(), s.t, idx );
					fault[idx] = false;
					time[idx] = tref[idx] = 0;
					tripped.remove( idx );
				}
			}
		}
	}
	
	@Override
	public boolean improvePolicy()
	{
		return false;
	}

	@Override
	public final CosmicAction getAction()
	{
		if( tripped.isEmpty() ) {
			return new CosmicNothingAction();
		}
		else {
			final TIntSet shunt_set = new TIntHashSet();
			shunt_set.addAll( tripped.valueCollection() );
			final int[] to_shed = shunt_set.toArray();
			final CosmicAction a = new ShedLoadAction( to_shed, Fn.repeat( amount, to_shed.length ) );
			if( reset_on_action ) {
				final TIntIntIterator itr = tripped.iterator();
				while( itr.hasNext() ) {
					itr.advance();
					tref[itr.key()] = time[itr.key()]; // Reset clock
				}
			}
			tripped.clear();
			return a;
		}
		
//		while( !tripped.isEmpty() ) {
//			final int r = rng.nextInt( tripped.size() );
//			final TIntIterator itr = tripped.iterator();
//			for( int i = 0; i < r; ++i ) {
//				itr.next();
//			}
//			final int fault_idx = itr.next();
//
//			final Shunt sh = feature.shunt( s, fault_idx );
//			if( sh != null ) {
//				return new TripShuntAction( sh.id() );
//			}
//			else {
//				tripped.remove( fault_idx );
//			}
//		}
//
//		return new CosmicNothingAction();
	}

	@Override
	public final void actionResult( final CosmicState sprime, final double[] r )
	{ }

	@Override
	public String getName()
	{
		return "HLS";
	}

	@Override
	public int hashCode()
	{
		final HashCodeBuilder hb = new HashCodeBuilder();
		hb.append( getClass() ).append( amount ).append( fault_threshold ).append( clear_threshold ).append( delay );
		return hb.toHashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof HystereticLoadShedding) ) {
			return false;
		}
		final HystereticLoadShedding that = (HystereticLoadShedding) obj;
		return feature.getClass().equals( that.feature.getClass() )
			   && amount == that.amount
			   && fault_threshold == that.fault_threshold
			   && clear_threshold == that.clear_threshold
			   && delay == that.delay;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "HLS[" ).append( feature ).append( "; " ).append( amount )
		  .append( "; " ).append( fault_threshold ).append( "; " ).append( clear_threshold )
		  .append( "; " ).append( delay ).append( "]" );
		return sb.toString();
	}
}
