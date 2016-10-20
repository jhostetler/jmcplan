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
package edu.oregonstate.eecs.mcplan.search.fsss;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.TrivialRepresenter;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * Adapts an FsssModel to return an instance of TrivialRepresenter from its
 * base_repr() method. All other methods forward to the wrapped model.
 * 
 * FIXME: This is probably not a good way to accomplish the trivial
 * abstraction. You might as well just specify the base_repr when you construct
 * the underlying model. That way you could also have multiple non-trivial
 * representations of the same problem.
 */
public class TrivialRepresenterFsssModelAdapter<S extends State, A extends VirtualConstructor<A>>
	extends FsssModel<S, A>
{
	private final FsssModel<S, A> model;
	
	private final TrivialRepresenter<S> trivial_repr = new TrivialRepresenter<S>();
	
	public TrivialRepresenterFsssModelAdapter( final FsssModel<S, A> model )
	{
		this.model = model;
	}
	
	@Override
	public FsssModel<S, A> create( final RandomGenerator rng )
	{
		return new TrivialRepresenterFsssModelAdapter<S, A>( model.create( rng ) );
	}
	
	@Override
	public double Vmin( final S s )
	{ return model.Vmin( s ); }

	@Override
	public double Vmax( final S s )
	{ return model.Vmax( s ); }
	
	@Override
	public double Vmin( final S s, final A a )
	{ return model.Vmin( s, a ); }

	@Override
	public double Vmax( final S s, final A a )
	{ return model.Vmax( s, a ); }
	
	@Override
	public double heuristic( final S s )
	{ return model.heuristic( s ); }

	@Override
	public double discount()
	{ return model.discount(); }

	@Override
	public RandomGenerator rng()
	{ return model.rng(); }

	@Override
	public FactoredRepresenter<S, ? extends FactoredRepresentation<S>> base_repr()
	{
		return trivial_repr;
	}

	@Override
	public Representer<S, ? extends Representation<S>> action_repr()
	{ return model.action_repr(); }

	@Override
	public S initialState()
	{ return model.initialState(); }

	@Override
	public Iterable<A> actions( final S s )
	{ return model.actions( s ); }

	@Override
	public S sampleTransition( final S s, final A a )
	{ return model.sampleTransition( s, a ); }

	@Override
	public double reward( final S s )
	{ return model.reward( s ); }

	@Override
	public double reward( final S s, final A a )
	{ return model.reward( s, a ); }

	@Override
	public int sampleCount()
	{ return model.sampleCount(); }

	@Override
	public void resetSampleCount()
	{ model.resetSampleCount(); }
}
