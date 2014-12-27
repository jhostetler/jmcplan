/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cards;

/**
 * @author jhostetler
 *
 */
public enum Rank
{
	R_X,
	
	R_A,
	R_2,
	R_3,
	R_4,
	R_5,
	R_6,
	R_7,
	R_8,
	R_9,
	R_T,
	R_J,
	R_Q,
	R_K;
	
	@Override
	public String toString()
	{
		switch( this ) {
			case R_A: return "A";
			case R_2: return "2";
			case R_3: return "3";
			case R_4: return "4";
			case R_5: return "5";
			case R_6: return "6";
			case R_7: return "7";
			case R_8: return "8";
			case R_9: return "9";
			case R_T: return "T";
			case R_J: return "J";
			case R_Q: return "Q";
			case R_K: return "K";
			case R_X: default: return "X";
		}
	}
}
