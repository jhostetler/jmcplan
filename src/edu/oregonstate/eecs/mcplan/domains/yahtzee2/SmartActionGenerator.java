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
public class SmartActionGenerator extends ActionGenerator<YahtzeeState, JointAction<YahtzeeAction>>
{
	private YahtzeeState s_ = null;
	private long t_ = 0L;
	
	private final ArrayList<JointAction<YahtzeeAction>> actions_
		= new ArrayList<JointAction<YahtzeeAction>>();
	private Iterator<JointAction<YahtzeeAction>> itr_ = null;
	
	@Override
	public SmartActionGenerator create()
	{
		return new SmartActionGenerator();
	}

	@Override
	public void setState( final YahtzeeState s, final long t, final int[] turn )
	{
		s_ = s;
		t_ = t;
		
		actions_.clear();
		
		if( s.rerolls > 0 ) {
//			for( int i = 1; i <= Hand.Nfaces; ++i ) {
//				actions_.add( new JointAction<YahtzeeAction>( new KeepAllAction( i ) ) );
//			}
			
			actions_.add( new JointAction<YahtzeeAction>( new KeepMostAction() ) );
			actions_.add( new JointAction<YahtzeeAction>( new KeepStraightAction() ) );
			// Reroll all action
			actions_.add( new JointAction<YahtzeeAction>( new KeepAction( Fn.repeat( 0, Hand.Nfaces ) ) ) );
		}
		
		// Add ScoreAction for each open category
//		for( final YahtzeeScores category : YahtzeeScores.values() ) {
//			if( !s.filled[category.ordinal()] ) {
//				actions_.add( new JointAction<YahtzeeAction>( new ScoreAction( category ) ) );
//			}
//		}
		actions_.add( new JointAction<YahtzeeAction>( new ScoreHighestAction() ) );
		actions_.add( new JointAction<YahtzeeAction>( new ScoreMinMaxAction() ) );
		
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
