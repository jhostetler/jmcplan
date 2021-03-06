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

import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

/**
 * @author jhostetler
 *
 */
public final class IslandAction extends CosmicAction
{
	private final TIntSet zone = new TIntHashSet();
	private final TIntSet cutset = new TIntHashSet();
	
	public IslandAction( final int zone, final int[] cutset )
	{
		this.zone.add( zone );
		this.cutset.addAll( cutset );
	}
	
	public IslandAction( final TIntCollection zone, final List<int[]> cutset )
	{
		this.zone.addAll( zone );
		for( final int[] cut : cutset ) {
			this.cutset.addAll( cut );
		}
	}
	
	public IslandAction( final TIntCollection zone, final TIntSet cutset )
	{
		this.zone.addAll( zone );
		this.cutset.addAll( cutset );
	}
	
	@Override
	public CosmicAction create()
	{
		return new IslandAction( zone, cutset );
	}

	@Override
	public MWNumericArray toMatlab( final CosmicParameters params, final double t )
	{
		final MWNumericArray m = MWNumericArray.newInstance(
			new int[] { cutset.size(), params.ev_cols }, MWClassID.DOUBLE, MWComplexity.REAL );
		int row = 1;
		final TIntIterator itr = cutset.iterator();
		while( itr.hasNext() ) {
			final int branch_id = itr.next();
			m.set( new int[] { row, params.ev_time }, t );
			m.set( new int[] { row, params.ev_type }, params.ev_trip_branch );
			m.set( new int[] { row, params.ev_branch_loc }, branch_id );
			row += 1;
		}
//		System.out.println( toString() + " -> " + m );
		return m;
	}
	
	@Override
	public void applyNonCosmicChanges( final CosmicState sprime )
	{
		sprime.islands.addAll( zone );
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ cutset.hashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof IslandAction) ) {
			return false;
		}
		final IslandAction that = (IslandAction) obj;
		return zone.equals( that.zone ) && cutset.equals( that.cutset );
	}

	@Override
	public String toString()
	{
		return "Island({" + StringUtils.join( zone.toArray(), ';' ) + "})";
	}
}
