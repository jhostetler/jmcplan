/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

/**
 * @author jhostetler
 *
 */
public class Transition<S, A>
{
	public final S s;
	public final A a;
	public final double r;
	public final S sprime;
	
	public Transition( final S s, final A a, final double r, final S sprime )
	{
		this.s = s;
		this.a = a;
		this.r = r;
		this.sprime = sprime;
	}
}