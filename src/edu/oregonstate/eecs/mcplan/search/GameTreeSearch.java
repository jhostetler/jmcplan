/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

/**
 * @author jhostetler
 *
 */
public interface GameTreeSearch<S, A> extends Runnable
{
	// TODO: Is there any reason to require this when you could just use pv.score?
	public abstract double score();
	
	public abstract PrincipalVariation<S, A> principalVariation();
	
	public abstract boolean isComplete();
}
