/**
 * 
 */
package edu.oregonstate.eecs.mcplan.experiments;

import edu.oregonstate.eecs.mcplan.domains.galcon.FastGalconEvent;
import edu.oregonstate.eecs.mcplan.domains.galcon.FastGalconState;
import edu.oregonstate.eecs.mcplan.domains.galcon.GalconSimulator;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveSimulator;

/**
 * @author jhostetler
 *
 */
public class Instance
{
	public final GalconSimulator sim;
	public final FastGalconState fast_state;
	public final SimultaneousMoveSimulator<FastGalconState, FastGalconEvent> fast_sim;
	
	public Instance( final Parameters params, final int instance_seed )
	{
		sim = new GalconSimulator(
			params.horizon, params.primitive_epoch, false, false,
			instance_seed, params.min_launch_percentage, params.launch_size_steps );
		fast_state = new FastGalconState(
			sim, params.policy_epoch, params.horizon,
			params.min_launch_percentage, params.launch_size_steps );
		fast_sim = fast_state;
	}
}
