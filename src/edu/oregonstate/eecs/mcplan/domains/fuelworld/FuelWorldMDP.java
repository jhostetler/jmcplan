/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.fuelworld;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.math3.distribution.AbstractIntegerDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import edu.oregonstate.eecs.mcplan.ActionSpace;
import edu.oregonstate.eecs.mcplan.JointPolicy;
import edu.oregonstate.eecs.mcplan.MarkovDecisionProblem;
import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.StateSpace;
import edu.oregonstate.eecs.mcplan.abstraction.WekaUtil;
import edu.oregonstate.eecs.mcplan.dp.SparseValueIterationSolver;
import edu.oregonstate.eecs.mcplan.sim.Episode;
import edu.oregonstate.eecs.mcplan.sim.RewardAccumulator;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.Generator;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;
import gnu.trove.list.TIntList;

/**
 * @author jhostetler
 *
 */
public class FuelWorldMDP extends MarkovDecisionProblem<FuelWorldState, FuelWorldAction>
{
	public final FuelWorldState s0;
	
	private final FuelWorldStateSpace S_;
	private final FuelWorldActionSpace A_;
	
	public FuelWorldMDP( final FuelWorldState s0 )
	{
		this.s0 = s0;
		S_ = new FuelWorldStateSpace( s0 );
		A_ = new FuelWorldActionSpace( s0 );
	}
	
	@Override
	public StateSpace<FuelWorldState> S()
	{
		return S_;
	}

	@Override
	public ActionSpace<FuelWorldState, FuelWorldAction> A()
	{
		return A_;
	}

	@Override
	public Pair<ArrayList<FuelWorldState>, ArrayList<Double>> sparseP( final FuelWorldState s, final FuelWorldAction a )
	{
		final ArrayList<FuelWorldState> succ = new ArrayList<FuelWorldState>();
		final ArrayList<Double> p = new ArrayList<Double>();
		
		if( a instanceof MoveAction ) {
			// Add expected running out of fuel cost
			final MoveAction move = (MoveAction) a;
			
			final AbstractIntegerDistribution f = move.getFuelConsumption( s );
			
			for( int i = 0; i <= s.fuel; ++i ) {
				final double pi = f.probability( i );
				if( pi > 0 ) {
					final FuelWorldState si = new FuelWorldState( s.rng, s.adjacency, s.goal, s.fuel_depots );
					si.location = move.dest;
					si.fuel = s.fuel - i;
					succ.add( si );
					p.add( pi );
				}
			}
			
			// 1.0 - P(consumption <= fuel)
			final double p_empty = 1.0 - f.cumulativeProbability( s.fuel );
			final FuelWorldState empty = new FuelWorldState( s.rng, s.adjacency, s.goal, s.fuel_depots );
			empty.fuel = 0;
			empty.location = s.goal;
			succ.add( empty );
			p.add( p_empty );
		}
		else if( a instanceof RefuelAction ) {
			assert( s.fuel_depots.contains( s.location ) );
			final FuelWorldState sprime = new FuelWorldState( s.rng, s.adjacency, s.goal, s.fuel_depots );
			sprime.fuel = s.fuel_capacity;
			sprime.location = s.location;
			succ.add( sprime );
			p.add( 1.0 );
		}
		
		return Pair.makePair( succ, p );
	}

	@Override
	public double[] P( final FuelWorldState s, final FuelWorldAction a )
	{
		throw new UnsupportedOperationException( "Use sparseP()" );
	}

	@Override
	public double P( final FuelWorldState s, final FuelWorldAction a, final FuelWorldState sprime )
	{
		throw new UnsupportedOperationException( "Use sparseP()" );
	}
	
	@Override
	public double R( final FuelWorldState s )
	{
		return 0;
	}

	@Override
	public double R( final FuelWorldState s, final FuelWorldAction a )
	{
		// All actions cost -1
		double r = -1.0;
		
		if( a instanceof MoveAction ) {
			// Add expected running out of fuel cost
			final MoveAction move = (MoveAction) a;
			final AbstractIntegerDistribution f = move.getFuelConsumption( s );
			// 1.0 - P(consumption <= fuel)
			final double p_empty = 1.0 - f.cumulativeProbability( s.fuel );
			r -= p_empty*100.0;
			
			if( move.dest == s.goal ) {
				r += (1.0 - p_empty)*20.0;
			}
		}
		
//		if( s.location == 24 ) {
//			System.out.print( a );
//			System.out.print( ": " );
//			System.out.println( r );
//		}
		
		return r;
	}

