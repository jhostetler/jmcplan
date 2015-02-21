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
		final double[] phi = new double[attributes.size()];
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
