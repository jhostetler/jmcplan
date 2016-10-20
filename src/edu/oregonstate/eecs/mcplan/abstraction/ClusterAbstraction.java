/* LICENSE
Copyright (c) 2013-2016, Jesse Hostetler (jessehostetler@gmail.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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
	public float[] phi()
	{
		return new float[] { cluster_ };
	}
}
