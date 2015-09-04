/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.ipc.crossing;

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
public class IpcCrossingDomains
{
	public static IpcCrossingState parse( final File domain, final File instance )
	{
		final RddlSpec spec = new RddlSpec( domain, instance, 1, 42, "unused" );
		
		final int T = spec.rddl_instance._nHorizon;
		final TYPE_NAME xposT = new TYPE_NAME( "xpos" );
		final TYPE_NAME yposT = new TYPE_NAME( "ypos" );
		final ArrayList<LCONST> xpos = spec.rddl_nonfluents._hmObjects.get( xposT )._alObjects;
		final ArrayList<LCONST> ypos = spec.rddl_nonfluents._hmObjects.get( yposT )._alObjects;
		final int width = xpos.size();
		final int height = ypos.size();
		
		int goal_x = -1;
		int goal_y = -1;
		double input_rate = Double.NaN;
		for( final PVAR_INST_DEF p : spec.rddl_nonfluents._alNonFluents ) {
			System.out.println( "non-fluent: " + p._sPredName._sPVarNameCanon );
			if( "goal".equals( p._sPredName._sPVarNameCanon ) ) {
				final LCONST gx = p._alTerms.get( 0 );
				final LCONST gy = p._alTerms.get( 1 );
				goal_x = Integer.parseInt( gx.toString().substring( 1 ) ) - 1;
				goal_y = Integer.parseInt( gy.toString().substring( 1 ) ) - 1;
				System.out.println( "goal( " + gx + "=" + goal_x + ", " + gy + "=" + goal_y + " )" );
			}
			else if( "input_rate".equals( p._sPredName._sPVarNameCanon ) ) {
				final Object rate = p._oValue;
				input_rate = (Double) rate;
				System.out.println( "input_rate: " + rate + " = " + input_rate );
			}
//			else {
//				throw new IllegalArgumentException( "Non-fluent '" + p + "'" );
//			}
		}
		
		final IpcCrossingParameters params = new IpcCrossingParameters(
			T, width, height, goal_x, goal_y, input_rate );
		final IpcCrossingState s0 = new IpcCrossingState( params );
		
		for( final PVAR_INST_DEF p : spec.rddl_instance._alInitState ) {
			System.out.println( "init-state: " + p._sPredName._sPVarNameCanon );
			
			if( "robot_at".equals( p._sPredName._sPVarNameCanon ) ) {
				final LCONST rx = p._alTerms.get( 0 );
				final LCONST ry = p._alTerms.get( 1 );
				final int robot_x = Integer.parseInt( rx.toString().substring( 1 ) ) - 1;
				final int robot_y = Integer.parseInt( ry.toString().substring( 1 ) ) - 1;
				s0.x = robot_x;
				s0.y = robot_y;
				System.out.println( "robot_at( " + rx + "=" + robot_x + ", " + ry + "=" + robot_y + " )" );
			}
			else if( "obstacle_at".equals( p._sPredName._sPVarNameCanon ) ) {
				final LCONST ox = p._alTerms.get( 0 );
				final LCONST oy = p._alTerms.get( 1 );
				final int obstacle_x = Integer.parseInt( ox.toString().substring( 1 ) ) - 1;
				final int obstacle_y = Integer.parseInt( oy.toString().substring( 1 ) ) - 1;
				s0.grid[obstacle_y][obstacle_x] = true;
				System.out.println( "obstacle_at( " + ox + "=" + obstacle_x + ", " + oy + "=" + obstacle_y + " )" );
			}
			else {
				throw new IllegalArgumentException( "init-state: '" + p + "'" );
			}
		}
		
		return s0;
	}
}
