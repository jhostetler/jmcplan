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
package edu.oregonstate.eecs.mcplan.domains.cosmic.policy;

import edu.oregonstate.eecs.mcplan.domains.cosmic.Bus;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicState;
import edu.oregonstate.eecs.mcplan.domains.cosmic.Shunt;
import gnu.trove.iterator.TIntIterator;

/**
 * @author jhostetler
 *
 */
public final class SelectNearestShunt
{
	public static Shunt forBus( final CosmicState s, final Bus b )
	{
		final CosmicState.DistanceBusPair[] db = s.nearestBusesByRowElectricalDistance( s.params, b.id() );
//		System.out.println( "Sorted buses:\n" + Arrays.toString( db ) );
		for( int i = 0; i < db.length; ++i ) {
			final CosmicState.DistanceBusPair p = db[i];
//			if( p.bus == bi ) {
//				continue;
//			}
			final TIntIterator sh_itr = s.params.shuntsForBus( p.bus );
			while( sh_itr.hasNext() ) {
				final int sh_id = sh_itr.next();
				final Shunt sh = s.shunt( sh_id );
//				System.out.println( "\t" + p.bus + " -> " + sh_id + ": " + sh );
				if( sh.factor() > 0 ) {
					return sh;
				}
			}
		}
		
		return null;
	}

	@Override
	public String toString()
	{
		return "Nearest";
	}

}
