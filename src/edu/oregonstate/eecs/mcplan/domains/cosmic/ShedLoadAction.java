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
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

/**
 * 
 */
public class ShedLoadAction extends CosmicAction
{
	public final int[] shunts;
	public final double[] amounts;
	
	public ShedLoadAction( final int[] shunts, final double[] amounts )
	{
		assert( shunts.length == amounts.length );
		this.shunts = shunts;
		this.amounts = amounts;
	}
	
	@Override
	public CosmicAction create()
	{
		return new ShedLoadAction( shunts, amounts );
	}

	@Override
	public MWNumericArray toMatlab( final CosmicParameters params, final double t )
	{
		final MWNumericArray a = MWNumericArray.newInstance(
			new int[] { shunts.length, params.ev_cols }, MWClassID.DOUBLE, MWComplexity.REAL );
		for( int i = 0; i < shunts.length; ++i ) {
			a.set( new int[] { i+1, params.ev_time }, t );
			a.set( new int[] { i+1, params.ev_type }, params.ev_shed_load );
			a.set( new int[] { i+1, params.ev_shunt_loc }, shunts[i] );
			a.set( new int[] { i+1, params.ev_change_by }, CosmicParameters.ev_change_by_percent );
			a.set( new int[] { i+1, params.ev_quantity }, amounts[i] );
		}
//		System.out.println( "ShedLoadAction -> " + a );
		return a;
	}
	
	@Override
	public void applyNonCosmicChanges( final CosmicState sprime )
	{ }

	@Override
	public int hashCode()
	{
		return getClass().hashCode() + 3*(Arrays.hashCode( shunts ) + 5*(Arrays.hashCode( shunts )));
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof ShedLoadAction) ) {
			return false;
		}
		final ShedLoadAction that = (ShedLoadAction) obj;
		return Arrays.equals( shunts, that.shunts ) && Arrays.equals( amounts, that.amounts );
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "ShedLoad(" ).append( "[" ).append( StringUtils.join( shunts, ';' ) ).append( "]" )
		  .append( "; [" ).append( StringUtils.join( amounts, ';' ) ).append( "]" ).append( ")" );
		return sb.toString();
	}

}
