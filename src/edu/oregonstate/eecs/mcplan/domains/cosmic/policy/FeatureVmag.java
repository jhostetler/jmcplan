/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic.policy;

import edu.oregonstate.eecs.mcplan.domains.cosmic.Bus;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicState;
import edu.oregonstate.eecs.mcplan.domains.cosmic.Shunt;

/**
 * @author jhostetler
 *
 */
public class FeatureVmag extends HystereticLoadShedding.Feature
{
	@Override
	public String toString()
	{
		return "bus.Vmag";
	}

	@Override
	public double[] forState( final CosmicState s )
	{
		final double[] result = new double[s.params.Nbus];
		int idx = 0;
		for( final Bus b : s.buses() ) {
			result[idx++] = b.Vmag();
		}
		return result;
	}

	@Override
	public Shunt shunt( final CosmicState s, final int fault_idx )
	{
		return SelectNearestShunt.forBus( s, s.bus( fault_idx+1 ) );
	}
}
