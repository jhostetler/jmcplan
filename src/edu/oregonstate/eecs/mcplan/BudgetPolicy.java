/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import edu.oregonstate.eecs.mcplan.search.fsss.Budget;

/**
 * @author jhostetler
 *
 */
public class BudgetPolicy<S, A> extends Policy<S, A>
{
	private final AnytimePolicy<S, A> pi;
	private final Budget budget;
	
	public BudgetPolicy( final AnytimePolicy<S, A> pi, final Budget budget )
	{
		this.pi = pi;
		this.budget = budget;
	}

	@Override
	public void setState( final S s, final long t )
	{
		pi.setState( s, t );
		budget.reset();
	}

	@Override
	public A getAction()
	{
		while( !budget.isExceeded() ) {
			pi.improvePolicy();
		}
		return pi.getAction();
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		pi.actionResult( sprime, r );
	}

	@Override
	public String getName()
	{
		return "BudgetPolicy(" + pi + "; " + budget + ")";
	}

	@Override
	public int hashCode()
	{
		return 3 + 5 * (pi.hashCode() + 7 * (budget.hashCode()));
	}

	@Override
	public boolean equals( final Object obj )
	{
		final BudgetPolicy<?, ?> that = (BudgetPolicy<?, ?>) obj;
		return pi.equals( that.pi ) && budget.equals( that.budget );
	}
}
