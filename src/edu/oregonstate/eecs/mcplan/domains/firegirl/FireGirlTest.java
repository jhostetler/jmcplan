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
package edu.oregonstate.eecs.mcplan.domains.firegirl;

import java.io.IOException;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import ch.qos.logback.classic.Level;
import edu.oregonstate.eecs.mcplan.LoggerManager;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * @author jhostetler
 *
 */
public class FireGirlTest
{

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main( final String[] args ) throws IOException
	{
		final int T = 200;
		final double discount = 0.9;
		final RandomGenerator episode_rng = new MersenneTwister( 42 );
		final RandomGenerator iid_rng = new MersenneTwister( 43 );
		final FireGirlParameters params = new FireGirlParameters( T, discount, new FireGirlLocalFeatureRepresenter() );
		
		double min_suppress_cost = 0;
		final MeanVarianceAccumulator mv_suppress_cost = new MeanVarianceAccumulator();
		
		final ch.qos.logback.classic.Logger Log = LoggerManager.getLogger( "log.domain" );
		Log.setLevel( Level.WARN );
		
		final int Ntrials = 1;
		for( int trial = 0; trial < Ntrials; ++trial ) {
			System.out.println( "trial: " + trial );
			final FireGirlState s = new FireGirlState( params );
			s.setRandomInitialState( episode_rng );
			for( int t = 0; t < T; ++t ) {
				Log.info( " === Year {} === ", t );
				final FireGirlAction a = (iid_rng.nextBoolean()
										 ? FireGirlAction.LetBurn : FireGirlAction.Suppress);
				final FireGirlState.YearResult result = s.doOneYear( episode_rng, a );
				if( a == FireGirlAction.Suppress ) {
					mv_suppress_cost.add( result.fire.sup_cost );
				}
				if( result.fire.sup_cost < min_suppress_cost ) {
					min_suppress_cost = result.fire.sup_cost;
				}
				
//				final File f = new File( "FireGirlTest_" + (t+1) + ".png" );
//				s.writePng( f );
			}
		}
		
		System.out.println( "min suppress cost: " + min_suppress_cost );
		System.out.println( "sup_cost mean: " + mv_suppress_cost.mean() );
		System.out.println( "sup_cost var: " + mv_suppress_cost.variance() );
	}

}
