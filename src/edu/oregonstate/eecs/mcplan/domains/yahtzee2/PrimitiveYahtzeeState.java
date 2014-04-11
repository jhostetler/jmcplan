/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;

/**
 * @author jhostetler
 *
 */
public class PrimitiveYahtzeeState extends FactoredRepresentation<YahtzeeState>
{
	private final double[] phi_;
	
	public PrimitiveYahtzeeState( final YahtzeeState s )
	{
		// Need: Hand, rerolls available, filled + score for each category,
		// Yahtzee & Upper bonuses
		final int Ncats = YahtzeeScores.values().length;
		phi_ = new double[Hand.Nfaces + 1 + (2 * Ncats) + 2];
		int idx = 0;
		final Hand h = s.hand();
		for( int i = 0; i < Hand.Nfaces; ++i ) {
			phi_[idx++] = h.dice[i];
		}
		phi_[idx++] = s.rerolls;
		for( final YahtzeeScores category : YahtzeeScores.values() ) {
			phi_[idx] = (s.filled[category.ordinal()] ? 1 : 0);
			phi_[idx + Ncats] = s.scores[category.ordinal()];
			++idx;
		}
		idx += Ncats;
		phi_[idx++] = s.yahtzee_bonus;
		phi_[idx++] = s.upper_bonus;
		assert( idx == phi_.length );
	}
	
	private PrimitiveYahtzeeState( final double[] phi )
	{
		phi_ = phi;
	}
	
	@Override
	public double[] phi()
	{
		return phi_;
	}

	@Override
	public PrimitiveYahtzeeState copy()
	{
		return new PrimitiveYahtzeeState( phi_ );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof PrimitiveYahtzeeState) ) {
			return false;
		}
		final PrimitiveYahtzeeState that = (PrimitiveYahtzeeState) obj;
		return Arrays.equals( phi_, that.phi_ );
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode( phi_ );
	}
}
