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
import edu.oregonstate.eecs.mcplan.ArrayFactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public class GridRepresenter<S> implements FactoredRepresenter<S, FactoredRepresentation<S>>
{
	private final FactoredRepresenter<S, ? extends FactoredRepresentation<S>> repr_;
	
	public final double[] grid;
	
	public GridRepresenter( final FactoredRepresenter<S, ? extends FactoredRepresentation<S>> repr,
							final double[] grid )
	{
		repr_ = repr;
		this.grid = grid;
	}
	
	@Override
	public FactoredRepresenter<S, FactoredRepresentation<S>> create()
	{
		return new GridRepresenter<S>( repr_.create(), grid );
	}

	@Override
	public FactoredRepresentation<S> encode( final S s )
	{
		final FactoredRepresentation<S> x = repr_.encode( s );
		final double[] xphi = x.phi();
		final double[] phi = new double[xphi.length];
		assert( grid.length == phi.length );
		for( int i = 0; i < phi.length; ++i ) {
			phi[i] = Math.floor( xphi[i] / grid[i] );
		}
		return new ArrayFactoredRepresentation<S>( phi );
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return repr_.attributes();
	}

}
