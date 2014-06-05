/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

/**
 * @author jhostetler
 *
 */
public enum Suit
{
	S_c,
	S_d,
	S_h,
	S_s,
	
	S_x;
	
	@Override
	public String toString()
	{
		switch( this ) {
			case S_c: return "c";
			case S_d: return "d";
			case S_h: return "h";
			case S_s: return "s";
			case S_x: default: return "x";
		}
	}
}
