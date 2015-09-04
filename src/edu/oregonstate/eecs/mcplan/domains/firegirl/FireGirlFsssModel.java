/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.firegirl;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssModel;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public final class FireGirlFsssModel extends FsssModel<FireGirlState, FireGirlAction>
{
	private final FireGirlParameters params;
	private final RandomGenerator rng;
	
	private final Representer<FireGirlState, ? extends Representation<FireGirlState>>
		action_repr = new FireGirlActionSetRepresenter();
	
	private int sample_count = 0;
	
	public FireGirlFsssModel( final FireGirlParameters params, final RandomGenerator rng )
	{
		this.params = params;
		this.rng = rng;
	}
	
	@Override
	public FsssModel<FireGirlState, FireGirlAction> create( final RandomGenerator rng )
	{
		return new FireGirlFsssModel( this.params, rng );
	}
	
	/**
	 * Calculates effective horizon, accounting for discounted vs. finite
	 * horizon case.
	 * @param s
	 * @return
	 */
	private double horizon( final FireGirlState s )
	{
		return (discount() == 1.0
				? params.T - s.year
				: 1.0 / (1.0 - discount()));
	}
	
	@Override
	public double Vmin( final FireGirlState s )
	{
		return s.r + discount()*Vmin( s, null );
	}

	@Override
	public double Vmax( final FireGirlState s )
	{
		return reward( s ) + discount()*Vmax( s, null );
	}

	@Override
	public double Vmin( final FireGirlState s, final FireGirlAction a )
	{
		final int max_cells = params.fire_iter_cap;
		final int max_days = 13; // 13 = min(t) : P(time > t) < 0.001
		return horizon( s )
			* (max_cells*params.fire_suppression_cost_per_cell + max_days*params.fire_suppression_cost_per_day);
	}

	@Override
	public double Vmax( final FireGirlState s, final FireGirlAction a )
	{
		final int logs_per_block = params.logging_block_width*params.logging_block_width;
		return horizon( s ) * params.max_growth*params.logging_max_cuts*logs_per_block;
	}

	@Override
	public double discount()
	{
		return params.discount;
	}

	@Override
	public double heuristic( final FireGirlState s )
	{
		return 0;
//		return s.totalValue();
	}

	@Override
	public RandomGenerator rng()
	{
		return rng;
	}

	@Override
	public FactoredRepresenter<FireGirlState, ? extends FactoredRepresentation<FireGirlState>> base_repr()
	{
		return params.base_repr;
	}

	@Override
	public Representer<FireGirlState, ? extends Representation<FireGirlState>> action_repr()
	{
		return action_repr;
	}

	@Override
	public FireGirlState initialState()
	{
		final FireGirlState s = new FireGirlState( params );
		s.setRandomInitialState( rng );
		return s;
	}

	@Override
	public Iterable<FireGirlAction> actions( final FireGirlState s )
	{
		return Fn.in( FireGirlAction.values() );
	}

	@Override
	public FireGirlState sampleTransition( final FireGirlState s, final FireGirlAction a )
	{
		final FireGirlState sprime = new FireGirlState( s );
		final FireGirlState.YearResult result = sprime.doOneYear( rng, a );
		sprime.r = result.logging + result.fire.sup_cost;
		sample_count += 1;
		return sprime;
	}

	@Override
	public double reward( final FireGirlState s )
	{
		return s.r;
	}

	@Override
	public double reward( final FireGirlState s, final FireGirlAction a )
	{
		return 0;
	}

	@Override
	public int sampleCount()
	{
		return sample_count;
	}

	@Override
	public void resetSampleCount()
	{
		sample_count = 0;
	}
}
