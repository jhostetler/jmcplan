/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.tamarisk;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author jhostetler
 *
 */
public class NothingAction extends TamariskAction
{
	private boolean done_ = false;
	
	@Override
	public double cost()
	{
		return 0.0;
	}
	
	@Override
	public void undoAction( final TamariskState s )
	{
		assert( done_ );
		done_ = false;
	}

	@Override
	public void doAction( final RandomGenerator rng, final TamariskState s )
	{
		assert( !done_ );
		done_ = true;
	}

	@Override
	public boolean isDone()
	{
		return done_;
	}

	@Override
	public boolean equals( final Object obj )
	{
		return obj != null && obj instanceof NothingAction;
	}

	@Override
	public int hashCode()
	{
		return 13;
	}

	@Override
	public String toString()
	{
		return "NothingAction";
	}

	@Override
	public TamariskAction create()
	{
		return new NothingAction();
	}

}
