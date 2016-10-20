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

import gnu.trove.list.TIntList;

import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

/**
 * @author jhostetler
 *
 */
public class ShedZoneAction extends CosmicAction
{
	public final int zone;
	public final double amount;
	
	public ShedZoneAction( final int zone, final double amount )
	{
		this.zone = zone;
		this.amount = amount;
	}

	@Override
	public ShedZoneAction create()
	{
		return new ShedZoneAction( zone, amount );
	}

	@Override
	public MWNumericArray toMatlab( final CosmicParameters params, final double t )
	{
		final TIntList shunts = params.shuntsForZone( zone );
		final MWNumericArray a = MWNumericArray.newInstance(
			new int[] { shunts.size(), params.ev_cols }, MWClassID.DOUBLE, MWComplexity.REAL );
		for( int i = 0; i < shunts.size(); ++i ) {
			a.set( new int[] { i+1, params.ev_time }, t );
			a.set( new int[] { i+1, params.ev_type }, params.ev_shed_load );
			a.set( new int[] { i+1, params.ev_shunt_loc }, shunts.get( i ) );
			a.set( new int[] { i+1, params.ev_change_by }, CosmicParameters.ev_change_by_percent );
			a.set( new int[] { i+1, params.ev_quantity }, amount );
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
		return getClass().hashCode() + 3*(zone + 5*(Double.valueOf( amount ).hashCode()));
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof ShedZoneAction) ) {
			return false;
		}
		final ShedZoneAction that = (ShedZoneAction) obj;
		return zone == that.zone && amount == that.amount;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "ShedZone(" ).append( zone ).append( "; " ).append( amount ).append( ")" );
		return sb.toString();
	}
}
