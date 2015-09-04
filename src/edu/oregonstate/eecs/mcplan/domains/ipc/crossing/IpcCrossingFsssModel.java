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
