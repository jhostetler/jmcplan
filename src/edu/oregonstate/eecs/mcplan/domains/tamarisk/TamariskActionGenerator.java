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
	
	public final boolean enable_eradicate_restore_actions = false;
	public final int Ncategories = (enable_eradicate_restore_actions ? 3 : 2);
	
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
		// Categories, plus the Nothing action.
		return Ncategories*Nreaches_ + 1;
	}

	@Override
	public boolean hasNext()
	{
		return category_ <= Ncategories && reach_ < Nreaches_;
	}

	@Override
	public TamariskAction next()
	{
		if( category_ == 0 ) {
			category_ += 1;
			return new NothingAction();
		}
		
		final int r = reach_;
		final int c = category_;
		reach_ += 1;
		if( reach_ == Nreaches_ ) {
			reach_ = 0;
			category_ += 1;
		}
		
		switch( c ) {
		case 1:
			return new EradicateAction( r );
		case 2:
			return new RestoreAction( r );
//		case 3:
//			return new EradicateRestoreAction( r );
		default:
			throw new IllegalStateException( "hasNext() == false" );
		}
	}
}
