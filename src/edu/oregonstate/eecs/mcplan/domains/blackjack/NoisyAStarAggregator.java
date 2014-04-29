/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.Representer;

/**
 * @author jhostetler
 *
 */
public class NoisyAStarAggregator implements Representer<BlackjackState, AStarAbstraction>
{
	// Optimal strategy computed with value iteration
	private final String[][] hard_actions_;
	private final String[][] soft_actions_;
	
	private final String[][] clean_hard_;
	private final String[][] clean_soft_;
	
	private final RandomGenerator rng_;
	private final double p_;
	
	private final BlackjackParameters params_;
	
	public NoisyAStarAggregator( final RandomGenerator rng, final double p,
								 final String[][] hard_actions, final String[][] soft_actions,
								 final BlackjackParameters params )
	{
		rng_ = rng;
		p_ = p;
		
		params_ = params;
		
//		hard_actions_ = new String[][]
//		{	{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
//			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
//			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
//			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
//			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
//			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
//			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
//			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
//			{"H", "H", "S", "S", "S", "H", "H", "H", "H", "H"},
//			{"S", "S", "S", "S", "S", "H", "H", "H", "H", "H"},
//			{"S", "S", "S", "S", "S", "H", "H", "H", "H", "H"},
//			{"S", "S", "S", "S", "S", "H", "H", "H", "H", "H"},
//			{"S", "S", "S", "S", "S", "H", "H", "H", "H", "H"},
//			{"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},
//			{"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},
//			{"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},
//			{"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},
//			{"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"}	};
//
//		soft_actions_ = new String[][]
//		{	{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
//			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
//			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
//			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
//			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
//			{"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
//			{"S", "S", "S", "S", "S", "S", "S", "H", "H", "H"},
//			{"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},
//			{"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},
//			{"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"}	};
		
		hard_actions_ 	= new String[params_.hard_hand_count][params_.dealer_showing_count];
		clean_hard_ 	= new String[params_.hard_hand_count][params_.dealer_showing_count];
		soft_actions_ 	= new String[params_.soft_hand_count][params_.dealer_showing_count];
		clean_soft_ 	= new String[params_.soft_hand_count][params_.dealer_showing_count];
		
		for( int i = 0; i < params_.hard_hand_count; ++i ) {
			for( int j = 0; j < params_.dealer_showing_count; ++j ) {
				clean_hard_[i][j] = hard_actions[i][j];
			}
		}
		
		for( int i = 0; i < params_.soft_hand_count; ++i ) {
			for( int j = 0; j < params_.dealer_showing_count; ++j ) {
				clean_soft_[i][j] = soft_actions[i][j];
			}
		}
		
		makeNoisy();
	}
	
	private void makeNoisy()
	{
		final boolean[] flip = new boolean[params_.hard_hand_count * params_.dealer_showing_count
		                                   + params_.soft_hand_count * params_.dealer_showing_count];
		for( int i = 0; i < flip.length * p_; ++i ) {
			flip[i] = true;
		}
		final int[] perm = new RandomDataGenerator( rng_ ).nextPermutation( flip.length, flip.length );
		int c = 0;
		
		for( int i = 0; i < params_.hard_hand_count; ++i ) {
			for( int j = 0; j < params_.dealer_showing_count; ++j ) {
				if( flip[perm[c++]] ) {
					if( "S".equals( clean_hard_[i][j] ) ) {
						hard_actions_[i][j] = "H";
					}
					else {
						hard_actions_[i][j] = "S";
					}
				}
				else {
					hard_actions_[i][j] = clean_hard_[i][j];
				}
			}
		}
		
		for( int i = 0; i < params_.soft_hand_count; ++i ) {
			for( int j = 0; j < params_.dealer_showing_count; ++j ) {
				if( flip[perm[c++]] ) {
					if( "S".equals( clean_soft_[i][j] ) ) {
						soft_actions_[i][j] = "H";
					}
					else {
						soft_actions_[i][j] = "S";
					}
				}
				else {
					soft_actions_[i][j] = clean_soft_[i][j];
				}
			}
		}
		
		assert( c == flip.length );
	}
	
	private NoisyAStarAggregator( final NoisyAStarAggregator that )
	{
		params_ = that.params_;
		rng_ = that.rng_;
		p_ = that.p_;
		hard_actions_ 	= new String[params_.hard_hand_count][params_.dealer_showing_count];
		clean_hard_ 	= that.clean_hard_;
		soft_actions_ 	= new String[params_.soft_hand_count][params_.dealer_showing_count];
		clean_soft_ 	= that.clean_soft_;
		
		makeNoisy();
	}

	@Override
	public Representer<BlackjackState, AStarAbstraction> create()
	{
		return new NoisyAStarAggregator( this );
	}

	@Override
	public AStarAbstraction encode( final BlackjackState s )
	{
		final int[] pv = params_.handValue( s.hand( 0 ) );
		final int dv = s.dealerUpcard().BlackjackValue();
//		System.out.println( s.hand( 0 ).toString() );
//		System.out.println( "pv: " + pv[0] + ", dv: " + dv );
		final String as;
		if( !s.passed( 0 ) ) {
			if( pv[1] == 0 ) {
				as = hard_actions_[pv[0] - params_.hard_hand_min][dv - params_.dealer_showing_min];
			}
			else { // pv[1] > 0
				as = soft_actions_[pv[0] - params_.soft_hand_min][dv - params_.dealer_showing_min];
			}
		}
		else {
			as = "T";
		}
		
		if( "S".equals( as ) ) {
			return new AStarAbstraction( new PassAction( 0 ), dv, pv[0] );
		}
		else if( "H".equals( as ) ) {
			return new AStarAbstraction( new HitAction( 0 ), dv, pv[0] );
		}
		else {
			return new AStarAbstraction( null, dv, pv[0] );
		}
	}
	
	@Override
	public String toString()
	{
		return "chi-inf";
	}
}
