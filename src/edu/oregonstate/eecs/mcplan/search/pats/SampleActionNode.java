/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.pats;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.sim.TransitionSimulator;

/**
 * @author jhostetler
 *
 */
public class SampleActionNode<S extends State, A>
{
	public final A a;
	private final ArrayList<SampleStateNode<S, A>> successors = new ArrayList<>();
	
	public SampleActionNode( final A a )
	{
		this.a = a;
	}
	
	public void sample( final TransitionSimulator<S, A> sim )
	{
		sim.sampleTransition( rng, s, a );
	}
	
	public void addSuccessor( final SampleStateNode<S, A> successor )
	{
		successors.add( successor );
	}
	
	public Iterable<SampleStateNode<S, A>> successors()
	{ return successors; }
}
