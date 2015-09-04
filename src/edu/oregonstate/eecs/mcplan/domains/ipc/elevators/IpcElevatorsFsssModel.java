/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.ipc.elevators;

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
public final class IpcElevatorsFsssModel extends FsssModel<IpcElevatorsState, IpcElevatorsAction>
{
	public final RandomGenerator rng;
	public final IpcElevatorsState s0;
	public final IpcElevatorsParameters params;
	
	private int sample_count = 0;
	
	private final IpcElevatorsActionSetRepresenter action_repr = new IpcElevatorsActionSetRepresenter();
	private final FactoredRepresenter<IpcElevatorsState, ? extends FactoredRepresentation<IpcElevatorsState>> base_repr;
	
	public IpcElevatorsFsssModel( final RandomGenerator rng, final IpcElevatorsState s0 )
	{
		this.rng = rng;
		this.s0 = s0;
		this.params = s0.params;
		
		base_repr = new IpcElevatorsGroundRepresenter( params );
	}

	@Override
	public FsssModel<IpcElevatorsState, IpcElevatorsAction> create( final RandomGenerator rng )
	{
		return new IpcElevatorsFsssModel( rng, s0 );
	}

	@Override
	public double Vmin( final IpcElevatorsState s )
	{
		return reward( s ) + (params.T - s.t) * (
					-(params.Nfloors * 2)
					+ (params.Nelevators * (params.elevator_penalty_right_dir + params.elevator_penalty_wrong_dir))
				);
	}

	@Override
	public double Vmax( final IpcElevatorsState s )
	{
		return reward( s ) + 0;
	}

	@Override
	public double Vmin( final IpcElevatorsState s, final IpcElevatorsAction a )
	{
		// In each time step, we could have two passengers waiting at each
		// floor and two passengers in every elevator, where one of the
		// passengers in each elevator is going the wrong way.
		return (params.T - s.t) * (
					-(params.Nfloors * 2)
					+ (params.Nelevators * (params.elevator_penalty_right_dir + params.elevator_penalty_wrong_dir))
				);
	}

	@Override
	public double Vmax( final IpcElevatorsState s, final IpcElevatorsAction a )
	{
		return 0;
	}

	@Override
	public double discount()
	{
		return 1.0;
	}

	@Override
	public double heuristic( final IpcElevatorsState s )
	{
		return 0;
	}

	@Override
	public RandomGenerator rng()
	{
		return rng;
	}

	@Override
	public FactoredRepresenter<IpcElevatorsState, ? extends FactoredRepresentation<IpcElevatorsState>> base_repr()
	{
		return base_repr;
	}

	@Override
	public Representer<IpcElevatorsState, ? extends Representation<IpcElevatorsState>> action_repr()
	{
		return action_repr;
	}

	@Override
	public IpcElevatorsState initialState()
	{
		return new IpcElevatorsState( s0 );
	}

	@Override
	public Iterable<IpcElevatorsAction> actions( final IpcElevatorsState s )
	{
		return Fn.in( new IpcElevatorsActionGenerator( params ) );
	}

	@Override
	public IpcElevatorsState sampleTransition( final IpcElevatorsState s, final IpcElevatorsAction a )
	{
		final IpcElevatorsState sprime = s.step( rng, a );
		sample_count += 1;
		return sprime;
	}

	@Override
	public double reward( final IpcElevatorsState s )
	{
		double r = 0;
		for( final IpcElevatorsState.Elevator e : s.elevators ) {
			if( e.person_in_elevator_going_up ) {
				r += e.dir_up ? params.elevator_penalty_right_dir : params.elevator_penalty_wrong_dir;
			}
			if( e.person_in_elevator_going_down ) {
				r += e.dir_up ? params.elevator_penalty_wrong_dir : params.elevator_penalty_right_dir;
			}
		}
		for( final byte f : s.floors ) {
			if( (f & IpcElevatorsState.passenger_waiting_up) != 0 ) {
				r += -1;
			}
			if( (f& IpcElevatorsState.passenger_waiting_down) != 0 ) {
				r += -1;
			}
		}
		return r;
	}

	@Override
	public double reward( final IpcElevatorsState s, final IpcElevatorsAction a )
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
