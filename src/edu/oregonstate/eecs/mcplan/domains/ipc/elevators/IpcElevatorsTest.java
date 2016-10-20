/* LICENSE
Copyright (c) 2013-2016, Jesse Hostetler (jessehostetler@gmail.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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
