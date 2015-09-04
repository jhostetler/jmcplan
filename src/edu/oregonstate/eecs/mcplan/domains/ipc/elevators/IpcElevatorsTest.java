/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.ipc.elevators;

import java.io.File;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class IpcElevatorsTest
{

	/**
	 * @param args
	 */
	public static void main( final String[] args )
	{
		final int T = 40;
		final int Nfloors = 5;
		final int Nelevators = 2;
		final double[] arrive_param = new double[] { 0, 0.5, 0.5, 0.5, 0 };
		final IpcElevatorsParameters params = new IpcElevatorsParameters( T, Nfloors, Nelevators, arrive_param );
		final IpcElevatorsActionGenerator g = new IpcElevatorsActionGenerator( params );
		int count = 0;
		IpcElevatorsAction aprime = null;
		while( g.hasNext() ) {
			final IpcElevatorsAction a = g.next();
			System.out.println( a );
			count += 1;
			
			assert( a.equals( a.create() ) );
			if( aprime != null ) {
				assert( !a.equals( aprime ) );
			}
			aprime = a;
		}
		System.out.println( "Nactions = " + count );
		
		final File domain = new File( "final_comp_2014/rddl_domains/elevators_mdp.rddl" );
		final File instance = new File( "final_comp_2014/rddl/elevators_inst_mdp__9.rddl" );
		final IpcElevatorsState s0 = IpcElevatorsDomains.parse( domain, instance );
		
		final RandomGenerator rng = new MersenneTwister( 44 );
		
		final IpcElevatorsFsssModel model = new IpcElevatorsFsssModel( rng, s0 );
		
		IpcElevatorsState s = model.initialState();
		for( int t = 0; t < s.params.T; ++t ) {
			System.out.println( s );
			final IpcElevatorsAction a = Fn.uniform_choice( rng, model.actions( s ) );
			System.out.println( a );
			s = model.sampleTransition( s, a );
		}
	}

}
