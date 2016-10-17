/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.pats;

/**
 * @author jhostetler
 *
 */
public interface Classifier<T>
{
	public abstract int Nclasses();
	public abstract int classify( final T t );
}
