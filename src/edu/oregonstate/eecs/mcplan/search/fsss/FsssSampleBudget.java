/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * Sample budget.
 */
public class FsssSampleBudget<S extends State, A extends VirtualConstructor<A>> implements Budget
{
	private final FsssModel<S, A> model;
	private final int budget;
	
	private boolean exceeded = false;
	private int exceeded_at = 0;
	
	public FsssSampleBudget( final FsssModel<S, A> model, final int budget )
	{
		this.model = model;
		this.budget = budget;
	}
	
	@Override
	public boolean isExceeded()
	{
		if( exceeded ) {
			return true;
		}
		final int count = model.sampleCount();
		if( count >= budget ) {
			exceeded_at = count;
			exceeded = true;
		}
		return exceeded;
	}

	@Override
	public void reset()
	{
		model.resetSampleCount();
		exceeded = false;
		exceeded_at = 0;
	}
	
	@Override
	public String toString()
	{
		return "SampleBudget[" + budget + "]";
	}

	@Override
	public double actualDouble()
	{
		return exceeded_at;
	}
}
