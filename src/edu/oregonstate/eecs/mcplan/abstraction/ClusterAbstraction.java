/**
 * 
 */
package edu.oregonstate.eecs.mcplan.abstraction;

import edu.oregonstate.eecs.mcplan.Representation;

/**
 * @author jhostetler
 *
 */
public class ClusterAbstraction<T> extends Representation<T>
{
	private final int cluster_;
	
	public ClusterAbstraction( final int cluster )
	{
		cluster_ = cluster;
	}
	
	@Override
	public Representation<T> copy()
	{
		return new ClusterAbstraction<T>( cluster_ );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof ClusterAbstraction<?>) ) {
			return false;
		}
		final ClusterAbstraction<?> that = (ClusterAbstraction<?>) obj;
		return cluster_ == that.cluster_;
	}

	@Override
	public int hashCode()
	{
		return 7 * cluster_;
	}
	
	@Override
	public String toString()
	{
		return "ClusterAbstraction[" + cluster_ + "]";
	}
}
