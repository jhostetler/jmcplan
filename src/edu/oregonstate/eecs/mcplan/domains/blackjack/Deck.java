/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

/**
 * @author jhostetler
 *
 */
public interface Deck
{
	public abstract Card deal();
	public abstract void undeal( final Card c );
}
