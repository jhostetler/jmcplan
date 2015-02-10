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
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * Provides the domain-specific functions that we need for FSSS.
 */
public abstract class FsssModel<S extends State, A extends VirtualConstructor<A>>
{
	public abstract double Vmin( final S s );
	public abstract double Vmax( final S s );
	public abstract double discount();
	
	public abstract double heuristic( final S s );
	
	public abstract RandomGenerator rng();
	
	public abstract FactoredRepresenter<S, ? extends FactoredRepresentation<S>> base_repr();
	
	/**
	 * This representer must map states that have different legal action sets
	 * to different Representations.
	 * @return
	 */
	public abstract Representer<S, ? extends Representation<S>> action_repr();
	
	public abstract S initialState();
	public abstract Iterable<A> actions( final S s );
	public abstract S sampleTransition( final S s, final A a );
	public abstract double reward( final S s );
	public abstract double reward( final S s, final A a );
	
	// TODO: These probably don't belong here
	public abstract int sampleCount();
	public abstract void resetSampleCount();
}
