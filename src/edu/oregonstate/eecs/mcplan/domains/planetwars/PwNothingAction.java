/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.planetwars;


/**
 * @author jhostetler
 *
 */
public class PwNothingAction extends PwEvent
{
	private boolean done = false;
	
	@Override
	public void doAction( final PwState s )
	{
		done = true;
	}

	@Override
	public boolean isDone()
	{
		return done;
	}

	@Override
	public void undoAction( final PwState s )
	{
		done = false;
	}

	@Override
	public PwNothingAction create()
	{
		return new PwNothingAction();
	}

	@Override
	public int hashCode()
	{
		return PwNothingAction.class.hashCode();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		return obj instanceof PwNothingAction;
	}
	
	@Override
	public String toString()
	{
		return "PwNothingAction";
	}
}
