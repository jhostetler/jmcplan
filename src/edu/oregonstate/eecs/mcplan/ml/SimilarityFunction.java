package edu.oregonstate.eecs.mcplan.ml;

/**
 * @author jhostetler
 *
 */
public interface SimilarityFunction
{
	public abstract double similarity( final double[] a, final double[] b );
}
