/**
 * 
 */
package edu.oregonstate.eecs.mcplan.abstraction;

import java.util.ArrayList;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;

/**
 * @author jhostetler
 *
 */
public class RandomClusterRepresenter<S extends State> implements Representer<S, ClusterAbstraction<S>>
{
	private final RandomGenerator rng_;
	private final int max_branching_;
	
	private final ArrayList<ClusterAbstraction<S>> clusters_ = new ArrayList<ClusterAbstraction<S>>();
	
	public RandomClusterRepresenter( final RandomGenerator rng, final int max_branching )
	{
		rng_ = rng;
		max_branching_ = max_branching;
	}
	
	@Override
	public Representer<S, ClusterAbstraction<S>> create()
	{
		return new RandomClusterRepresenter<S>( rng_, max_branching_ );
	}

	@Override
	public ClusterAbstraction<S> encode( final S s )
	{
		final int i = rng_.nextInt( max_branching_ );
		final ClusterAbstraction<S> c;
		if( i >= clusters_.size() ) {
			c = new ClusterAbstraction<S>( clusters_.size() );
		}
		else {
			c = clusters_.get( i );
		}
		return c;
	}
}
