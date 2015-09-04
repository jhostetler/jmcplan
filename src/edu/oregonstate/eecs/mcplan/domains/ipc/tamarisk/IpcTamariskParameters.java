/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.ipc.tamarisk;


/**
 * @author jhostetler
 *
 */
public final class IpcTamariskParameters
{
	// Nonfluents: constant parameters
	public final double eradication_rate = 0.9;
	public final double restoration_rate = 0.9;
	public final double downstream_spread_rate = 0.6;
	public final double upstream_spread_rate = 0.15;
	public final double death_rate_tamarisk = 0.05;
	public final double death_rate_native = 0.05;
	
	// These are parameters I have added to address native spreading and
	// competition if a tamarisk and native both appear at a slot.
	public final double exogenous_prod_rate_native = 0.1;
	public final double exogenous_prod_rate_tamarisk = 0.1;
	public final double competition_win_rate_native = 0.2;
	public final double competition_win_rate_tamarisk = 0.8;
		
	// Invasion and vulnerability costs (independent of actions)
	//
	// NOTE: Using negative numbers for costs
	public final double cost_per_invaded_reach = -5.0;
	public final double cost_per_tree = -0.5;
	public final double cost_per_empty_slot = -0.25;
	
	// From https://slots.google.com/slot/rlcompetition2014/domains/invasive-species:
	//
	// The following components of the cost function depend on the action being taken
	// and are multiplied by the number of habitat slots being treated by that action.
	//
	// NOTE: Using negative numbers for costs
	public final double eradication_cost = -0.49;
	public final double restoration_cost = -0.9;
	public final double restoration_cost_for_empty_slot = -0.4;
//	public final double restoration_cost_for_invaded_slot = -0.8;
	
	public final int T;
	public final int Nreaches;
	public final int cells_per_reach;
	
	public final int[][] upstream;
	public final int[][] downstream;
	
	public IpcTamariskParameters( final int T, final int Nreaches, final int cells_per_reach,
								  final int[][] upstream, final int[][] downstream )
	{
		this.T = T;
		this.Nreaches = Nreaches;
		this.cells_per_reach = cells_per_reach;
		this.upstream = upstream;
		this.downstream = downstream;
	}
}
