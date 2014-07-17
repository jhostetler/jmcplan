/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

/**
 * @author jhostetler
 *
 */
public class SummaryStatistics implements StatisticAccumulator
{
	public MeanVarianceAccumulator moments = new MeanVarianceAccumulator();
	public MinMaxAccumulator extrema = new MinMaxAccumulator();
	public QuantileAccumulator quartiles = new QuantileAccumulator( 0.25, 0.5, 0.75 );
	
	@Override
	public void add( final double d )
	{
		moments.add( d );
		extrema.add( d );
		quartiles.add( d );
	}
}
