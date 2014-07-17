/**
 * 
 */
package edu.oregonstate.eecs.mcplan.ml;

import org.apache.commons.math3.linear.RealVector;

/**
 * @author jhostetler
 *
 */
public class InnerProductKernel implements KernelFunction<RealVector>
{
	@Override
	public double apply( final RealVector x, final RealVector y )
	{
		return x.dotProduct( y );
	}
}
