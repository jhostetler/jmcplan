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

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;

/**
 * @author jhostetler
 *
 */
public class ProjectionRepresentation<S> extends FactoredRepresentation<S>
{
	public final int[] indices;
	private final double[] proj;
	
	public ProjectionRepresentation( final int[] indices, final double[] phi )
	{
		this.indices = indices;
		proj = new double[indices.length];
		for( int i = 0; i < indices.length; ++i ) {
			proj[i] = phi[indices[i]];
		}
	}
	
	private ProjectionRepresentation( final double[] proj, final int[] indices )
	{
		this.proj = proj;
		this.indices = indices;
	}
	
	@Override
	public double[] phi()
	{
		return proj;
	}

	@Override
	public ProjectionRepresentation<S> copy()
	{
		return new ProjectionRepresentation<S>( proj, indices );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( this == obj ) {
			return true;
		}
		if( obj == null ) {
			return false;
		}
		if( getClass() != obj.getClass() ) {
			return false;
		}
		final ProjectionRepresentation<?> other = (ProjectionRepresentation<?>) obj;
		if( !Arrays.equals( indices, other.indices ) ) {
			throw new UnsupportedOperationException( "Comparing Projections onto different index sets" );
		}
		if( !Arrays.equals( proj, other.proj ) ) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode( indices );
		result = prime * result + Arrays.hashCode( proj );
		return result;
	}

}
