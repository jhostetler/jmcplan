/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.ipc.elevators;

import java.io.File;
import java.util.ArrayList;

import rddl.RDDL.LCONST;
import rddl.RDDL.PVAR_INST_DEF;
import rddl.RDDL.TYPE_NAME;
import edu.oregonstate.eecs.mcplan.rddl.RddlSpec;

/**
 * @author jhostetler
 *
 */
public class IpcElevatorsDomains
{
	public static IpcElevatorsState parse( final File domain, final File instance )
	{
		final RddlSpec spec = new RddlSpec( domain, instance, 1, 42, "unused" );
		
		final int T = spec.rddl_instance._nHorizon;
		final TYPE_NAME elevatorT = new TYPE_NAME( "elevator" );
		final TYPE_NAME floorT = new TYPE_NAME( "floor" );
		final ArrayList<LCONST> elevator = spec.rddl_nonfluents._hmObjects.get( elevatorT )._alObjects;
		final ArrayList<LCONST> floor = spec.rddl_nonfluents._hmObjects.get( floorT )._alObjects;
		final int Nelevators = elevator.size();
		final int Nfloors = floor.size();
		
		final double[] arrive_param = new double[Nfloors];
		for( final PVAR_INST_DEF p : spec.rddl_nonfluents._alNonFluents ) {
			System.out.println( "non-fluent: " + p._sPredName._sPVarNameCanon );
			if( "arrive_param".equals( p._sPredName._sPVarNameCanon ) ) {
				final LCONST e = p._alTerms.get( 0 );
				// Elevators are always 'eN', where 'N' is an integer
				final int eidx = Integer.parseInt( e._sConstValue.substring( 1 ) );
				final double rate = (Double) p._oValue;
				arrive_param[eidx] = rate;
				System.out.println( "arrive_param(" + e + ") = " + rate );
			}
//			else {
//				throw new IllegalArgumentException( "Non-fluent '" + p + "'" );
//			}
		}
		
		final IpcElevatorsParameters params = new IpcElevatorsParameters(
			T, Nfloors, Nelevators, arrive_param );
		final IpcElevatorsState s0 = new IpcElevatorsState( params );
		
		for( final PVAR_INST_DEF p : spec.rddl_instance._alInitState ) {
			System.out.println( "init-state: " + p._sPredName._sPVarNameCanon );
			
			if( "elevator_at_floor".equals( p._sPredName._sPVarNameCanon ) ) {
				final LCONST e = p._alTerms.get( 0 );
				final LCONST f = p._alTerms.get( 1 );
				final int eidx = Integer.parseInt( e._sConstValue.substring( 1 ) );
				final int fidx = Integer.parseInt( f._sConstValue.substring( 1 ) );
				s0.elevators[eidx].at_floor = (byte) fidx;
				System.out.println( "elevator_at_floor( " + e + "=" + eidx + ", " + f + "=" + fidx + " )" );
			}
			else {
				throw new IllegalArgumentException( "init-state: '" + p + "'" );
			}
		}
		
		return s0;
	}
}
