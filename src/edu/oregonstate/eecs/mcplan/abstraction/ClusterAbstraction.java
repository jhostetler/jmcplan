/**
 * 
 */
package edu.oregonstate.eecs.mcplan.abstraction;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;

/**
 * @author jhostetler
 *
 */
public class ClusterAbstraction<T> extends FactoredRepresentation<T>
{
	private static final ArrayList<Attribute> attributes_ = new ArrayList<Attribute>();
	static {
		attributes_.add( new Attribute( "__cluster__" ) );
	}
	
	public static ArrayList<Attribute> attributes()
	{
		return attributes_;
	}
	
	// -----------------------------------------------------------------------
	
	public final int cluster_;
	
	public ClusterAbstraction( final int cluster )
	{
		cluster_ = cluster;
	}
	
	@Override
	public ClusterAbstraction<T> copy()
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

	@Override
	public double[] phi()
	{
		return new double[] { cluster_ };
	}
}
