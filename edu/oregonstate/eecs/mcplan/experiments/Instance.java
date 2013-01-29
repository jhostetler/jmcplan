/**
 * 
 */
package edu.oregonstate.eecs.mcplan.experiments;

import java.io.PrintStream;

import edu.oregonstate.eecs.mcplan.agents.galcon.CoarseSimulation;
import edu.oregonstate.eecs.mcplan.domains.galcon.FastGalconEvent;
import edu.oregonstate.eecs.mcplan.domains.galcon.FastGalconState;
import edu.oregonstate.eecs.mcplan.domains.galcon.GalconSimulator;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveSimulator;

/**
 * @author jhostetler
 *
 */
public class Instance implements CsvWriter, Copyable<Instance>
{
	public final GalconParameters params;
	public final int instance_seed;
	public final GalconSimulator sim;
	public final FastGalconState fast_state;
	public final SimultaneousMoveSimulator<FastGalconState, FastGalconEvent> fast_sim;
	public final CoarseSimulation<FastGalconState, FastGalconEvent> durative_sim;
	
	public Instance( final GalconParameters params, final int instance_seed )
	{
		this.params = params;
		this.instance_seed = instance_seed;
		sim = new GalconSimulator(
			params.horizon, params.primitive_epoch, false, false,
			instance_seed, params.min_launch_percentage, params.launch_size_steps );
		fast_state = new FastGalconState(
			sim, params.primitive_epoch, params.horizon,
			params.min_launch_percentage, params.launch_size_steps );
		fast_sim = fast_state;
		durative_sim = new CoarseSimulation<FastGalconState, FastGalconEvent>( fast_sim, params.policy_epoch );
	}
	
	public Instance( final Instance that )
	{
		params = that.params;
		instance_seed = that.instance_seed;
		sim = that.sim;
		fast_state = that.fast_state.copy();
		fast_sim = fast_state;
		durative_sim = new CoarseSimulation<FastGalconState, FastGalconEvent>( fast_sim, params.policy_epoch );
	}
	
	@Override
	public Instance copy()
	{
		return new Instance( this );
	}

	@Override
	public void writeCsv( final PrintStream out )
	{
		out.println( "key,value" );
		out.println( "instance_seed," + instance_seed );
		params.writeCsv( out );
	}
}
