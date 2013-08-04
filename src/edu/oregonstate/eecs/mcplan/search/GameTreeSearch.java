/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;

/**
 * @author jhostetler
 *
 */
public interface GameTreeSearch<S, F extends Representer<S, F>, A> extends Runnable
{
	// TODO: Is there any reason to require this when you could just use pv.score?
	public abstract double score();
	
	public abstract PrincipalVariation<Representation<S, F>, A> principalVariation();
	
	public abstract boolean isComplete();
}
