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
