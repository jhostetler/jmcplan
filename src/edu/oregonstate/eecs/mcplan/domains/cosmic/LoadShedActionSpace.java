/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import java.util.Iterator;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.oregonstate.eecs.mcplan.ActionSet;
import edu.oregonstate.eecs.mcplan.ActionSpace;
import edu.oregonstate.eecs.mcplan.Representation;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * @author jhostetler
 *
 */
public class LoadShedActionSpace extends ActionSpace<CosmicState, CosmicAction>
{
	private static final class A extends ActionSet<CosmicState, CosmicAction>
	{
		private class Itr implements Iterator<CosmicAction>
		{
			int i = 0;
			int shunt = 1; // 'shunt' is a Matlab index so it starts at 1
			
			@Override
			public boolean hasNext()
			{
				return i < size();
			}

			@Override
			public CosmicAction next()
			{
				assert( hasNext() );
				final CosmicAction a;
				if( i == 0 ) {
					a = new CosmicNothingAction();
				}
				else {
					while( tripped_shunts.contains( shunt ) ) {
						shunt += 1;
					}
					
					a = new TripShuntAction( shunt );
				}
				
				i += 1;
				return a;
			}

			@Override
			public void remove()
			{ throw new UnsupportedOperationException(); }
		}
		
		final CosmicParameters params;
		final TIntSet tripped_shunts;
		
		public A( final CosmicState s )
		{
			params = s.params;
			
			tripped_shunts = new TIntHashSet();
			for( final Shunt sh : s.shunts() ) {
				// Exclude shunts that are already tripped, and shunts that
				// generate negative power.
				if( sh.factor() <= 0 || sh.P() < 0 ) {
					tripped_shunts.add( sh.id() );
				}
			}
		}
		
		private A( final A that )
		{
			params = that.params;
			tripped_shunts = that.tripped_shunts;
		}

		@Override
		public Iterator<CosmicAction> iterator()
		{
			return new Itr();
		}

		@Override
		public int size()
		{
			return 1 + (params.Nshunt - tripped_shunts.size());
		}

		@Override
		public Representation<CosmicState> copy()
		{
			return new A( this );
		}

		@Override
		public boolean equals( final Object obj )
		{
			if( !(obj instanceof A) ) {
				return false;
			}
			final A that = (A) obj;
			return tripped_shunts.equals( that.tripped_shunts );
		}

		@Override
		public int hashCode()
		{
			final HashCodeBuilder hb = new HashCodeBuilder();
			hb.append( getClass().hashCode() ).append( tripped_shunts );
			return hb.toHashCode();
		}
		
	}
	
	// -----------------------------------------------------------------------

	@Override
	public ActionSet<CosmicState, CosmicAction> getActionSet( final CosmicState s )
	{
		return new A( s );
	}
	
	@Override
	public String toString()
	{
		return "LoadShed";
	}

	@Override
	public int cardinality()
	{
		throw new UnsupportedOperationException();
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
	public int index( final CosmicAction a )
	{
		throw new UnsupportedOperationException();
	}

}
