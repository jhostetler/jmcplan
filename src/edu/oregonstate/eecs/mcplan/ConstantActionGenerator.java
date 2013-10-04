/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * An ActionGenerator that generates the same list of actions in every state.
 * 
 * @author jhostetler
 */
public class ConstantActionGenerator<S, A extends VirtualConstructor<A>>
	extends ActionGenerator<S, A>
{
	private final ArrayList<A> actions_;
	private Iterator<A> itr_;
	
	public ConstantActionGenerator( final ArrayList<A> actions )
	{
		actions_ = actions;
		itr_ = actions.iterator();
	}

	@Override
	public boolean hasNext()
	{
		return itr_.hasNext();
	}

	@Override
	public A next()
	{
		return itr_.next().create();
	}

	@Override
	public void setState( final S s, final long t, final int turn )
	{
		itr_ = actions_.iterator();
	}

	@Override
	public ConstantActionGenerator<S, A> create()
	{
		return new ConstantActionGenerator<S, A>( new ArrayList<A>( actions_ ) );
	}

	@Override
	public int size()
	{
		return actions_.size();
	}

}
