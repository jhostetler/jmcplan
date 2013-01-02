/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.util.List;

/**
 * @author jhostetler
 *
 */
public interface ForwardGraphIterator<Node>
{
	public abstract List<Node> successors();
	public abstract boolean hasSuccessors();
}
