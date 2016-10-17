/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.tamarisk;

import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public abstract class TamariskAction
	extends UndoableAction<TamariskState> implements VirtualConstructor<TamariskAction>
{
	public abstract double cost();
	
	@Override
	public abstract boolean equals( final Object obj );
	@Override
	public abstract int hashCode();
	@Override
	public abstract String toString();
}
