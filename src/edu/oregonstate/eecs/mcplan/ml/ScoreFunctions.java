/**
 * 
 */
package edu.oregonstate.eecs.mcplan.ml;

/**
 * @author jhostetler
 *
 */
public class ScoreFunctions
{
	/**
	 * Aikake's information criterion.
	 * @param k Number of parameters
	 * @param log_likelihood
	 * @return
	 */
	public static double aic( final int k, final double log_likelihood )
	{
		return -2 * log_likelihood + 2 * k;
	}
	
	/**
	 * Aikake's information criterion with finite sample correction.
	 * @param n Number of samples
	 * @param k Number of parameters
	 * @param log_likelihood
	 * @return
	 */
	public static double aicc( final int n, final int k, final double log_likelihood )
	{
		assert( n > k + 1 );
		return aic( k, log_likelihood ) + (2 * k * (k + 1)) / (n - k - 1);
	}
	
	/**
	 * Bayesian information criterion.
	 * @param n Number of samples
	 * @param k Number of parameters
	 * @param log_likelihood
	 * @return
	 */
	public static double bic( final int n, final int k, final double log_likelihood )
	{
		return -2 * log_likelihood + k * Math.log( n );
	}
}
