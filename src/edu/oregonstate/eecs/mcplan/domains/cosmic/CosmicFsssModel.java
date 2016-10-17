/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.ActionSpace;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssModel;

/**
 * @deprecated
 * @author jhostetler
 */
@Deprecated
public class CosmicFsssModel extends FsssModel<CosmicState, CosmicAction>
{
	private final RandomGenerator rng;
	private final CosmicParameters params;
	private final CosmicState s0;
	private final FactoredRepresenter<CosmicState, ? extends FactoredRepresentation<CosmicState>> base_repr;
	
	private final ActionSpace<CosmicState, CosmicAction> action_space;
	
	private final double Rmax;
	
	private int sample_count = 0;
	
	public CosmicFsssModel( final RandomGenerator rng, final CosmicParameters params, final CosmicState s0,
							final ActionSpace<CosmicState, CosmicAction> action_space,
							final FactoredRepresenter<CosmicState, ? extends FactoredRepresentation<CosmicState>> base_repr )
	{
		this.rng = rng;
		this.params = params;
		this.s0 = s0;
		this.base_repr = base_repr;
		this.action_space = action_space;
		
		double r = 0.0;
		for( final Shunt sh : s0.shunts() ) {
			r += sh.P() * sh.value();
		}
		Rmax = r;
	}
	
	@Override
	public FsssModel<CosmicState, CosmicAction> create( final RandomGenerator rng )
	{
		return new CosmicFsssModel( rng, params, s0, action_space, base_repr );
	}

	@Override
	public double Vmin( final CosmicState s )
	{
		return 0;
	}

	@Override
	public double Vmax( final CosmicState s )
	{
		return reward( s ) + Rmax * Math.max( 0, params.T - s.t );
	}

	@Override
	public double Vmin( final CosmicState s, final CosmicAction a )
	{
		return 0;
	}

	@Override
	public double Vmax( final CosmicState s, final CosmicAction a )
	{
		return reward(s, a) + Rmax * Math.max( 0, params.T - s.t - 1 );
	}

	@Override
	public double discount()
	{
		return 1.0;
	}

	@Override
	public double heuristic( final CosmicState s )
	{
		return Vmax( s );
	}

	@Override
	public RandomGenerator rng()
	{
		return rng;
	}

	@Override
	public FactoredRepresenter<CosmicState, ? extends FactoredRepresentation<CosmicState>> base_repr()
	{
		return base_repr;
	}

	@Override
	public Representer<CosmicState, ? extends Representation<CosmicState>> action_repr()
	{
		return action_space;
	}

	@Override
	public CosmicState initialState()
	{
		return s0.copy();
	}

	@Override
	public Iterable<CosmicAction> actions( final CosmicState s )
	{
		return action_space.getActionSet( s );
	}

	@Override
	public CosmicState sampleTransition( final CosmicState s, final CosmicAction a )
	{
		final CosmicState sprime = params.cosmic.take_action( s, a, params.delta_t );
		sample_count += 1;
		return sprime;
	}

	@Override
	public double reward( final CosmicState s )
	{
		// Reward for supplying loads
		double r = 0;
		for( final Shunt sh : s.shunts() ) {
			final double p = sh.current_P();
			assert( p >= 0 );
			r += p * sh.value();
		}
		// If Voltage != 1pu, then supplied power may be larger than sh.P().
		// We take the min here so that the agent is not rewarded for driving
		// the voltage away from 1 in order to supply more power.
		return Math.min( r, Rmax );
		
		// Reward for keeping Vmag near unity
//		double r = 0;
//		final MWNumericArray ps_bus = (MWNumericArray) s.ps.getField( "bus", 1 );
//		final int[] idx = new int[] { 0, params.bu_col_names.get( "Vmag" ) };
//		for( int i = 0; i < params.Nbus; ++i ) {
//			idx[0] = i;
//			final double Vmag = ps_bus.getDouble( idx );
//			final double d = 1.0 - Vmag;
//			r += -d*d;
//		}
//		return r;
	}

	@Override
	public double reward( final CosmicState s, final CosmicAction a )
	{
		// TODO: Action cost
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
