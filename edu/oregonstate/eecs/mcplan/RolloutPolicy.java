/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.oregonstate.eecs.mcplan.search.MctsVisitor;
import edu.oregonstate.eecs.mcplan.search.RolloutSearch;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * @author jhostetler
 *
 */
public class RolloutPolicy<S, A extends UndoableAction<S, A>> implements AnytimePolicy<S, A>
{
	private static final Logger log = LoggerFactory.getLogger( RolloutPolicy.class );
	
	private final UndoSimulator<S, A> sim_;
	private final ActionGenerator<S, A> action_gen_;
	private final double c_;
	private final List<Policy<S, A>> rollout_policies_;
	private final MctsVisitor<S, A> visitor_;
	
	public RolloutPolicy( final UndoSimulator<S, A> sim, final ActionGenerator<S, A> action_gen,
						  final double c, final List<Policy<S, A>> rollout_policies, final MctsVisitor<S, A> visitor )
	{
		sim_ = sim;
		action_gen_ = action_gen;
		c_ = c;
		rollout_policies_ = rollout_policies;
		visitor_ = visitor;
	}
	
	@Override
	public void setState( final S s, final long t )
	{ }

	@Override
	public A getAction()
	{
		return getAction( maxControl() );
	}

	@Override
	public void actionResult( final A a, final S sprime, final double r )
	{
		// TODO:
	}

	@Override
	public String getName()
	{
		return "RolloutPolicy";
	}

	@Override
	public long minControl()
	{
		return 0;
	}

	@Override
	public long maxControl()
	{
		return Long.MAX_VALUE;
	}

	@Override
	public A getAction( final long control )
	{
		final RolloutSearch<S, A> search = new RolloutSearch<S, A>(
			sim_, action_gen_, c_, control, rollout_policies_, visitor_ );
		search.run();
		final double[] qmean = new double[search.q_.length];
		final double[] qvar = new double[search.q_.length];
		for( int i = 0; i < search.q_.length; ++i ) {
			final MeanVarianceAccumulator mv = search.q_[i];
			qmean[i] = mv.mean();
			qvar[i] = mv.variance();
		}
		log.info( "qmean = {}", Arrays.toString( qmean ) );
		log.info( "qvar = {}", Arrays.toString( qvar ) );
		log.info( "na = {}", Arrays.toString( search.na_ ) );
		log.info( "ns = {}", search.ns_ );
		return search.principalVariation().actions.get( 0 );
	}

}
