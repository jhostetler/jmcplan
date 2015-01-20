/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;

/**
 * @author jhostetler
 *
 */
public interface RefineableRepresenter<T, U extends Representation<T>> extends Representer<T, U>
{
	public abstract U addTrainingSample( final T s, final FactoredRepresentation<T> x );
	public abstract void refine();
}
