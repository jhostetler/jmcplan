/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import java.util.ArrayList;
import java.util.Iterator;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.JointAction;

/**
 * @author jhostetler
 *
 */
public class BlackjackActionGenerator extends ActionGenerator<BlackjackState, JointAction<BlackjackAction>>
{
	private final ArrayList<JointAction<BlackjackAction>> actions_
		= new ArrayList<JointAction<BlackjackAction>>();
	private Iterator<JointAction<BlackjackAction>> itr_ = null;
	
	private final int nagents_;
	
	public BlackjackActionGenerator( final int nagents )
	{
		nagents_ = nagents;
	}
	
	@Override
	public ActionGenerator<BlackjackState, JointAction<BlackjackAction>> create()
	{
		return new BlackjackActionGenerator( nagents_ );
	}

	@Override
	public void setState( final BlackjackState s, final long t, final int[] turn )
	{
		actions_.clear();
		JointAction.Builder<BlackjackAction> j = new JointAction.Builder<BlackjackAction>( nagents_ );
		for( final int p : turn ) {
			j.a( p, new HitAction( p ) );
		}
		actions_.add( j.finish() );
		j = new JointAction.Builder<BlackjackAction>( nagents_ );
		for( final int p : turn ) {
			j.a( p, new PassAction( p ) );
		}
		actions_.add( j.finish() );
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
	public JointAction<BlackjackAction> next()
	{
		return itr_.next();
	}

}
