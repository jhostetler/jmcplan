/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.frogger;

import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class FroggerParameters
{
	public final int lanes = 7;
	public final int road_length = 9;
	public final double[] arrival_prob = Fn.repeat( 0.2, lanes );
	public final double lane_switch_prob = 0.1;
}
