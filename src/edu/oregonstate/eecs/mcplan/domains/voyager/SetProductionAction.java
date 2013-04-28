/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jhostetler
 *
 */
public class SetProductionAction extends VoyagerEvent
{
	private static final Logger log = LoggerFactory.getLogger( SetProductionAction.class );
	
	private final Planet p_;
	private List<EntityType> old_production_ = null;
	private final List<EntityType> new_production_;
	private boolean done_ = false;
	private final String repr_;
	
	/**
	 * 
	 */
	public SetProductionAction( final Planet p, final List<EntityType> production )
	{
		p_ = p;
		new_production_ = production;
		repr_ = "SetProductionAction[p = " + p_ + ", production = " + production.toString() + "]";
	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.agents.galcon.UndoableAction#undoAction(java.lang.Object)
	 */
	@Override
	public void undoAction( final VoyagerState s )
	{
		log.debug( "undo {}", repr_ );
		assert( done_ );
		p_.setProductionSchedule( old_production_ );
		done_ = false;
	}

	/* (non-Javadoc)
	 * @see edu.oregonstate.eecs.mcplan.agents.galcon.Action#doAction(java.lang.Object)
	 */
	@Override
	public void doAction( final VoyagerState s )
	{
		log.debug( "do {}", repr_ );
		assert( !done_ );
		old_production_ = p_.productionSchedule();
		p_.setProductionSchedule( new_production_ );
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
	public SetProductionAction create()
	{
		return new SetProductionAction( p_, new_production_ );
	}
	
	@Override
	public String toString()
	{
		return repr_;
	}

}
