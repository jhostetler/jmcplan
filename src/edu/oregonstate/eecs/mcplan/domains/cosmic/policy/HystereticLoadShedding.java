/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic.policy;

import java.util.Arrays;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicAction;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicNothingAction;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicParameters;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicState;
import edu.oregonstate.eecs.mcplan.domains.cosmic.Shunt;
import edu.oregonstate.eecs.mcplan.domains.cosmic.TripShuntAction;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;


/**
 * Under-voltage load shedding with response hysteresis.
 */
public class HystereticLoadShedding extends Policy<CosmicState, CosmicAction>
{
	public static abstract class Feature
	{
		public abstract double[] forState( final CosmicState s );
		
		public abstract Shunt shunt( final CosmicState s, final int fault_idx );
		
		@Override
		public abstract String toString();
	}
	
	// -----------------------------------------------------------------------
	
	private final RandomGenerator rng;
	
	private final Feature feature;
	private final double fault_threshold;
	private final double clear_threshold;
	private final double delay;
	
	private final boolean[] fault;
	private final double[] tref;
	private final double[] time;
	
	private final TIntSet tripped = new TIntHashSet();
	private CosmicState s = null;
	
	public HystereticLoadShedding( final RandomGenerator rng, final Feature feature,
								   final CosmicParameters params,
								   final double fault_threshold, final double clear_threshold, final double delay )
	{
		this.rng = rng;
		this.feature = feature;
		this.fault_threshold = fault_threshold;
		this.clear_threshold = clear_threshold;
		this.delay = delay;
		
		fault = new boolean[params.Nbus];
		time = new double[params.Nbus];
		tref = new double[params.Nbus];
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
	public final void setState( final CosmicState s, final long t )
	{
		this.s = s;
		tripped.clear();
		final double[] f = feature.forState( s );
		for( int idx = 0; idx < f.length; ++idx ) {
			if( fault[idx] ) { // Under-voltage condition ongoing
				if( f[idx] < clear_threshold ) {
					time[idx] = s.t;
					if( time[idx] - tref[idx] > delay ) {
//						System.out.println( "" + this + ": Fault index " + idx );
						tripped.add( idx );
					}
				}
				else {
					fault[idx] = false;
					time[idx] = tref[idx] = 0;
				}
			}
			else if( f[idx] < fault_threshold ) { // Trigger under-voltage condition
				fault[idx] = true;
				time[idx] = tref[idx] = s.t;
			}
		}
	}

	@Override
	public final CosmicAction getAction()
	{
		while( !tripped.isEmpty() ) {
			final int r = rng.nextInt( tripped.size() );
			final TIntIterator itr = tripped.iterator();
			for( int i = 0; i < r; ++i ) {
				itr.next();
			}
			final int fault_idx = itr.next();
			
			final Shunt sh = feature.shunt( s, fault_idx );
			if( sh != null ) {
				return new TripShuntAction( sh.id() );
			}
			else {
				tripped.remove( fault_idx );
			}
		}
		
		return new CosmicNothingAction();
	}

	@Override
	public final void actionResult( final CosmicState sprime, final double[] r )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName()
	{
		return "LoadShedding";
	}

	@Override
	public int hashCode()
	{
		final HashCodeBuilder hb = new HashCodeBuilder();
		hb.append( getClass() ).append( fault_threshold ).append( clear_threshold ).append( delay );
		return hb.toHashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof HystereticLoadShedding) ) {
			return false;
		}
		final HystereticLoadShedding that = (HystereticLoadShedding) obj;
		return fault_threshold == that.fault_threshold
			   && clear_threshold == that.clear_threshold
			   && delay == that.delay;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "LS_H[" ).append( feature ).append( "; " ).append( fault_threshold )
		  .append( "; " ).append( clear_threshold ).append( "; " ).append( delay ).append( "]" );
		return sb.toString();
	}
}
