/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.pats;

/**
 * @author jhostetler
 *
 */
public interface BoundedValueModel<S, A> extends ValueModel<S, A>
{
	public abstract double U( final S s );
	public abstract double L( final S s );
	public abstract double U( final S s, final A a );
	public abstract double L( final S s, final A a );
}
