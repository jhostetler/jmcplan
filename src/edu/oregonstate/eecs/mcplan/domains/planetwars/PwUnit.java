/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.planetwars;

/**
 * @author jhostetler
 *
 */
public class PwUnit
{
	public final int id;
	public final int cost;
	public final int supply;
	public final int attack;
	public final int hp;
	
	public PwUnit( final int id, final int cost, final int supply, final int attack, final int hp )
	{
		this.id = id;
		this.cost = cost;
		this.supply = supply;
		this.attack = attack;
		this.hp = hp;
	}
}
