/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * Generates KeepAction if rerolls left > 0, otherwise generates ScoreAction.
 */
public class YahtzeePhasedActionGenerator extends ActionGenerator<YahtzeeState, YahtzeeAction>
{
	private boolean rerolls = false;
	private Hand h = null;
	private Fn.MultisetPowerSetGenerator power_set = null;
	private int cat_idx = 0;
	private boolean[] filled = null;
	private int Nunfilled = 0;
	
	@Override
	public ActionGenerator<YahtzeeState, YahtzeeAction> create()
	{
		return new YahtzeePhasedActionGenerator();
	}

	@Override
	public void setState( final YahtzeeState s, final long t )
	{
		rerolls = s.rerolls > 0;
		h = s.hand();
		if( rerolls ) {
			power_set = new Fn.MultisetPowerSetGenerator( Arrays.copyOf( h.dice, h.dice.length ) );
		}
		cat_idx = 0;
		filled = Arrays.copyOf( s.filled, s.filled.length );
		Nunfilled = filled.length - Fn.sum( filled );
	}

	@Override
	public int size()
	{
		if( rerolls ) {
			return Fn.multisetPowerSetCardinality( h.dice );
		}
		else {
			return Nunfilled;
		}
	}

	@Override
	public boolean hasNext()
	{
		if( rerolls ) {
			return power_set.hasNext();
		}
		else {
			return (cat_idx < YahtzeeScores.values().length && Nunfilled > 0);
		}
	}

	@Override
	public YahtzeeAction next()
	{
		if( rerolls ) {
			return new KeepAction( power_set.next() );
		}
		else {
			while( cat_idx < YahtzeeScores.values().length ) {
				final YahtzeeScores category = YahtzeeScores.values()[cat_idx++];
				if( !filled[category.ordinal()] ) {
					filled[category.ordinal()] = true;
					Nunfilled -= 1;
					return new ScoreAction( category );
				}
			}
		}
		
		throw new IllegalStateException( "hasNext() was false" );
	}

}
