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
package edu.oregonstate.eecs.mcplan.domains.planetwars;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.ArrayFactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public class PwGroundRepresenter implements FactoredRepresenter<PwState, FactoredRepresentation<PwState>>
{
	private final ArrayList<Attribute> attributes;
	
	public PwGroundRepresenter( final PwState s )
	{
		attributes = new ArrayList<Attribute>();
		
		attributes.add( new Attribute( "winner" ) );
		for( final PwPlayer y : PwPlayer.competitors ) {
			attributes.add( new Attribute( "y" + y.id + "_supply" ) );
		}
		
		for( final PwPlanet p : s.planets ) {
			final String pname = "p" + p.id;
			attributes.add( new Attribute( pname + "_owner" ) );
			attributes.add( new Attribute( pname + "_next" ) );
			attributes.add( new Attribute( pname + "_overflow" ) );
			attributes.add( new Attribute( pname + "_production" ) );
			
			for( final PwUnit u : s.game.units() ) {
				final String uname = pname + "u" + u.id;
				attributes.add( new Attribute( uname + "_stored") );
				attributes.add( new Attribute( uname + "_remaining") );
			}
			
			for( final PwPlayer y : PwPlayer.competitors ) {
				final String yname = pname + "y" + y.id;
				attributes.add( new Attribute( yname + "_carry") );
				
				for( final PwUnit u : s.game.units() ) {
					final String uname = yname + "u" + u.id;
					attributes.add( new Attribute( uname + "_pop") );
				}
			}
		}
		
		for( final PwRoute r : s.routes ) {
			final String rname_ab = "r" + r.a.id + "_" + r.b.id;
			for( int t = 0; t <= r.length; ++t ) {
				for( final PwPlayer y : PwPlayer.competitors ) {
					for( final PwUnit u : s.game.units() ) {
						final String uname = rname_ab + "_t" + t + "y" + y.id + "u" + u.id;
						attributes.add( new Attribute( uname + "_pop" ) );
					}
				}
			}
			
			final String rname_ba = "r" + r.b.id + "_" + r.a.id;
			for( int t = 0; t <= r.length; ++t ) {
				for( final PwPlayer y : PwPlayer.competitors ) {
					for( final PwUnit u : s.game.units() ) {
						final String uname = rname_ba + "_t" + t + "y" + y.id + "u" + u.id;
						attributes.add( new Attribute( uname + "_pop" ) );
					}
				}
			}
		}
	}
	
	private PwGroundRepresenter( final PwGroundRepresenter that )
	{
		this.attributes = that.attributes;
	}
	
	@Override
	public PwGroundRepresenter create()
	{
		return new PwGroundRepresenter( this );
	}

	@Override
	public FactoredRepresentation<PwState> encode( final PwState s )
	{
		final float[] phi = new float[attributes.size()];
		int idx = 0;
		
		phi[idx++] = s.winner() == null ? -1 : s.winner().id; //attributes.add( new Attribute( "winner" ) );
		for( final PwPlayer y : PwPlayer.competitors ) {
			phi[idx++] = s.supply( y ); //attributes.add( new Attribute( "y" + y.id + "_supply" ) );
		}
		
		for( final PwPlanet p : s.planets ) {
//			final String pname = "p" + p.id;
			phi[idx++] = p.owner().id; //attributes.add( new Attribute( pname + "_owner" ) );
			phi[idx++] = p.nextProduced().id; //attributes.add( new Attribute( pname + "_next" ) );
			phi[idx++] = p.overflowProduction(); //attributes.add( new Attribute( pname + "_overflow" ) );
			phi[idx++] = p.production(); //attributes.add( new Attribute( pname + "_production" ) );
			
			for( final PwUnit u : s.game.units() ) {
//				final String uname = pname + "u" + u.id;
				phi[idx++] = p.storedProduction( u ); //attributes.add( new Attribute( uname + "_stored") );
				phi[idx++] = p.remainingProduction( u ); //attributes.add( new Attribute( uname + "_remaining") );
			}
			
			for( final PwPlayer y : PwPlayer.competitors ) {
//				final String yname = pname + "y" + y.id;
				phi[idx++] = p.carryDamage( y ); //attributes.add( new Attribute( yname + "_carry") );
				
				for( final PwUnit u : s.game.units() ) {
//					final String uname = yname + "u" + u.id;
					phi[idx++] = p.population( y, u ); //attributes.add( new Attribute( uname + "_pop") );
				}
			}
		}
		
		for( final PwRoute r : s.routes ) {
//			final String rname_ab = "r" + r.a.id + "_" + r.b.id;
			for( int t = 0; t <= r.length; ++t ) {
				for( final PwPlayer y : PwPlayer.competitors ) {
					for( final PwUnit u : s.game.units() ) {
//						final String uname = rname_ab + "_t" + t + "y" + y.id + "u" + u.id;
						phi[idx++] = r.populationAB( t, y, u ); //attributes.add( new Attribute( uname + "_pop" ) );
					}
				}
			}
			
//			final String rname_ba = "r" + r.b.id + "_" + r.a.id;
			for( int t = 0; t <= r.length; ++t ) {
				for( final PwPlayer y : PwPlayer.competitors ) {
					for( final PwUnit u : s.game.units() ) {
//						final String uname = rname_ba + "_t" + t + "y" + y.id + "u" + u.id;
						phi[idx++] = r.populationBA( t, y, u ); //attributes.add( new Attribute( uname + "_pop" ) );
					}
				}
			}
		}
		
		return new ArrayFactoredRepresentation<PwState>( phi );
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes;
	}
}
