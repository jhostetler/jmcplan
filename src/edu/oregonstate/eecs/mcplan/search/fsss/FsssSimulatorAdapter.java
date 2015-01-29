/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.sim.Simulator;

/**
 * @author jhostetler
 *
 */
public class FsssSimulatorAdapter<S extends State, A extends VirtualConstructor<A>> implements Simulator<S, A>
{
	private final FsssModel<S, A> model;
	private S s;
	private double r;
	
	private long t = 0;
	
	public FsssSimulatorAdapter( final FsssModel<S, A> model, final S s0 )
	{
		this.model = model;
		s = s0;
		r = model.reward( s0 );
	}
	
	@Override
	public S state()
	{ return s; }

	@Override
	public void takeAction( final JointAction<A> j )
	{
		assert( j.size() == 1 );
		final A a = j.get( 0 );
		r = model.reward( s, a );
		s = model.sampleTransition( s, a );
		r += model.reward( s );
		t += 1;
	}

	@Override
	public long depth()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long t()
	{
		return t;
	}

	@Override
	public int nagents()
	{
		return 1;
	}

	@Override
	public int[] turn()
	{
		return new int[] { 0 };
	}

	@Override
	public double[] reward()
	{
		return new double[] { r };
	}

	@Override
	public boolean isTerminalState()
	{
		return s.isTerminal();
	}

	@Override
	public long horizon()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String detailString()
	{ return "FsssSimulatorAdapter"; }
	
}