/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic.policy;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.domains.cosmic.Bus;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicState;
import edu.oregonstate.eecs.mcplan.domains.cosmic.Shunt;
import gnu.trove.iterator.TIntIterator;

/**
 * @author jhostetler
 *
 */
public class SelectNearestShunt extends HystereticLoadShedding.ShuntSelector
{

	@Override
	public Shunt shunt( final CosmicState s, final Bus b )
	{
		final int bi = b.id();
		final CosmicState.DistanceBusPair[] db = s.nearestBusesByRowElectricalDistance( s.params, bi );
		System.out.println( "Sorted buses:\n" + Arrays.toString( db ) );
		for( int i = 0; i < db.length; ++i ) {
			final CosmicState.DistanceBusPair p = db[i];
//			if( p.bus == bi ) {
//				continue;
//			}
			final TIntIterator sh_itr = s.params.bus_to_shunts.get( p.bus ).iterator();
			while( sh_itr.hasNext() ) {
				final int sh_id = sh_itr.next();
				final Shunt sh = s.shunt( sh_id );
				System.out.println( "\t" + p.bus + " -> " + sh_id + ": " + sh );
				if( sh.factor() > 0 ) {
					return sh;
				}
			}
		}
		
		return null;
	}

	@Override
	public String toString()
	{
		return "Nearest";
	}

}
