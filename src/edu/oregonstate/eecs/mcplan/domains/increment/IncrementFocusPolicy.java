/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.increment;

import edu.oregonstate.eecs.mcplan.AnytimePolicy;

/**
 * This policy for the IncrementGame increments the specified counter in
 * every state.
 * 
 * @author jhostetler
 */
public class IncrementFocusPolicy extends AnytimePolicy<IncrementState, IncrementEvent>
{
	public final int player;
	public final int counter;
	
	public IncrementFocusPolicy( final int player, final int counter )
	{
		this.player = player;
		this.counter = counter;
	}
	
	@Override
	public void setState( final IncrementState s, final long t )
	{ }

	@Override
	public IncrementEvent getAction()
	{
		return new IncrementAction( player, counter );
	}

	@Override
	public void actionResult( final IncrementState sprime, final double[] r )
	{ }

	@Override
	public String getName()
	{
		return "IncrementFocusPolicy[" + this.player + ", " + this.counter + "]";
	}

	@Override
	public boolean improvePolicy()
	{
		return false;
	}

	@Override
	public int hashCode()
	{
		return getName().hashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		final IncrementFocusPolicy that = (IncrementFocusPolicy) obj;
		return player == that.player && counter == that.counter;
	}

}
