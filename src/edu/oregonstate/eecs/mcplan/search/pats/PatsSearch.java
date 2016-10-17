/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.pats;

/**
 * @author jhostetler
 *
 */
public class PatsSearch<S, A> implements Runnable
{
	private final AbstractionGraph<S, A> g;
	
	public PatsSearch( final AbstractionGraph<S, A> g )
	{
		this.g = g;
	}
	
	@Override
	public void run()
	{
		
	}
}
