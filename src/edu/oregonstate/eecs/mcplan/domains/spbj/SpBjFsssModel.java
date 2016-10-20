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
package edu.oregonstate.eecs.mcplan.domains.spbj;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.domains.cards.InfiniteSpanishDeck;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssModel;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class SpBjFsssModel extends FsssModel<SpBjState, SpBjAction>
{
	private final RandomGenerator rng;
	private final SpBjNullRepresenter base_repr = new SpBjNullRepresenter();
	private final SpBjActionSetRepresenter action_repr = new SpBjActionSetRepresenter();
	
	private int sample_count = 0;
	
	private final InfiniteSpanishDeck deck;
	
	public SpBjFsssModel( final RandomGenerator rng )
	{
		this.rng = rng;
		deck = new InfiniteSpanishDeck( rng );
	}
	
	@Override
	public SpBjFsssModel create( final RandomGenerator rng )
	{
		return new SpBjFsssModel( rng );
	}
	
	@Override
	public double Vmin( final SpBjState s )
	{ return -Vmax( s ); }

	@Override
	public double Vmax( final SpBjState s )
	{ return SpBjHand.max_bet * SpBjHand.max_hands; }
	
	@Override
	public double Vmin( final SpBjState s, final SpBjAction a )
	{ return -Vmax( s, a ); }

	@Override
	public double Vmax( final SpBjState s, final SpBjAction a )
	{ return SpBjHand.max_bet *SpBjHand.max_hands; }
	
	@Override
	public double heuristic( final SpBjState s )
	{ return 0; }

	@Override
	public double discount()
	{ return 1.0; }
	
	@Override
	public SpBjState initialState()
	{
		final SpBjState s0 = new SpBjState( deck );
		s0.init();
		return s0;
	}

	@Override
	public Iterable<SpBjAction> actions( final SpBjState s )
	{
		final SpBjActionGenerator actions = new SpBjActionGenerator();
		actions.setState( s, 0L );
		return Fn.in( actions );
	}

	@Override
	public SpBjState sampleTransition( final SpBjState s, final SpBjAction a )
	{
		sample_count += 1;
		final SpBjState sprime = s.copy();
		a.create().doAction( rng, sprime );
		return sprime;
	}
	
	@Override
	public double reward( final SpBjState s )
	{
		return s.r;
	}

	@Override
	public double reward( final SpBjState s, final SpBjAction a )
	{
//		return s.r;
		return 0;
	}

	@Override
	public FactoredRepresenter<SpBjState, ? extends FactoredRepresentation<SpBjState>> base_repr()
	{
		return base_repr;
	}

	@Override
	public Representer<SpBjState, ? extends Representation<SpBjState>> action_repr()
	{
		return action_repr;
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

	@Override
	public RandomGenerator rng()
	{
		return rng;
	}
}
