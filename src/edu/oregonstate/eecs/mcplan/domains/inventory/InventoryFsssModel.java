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
package edu.oregonstate.eecs.mcplan.domains.inventory;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.TrivialRepresenter;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssModel;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class InventoryFsssModel extends FsssModel<InventoryState, InventoryAction>
{
	private final RandomGenerator rng;
	public final InventoryProblem problem;
	
	private final double Vmin;
	private final double Vmax;
	
	private final InventoryNullRepresenter base_repr;
	
	private int sample_count = 0;
	
	public InventoryFsssModel( final RandomGenerator rng, final InventoryProblem problem )
	{
		this.rng = rng;
		this.problem = problem;
		base_repr = new InventoryNullRepresenter( problem );
		
		final double gamma = discount();
		assert( gamma < 1.0 );
		
		// +1 for per-order cost
		Vmin = (1.0 / (1.0 - gamma)) * -(problem.Nproducts*problem.max_inventory*problem.warehouse_cost + 1);
		double r = 0;
		for( int i = 0; i < problem.Nproducts; ++i ) {
			r += problem.price[i] * problem.max_demand;
		}
		Vmax = (1.0 / (1.0 - gamma)) * r;
	}
	
	@Override
	public FsssModel<InventoryState, InventoryAction> create(
			final RandomGenerator rng )
	{
		return new InventoryFsssModel( rng, problem );
	}

	@Override
	public double Vmin( final InventoryState s, final InventoryAction a )
	{
		return Vmin;
	}

	@Override
	public double Vmax( final InventoryState s, final InventoryAction a )
	{
		return Vmax;
	}
	
	@Override
	public double Vmin( final InventoryState s )
	{
		return Vmin;
	}

	@Override
	public double Vmax( final InventoryState s )
	{
		return Vmax;
	}
	
	@Override
	public double heuristic( final InventoryState s )
	{
		return 0;
	}

	@Override
	public double discount()
	{
		return 0.9;
	}
	
	@Override
	public RandomGenerator rng()
	{
		return rng;
	}

	/**
	 * We use a random initial demand for our experiments.
	 * @see edu.oregonstate.eecs.mcplan.search.fsss.FsssModel#initialState()
	 */
	@Override
	public InventoryState initialState()
	{
		final InventoryState s = new InventoryState( rng, problem );
		for( int i = 0; i < s.problem.Nproducts; ++i ) {
			s.demand[i] = rng.nextInt( s.problem.max_demand + 1 );
		}
		return s;
	}

	@Override
	public Iterable<InventoryAction> actions( final InventoryState s )
	{
		final InventoryActionGenerator actions = new InventoryActionGenerator();
		actions.setState( s, 0L );
		return Fn.takeAll( actions );
	}

	@Override
	public InventoryState sampleTransition( final InventoryState s, final InventoryAction a )
	{
		sample_count += 1;
		
		final InventoryState sprime = s.copy();
		a.create().doAction( sprime );
		InventorySimulator.applyDynamics( sprime );
		
		return sprime;
	}
	
	@Override
	public double reward( final InventoryState s )
	{
		return s.r;
	}

	@Override
	public double reward( final InventoryState s, final InventoryAction a )
	{
		return a.reward();
	}

	@Override
	public FactoredRepresenter<InventoryState, ? extends FactoredRepresentation<InventoryState>> base_repr()
	{
		return base_repr;
	}

	@Override
	public Representer<InventoryState, ? extends Representation<InventoryState>> action_repr()
	{
		return new TrivialRepresenter<InventoryState>();
	}

	@Override
	public int sampleCount()
	{
		return sample_count;
	}
	
	@Override
	public void resetSampleCount()
	{
		sample_count = 0;
	}
}
