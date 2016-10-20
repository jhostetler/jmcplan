/* LICENSE
Copyright (c) 2013-2016, Jesse Hostetler (jessehostetler@gmail.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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
		while( !budget.isExceeded() && pi.improvePolicy() );
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
