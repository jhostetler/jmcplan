/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;

/**
 * @author jhostetler
 *
 */
public abstract class FsssModel<S extends State, A>
{
	public abstract double Vmin();
	public abstract double Vmax();
	public abstract double discount();
	
	public abstract FactoredRepresenter<S, ? extends FactoredRepresentation<S>> base_repr();
	
	/**
	 * This representer must return distinct Representation objects for states
	 * that have different legal action sets.
	 * @return
	 */
	public abstract Representer<S, ? extends Representation<S>> action_repr();
	
	public abstract Iterable<A> actions( final S s );
	public abstract S sampleTransition( final S s, final A a );
	public abstract double reward( final S s, final A a );
}
