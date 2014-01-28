/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;


/**
 * @author jhostetler
 *
 */
public class Blackjack
{
	public static int[] handValue( final Iterable<Card> hand )
	{
		int total = 0;
		int high_aces = 0;
		for( final Card c : hand ) {
			total += c.BlackjackValue();
			if( c.rank == Rank.R_A ) {
				high_aces += 1;
			}
			while( total > 21 && high_aces > 0 ) {
				total -= 10;
				high_aces -= 1;
			}
		}
		return new int[] { total, high_aces };
	}
}
