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
package edu.oregonstate.eecs.mcplan.domains.racegrid;

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
public class RacegridFsssModel extends FsssModel<RacegridState, RacegridAction>
{
	private final RandomGenerator rng;
	private final RacegridState ex;
	private final double slip;
	private final ShortestPathHeuristic h;
	
	private final TerminatingRacegridRepresenter base_repr = new TerminatingRacegridRepresenter();
	private final RacegridActionSetRepresenter action_repr = new RacegridActionSetRepresenter();
	
	private int sample_count = 0;
	
	public RacegridFsssModel( final RandomGenerator rng, final RacegridState ex, final double slip )
	{
		this.rng = rng;
		this.ex = ex;
		this.slip = slip;
		this.h = new ShortestPathHeuristic( ex );
	}
	
	private RacegridFsssModel( final RacegridFsssModel that, final RandomGenerator rng )
	{
		this.rng = rng;
		this.ex = that.ex;
		this.slip = that.slip;
		this.h = that.h;
	}
	
	@Override
	public FsssModel<RacegridState, RacegridAction> create( final RandomGenerator rng )
	{
		return new RacegridFsssModel( this, rng );
	}
	
	@Override
	public double Vmin( final RacegridState s )
	{
		// You might drive around aimlessly up to the time limit, then crash
		// on the last time step.
		return -(s.T - s.t) - s.T;
	}

	@Override
	public double Vmax( final RacegridState s )
	{
		// -(normalized distance to goal)
		return h.evaluate( s );
//		return 0;
	}
	
	@Override
	public double Vmin( final RacegridState s, final RacegridAction a )
	{
		// Redundancy for emphasis
		return reward( s, a ) + ( -(s.T - (s.t + 1)) - s.T );
	}

	@Override
	public double Vmax( final RacegridState s, final RacegridAction a )
	{
//		return -1;
//		return 0;
		return reward( s, a );
	}
	
	@Override
	public double heuristic( final RacegridState s )
	{
//		return 4*h.evaluate( s );
//		return h.evaluate( s ) + Math.sqrt( s.dx*s.dx + s.dy*s.dy );
		return h.evaluate( s );
	}

	@Override
	public double discount()
	{
		return 1.0;
	}

	@Override
	public RandomGenerator rng()
	{
		return rng;
	}

	@Override
	public FactoredRepresenter<RacegridState, ? extends FactoredRepresentation<RacegridState>> base_repr()
	{
		return base_repr;
	}

	@Override
	public Representer<RacegridState, ? extends Representation<RacegridState>> action_repr()
	{
		return action_repr;
	}

	@Override
	public RacegridState initialState()
	{
		final RacegridState s = new RacegridState( ex );
		s.setRandomStartState( rng );
		return s;
	}

	@Override
	public Iterable<RacegridAction> actions( final RacegridState s )
	{
		final RacegridActionGenerator actions = new RacegridActionGenerator();
		return Fn.in( actions );
	}

	@Override
	public RacegridState sampleTransition( final RacegridState s, final RacegridAction a )
	{
		sample_count += 1;
		final RacegridState sprime = new RacegridState( s );
		a.create().doAction( rng, sprime );
		RacegridDynamics.applyDynamics( rng, sprime, slip );
		return sprime;
	}

	@Override
	public double reward( final RacegridState s )
	{
		if( s.crashed ) {
			return -s.T;
		}
		else {
			return 0;
		}
		
//		else if( s.goal ) {
//			return 0;
//		}
//		else {
//			return -1;
//		}
	}

	@Override
	public double reward( final RacegridState s, final RacegridAction a )
	{
		return -1;
//		return 0;
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
