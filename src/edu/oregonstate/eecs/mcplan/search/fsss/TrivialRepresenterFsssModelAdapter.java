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
