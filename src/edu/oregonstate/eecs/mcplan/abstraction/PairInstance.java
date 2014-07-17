/**
 * 
 */
package edu.oregonstate.eecs.mcplan.abstraction;

/**
 * @author jhostetler
 *
 */
public class PairInstance
{
	public final double[] a;
	public final double[] b;
	public final double label;
	
	public PairInstance( final double[] a, final double[] b, final int label )
	{
		this.a = a; this.b = b; this.label = label;
	}
}
