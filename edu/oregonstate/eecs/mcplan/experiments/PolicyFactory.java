/**
 * 
 */
package edu.oregonstate.eecs.mcplan.experiments;

import edu.oregonstate.eecs.mcplan.AnytimePolicy;

/**
 * @author jhostetler
 *
 */
public interface PolicyFactory<S, A, P, I>
{
	public abstract AnytimePolicy<S, A> create( final Environment env, final P params, final I instance );
}
