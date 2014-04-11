/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

import java.util.ArrayList;
import java.util.Iterator;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class YahtzeeActionGenerator extends ActionGenerator<YahtzeeState, JointAction<YahtzeeAction>>
{
	private YahtzeeState s_ = null;
	private long t_ = 0L;
	
	private final ArrayList<JointAction<YahtzeeAction>> actions_
		= new ArrayList<JointAction<YahtzeeAction>>();
	private Iterator<JointAction<YahtzeeAction>> itr_ = null;
	
	@Override
	public YahtzeeActionGenerator create()
	{
		return new YahtzeeActionGenerator();
	}

	@Override
	public void setState( final YahtzeeState s, final long t, final int[] turn )
	{
		s_ = s;
		t_ = t;
		
		actions_.clear();
		
		// Convert hand to list of face *indices*
		final int[] faces = new int[Hand.Ndice];
		final Hand h = s.hand();
		int idx = 0;
		for( int i = 0; i < Hand.Nfaces; ++i ) {
			for( int j = 0; j < h.dice[i]; ++j ) {
				faces[idx++] = i;
			}
		}
		
		if( s.rerolls > 0 ) {
			// Add KeepAction for each subset of dice, *except* the entire set
			for( final int[] subset : Fn.in( new Fn.MultisetPowerSetGenerator( h.dice ) ) ) {
				actions_.add( new JointAction<YahtzeeAction>( new KeepAction( subset ) ) );
			}
			actions_.remove( actions_.size() - 1 ); // Drop the "entire set" subset
		}
		
		// Add ScoreAction for each open category
		for( final YahtzeeScores category : YahtzeeScores.values() ) {
			if( !s.filled[category.ordinal()] ) {
				actions_.add( new JointAction<YahtzeeAction>( new ScoreAction( category ) ) );
			}
		}
		
		itr_ = actions_.iterator();
	}

	@Override
	public int size()
	{
		return actions_.size();
	}

	@Override
	public boolean hasNext()
	{
		return itr_.hasNext();
	}

	@Override
	public JointAction<YahtzeeAction> next()
	{
		return itr_.next();
	}

}
