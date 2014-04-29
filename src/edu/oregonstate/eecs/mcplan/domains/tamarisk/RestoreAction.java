/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.tamarisk;

import java.util.Arrays;

/**
 * @author jhostetler
 *
 */
public class RestoreAction extends TamariskAction
{
	public final int reach;
	private Species[] old_species_ = null;
	private double cost_ = 0.0;
	
	public RestoreAction( final int reach )
	{
		this.reach = reach;
	}
	
	@Override
	public double cost()
	{
		return cost_;
	}
	
	@Override
	public void undoAction( final TamariskState s )
	{
		assert( old_species_ != null );
		for( int i = 0; i < s.params.Nhabitats; ++i ) {
			s.habitats[reach][i] = old_species_[i];
		}
		old_species_ = null;
		cost_ = 0.0;
	}

	@Override
	public void doAction( final TamariskState s )
	{
		assert( old_species_ == null );
		old_species_ = Arrays.copyOf( s.habitats[reach], s.params.Nhabitats );
		cost_ = s.params.restore_cost;
		for( int i = 0; i < s.params.Nhabitats; ++i ) {
			if( old_species_[i] == Species.None ) {
				cost_ += s.params.restore_cost_per_empty;
				final double r = s.rng.nextDouble();
				if( r < s.params.restore_rate ) {
					s.habitats[reach][i] = Species.Native;
				}
			}
		}
	}

	@Override
	public boolean isDone()
	{
		return old_species_ != null;
	}

	@Override
	public RestoreAction create()
	{
		return new RestoreAction( reach );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof RestoreAction) ) {
			return false;
		}
		final RestoreAction that = (RestoreAction) obj;
		return reach == that.reach;
	}

	@Override
	public int hashCode()
	{
		return 19 + 37 * reach;
	}

	@Override
	public String toString()
	{
		return "RestoreAction[" + reach + "]";
	}

}
