/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.tamarisk;

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author jhostetler
 *
 */
public class EradicateAction extends TamariskAction
{
	public final int reach;
	private Species[] old_species_ = null;
	private double cost_ = 0.0;
	
	public EradicateAction( final int reach )
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
	public void doAction( final RandomGenerator rng, final TamariskState s )
	{
		assert( old_species_ == null );
		old_species_ = Arrays.copyOf( s.habitats[reach], s.params.Nhabitats );
		cost_ = s.params.eradicate_cost;
		for( int i = 0; i < s.params.Nhabitats; ++i ) {
			if( old_species_[i] == Species.Tamarisk ) {
				cost_ += s.params.eradicate_cost_per_habitat;
				final double r = s.rng.nextDouble();
				if( r < s.params.eradicate_rate ) {
					s.habitats[reach][i] = Species.None;
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
	public TamariskAction create()
	{
		return new EradicateAction( reach );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof EradicateAction) ) {
			return false;
		}
		final EradicateAction that = (EradicateAction) obj;
		return reach == that.reach;
	}

	@Override
	public int hashCode()
	{
		return 17 + 31 * reach;
	}

	@Override
	public String toString()
	{
		return "EradicateAction[" + reach + "]";
	}

}
