/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.ipc.tamarisk;

import java.io.File;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class IpcTamariskTest
{

	/**
	 * @param args
	 */
	public static void main( final String[] args )
	{
		final File domain = new File( "final_comp_2014/rddl_domains/tamarisk_mdp.rddl" );
		final File instance = new File( "final_comp_2014/rddl/tamarisk_inst_mdp__1.rddl" );
		final IpcTamariskState s0 = IpcTamariskDomains.parse( domain, instance );
		
		final RandomGenerator rng = new MersenneTwister( 42 );
		
		final IpcTamariskFsssModel model = new IpcTamariskFsssModel(
			rng, s0.params, s0, new IpcTamariskReachRepresenter( s0.params ) );
		
		IpcTamariskState s = model.initialState();
		for( int t = 0; t < s.params.T; ++t ) {
			System.out.println( s );
			final IpcTamariskAction a = Fn.uniform_choice( rng, model.actions( s ) );
			System.out.println( a );
			s = model.sampleTransition( s, a );
		}
		
		System.out.println( s );
	}

}
