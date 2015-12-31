/**
 * 
 */
package edu.oregonstate.eecs.mcplan;


/**
 * @author jhostetler
 *
 */
public abstract class ActionSpace<S, A> implements Representer<S, Representation<S>>
{
	public abstract ActionSet<S, A> getActionSet( final S s );
	
	/**
	 * FIXME: This is a temporary, intermediate step toward a larger interface
	 * change.
	 * @deprecated
	 * @see edu.oregonstate.eecs.mcplan.Representer#encode(java.lang.Object)
	 */
	@Deprecated
	@Override
	public final Representation<S> encode( final S s )
	{ return getActionSet( s ); }
	
	/**
	 * @deprecated
	 * @see edu.oregonstate.eecs.mcplan.ActionSpace#encode(Object)
	 * @see edu.oregonstate.eecs.mcplan.Representer#create()
	 */
	@Deprecated
	@Override
	public final ActionSpace<S, A> create()
	{ throw new UnsupportedOperationException(); }
	
	public abstract int cardinality();
	public abstract boolean isFinite();
	public abstract boolean isCountable();
	// FIXME: This could be more efficiently implemented as a field in the
	// action object, but then all actions would have to be created by an
	// ActionSpace.
	public abstract int index( final A a );
}
