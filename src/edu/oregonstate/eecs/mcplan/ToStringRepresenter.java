/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * @author jhostetler
 *
 */
public class ToStringRepresenter<S> implements Representer<S, StringRepresentation<S>>
{

	@Override
	public Representer<S, StringRepresentation<S>> create()
	{
		return new ToStringRepresenter<>();
	}

	@Override
	public StringRepresentation<S> encode( final S s )
	{
		return new StringRepresentation<>( s.toString() );
	}

}
