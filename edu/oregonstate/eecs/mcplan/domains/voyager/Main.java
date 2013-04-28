/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.random.MersenneTwister;

import edu.oregonstate.eecs.mcplan.experiments.Environment;
import edu.oregonstate.eecs.mcplan.experiments.Experiment;
import edu.oregonstate.eecs.mcplan.experiments.ExperimentalSetup;
import edu.oregonstate.eecs.mcplan.experiments.MultipleInstanceMultipleWorldGenerator;
import edu.oregonstate.eecs.mcplan.experiments.PolicyComparison;
import edu.oregonstate.eecs.mcplan.experiments.PolicyFactory;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveListener;

/**
 * @author jhostetler
 *
 */
public class Main
{
	private static PolicyFactory<VoyagerState, VoyagerEvent, VoyagerParameters, VoyagerInstance>
	createPolicy( final String[] args )
	{
		final String policy_name = args[0];
		final String[] policy_args = Arrays.copyOfRange( args, 1, args.length );
		if( "FastExpandPolicy".equals( policy_name ) ) {
			// TODO:
			return null;
		}
		else {
			throw new IllegalArgumentException();
		}
	}
	
	public static File createDirectory( final String[] args )
	{
		final File r = new File( args[0] );
		r.mkdir();
		final File d = new File( r, "x" + args[1] + "_" + args[2] + "_" + args[3] );
		d.mkdir();
		return d;
	}
	
	/**
	 * @param args
	 */
	public static void main( final String[] args )
	{
		System.out.println( args.toString() );
		final String batch_name = args[0];
		final String[] instance_args = args[1].split( "," );
		final String[] pi_args = args[2].split( "," );
		final String[] phi_args = args[3].split( "," );
		
		final File root_directory = createDirectory( args );
		final int Nplanets = Integer.parseInt( instance_args[0] );
		final int policy_epoch = Integer.parseInt( instance_args[1] );
		final int Nworlds = Integer.parseInt( instance_args[2] );
		final int max_time = Integer.parseInt( instance_args[3] );
		final int Nanytime = Integer.parseInt( instance_args[4] );
		
		// FIXME: This default_params thing is too error-prone! There's no
		// easy way to know whether you need to set a parameter in
		// 1) default_params
		// 2) an element of ps
		// 3) both places
		final VoyagerParameters default_params = new VoyagerParameters.Builder()
			.Nplanets( Nplanets ).policy_epoch( policy_epoch ).finish();
		final Environment default_environment = new Environment.Builder()
			.root_directory( root_directory )
			.rng( new MersenneTwister( default_params.master_seed ) )
			.finish();
		
		final int[] anytime_times = new int[Nanytime];
		anytime_times[Nanytime - 1] = max_time;
		for( int i = Nanytime - 2; i >= 0; --i ) {
			anytime_times[i] = anytime_times[i + 1] / 2;
		}
		
		final List<VoyagerParameters> ps = new ArrayList<VoyagerParameters>( Nanytime );
		for( final int t : anytime_times ) {
			ps.add( new VoyagerParameters.Builder()
					.max_time( t ).Nplanets( Nplanets )
					.policy_epoch( policy_epoch ).finish() );
		}
		
		final List<VoyagerInstance> ws = new ArrayList<VoyagerInstance>( Nworlds );
		for( int i = 0; i < Nworlds; ++i ) {
			// FIXME: Why default_params and not ps.get( i ) ?
			ws.add( new VoyagerInstance( default_params, default_environment.rng.nextInt() ) );
		}
		
		final MultipleInstanceMultipleWorldGenerator<VoyagerParameters, VoyagerInstance>
			experimental_setups = new MultipleInstanceMultipleWorldGenerator<VoyagerParameters, VoyagerInstance>(
				default_environment, ps, ws );
		
		final ArrayList<SimultaneousMoveListener<VoyagerState, VoyagerEvent>> extra_listeners
			= new ArrayList<SimultaneousMoveListener<VoyagerState, VoyagerEvent>>();
		
		if( default_params.use_monitor ) {
			// TODO: Actually create the viewport!
			extra_listeners.add( new VisualizationUpdater() );
		}
		
		final Experiment<VoyagerParameters, VoyagerInstance> experiment
			= new PolicyComparison<VoyagerState, VoyagerEvent, VoyagerParameters, VoyagerInstance>(
				createPolicy( pi_args ), createPolicy( phi_args ), extra_listeners );
		
		while( experimental_setups.hasNext() ) {
			final ExperimentalSetup<VoyagerParameters, VoyagerInstance> setup = experimental_setups.next();
			experiment.setup( setup.environment, setup.parameters, setup.world );
			experiment.run();
			experiment.finish();
		}
		
		System.exit( 0 );
	}
}
