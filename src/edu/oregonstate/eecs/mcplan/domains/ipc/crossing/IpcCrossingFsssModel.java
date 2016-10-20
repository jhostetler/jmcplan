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
package edu.oregonstate.eecs.mcplan.domains.ipc.crossing;

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
public class IpcCrossingFsssModel extends FsssModel<IpcCrossingState, IpcCrossingAction>
{
	private final RandomGenerator rng;
	private final IpcCrossingState s0;
	private final IpcCrossingParameters params;
	
	private final IpcCrossingGroundRepresenter base_repr;
	private static final IpcCrossingActionSetRepresenter action_repr = new IpcCrossingActionSetRepresenter();
	
	private int sample_count = 0;
	
	public IpcCrossingFsssModel( final RandomGenerator rng, final IpcCrossingState s0 )
	{
		this.rng = rng;
		this.s0 = s0;
		this.params = s0.params;
		base_repr = new IpcCrossingGroundRepresenter( s0.params );
	}
	
	@Override
	public FsssModel<IpcCrossingState, IpcCrossingAction> create( final RandomGenerator rng )
	{
		return new IpcCrossingFsssModel( rng, s0 );
	}

	@Override
	public double Vmin( final IpcCrossingState s )
	{
		return reward( s ) + -(params.T - s.t);
	}

	@Override
	public double Vmax( final IpcCrossingState s )
	{
		return 0;
	}

	@Override
	public double Vmin( final IpcCrossingState s, final IpcCrossingAction a )
	{
		return -(params.T - s.t);
	}

	@Override
	public double Vmax( final IpcCrossingState s, final IpcCrossingAction a )
	{
		return 0;
	}

	@Override
	public double discount()
	{
		return 1.0;
	}

	@Override
	public double heuristic( final IpcCrossingState s )
	{
		return 0;
	}

	@Override
	public RandomGenerator rng()
	{
		return rng;
	}

	@Override
	public FactoredRepresenter<IpcCrossingState, ? extends FactoredRepresentation<IpcCrossingState>> base_repr()
	{
		return base_repr;
	}

	@Override
	public Representer<IpcCrossingState, ? extends Representation<IpcCrossingState>> action_repr()
	{
		return action_repr;
	}

	@Override
	public IpcCrossingState initialState()
	{
		return new IpcCrossingState( s0 );
	}

	@Override
	public Iterable<IpcCrossingAction> actions( final IpcCrossingState s )
	{
		return Fn.in( new IpcCrossingActionGenerator() );
	}

	@Override
	public IpcCrossingState sampleTransition( final IpcCrossingState s, final IpcCrossingAction a )
	{
		sample_count += 1;
		return s.step( rng, a );
	}

	@Override
	public double reward( final IpcCrossingState s )
	{
		return (s.goal ? 0 : -1);
	}

	@Override
	public double reward( final IpcCrossingState s, final IpcCrossingAction a )
	{
		return 0;
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
