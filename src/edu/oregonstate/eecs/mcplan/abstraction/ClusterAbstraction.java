/**
 * 
 */
package edu.oregonstate.eecs.mcplan.abstraction;

import java.util.ArrayList;

import weka.core.Attribute;

import com.google.common.base.Strings;

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
	private final String hint_;
	
	public ClusterAbstraction( final int cluster )
	{
		this( cluster, null );
	}
	
	public ClusterAbstraction( final int cluster, final String hint )
	{
		cluster_ = cluster;
		hint_ = hint;
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
		final StringBuilder sb = new StringBuilder();
		sb.append( "ClusterAbstraction[" ).append( cluster_ ).append( "]" );
		if( !Strings.isNullOrEmpty( hint_ ) ) {
			sb.append( "[" ).append( hint_ ).append( "]" );
		}
		return sb.toString();
	}

	@Override
	public double[] phi()
	{
		return new double[] { cluster_ };
	}
}
