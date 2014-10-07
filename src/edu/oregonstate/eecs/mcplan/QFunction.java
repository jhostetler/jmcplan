/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import gnu.trove.list.TDoubleList;

import java.util.ArrayList;

/**
 * @author jhostetler
 *
 */
public interface QFunction<S, A>
{
	public void calculate( final S s );
	
	public Pair<ArrayList<A>, TDoubleList> get();
}
