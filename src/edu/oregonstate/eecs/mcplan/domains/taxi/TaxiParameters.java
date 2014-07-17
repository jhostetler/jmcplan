/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.taxi;

/**
 * @author jhostetler
 *
 */
public class TaxiParameters
{
	public final int Nother_taxis;
	public final double slip;
	
	public TaxiParameters( final int Nother_taxis, final double slip )
	{
		this.Nother_taxis = Nother_taxis;
		this.slip = slip;
	}
}
