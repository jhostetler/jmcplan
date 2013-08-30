/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.io.PrintStream;
import java.util.ArrayList;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.Countdown;

/**
 * @author jhostetler
 *
 */
public class UctPolicy<S, F extends Representer<S, F>, A extends VirtualConstructor<A>>
	implements AnytimePolicy<S, A>
{
	private static final Logger log = LoggerFactory.getLogger( UctPolicy.class );
	
	private final UndoSimulator<S, A> sim_;
	private final Representer<S, F> repr_;
	private final ActionGenerator<S, ? extends A> action_gen_;
	private final ArrayList<Policy<S, A>> default_policies_;
	private final double c_;
	private final RandomGenerator rng_;
	private final MctsNegamaxVisitor<S, A> visitor_;
	private final PrintStream log_stream_;
	
	private S s_ = null;
	private long t_ = 0L;
	private final Policy<S, A> policy_used_ = null;
	
	public UctPolicy( final UndoSimulator<S, A> sim,
					  final Representer<S, F> repr,
					  final ActionGenerator<S, ? extends A> action_gen,
					  final ArrayList<Policy<S, A>> default_policies,
					  final double c, final long seed,
					  final MctsNegamaxVisitor<S, A> visitor,
					  final PrintStream log_stream )
	{
		sim_ = sim;
		repr_ = repr;
		action_gen_ = action_gen;
		default_policies_ = default_policies;
		c_ = c;
		rng_ = new MersenneTwister( seed );
		visitor_ = visitor;
		log_stream_ = log_stream;
	}
	
	public UctPolicy( final UndoSimulator<S, A> sim,
					  final Representer<S, F> repr,
					  final ActionGenerator<S, ? extends A> action_gen,
					  final ArrayList<Policy<S, A>> default_policies,
					  final double c, final boolean contingent, final long seed,
					  final MctsNegamaxVisitor<S, A> visitor )
	{
		this( sim, repr, action_gen, default_policies, c, seed, visitor, System.out );
	}
	
	@Override
	public void setState( final S s, final long t )
	{
		s_ = s;
		t_ = t;
	}

	@Override
	public A getAction()
	{
		return getAction( maxControl() );
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName()
	{
		return "UctPolicy";
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
		log.info( "getAction( {} ), player {}", control, sim_.getTurn() );
		final MctsNegamaxVisitor<S, A> time_limit
			= new TimeLimitMctsNegamaxVisitor<S, A>( visitor_, new Countdown( control ) );
		final UctNegamaxSearch<S, F, A> search
			= new UctNegamaxSearch<S, F, A>( sim_, repr_, action_gen_.create(), c_,
									  rng_, default_policies_, time_limit );
		search.run();
		if( log_stream_ != null ) {
			log_stream_.println( "[t = " + t_ + "]" );
			search.printTree( log_stream_ );
		}
		
		if( search.principalVariation() != null && search.principalVariation().actions.get( 0 ) != null ) {
			log.info( "PV: {}", search.principalVariation() );
			return search.principalVariation().actions.get( 0 );
		}
		else {
			log.info( "! Using default policy" );
			final Policy<S, A> pi = default_policies_.get( sim_.getTurn() );
			pi.setState( s_, t_ );
			return pi.getAction();
		}
	}

}
