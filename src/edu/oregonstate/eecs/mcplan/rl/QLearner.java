/**
 * 
 */
package edu.oregonstate.eecs.mcplan.rl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.JointPolicy;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.domains.taxi.PrimitiveTaxiRepresentation;
import edu.oregonstate.eecs.mcplan.domains.taxi.PrimitiveTaxiRepresenter;
import edu.oregonstate.eecs.mcplan.domains.taxi.TaxiAction;
import edu.oregonstate.eecs.mcplan.domains.taxi.TaxiActionGenerator;
import edu.oregonstate.eecs.mcplan.domains.taxi.TaxiSimulator;
import edu.oregonstate.eecs.mcplan.domains.taxi.TaxiState;
import edu.oregonstate.eecs.mcplan.domains.taxi.TaxiWorlds;
import edu.oregonstate.eecs.mcplan.sim.AverageRewardAccumulator;
import edu.oregonstate.eecs.mcplan.sim.Episode;
import edu.oregonstate.eecs.mcplan.util.Fn;
import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

/**
 * A basic Q-learning implementation using constant learning rate and
 * epsilon-greedy exploration.
 * 
 * TODO: Generalize
 * 
 * @author jhostetler
 */
public class QLearner<S extends State, X extends Representation<S>, A extends VirtualConstructor<A>>
	extends Policy<S, A>
{
	public final Map<X, TObjectDoubleMap<A>> values = new HashMap<X, TObjectDoubleMap<A>>();
	
	private final RandomGenerator rng_;
	private final ActionGenerator<S, A> action_gen_;
	
	public final Representer<S, X> repr;
	public final double gamma;
	public final double Vmax;
	public final double epsilon;
	public final double alpha;
	
	private S s_ = null;
	private X x_ = null;
	private A a_ = null;
	private TObjectDoubleMap<A> Qfunction_ = null;
	
	private long t_ = 0L;
	private final int[] turn_;
	
	public QLearner( final int[] turn, final RandomGenerator rng, final Representer<S, X> repr,
					 final ActionGenerator<S, A> action_gen,
					 final double gamma, final double Vmax, final double epsilon, final double alpha )
	{
		turn_ = turn;
		rng_ = rng;
		action_gen_ = action_gen;
		
		this.repr = repr;
		this.gamma = gamma;
		this.Vmax = Vmax;
		this.epsilon = epsilon;
		this.alpha = alpha;
	}
	
	private TObjectDoubleMap<A> getQFunction( final S s, final X x, final long t, final int[] turn )
	{
		TObjectDoubleMap<A> Qfunction = values.get( x );
		if( Qfunction == null ) {
			Qfunction = new TObjectDoubleHashMap<A>();
			action_gen_.setState( s, t, turn );
			while( action_gen_.hasNext() ) {
				Qfunction.put( action_gen_.next(), Vmax );
			}
			values.put( x, Qfunction );
		}
		return Qfunction;
	}
	
	private A maxA( final TObjectDoubleMap<A> Qfunction )
	{
		double best = -Double.MAX_VALUE;
		A best_a = null;
		final TObjectDoubleIterator<A> itr = Qfunction.iterator();
		while( itr.hasNext() ) {
			itr.advance();
			final double d = itr.value();
			if( d > best ) {
				best = d;
				best_a = itr.key();
			}
		}
		return best_a;
	}
	
	private double maxQ( final TObjectDoubleMap<A> Qfunction )
	{
		double best = -Double.MAX_VALUE;
		final TObjectDoubleIterator<A> itr = Qfunction.iterator();
		while( itr.hasNext() ) {
			itr.advance();
			final double d = itr.value();
			if( d > best ) {
				best = d;
			}
		}
		return best;
	}

	private double learningRate( final S s, final A a )
	{
		return alpha;
	}

	@Override
	public void setState( final S s, final long t )
	{
		s_ = s;
		t_ = t;
		x_ = repr.encode( s );
		Qfunction_ = getQFunction( s_, x_, t_, turn_ );
	}

	@Override
	public A getAction()
	{
		final double r = rng_.nextDouble();
		
		if( r < epsilon ) {
			action_gen_.setState( s_, t_, turn_ );
			a_ = Fn.uniform_choice( rng_, action_gen_ );
		}
		else {
			a_ = maxA( Qfunction_ ).create();
		}
		
		return a_;
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		final X xprime = repr.encode( sprime );
		// FIXME: We can't know 'tprime' in general since we don't know how
		// many other agents there are. As of [2014/06/23], we have no
		// nonstationary domains, so it doesn't matter.
		final TObjectDoubleMap<A> Qfunction_prime = getQFunction( sprime, xprime, 0xDEADBEEF, turn_ );
		
		final double Qa = Qfunction_.get( a_ );
		final double max_q = maxQ( Qfunction_prime );
		final double err = r[0] + gamma*max_q - Qa;
		final double old = Qfunction_.get( a_ );
		Qfunction_.put( a_, old + learningRate( s_, a_ ) * err );
	}

	@Override
	public String getName()
	{
		return "QLearning";
	}

	@Override
	public int hashCode()
	{
		return QLearner.class.hashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		return obj instanceof QLearner;
	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] argv )
	{
		final RandomGenerator rng = new MersenneTwister( 43 );
		
		final int Nother_taxis = 0;
		final TaxiState state_prototype = TaxiWorlds.dietterich2000( Nother_taxis );
		final double slip = 0.0;
		final int T = 100000;
		
		final double gamma = 0.9;
		final double Vmax = 20.0;
		final double epsilon = 0.1;
		final double alpha = 0.1;
		final QLearner<TaxiState, PrimitiveTaxiRepresentation, TaxiAction> learner
			= new QLearner<TaxiState, PrimitiveTaxiRepresentation, TaxiAction>(
				new int[] { 0 }, rng, new PrimitiveTaxiRepresenter( state_prototype ), new TaxiActionGenerator(),
				gamma, Vmax, epsilon, alpha );
		
//		final int scale = 20;
//		final TaxiVisualization vis = new TaxiVisualization( null, state_prototype.topology, state_prototype.locations, scale );
//		final EpisodeListener<TaxiState, TaxiAction> updater = vis.updater( 0 );
		
		final AverageRewardAccumulator<TaxiState, TaxiAction> avg = new AverageRewardAccumulator<TaxiState, TaxiAction>( 1 );
		final double lag = -Double.MAX_VALUE;
		
		final Map<PrimitiveTaxiRepresentation, TObjectDoubleMap<TaxiAction>> old_values
			= new HashMap<PrimitiveTaxiRepresentation, TObjectDoubleMap<TaxiAction>>();
		
		int ns = 500;
		for( int i = 0; i < Nother_taxis; ++i ) {
			ns *= 25 - i - 1;
		}
		final int Nstates = ns;
		
		int count = 0;
		while( true ) {
			final TaxiState state = TaxiWorlds.dietterich2000( Nother_taxis );
			final TaxiSimulator sim = new TaxiSimulator( rng, state, slip, T );
			final Episode<TaxiState, TaxiAction> episode
				= new Episode<TaxiState, TaxiAction>( sim, JointPolicy.create( learner ), T );
			episode.addListener( avg );
//			episode.addListener( updater );
	//		episode.addListener( new LoggingEpisodeListener<TaxiState, TaxiAction>() );
			episode.run();
			
//			final double diff = Math.abs( avg.reward[0].mean() - lag );
			
//			System.out.println( "Episode " + count + ": avg reward = " + avg.reward[0].mean() );
			
			count += 1;
			
			if( (count % 10000 == 0) && learner.values.size() == Nstates ) {
//				System.out.println( "learner.values.size() == " + Nstates );
				boolean complete = true;
				double norm = 0.0;
				for( final Map.Entry<PrimitiveTaxiRepresentation, TObjectDoubleMap<TaxiAction>> e : learner.values.entrySet() ) {
					final TObjectDoubleMap<TaxiAction> new_q = e.getValue();
					TObjectDoubleMap<TaxiAction> old_q = old_values.get( e.getKey() );
					if( old_q == null ) {
						old_q = new TObjectDoubleHashMap<TaxiAction>();
						old_values.put( e.getKey(), old_q );
						complete = false;
					}
					final TObjectDoubleIterator<TaxiAction> itr = new_q.iterator();
					while( itr.hasNext() ) {
						itr.advance();
						final TaxiAction a = itr.key();
						final double new_qa = itr.value();
						final double old_qa = old_q.get( a );
						final double diff = new_qa - old_qa;
						norm += diff*diff;
						old_q.put( a, new_qa );
					}
				}
				System.out.println( "Qnorm = " + norm );
				if( complete && norm < 1e-6 ) {
					break;
				}
			}
		}
		
	}
}
