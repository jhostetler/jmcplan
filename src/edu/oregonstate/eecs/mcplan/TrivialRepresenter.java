/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * @author jhostetler
 *
 */
public class TrivialRepresenter<S> implements Representer<S, TrivialRepresentation<S>>
{
	@Override
	public Representer<S, TrivialRepresentation<S>> create()
	{
		return new TrivialRepresenter<S>();
	}

	@Override
	public TrivialRepresentation<S> encode( final S s )
	{
		return new TrivialRepresentation<S>();
	}
}
