/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic.policy;

import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicAction;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicState;
import edu.oregonstate.eecs.mcplan.domains.cosmic.Shunt;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * @author jhostetler
 *
 */
public class FixedRateLoadShedding extends Policy<CosmicState, CosmicAction>
{
	private final double rate;
	private final double target;
	private final double increment;
	
	private TIntList shunts = null;
	
	public FixedRateLoadShedding( final double rate, final double target, final double increment )
	{
		this.rate = rate;
		this.target = target;
		this.increment = increment;
	}
	
	@Override
	public void setState( final CosmicState s, final long t )
	{
		shunts = new TIntArrayList();
		for( final Shunt sh : s.shunts() ) {
			if( sh.factor() > 0 && sh.current_P() > 0 ) {
				shunts.add( sh.id() );
			}
		}
	}

	@Override
	public CosmicAction getAction()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void actionResult( final CosmicState sprime, final double[] r )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int hashCode()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean equals( final Object that )
	{
		// TODO Auto-generated method stub
		return false;
	}

}
