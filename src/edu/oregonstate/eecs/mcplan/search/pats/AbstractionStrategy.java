/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.pats;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author jhostetler
 *
 */
public class AbstractionStrategy<S, A>
{
	private final AbstractionGraph<S, A> g;
	
	private final RandomGenerator rng = new MersenneTwister( 42 );
	
	public AbstractionStrategy( final AbstractionGraph<S, A> g )
	{
		this.g = g;
	}
	
	public AbstractionGraph<S, A> getAbstractionGraph()
	{
		return g;
	}
	
	public void refine()
	{
		final int N = g.snodes.size() + g.anodes.size();
		final int i = rng.nextInt( N );
		if( i < g.snodes.size() ) {
			final AbstractionGraph.SNode<S, A> sn = g.snodes.get( i );
			sn.abstraction.refine();
		}
		else {
			final AbstractionGraph.ANode<S, A> an = g.anodes.get( i - g.snodes.size() );
			an.abstraction.refine();
		}
	}
}
