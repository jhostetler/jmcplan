/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;

/**
 * @author jhostetler
 *
 */
public class ReprWrapper<S> implements Representer<S, Representation<S>>
{
	private final Representer<S, ? extends Representation<S>> repr_;
	
	public ReprWrapper( final Representer<S, ? extends Representation<S>> repr )
	{
		repr_ = repr;
	}
	
	@Override
	public Representer<S, Representation<S>> create()
	{
		return new ReprWrapper<S>( repr_.create() );
	}

	@Override
	public Representation<S> encode( final S s )
	{
		return repr_.encode( s );
	}

}
