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
package edu.oregonstate.eecs.mcplan.domains.ipc.tamarisk;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssModel;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public final class IpcTamariskFsssModel extends FsssModel<IpcTamariskState, IpcTamariskAction>
{
	private final RandomGenerator rng;
	private final IpcTamariskParameters params;
	private final IpcTamariskState s0;
	
	private final FactoredRepresenter<IpcTamariskState, ? extends FactoredRepresentation<IpcTamariskState>> base_repr;
	private final IpcTamariskActionSetRepresenter action_repr = new IpcTamariskActionSetRepresenter();
	
	private int sample_count = 0;
	
	private final double min_state_reward;
	private final double min_action_reward;
	
	public IpcTamariskFsssModel( final RandomGenerator rng, final IpcTamariskParameters params, final IpcTamariskState s0,
		final FactoredRepresenter<IpcTamariskState, ? extends FactoredRepresentation<IpcTamariskState>> base_repr )
	{
		this.rng = rng;
		this.params = params;
		this.s0 = s0;
		this.base_repr = base_repr;
		
		min_state_reward = params.Nreaches*(params.cost_per_invaded_reach + params.cells_per_reach*params.cost_per_tree);
		min_action_reward = params.restoration_cost + params.cells_per_reach*params.restoration_cost_for_empty_slot;
	}
	
	@Override
	public FsssModel<IpcTamariskState, IpcTamariskAction> create( final RandomGenerator rng )
	{
		return new IpcTamariskFsssModel( rng, params, s0, base_repr );
	}

	@Override
	public double Vmin( final IpcTamariskState s )
	{
		return reward( s ) + (params.T - s.t)*(min_action_reward + min_state_reward);
	}

	@Override
	public double Vmax( final IpcTamariskState s )
	{
		return 0;
	}

	@Override
	public double Vmin( final IpcTamariskState s, final IpcTamariskAction a )
	{
		return reward( s, a ) + (params.T - s.t)*min_state_reward;
	}

	@Override
	public double Vmax( final IpcTamariskState s, final IpcTamariskAction a )
	{
		return 0;
	}

	@Override
	public double discount()
	{
		return 1.0;
	}

	@Override
	public double heuristic( final IpcTamariskState s )
	{
		return 0;
	}

	@Override
	public RandomGenerator rng()
	{
		return rng;
	}

	@Override
	public FactoredRepresenter<IpcTamariskState, ? extends FactoredRepresentation<IpcTamariskState>> base_repr()
	{
		return base_repr;
	}

	@Override
	public Representer<IpcTamariskState, ? extends Representation<IpcTamariskState>> action_repr()
	{
		return action_repr;
	}

	@Override
	public IpcTamariskState initialState()
	{
		return new IpcTamariskState( s0 );
	}

	@Override
	public Iterable<IpcTamariskAction> actions( final IpcTamariskState s )
	{
		return Fn.in( new IpcTamariskActionGenerator( params ) );
	}

	@Override
	public IpcTamariskState sampleTransition( final IpcTamariskState s,
			final IpcTamariskAction a )
	{
		sample_count += 1;
		return s.step( rng, a );
	}

	@Override
	public double reward( final IpcTamariskState s )
	{
		double cost = 0;
		for( final byte[] r : s.reaches ) {
			boolean invaded = false;
			for( final byte plant : r ) {
				if( plant == 0 ) {
					cost += params.cost_per_empty_slot;
				}
				else if( (plant & IpcTamariskState.Tamarisk) != 0 ) {
					cost += params.cost_per_tree;
					invaded = true;
				}
			}
			if( invaded ) {
				cost += params.cost_per_invaded_reach;
			}
		}
		return cost;
	}

	@Override
	public double reward( final IpcTamariskState s, final IpcTamariskAction a )
	{
		if( a.type == IpcTamariskActionSet.Eradicate ) {
			return params.eradication_cost;
		}
		else if( a.type == IpcTamariskActionSet.Restore ) {
			double cost = params.restoration_cost;
			for( final byte plant : s.reaches[a.reach] ) {
				if( plant == 0 ) {
					cost += params.restoration_cost_for_empty_slot;
				}
			}
			return cost;
		}
		else {
			return 0;
		}
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
