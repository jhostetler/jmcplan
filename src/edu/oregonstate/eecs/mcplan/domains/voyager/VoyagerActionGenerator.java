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
import java.util.List;
import java.util.ListIterator;

import edu.oregonstate.eecs.mcplan.ActionGenerator;

/**
 * @author jhostetler
 *
 */
public class VoyagerActionGenerator extends ActionGenerator<VoyagerState, VoyagerAction>
{
	private Player player_ = null;
	private List<VoyagerAction> actions_ = new ArrayList<VoyagerAction>();
	private ListIterator<VoyagerAction> itr_ = null;
	
	@Override
	public boolean hasNext()
	{
		return itr_.hasNext();
	}

	@Override
	public VoyagerAction next()
	{
		return itr_.next();
	}

	@Override
	public VoyagerActionGenerator create()
	{
		return new VoyagerActionGenerator();
	}
	
	private void addPlanetActions( final VoyagerState state, final Planet p )
	{
		for( int w = 0; w <= p.population( player_, Unit.Worker ); ++w ) {
			for( int s = 0; s <= p.population( player_, Unit.Soldier ); ++s ) {
				if( w + s > 0 ) {
					for( final Planet dest : state.planets ) {
						if( !dest.equals( p ) && state.adjacent( p, dest ) ) {
							actions_.add( new LaunchAction( player_, p, dest, new int[] { w, s } ) );
						}
					}
				}
			}
		}
		for( final Unit u : Unit.values() ) {
			if( p.nextProduced() != u ) {
				actions_.add( new SetProductionAction( p, u ) );
			}
		}
	}

	@Override
	public void setState( final VoyagerState s, final long t, final int[] turn )
	{
		assert( turn.length == 1 );
		player_ = Player.values()[turn[0]];
		actions_ = new ArrayList<VoyagerAction>();
		for( final Planet p : s.planets ) {
			if( p.owner() == player_ ) {
				addPlanetActions( s, p );
			}
		}
		actions_.add( new NothingAction() );
		itr_ = actions_.listIterator();
	}

	@Override
	public int size()
	{
		return actions_.size();
	}
}
