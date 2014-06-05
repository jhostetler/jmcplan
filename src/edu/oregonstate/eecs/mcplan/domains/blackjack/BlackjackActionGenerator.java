/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import java.util.ArrayList;
import java.util.Iterator;

import edu.oregonstate.eecs.mcplan.ActionGenerator;

/**
 * @author jhostetler
 *
 */
public class BlackjackActionGenerator extends ActionGenerator<BlackjackState, BlackjackAction>
{
	private final ArrayList<BlackjackAction> actions_
		= new ArrayList<BlackjackAction>();
	private Iterator<BlackjackAction> itr_ = null;
	
	@Override
	public ActionGenerator<BlackjackState, BlackjackAction> create()
	{
		return new BlackjackActionGenerator();
	}

	@Override
	public void setState( final BlackjackState s, final long t, final int[] turn )
	{
		actions_.clear();
		actions_.add( new HitAction( 0 ) );
		actions_.add( new PassAction( 0 ) );
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
	public BlackjackAction next()
	{
		return itr_.next();
	}

}
