/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;


/**
 * @author jhostetler
 *
 */
public class NothingAction extends VoyagerAction
{
	private boolean done_ = false;
	
	/**
	 * 
	 */
	public NothingAction()
	{ }

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.agents.galcon.UndoableAction#undoAction(java.lang.Object)
	 */
	@Override
	public void undoAction( final VoyagerState s )
	{
		assert( done_ );
		done_ = false;
	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.agents.galcon.Action#doAction(java.lang.Object)
	 */
	@Override
	public void doAction( final VoyagerState s )
	{
		assert( !done_ );
		done_ = true;
	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.agents.galcon.Action#isDone()
	 */
	@Override
	public boolean isDone()
	{
		return done_;
	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.agents.galcon.Action#create()
	 */
	@Override
	public NothingAction create()
	{
		return new NothingAction();
	}
	
	@Override
	public String toString()
	{
		return "NothingAction";
	}
	
	@Override
	public int hashCode()
	{
		return 1;
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		return obj != null && obj instanceof NothingAction;
	}
}
