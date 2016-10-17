/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.planetwars;

import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public abstract class PwEvent extends UndoableAction<PwState> implements VirtualConstructor<PwEvent>
{
	@Override
	public abstract int hashCode();
	
	@Override
	public abstract boolean equals( final Object obj );
	
	@Override
	public abstract String toString();
}
