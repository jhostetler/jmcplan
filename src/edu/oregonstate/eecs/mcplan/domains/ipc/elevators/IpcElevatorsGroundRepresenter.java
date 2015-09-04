/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.ipc.elevators;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.ArrayFactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public final class IpcElevatorsGroundRepresenter
	implements FactoredRepresenter<IpcElevatorsState, FactoredRepresentation<IpcElevatorsState>>
{
	private final IpcElevatorsParameters params;
	private final ArrayList<Attribute> attributes;
	
	public IpcElevatorsGroundRepresenter( final IpcElevatorsParameters params )
	{
		this.params = params;
		attributes = new ArrayList<Attribute>();
		for( int i = 0; i < params.Nelevators; ++i ) {
			attributes.add( new Attribute( "e" + i ) );
			attributes.add( new Attribute( "e" + i + "closed" ) );
			attributes.add( new Attribute( "e" + i + "up" ) );
			attributes.add( new Attribute( "e" + i + "paxu" ) );
			attributes.add( new Attribute( "e" + i + "paxd" ) );
		}
		for( int i = 0; i < params.Nfloors; ++i ) {
			attributes.add( new Attribute( "f" + i + "u" ) );
			attributes.add( new Attribute( "f" + i + "d" ) );
		}
	}
	
	private IpcElevatorsGroundRepresenter( final IpcElevatorsGroundRepresenter that )
	{
		params = that.params;
		attributes = that.attributes;
	}

	@Override
	public FactoredRepresenter<IpcElevatorsState, FactoredRepresentation<IpcElevatorsState>> create()
	{
		return new IpcElevatorsGroundRepresenter( this );
	}

	@Override
	public FactoredRepresentation<IpcElevatorsState> encode( final IpcElevatorsState s )
	{
		final double[] phi = new double[attributes.size()];
		int idx = 0;
		for( int i = 0; i < params.Nelevators; ++i ) {
//			attributes.add( new Attribute( "e" + i ) );
			final IpcElevatorsState.Elevator e = s.elevators[i];
			phi[idx++] = e.at_floor;
//			attributes.add( new Attribute( "e" + i + "closed" ) );
			phi[idx++] = e.closed ? 1 : 0;
//			attributes.add( new Attribute( "e" + i + "up" ) );
			phi[idx++] = e.dir_up ? 1 : 0;
//			attributes.add( new Attribute( "e" + i + "paxu" ) );
			phi[idx++] = e.person_in_elevator_going_up ? 1 : 0;
//			attributes.add( new Attribute( "e" + i + "paxd" ) );
			phi[idx++] = e.person_in_elevator_going_down ? 1 : 0;
		}
		for( int i = 0; i < params.Nfloors; ++i ) {
//			attributes.add( new Attribute( "f" + i + "u" ) );
			final byte f = s.floors[i];
			phi[idx++] = ((f & IpcElevatorsState.passenger_waiting_up) != 0) ? 1 : 0;
//			attributes.add( new Attribute( "f" + i + "d" ) );
			phi[idx++] = ((f & IpcElevatorsState.passenger_waiting_down) != 0) ? 1 : 0;
		}
		assert( idx == phi.length );
		return new ArrayFactoredRepresentation<IpcElevatorsState>( phi );
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes;
	}
}
