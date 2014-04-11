/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

/**
 * @author jhostetler
 *
 */
public class BlackjackParameters
{
	public final int max_score = 32; //21; //32; //21; //32; //27; //32; // 42; // 35; // 27; // 23; // 27; // 21
	public final int busted_score = max_score + 1;
	public final int dealer_threshold = max_score - 5;
	
	public final int dealer_showing_count = 10;
	public final int dealer_showing_min = 2;
	
	public final int hard_hand_min = 4;
	public final int hard_hand_count = max_score - hard_hand_min + 1; // 18;
	
	public final int soft_hand_min = 12;
	public final int soft_hand_count = max_score - soft_hand_min + 1; // 10;
	
	public int[] handValue( final Iterable<Card> hand )
	{
		int total = 0;
		int high_aces = 0;
		for( final Card c : hand ) {
			total += c.BlackjackValue();
			if( c.rank == Rank.R_A ) {
				high_aces += 1;
			}
			while( total > max_score && high_aces > 0 ) {
				total -= 10;
				high_aces -= 1;
			}
		}
		return new int[] { total, high_aces };
	}
}
