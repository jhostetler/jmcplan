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
package edu.oregonstate.eecs.mcplan.rddl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import rddl.RDDL.LCONST;
import rddl.RDDL.PVAR_NAME;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import gnu.trove.map.TObjectIntMap;

/**
 * @author jhostetler
 *
 */
public class RddlRepresentation extends FactoredRepresentation<RDDLState>
{
	private final float[] phi;
	
	public RddlRepresentation( final RddlSpec spec, final rddl.State s )
	{
//		System.out.println( "RddlRepresentation:" );
//		phi = Fn.copy( spec.state_defaults );
		phi = new float[spec.Nstate_vars];
		for( final Map.Entry<PVAR_NAME, HashMap<ArrayList<LCONST>, Object>> e : s._state.entrySet() ) {
			final PVAR_NAME name = e.getKey();
//			System.out.println( "\t" + name );
			final TObjectIntMap<ArrayList<LCONST>> assign_map = spec.state_var_indices.get( name );
//			for( final ArrayList<LCONST> k : assign_map.keySet() ) {
//				System.out.println( "\t\t" + k + " => " + assign_map.get( k ) );
//			}
			for( final Map.Entry<ArrayList<LCONST>, Object> b : e.getValue().entrySet() ) {
				final ArrayList<LCONST> a = b.getKey();
				final Object v = b.getValue();
				
				final int idx = assign_map.get( a );
				
				phi[idx] = (float) spec.valueToDouble( name, v );
			}
		}
		
//		System.out.println( Arrays.toString( phi ) );
	}
	
	private RddlRepresentation( final RddlRepresentation that )
	{
		this.phi = that.phi;
	}
	
	@Override
	public float[] phi()
	{
		return phi;
	}

	@Override
	public RddlRepresentation copy()
	{
		return new RddlRepresentation( this );
	}

	@Override
	public boolean equals( final Object obj )
	{
		final RddlRepresentation that = (RddlRepresentation) obj;
		return Arrays.equals( phi, that.phi );
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode( phi );
	}

}
