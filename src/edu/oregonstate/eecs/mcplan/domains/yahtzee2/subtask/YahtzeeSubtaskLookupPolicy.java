/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2.subtask;

import java.util.Map;

import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.Hand;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.YahtzeeAction;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.YahtzeeState;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * Adapts a map of YahtzeeDiceState => YahtzeeAction to be a Policy over
 * YahtzeeStates.
 * 
 * This class has *reference semantics*.
 */
public class YahtzeeSubtaskLookupPolicy extends Policy<YahtzeeState, YahtzeeAction>
{
	private final Map<YahtzeeDiceState, YahtzeeAction> actions;
	private YahtzeeDiceState s = null;
	
	public YahtzeeSubtaskLookupPolicy( final Map<YahtzeeDiceState, YahtzeeAction> actions )
	{
		this.actions = actions;
	}
	
	@Override
	public void setState( final YahtzeeState s, final long t )
	{
		assert( s.rerolls > 0 );
		this.s = new YahtzeeDiceState( new Hand( Fn.copy( s.hand().dice ) ), s.rerolls );
	}

	@Override
	public YahtzeeAction getAction()
	{
		return actions.get( s );
	}

	@Override
	public void actionResult( final YahtzeeState sprime, final double[] r )
	{ }

	@Override
	public String getName()
	{
		return "YahtzeeSubtaskLookupPolicy@" + Integer.toHexString( hashCode() );
	}

	@Override
	public int hashCode()
	{
		return System.identityHashCode( this );
	}

	@Override
	public boolean equals( final Object obj )
	{
		return this == obj;
	}
}
