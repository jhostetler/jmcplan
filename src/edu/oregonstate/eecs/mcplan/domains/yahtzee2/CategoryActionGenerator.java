package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class CategoryActionGenerator extends ActionGenerator<YahtzeeState, YahtzeeAction>
{
	private YahtzeeState s_ = null;
	private int i = 0;
	private int N = 0;
	private int count = 0;
	
	@Override
	public ActionGenerator<YahtzeeState, YahtzeeAction> create()
	{
		return new CategoryActionGenerator();
	}

	@Override
	public void setState( final YahtzeeState s, final long t )
	{
		s_ = s;
		i = 0;
		count = 0;
		N = YahtzeeScores.values().length - Fn.sum( s.filled );
	}

	@Override
	public int size()
	{
		return N;
	}

	@Override
	public boolean hasNext()
	{
		return count < N;
	}

	@Override
	public YahtzeeAction next()
	{
		while( s_.filled[i] ) {
			i += 1;
		}
		count += 1;
		return new CategoryAction( YahtzeeScores.values()[i++] );
	}
}
