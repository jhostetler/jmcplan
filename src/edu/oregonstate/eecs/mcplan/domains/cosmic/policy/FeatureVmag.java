/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic.policy;

import edu.oregonstate.eecs.mcplan.domains.cosmic.Bus;

/**
 * @author jhostetler
 *
 */
public class FeatureVmag extends HystereticLoadShedding.Feature
{
	@Override
	public double forBus( final Bus b )
	{
		return b.Vmag();
	}

	@Override
	public String toString()
	{
		return "Vmag";
	}
}
