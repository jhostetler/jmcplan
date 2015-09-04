/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.ipc.crossing;

import java.io.File;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class IpcCrossingTest
{

	/**
	 * @param args
	 */
	public static void main( final String[] args )
	{
		final File domain = new File( "final_comp_2014/rddl_domains/crossing_traffic_mdp.rddl" );
		final File instance = new File( "final_comp_2014/rddl/crossing_traffic_inst_mdp__4.rddl" );
		final IpcCrossingState s0 = IpcCrossingDomains.parse( domain, instance );
		
		final RandomGenerator rng = new MersenneTwister( 42 );
		
		final IpcCrossingFsssModel model = new IpcCrossingFsssModel( rng, s0 );
		
		IpcCrossingState s = model.initialState();
		for( int t = 0; t < s.params.T; ++t ) {
			System.out.println( s );
			final IpcCrossingAction a = Fn.uniform_choice( rng, model.actions( s ) );
			System.out.println( a );
			s = model.sampleTransition( s, a );
		}
		
		System.out.println( s );
	}

}
