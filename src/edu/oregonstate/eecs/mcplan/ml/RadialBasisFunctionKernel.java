/**
 * 
 */
package edu.oregonstate.eecs.mcplan.ml;

import org.apache.commons.math3.linear.RealVector;

/**
 * @author jhostetler
 *
 */
public class RadialBasisFunctionKernel implements KernelFunction<RealVector>
{
	public final double sigma;
	private final double gamma_;
	
	public RadialBasisFunctionKernel( final double sigma )
	{
		this.sigma = sigma;
		gamma_ = -1.0 / 2*sigma*sigma;
	}
	
	@Override
	public double apply( final RealVector x, final RealVector y )
	{
		final RealVector diff = x.subtract( y );
		final double sq_norm2 = diff.dotProduct( diff );
		return Math.exp( gamma_ * sq_norm2 );
	}
}
