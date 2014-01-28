/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

/**
 * @author jhostetler
 *
 */
public enum Card
{
	C_X( Rank.R_X, Suit.S_x ),
	C_Ac( Rank.R_A, Suit.S_c ), C_Ad( Rank.R_A, Suit.S_d ), C_Ah( Rank.R_A, Suit.S_h ), C_As( Rank.R_A, Suit.S_s ),
	C_2c( Rank.R_2, Suit.S_c ), C_2d( Rank.R_2, Suit.S_d ), C_2h( Rank.R_2, Suit.S_h ), C_2s( Rank.R_2, Suit.S_s ),
	C_3c( Rank.R_3, Suit.S_c ), C_3d( Rank.R_3, Suit.S_d ), C_3h( Rank.R_3, Suit.S_h ), C_3s( Rank.R_3, Suit.S_s ),
	C_4c( Rank.R_4, Suit.S_c ), C_4d( Rank.R_4, Suit.S_d ), C_4h( Rank.R_4, Suit.S_h ), C_4s( Rank.R_4, Suit.S_s ),
	C_5c( Rank.R_5, Suit.S_c ), C_5d( Rank.R_5, Suit.S_d ), C_5h( Rank.R_5, Suit.S_h ), C_5s( Rank.R_5, Suit.S_s ),
	C_6c( Rank.R_6, Suit.S_c ), C_6d( Rank.R_6, Suit.S_d ), C_6h( Rank.R_6, Suit.S_h ), C_6s( Rank.R_6, Suit.S_s ),
	C_7c( Rank.R_7, Suit.S_c ), C_7d( Rank.R_7, Suit.S_d ), C_7h( Rank.R_7, Suit.S_h ), C_7s( Rank.R_7, Suit.S_s ),
	C_8c( Rank.R_8, Suit.S_c ), C_8d( Rank.R_8, Suit.S_d ), C_8h( Rank.R_8, Suit.S_h ), C_8s( Rank.R_8, Suit.S_s ),
	C_9c( Rank.R_9, Suit.S_c ), C_9d( Rank.R_9, Suit.S_d ), C_9h( Rank.R_9, Suit.S_h ), C_9s( Rank.R_9, Suit.S_s ),
	C_Tc( Rank.R_T, Suit.S_c ), C_Td( Rank.R_T, Suit.S_d ), C_Th( Rank.R_T, Suit.S_h ), C_Ts( Rank.R_T, Suit.S_s ),
	C_Jc( Rank.R_J, Suit.S_c ), C_Jd( Rank.R_J, Suit.S_d ), C_Jh( Rank.R_J, Suit.S_h ), C_Js( Rank.R_J, Suit.S_s ),
	C_Qc( Rank.R_Q, Suit.S_c ), C_Qd( Rank.R_Q, Suit.S_d ), C_Qh( Rank.R_Q, Suit.S_h ), C_Qs( Rank.R_Q, Suit.S_s ),
	C_Kc( Rank.R_K, Suit.S_c ), C_Kd( Rank.R_K, Suit.S_d ), C_Kh( Rank.R_K, Suit.S_h ), C_Ks( Rank.R_K, Suit.S_s );
	
	public final Rank rank;
	public final Suit suit;
	
	private Card( final Rank r, final Suit s )
	{
		rank = r;
		suit = s;
	}
	
	@Override
	public String toString()
	{
		return rank.toString() + suit.toString();
	}
	
	public int AceHighRank()
	{
		switch( this ) {
			case C_2c: case  C_2d: case  C_2h: case  C_2s: return 2;
			case C_3c: case  C_3d: case  C_3h: case  C_3s: return 3;
			case C_4c: case  C_4d: case  C_4h: case  C_4s: return 4;
			case C_5c: case  C_5d: case  C_5h: case  C_5s: return 5;
			case C_6c: case  C_6d: case  C_6h: case  C_6s: return 6;
			case C_7c: case  C_7d: case  C_7h: case  C_7s: return 7;
			case C_8c: case  C_8d: case  C_8h: case  C_8s: return 8;
			case C_9c: case  C_9d: case  C_9h: case  C_9s: return 9;
			case C_Tc: case  C_Td: case  C_Th: case  C_Ts: return 10;
			case C_Jc: case  C_Jd: case  C_Jh: case  C_Js: return 11;
			case C_Qc: case  C_Qd: case  C_Qh: case  C_Qs: return 12;
			case C_Kc: case  C_Kd: case  C_Kh: case  C_Ks: return 13;
			case C_Ac: case  C_Ad: case  C_Ah: case  C_As: return 14;
			case C_X: default: return 0;
		}
	}
	
	public int AceLowRank()
	{
		switch( this ) {
			case C_Ac: case  C_Ad: case  C_Ah: case  C_As: return 1;
			case C_2c: case  C_2d: case  C_2h: case  C_2s: return 2;
			case C_3c: case  C_3d: case  C_3h: case  C_3s: return 3;
			case C_4c: case  C_4d: case  C_4h: case  C_4s: return 4;
			case C_5c: case  C_5d: case  C_5h: case  C_5s: return 5;
			case C_6c: case  C_6d: case  C_6h: case  C_6s: return 6;
			case C_7c: case  C_7d: case  C_7h: case  C_7s: return 7;
			case C_8c: case  C_8d: case  C_8h: case  C_8s: return 8;
			case C_9c: case  C_9d: case  C_9h: case  C_9s: return 9;
			case C_Tc: case  C_Td: case  C_Th: case  C_Ts: return 10;
			case C_Jc: case  C_Jd: case  C_Jh: case  C_Js: return 11;
			case C_Qc: case  C_Qd: case  C_Qh: case  C_Qs: return 12;
			case C_Kc: case  C_Kd: case  C_Kh: case  C_Ks: return 13;
			case C_X: default: return 0;
		}
	}
	
	public int BlackjackValue()
	{
		switch( this ) {
			case C_2c: case  C_2d: case  C_2h: case  C_2s: return 2;
			case C_3c: case  C_3d: case  C_3h: case  C_3s: return 3;
			case C_4c: case  C_4d: case  C_4h: case  C_4s: return 4;
			case C_5c: case  C_5d: case  C_5h: case  C_5s: return 5;
			case C_6c: case  C_6d: case  C_6h: case  C_6s: return 6;
			case C_7c: case  C_7d: case  C_7h: case  C_7s: return 7;
			case C_8c: case  C_8d: case  C_8h: case  C_8s: return 8;
			case C_9c: case  C_9d: case  C_9h: case  C_9s: return 9;
			case C_Tc: case  C_Td: case  C_Th: case  C_Ts: return 10;
			case C_Jc: case  C_Jd: case  C_Jh: case  C_Js: return 10;
			case C_Qc: case  C_Qd: case  C_Qh: case  C_Qs: return 10;
			case C_Kc: case  C_Kd: case  C_Kh: case  C_Ks: return 10;
			case C_Ac: case  C_Ad: case  C_Ah: case  C_As: return 11;
			case C_X: default: return 0;
		}
	}
}