	// -----------------------------------------------------------------------
	
	public static void main( final String[] argv )
	{
		final RandomGenerator rng = new MersenneTwister( 42 );
		final double discount = 0.99;
		final boolean choices = true;
		final FuelWorldState template;
		if( choices ) {
			template = FuelWorldState.createDefaultWithChoices( rng );
		}
		else {
			template = FuelWorldState.createDefault( rng );
		}
		
		for( int i = 0; i < template.adjacency.size(); ++i ) {
			System.out.print( i );
			System.out.print( " -> {" );
			final TIntList succ = template.adjacency.get( i );
			for( int j = 0; j < succ.size(); ++j ) {
				System.out.print( " " + succ.get( j ) );
			}
			System.out.println( " }" );
		}
		
		final FuelWorldMDP mdp = new FuelWorldMDP( template );
		final int Nfeatures = new PrimitiveFuelWorldRepresentation( template ).phi().length;
		final SparseValueIterationSolver<FuelWorldState, FuelWorldAction> vi
			= new SparseValueIterationSolver<FuelWorldState, FuelWorldAction>( mdp, discount );
		vi.run();
		
		final PrimitiveFuelWorldRepresenter repr = new PrimitiveFuelWorldRepresenter();
		final ArrayList<Attribute> attr = new ArrayList<Attribute>();
		attr.addAll( repr.attributes() );
		attr.add( WekaUtil.createNominalAttribute( "__label__", mdp.A().cardinality() ) );
		final Instances instances = WekaUtil.createEmptyInstances(
			"fuelworld" + (choices ? "_choices" : "") + "_pistar", attr );
		final Policy<FuelWorldState, FuelWorldAction> pistar = vi.pistar();
		final Generator<FuelWorldState> g = mdp.S().generator();
		while( g.hasNext() ) {
			final FuelWorldState s = g.next();
			if( s.location == s.goal ) {
				continue;
			}
			pistar.setState( s, 0L );
			final FuelWorldAction astar = pistar.getAction();
			System.out.println( "" + s + " -> " + astar );
			final double[] phi = new double[Nfeatures + 1];
			Fn.memcpy( phi, new PrimitiveFuelWorldRepresentation( s ).phi(), Nfeatures );
			phi[Nfeatures] = mdp.A().index( astar );
			WekaUtil.addInstance( instances, new DenseInstance( 1.0, phi ) );
		}
		
		WekaUtil.writeDataset( new File( "." ), instances );
		
		final MeanVarianceAccumulator ret = new MeanVarianceAccumulator();
		final MeanVarianceAccumulator steps = new MeanVarianceAccumulator();
		final int Ngames = 100000;
		for( int i = 0; i < Ngames; ++i ) {
			final FuelWorldState s0 = FuelWorldState.createDefaultWithChoices( rng );
			final FuelWorldSimulator sim = new FuelWorldSimulator( s0 );
			
			final Episode<FuelWorldState, FuelWorldAction> episode
				= new Episode<FuelWorldState, FuelWorldAction>( sim, JointPolicy.create( pistar ) );
			final RewardAccumulator<FuelWorldState, FuelWorldAction> racc
				= new RewardAccumulator<FuelWorldState, FuelWorldAction>( sim.nagents(), discount );
			episode.addListener( racc );
			
			final long tstart = System.nanoTime();
			episode.run();
			final long tend = System.nanoTime();
			final double elapsed_ms = (tend - tstart) * 1e-6;
			
			ret.add( racc.v()[0] );
			steps.add( racc.steps() );
		}
		
		System.out.println( "****************************************" );
		System.out.println( "Average return: " + ret.mean() );
		System.out.println( "Return variance: " + ret.variance() );
		System.out.println( "Confidence: " + ret.confidence() );
		System.out.println( "Steps (mean): " + steps.mean() );
		System.out.println( "Steps (var): " + steps.variance() );
	}
}
