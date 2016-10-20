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
import java.util.Arrays;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public class ProjectionRepresenter<S> implements FactoredRepresenter<S, ProjectionRepresentation<S>>
{
	private final FactoredRepresenter<S, ? extends FactoredRepresentation<S>> base_repr;
	private final int[] indices;
	
	private final ArrayList<Attribute> attributes = new ArrayList<Attribute>();
	
	public ProjectionRepresenter(
		final FactoredRepresenter<S, ? extends FactoredRepresentation<S>> base_repr, final int[] indices )
	{
		this.base_repr = base_repr;
		this.indices = indices;
		
		Arrays.sort( indices );
		final ArrayList<Attribute> base_attributes = base_repr.attributes();
		for( final int i : indices ) {
			attributes.add( base_attributes.get( i ) );
		}
	}
	
	@Override
	public FactoredRepresenter<S, ProjectionRepresentation<S>> create()
	{
		return new ProjectionRepresenter<S>( base_repr.create(), indices );
	}

	@Override
	public ProjectionRepresentation<S> encode( final S s )
	{
		final double[] x = base_repr.encode( s ).phi();
		return new ProjectionRepresentation<S>( indices, x );
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes;
	}
}
