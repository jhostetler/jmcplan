/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.advising;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.abstraction.IndexRepresentation;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssModel;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class AdvisingFsssModel extends FsssModel<AdvisingState, TakeCourseAction>
{
	private static class ActionSetRepresenter implements Representer<AdvisingState, Representation<AdvisingState>>
	{
		@Override
		public Representer<AdvisingState, Representation<AdvisingState>> create()
		{
			return new ActionSetRepresenter();
		}
	
		@Override
		public Representation<AdvisingState> encode( final AdvisingState s )
		{
			if( s.isTerminal() ) {
				return new IndexRepresentation<AdvisingState>( 0 );
			}
			else {
				return new IndexRepresentation<AdvisingState>( 1 );
			}
		}
	}
	
	// -----------------------------------------------------------------------
	
	private final AdvisingParameters params;
	
	private int sample_count = 0;
	
	private final AdvisingGroundRepresenter base_repr;
	private final ActionSetRepresenter action_repr = new ActionSetRepresenter();
	
	public AdvisingFsssModel( final AdvisingParameters params )
	{
		this.params = params;
		base_repr = new AdvisingGroundRepresenter( params );
	}
	
	@Override
	public double Vmin( final AdvisingState s )
	{
		// Note: This was incorrect for the UAI 2015 submission. Scaling by time
		// remaining was absent.
		return (params.T - s.t) * s.params.requirements.length * AdvisingParameters.missing_requirement_reward;
	}

	@Override
	public double Vmax( final AdvisingState s )
	{
		return 0;
	}
	
	@Override
	public double Vmin( final AdvisingState s, final TakeCourseAction a )
	{
		return reward( s, a )
			   + (params.T - (s.t + 1)) * s.params.requirements.length * AdvisingParameters.missing_requirement_reward;
	}

	@Override
	public double Vmax( final AdvisingState s, final TakeCourseAction a )
	{
		return reward( s, a );
	}

	@Override
	public double discount()
	{
		// See: academic_advising_int_mdp__*
		return 1.0;
	}

	@Override
	public double heuristic( final AdvisingState s )
	{
		return Vmax( s );
	}

	@Override
	public RandomGenerator rng()
	{
		return params.rng;
	}

	@Override
	public FactoredRepresenter<AdvisingState, ? extends FactoredRepresentation<AdvisingState>> base_repr()
	{
		return base_repr;
	}

	@Override
	public Representer<AdvisingState, ? extends Representation<AdvisingState>> action_repr()
	{
		return action_repr;
	}

	@Override
	public AdvisingState initialState()
	{
		return new AdvisingState( params );
	}

	@Override
	public Iterable<TakeCourseAction> actions( final AdvisingState s )
	{
		final AdvisingActions actions = new AdvisingActions( s.params );
		actions.setState( s, s.t );
		return Fn.takeAll( actions );
	}

	@Override
	public AdvisingState sampleTransition( final AdvisingState s, final TakeCourseAction a )
	{
		final AdvisingState sprime = new AdvisingState( s );
		
		a.doAction( sprime );
		
		sample_count += 1;
		return sprime;
	}

	@Override
	public double reward( final AdvisingState s )
	{
		double r = 0;
		for( final int req : s.params.requirements ) {
			if( s.grade[req] < s.params.passing_grade ) {
				r += AdvisingParameters.missing_requirement_reward;
			}
		}
		return r;
	}

	@Override
	public double reward( final AdvisingState s, final TakeCourseAction a )
	{
		return a.reward( s );
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
