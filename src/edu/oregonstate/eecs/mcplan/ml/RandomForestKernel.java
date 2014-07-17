/**
 * 
 */
package edu.oregonstate.eecs.mcplan.ml;

import hr.irb.fastRandomForest.NakedFastRandomTree;

/**
 * @author jhostetler
 *
 */
public class RandomForestKernel implements KernelFunction<NakedFastRandomTree[]>
{
	@Override
	public double apply( final NakedFastRandomTree[] x, final NakedFastRandomTree[] y )
	{
		assert( x.length == y.length );
		int matches = 0;
		for( int i = 0; i < x.length; ++i ) {
			if( x[i] == y[i] ) {
				matches += 1;
			}
		}
		return matches / ((double) x.length);
	}
}
