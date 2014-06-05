/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.tamarisk;

import edu.oregonstate.eecs.mcplan.ActionGenerator;

/**
 * @author jhostetler
 *
 */
public class TamariskActionGenerator extends ActionGenerator<TamariskState, TamariskAction>
{
	int category_ = 0;
	int reach_ = 0;
	int Nreaches_ = 0;
	
	@Override
	public ActionGenerator<TamariskState, TamariskAction> create()
	{
		return new TamariskActionGenerator();
	}

	@Override
	public void setState( final TamariskState s, final long t, final int[] turn )
	{
		category_ = 0;
		reach_ = 0;
		Nreaches_ = s.params.Nreaches;
	}

	@Override
	public int size()
	{
		// Three categories, plus the Nothing action.
		return 3*Nreaches_ + 1;
	}

	@Override
	public boolean hasNext()
	{
		return category_ < 4 && reach_ < Nreaches_;
	}

	@Override
	public TamariskAction next()
	{
		final int r = reach_;
		final int c = category_;
		reach_ += 1;
		if( reach_ == Nreaches_ ) {
			reach_ = 0;
			category_ += 1;
		}
		
		switch( c ) {
		case 0:
			return new NothingAction();
		case 1:
			return new EradicateAction( r );
		case 2:
			return new RestoreAction( r );
		case 3:
			return new EradicateRestoreAction( r );
		default:
			throw new IllegalStateException( "hasNext() == false" );
		}
	}
}
