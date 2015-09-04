/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.ipc.elevators;

/**
 * @author jhostetler
 *
 */
public final class IpcElevatorsParameters
{
	public final double elevator_penalty_right_dir = -0.75;
	public final double elevator_penalty_wrong_dir = -3.0;

	public final int T;
	public final int Nfloors;
	public final int Nelevators;
	public final double[] arrive_param;
	
	public IpcElevatorsParameters( final int T, final int Nfloors, final int Nelevators, final double[] arrive_param )
	{
		this.T = T;
		this.Nfloors = Nfloors;
		this.Nelevators = Nelevators;
		this.arrive_param = arrive_param;
	}
}
