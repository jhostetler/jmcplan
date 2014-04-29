/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.tamarisk;


/**
 * @author jhostetler
 *
 */
public class EradicateRestoreAction extends TamariskAction
{
	public final int reach;
	private final EradicateAction eradicate_;
	private final RestoreAction restore_;
	
	public EradicateRestoreAction( final int reach )
	{
		this.reach = reach;
		eradicate_ = new EradicateAction( reach );
		restore_ = new RestoreAction( reach );
	}
	
	@Override
	public double cost()
	{
		return eradicate_.cost() + restore_.cost();
	}
	
	@Override
	public void undoAction( final TamariskState s )
	{
		restore_.undoAction( s );
		eradicate_.undoAction( s );
	}

	@Override
	public void doAction( final TamariskState s )
	{
		eradicate_.doAction( s );
		restore_.doAction( s );
	}

	@Override
	public boolean isDone()
	{
		return eradicate_.isDone() && restore_.isDone();
	}

	@Override
	public EradicateRestoreAction create()
	{
		return new EradicateRestoreAction( reach );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof EradicateRestoreAction) ) {
			return false;
		}
		final EradicateRestoreAction that = (EradicateRestoreAction) obj;
		return eradicate_.equals( that.eradicate_ ) && restore_.equals( that.restore_ );
	}

	@Override
	public int hashCode()
	{
		return 61 + 67 * (eradicate_.hashCode() + 71 * (restore_.hashCode()));
	}

	@Override
	public String toString()
	{
		return "EradicateRestoreAction[" + reach + "]";
	}
}
