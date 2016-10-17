/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import org.apache.commons.math3.random.RandomGenerator;


/**
 * @author jhostetler
 *
 */
public class NothingAction extends BlackjackAction
{
	private boolean done_ = false;
	
	@Override
	public void undoAction( final BlackjackState s )
	{ done_ = false; }

	@Override
	public void doAction( final RandomGenerator rng, final BlackjackState s )
	{ done_ = true; }

	@Override
	public boolean isDone()
	{ return done_; }

	@Override
	public BlackjackAction create()
	{
		return new NothingAction();
	}

}
