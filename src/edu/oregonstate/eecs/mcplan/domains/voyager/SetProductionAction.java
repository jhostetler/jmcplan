/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jhostetler
 *
 */
public class SetProductionAction extends VoyagerAction
{
	private static final Logger log = LoggerFactory.getLogger( SetProductionAction.class );
	
	private final Planet p_;
	private Unit old_production_ = null;
	private final Unit new_production_;
	private boolean done_ = false;
	private final String repr_;
	
	/**
	 * 
	 */
	public SetProductionAction( final Planet p, final Unit production )
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
		p_.setProduction( old_production_ );
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
		old_production_ = p_.nextProduced();
		p_.setProduction( new_production_ );
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
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder( 19, 37 )
			.append( p_ ).append( new_production_ ).toHashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof SetProductionAction) ) {
			return false;
		}
		final SetProductionAction that = (SetProductionAction) obj;
		return p_.equals( that.p_ ) && new_production_ == that.new_production_;
	}
}
