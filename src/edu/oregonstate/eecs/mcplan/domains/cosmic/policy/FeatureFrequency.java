/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic.policy;

import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicState;
import edu.oregonstate.eecs.mcplan.domains.cosmic.Shunt;

/**
 * @author jhostetler
 *
 */
public class FeatureFrequency extends HystereticLoadShedding.Feature
{
	@Override
	public String toString()
	{
		return "shunt.omega_pu";
	}

	@Override
	public double[] forState( final CosmicState s )
	{
		final double[] result = new double[s.params.Nshunt];
		int idx = 0;
		for( final Shunt sh : s.shunts() ) {
			if( sh.hasLoad() ) {
				result[idx++] = sh.load_freq( s );
			}
			else {
				result[idx++] = 1.0; // Default value for "per unit"
			}
		}
		return result;
	}

	@Override
	public Shunt shunt( final CosmicState s, final int fault_idx )
	{
		return s.shunt( fault_idx+1 );
	}
}
