package edu.oregonstate.eecs.mcplan.domains.ipc.elevators;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.util.Fn;

public final class IpcElevatorsState implements State
{
	public static class Elevator
	{
		public byte at_floor = 0;
		public boolean dir_up = false;
		public boolean closed = false;
		public boolean person_in_elevator_going_up = false;
		public boolean person_in_elevator_going_down = false;
		
		public Elevator() { }
		
		public Elevator( final Elevator that )
		{
			this.at_floor = that.at_floor;
			this.dir_up = that.dir_up;
			this.closed = that.closed;
			this.person_in_elevator_going_up = that.person_in_elevator_going_up;
			this.person_in_elevator_going_down = that.person_in_elevator_going_down;
		}
	}
	
	public static final byte passenger_none			= 0;
	public static final byte passenger_waiting_up	= 0b01;
	public static final byte passenger_waiting_down	= 0b10;
	public static final byte passenger_waiting_both	= passenger_waiting_up | passenger_waiting_down;
	
	public final IpcElevatorsParameters params;
	
	public final byte[] floors;
	public final Elevator[] elevators;
	
	public int t = 0;
	
	public IpcElevatorsState( final IpcElevatorsParameters params )
	{
		this.params = params;
		
		floors = new byte[params.Nfloors];
		elevators = new Elevator[params.Nelevators];
		for( int e = 0; e < params.Nelevators; ++e ) {
			elevators[e] = new Elevator();
		}
	}
	
	public IpcElevatorsState( final IpcElevatorsState that )
	{
		this.params = that.params;
		this.t = that.t;
		this.floors = Fn.copy( that.floors );
		this.elevators = new Elevator[params.Nelevators];
		for( int e = 0; e < params.Nelevators; ++e ) {
			this.elevators[e] = new Elevator( that.elevators[e] );
		}
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		for( int f = params.Nfloors - 1; f >= 0; --f ) {
			final byte floor = floors[f];
			sb.append( ((floor & passenger_waiting_up) != 0) ? "u" : "." );
			for( int e = 0; e < params.Nelevators; ++e ) {
				sb.append( "|" );
				final Elevator elevator = elevators[e];
				if( elevator.at_floor == f ) {
					sb.append( elevator.closed ? "X" : "O" );
					sb.append( elevator.dir_up ? "^" : "v" );
					sb.append( elevator.person_in_elevator_going_up ? "u" : "." );
					sb.append( elevator.person_in_elevator_going_down ? "d" : "." );
				}
				else {
					sb.append( "...." );
				}
			}
			sb.append( "|" );
			sb.append( ((floor & passenger_waiting_down) != 0) ? "d" : "." );
			if( f > 0 ) {
				sb.append( "\n" );
			}
		}
		return sb.toString();
	}
	
	@Override
	public boolean isTerminal()
	{
		return t >= params.T;
	}
	
	public IpcElevatorsState step( final RandomGenerator rng, final IpcElevatorsAction a )
	{
		// NOTE: There are some weird things about the RDDL code from IPC 2014:
		// * It is possible for the same passenger to get into two elevators
		// during the same time step, because passenger-waiting is checked by
		// all elevators before it changes to passenger-waiting'.
		// * Elevators don't know how many people are in them (which is
		// probably why all the people have to get off at the same floor).
		//
		// We preserve these behaviors because we want an exact translation.
		
		final IpcElevatorsState sprime = new IpcElevatorsState( this );
		sprime.t += 1;
		
		// First the uncontrolled dynamics, then the actions
		for( int f = 0; f < floors.length; ++f ) {
			final byte floor = floors[f];
			// Keep track of which passengers we picked up
			byte mask = 0;
			for( int e = 0; e < elevators.length; ++e ) {
				final Elevator elevator = elevators[e];
				if( elevator.closed ) {
					continue;
				}
				
				if( elevator.at_floor == f ) {
					// Drop people off at destinations
					if( f == 0 ) {
						sprime.elevators[e].person_in_elevator_going_down = false;
					}
					else if( f == params.Nfloors - 1 ) {
						sprime.elevators[e].person_in_elevator_going_up = false;
					}
					
					// Pick up waiting passengers
					// NOTE: All IPC instances are parameterized so that people
					// never spawn at destination floors. Since this is a
					// per-instance parameter, we handle spawns at destinations
					// properly regardless instead of optimizing it out.
					if( elevator.dir_up && (floor & passenger_waiting_up) != 0 ) {
						sprime.elevators[e].person_in_elevator_going_up = true;
						mask |= passenger_waiting_up;
					}
					else if( !elevator.dir_up && (floor & passenger_waiting_down) != 0 ) {
						sprime.elevators[e].person_in_elevator_going_down = true;
						mask |= passenger_waiting_down;
					}
				}
			}
			
			// Spawn new people on empty floors
			byte floor_prime = (byte) (floor ^ mask);
			if( (floor_prime & passenger_waiting_up) == 0 ) {
				if( rng.nextDouble() < params.arrive_param[f] ) {
					floor_prime |= passenger_waiting_up;
				}
			}
			if( (floor_prime & passenger_waiting_down) == 0 ) {
				if( rng.nextDouble() < params.arrive_param[f] ) {
					floor_prime |= passenger_waiting_down;
				}
			}
			sprime.floors[f] = floor_prime;
		}
		
		// Actions
		for( int e = 0; e < a.actions.length; ++e ) {
			final IpcElevatorsAction.Type t = a.actions[e];
			final Elevator elevator_prime = sprime.elevators[e];
			switch( t ) {
			case MoveCurrentDir:
				if( elevator_prime.closed ) { // no-op if door is open
					if( elevator_prime.dir_up && elevator_prime.at_floor < params.Nfloors - 1 ) {
						elevator_prime.at_floor += 1;
					}
					else if( !elevator_prime.dir_up && elevator_prime.at_floor > 0 ) {
						elevator_prime.at_floor -= 1;
					}
				}
				break;
			case OpenDoorGoingUp:
				elevator_prime.closed = false;
				elevator_prime.dir_up = true;
				break;
			case OpenDoorGoingDown:
				elevator_prime.closed = false;
				elevator_prime.dir_up = false;
				break;
			case CloseDoor:
				elevator_prime.closed = true;
				break;
			default:
				throw new IllegalArgumentException( "t = " + t );
			}
		}
		
		return sprime;
	}
}
