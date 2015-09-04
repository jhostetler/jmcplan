/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.ipc.crossing;

/**
 * @author jhostetler
 *
 */
public class IpcCrossingParameters
{
	public final double input_rate;
	
	public final int T;
	
	public final int width;
	public final int height;
	
	public final int goal_x;
	public final int goal_y;
	
	public IpcCrossingParameters( final int T, final int width, final int height,
								  final int goal_x, final int goal_y, final double input_rate )
	{
		this.T = T;
		this.width = width;
		this.height = height;
		this.goal_x = goal_x;
		this.goal_y = goal_y;
		this.input_rate = input_rate;
	}
}
