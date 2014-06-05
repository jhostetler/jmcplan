/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racegrid;

import edu.oregonstate.eecs.mcplan.ActionGenerator;

/**
 * @author jhostetler
 *
 */
public class RacegridActionGenerator extends ActionGenerator<RacegridState, RacegridAction>
{
	private final int range_ = 1;
	private int x_ = -range_;
	private int y_ = -range_;
	
	@Override
	public ActionGenerator<RacegridState, RacegridAction> create()
	{
		return new RacegridActionGenerator();
	}

	@Override
	public void setState( final RacegridState s, final long t, final int[] turn )
	{
		x_ = -range_;
		y_ = -range_;
	}

	@Override
	public int size()
	{
		return (2*range_+1)*(2*range_+1);
	}

	@Override
	public boolean hasNext()
	{
		return x_ <= range_ && y_ <= range_;
	}

	@Override
	public RacegridAction next()
	{
		final AccelerateAction a = new AccelerateAction( x_, y_ );
		x_ += 1;
		if( x_ > range_ ) {
			x_ = -range_;
			y_ += 1;
		}
		return a;
	}
}
