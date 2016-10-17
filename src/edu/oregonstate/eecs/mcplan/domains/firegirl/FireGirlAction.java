/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.firegirl;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.Action;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public enum FireGirlAction implements Action<FireGirlState>, VirtualConstructor<FireGirlAction>
{
	LetBurn( false ),
	Suppress( true );
	
	public final boolean suppress;
	
	private FireGirlAction( final boolean suppress )
	{
		this.suppress = suppress;
	}
	
	@Override
	public FireGirlAction create()
	{
		return this;
	}

	@Override
	public void doAction( final RandomGenerator rng, final FireGirlState s )
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	
}
