/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.planetwars.a;

import edu.oregonstate.eecs.mcplan.domains.planetwars.PwUnit;

/**
 * @author jhostetler
 *
 */
public enum PwA_Units
{
	Unit( new PwUnit( 0, 1, 1, 10, 10 ) );
	
	public final PwUnit u;
	PwA_Units( final PwUnit u )
	{
		this.u = u;
	}
}
