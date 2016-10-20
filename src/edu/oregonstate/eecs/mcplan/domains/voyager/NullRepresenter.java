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
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.Representation;

/**
 * Maps all states into the same representation.
 */
public class NullRepresenter implements FactoredRepresenter<VoyagerState, NullRepresenter>
{
	private static class NullRepresentation extends FactoredRepresentation<VoyagerState, NullRepresenter>
	{
		private static Object TheRepresentation = new Object();
		
		@Override
		public Representation<VoyagerState, NullRepresenter> copy()
		{
			return new NullRepresentation();
		}
		
		@Override
		public double[] phi()
		{
			return new double[0];
		}

		@Override
		public boolean equals( final Object obj )
		{
			return TheRepresentation == obj;
		}

		@Override
		public int hashCode()
		{
			return TheRepresentation.hashCode();
		}
	}
	
	@Override
	public FactoredRepresentation<VoyagerState, NullRepresenter> encode( final VoyagerState s )
	{
		return new NullRepresentation();
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return new ArrayList<Attribute>();
	}

}
