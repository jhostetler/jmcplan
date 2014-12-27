/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cards;


/**
 * @author jhostetler
 *
 */
public interface Deck
{
	public abstract Card deal();
	public abstract void undeal( final Card c );
	
	public abstract Deck copy();
}
