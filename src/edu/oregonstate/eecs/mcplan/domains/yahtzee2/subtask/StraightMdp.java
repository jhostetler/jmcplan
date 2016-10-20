/* LICENSE
Copyright (c) 2013-2016, Jesse Hostetler (jessehostetler@gmail.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2.subtask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import edu.oregonstate.eecs.mcplan.ActionSpace;
import edu.oregonstate.eecs.mcplan.MarkovDecisionProblem;
import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.StateSpace;
import edu.oregonstate.eecs.mcplan.abstraction.WekaUtil;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.Hand;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.KeepAction;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.YahtzeeAction;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.YahtzeeScores;
import edu.oregonstate.eecs.mcplan.dp.SparseValueIterationSolver;
import edu.oregonstate.eecs.mcplan.util.Csv;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.Generator;
import edu.oregonstate.eecs.mcplan.util.ValueType;

/**
 * @author jhostetler
 *
 */
public class StraightMdp extends MarkovDecisionProblem<YahtzeeDiceState, YahtzeeAction>
{
	public final boolean small;
	
	private final YahtzeeSubtaskStateSpace S = new YahtzeeSubtaskStateSpace();
	private final YahtzeeSubtaskActionSpace A = new YahtzeeSubtaskActionSpace();
	
	public StraightMdp( final boolean small )
	{
		this.small = small;
	}
	
	@Override
	public StateSpace<YahtzeeDiceState> S()
	{
		return S;
	}

	@Override
	public ActionSpace<YahtzeeDiceState, YahtzeeAction> A()
	{
		return A;
	}

	@Override
	public Pair<ArrayList<YahtzeeDiceState>, ArrayList<Double>> sparseP(
			final YahtzeeDiceState s, final YahtzeeAction a )
	{
		final KeepAction ka = (KeepAction) a;
		final int Nrerolls = Hand.Ndice - ka.Nkeepers;
		final Fn.MultinomialTermGenerator g = new Fn.MultinomialTermGenerator( Nrerolls, Hand.Nfaces );
		
		final ArrayList<YahtzeeDiceState> ss = new ArrayList<YahtzeeDiceState>();
		final ArrayList<Double> P = new ArrayList<Double>();
		
		final double Z = Math.pow( Hand.Nfaces, Nrerolls );
		
		double sum = 0;
		while( g.hasNext() ) {
			final int[] k = g.next();
			final double c = Fn.multinomialCoefficient( Nrerolls, k );
			Fn.vplus_inplace( k, ka.keepers );
			ss.add( new YahtzeeDiceState( new Hand( k ), s.rerolls - 1 ) );
			final double p = c / Z;
			sum += p;
			P.add( p );
		}
		P.set( P.size() - 1, P.get( P.size() - 1 ) + (1.0 - sum) ); // Ensure P is a proper distribution
		
		return Pair.makePair( ss, P );
	}

	@Override
	public double[] P( final YahtzeeDiceState s, final YahtzeeAction a )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double P( final YahtzeeDiceState s, final YahtzeeAction a, final YahtzeeDiceState sprime )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double R( final YahtzeeDiceState s )
	{
		if( s.isTerminal() ) {
			if( small ) {
				if( YahtzeeScores.SmallStraight.isSatisfiedBy( s.hand ) ) {
					return YahtzeeScores.SmallStraight.score( s.hand );
				}
			}
			else {
				if( YahtzeeScores.LargeStraight.isSatisfiedBy( s.hand ) ) {
					return YahtzeeScores.LargeStraight.score( s.hand );
				}
			}
			
			// Small shaping reward to encourage tie-breaking in favor of
			// larger dice values.
			return s.hand.sum() / 100.0;
//			return 0;
		}
		else {
			return 0;
		}
	}

	@Override
	public double R( final YahtzeeDiceState s, final YahtzeeAction a )
	{
		return 0;
	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] argv ) throws FileNotFoundException
	{
//		final StraightMdp M = new StraightMdp( false );
//		final YahtzeeDiceState s = new YahtzeeDiceState( new Hand( new int[] { 0, 2, 2, 1, 0, 0 } ), 1 );
//		final KeepAction a = new KeepAction( new int[] { 0, 1, 1, 0, 0, 0 } );
//
//		final Pair<ArrayList<YahtzeeDiceState>, ArrayList<Double>> P = M.sparseP( s, a );
//
//		for( int i = 0; i < P.first.size(); ++i ) {
//			System.out.println( P.first.get( i ) + " (" + P.second.get( i ) + ")" );
//		}
		
		
		
		final RandomGenerator rng = new MersenneTwister( 42 );
		final double discount = 1.0;
		
		final boolean small = false;
		final StraightMdp mdp = new StraightMdp( small );
		final int Nfeatures = Hand.Nfaces + 1; // +1 for rerolls
		final SparseValueIterationSolver<YahtzeeDiceState, YahtzeeAction> vi
			= new SparseValueIterationSolver<YahtzeeDiceState, YahtzeeAction>( mdp, discount, 1e-16 );
		vi.run();
		
		final ArrayList<Attribute> attr = new ArrayList<Attribute>();
		attr.addAll( YahtzeeSubtaskStateSpace.attributes() );
		attr.add( WekaUtil.createNominalAttribute( "__label__", mdp.A().cardinality() ) );
		final Instances instances = WekaUtil.createEmptyInstances(
			"yahtzee_straight_" + (small ? "small" : "large") + "_pistar", attr );
		final Policy<YahtzeeDiceState, YahtzeeAction> pistar = vi.pistar();
		final Generator<YahtzeeDiceState> g = mdp.S().generator();
		while( g.hasNext() ) {
			final YahtzeeDiceState s = g.next();
			if( s.isTerminal() ) {
				continue;
			}
			pistar.setState( s, 0L );
			final YahtzeeAction astar = pistar.getAction();
			System.out.println( "" + s + " -> " + astar );
			final double[] phi = new double[Nfeatures + 1];
			int idx = 0;
			for( int i = 0; i < Hand.Nfaces; ++i ) {
				phi[idx++] = s.hand.dice[i];
			}
			phi[idx++] = s.rerolls;
			phi[Nfeatures] = mdp.A().index( astar );
			WekaUtil.addInstance( instances, new DenseInstance( 1.0, phi ) );
		}
		
		WekaUtil.writeDataset( new File( "." ), instances );
		
		final Csv.Writer csv = new Csv.Writer( new PrintStream(
				new FileOutputStream( new File( instances.relationName() + "_action-key.csv" ) ) ) );
		for( final Map.Entry<ValueType<int[]>, Integer> e : YahtzeeSubtaskActionSpace.index_map.entrySet() ) {
			csv.cell( e.getValue() ).cell( new KeepAction( e.getKey().get() ) ).newline();
		}
		
//		final MeanVarianceAccumulator ret = new MeanVarianceAccumulator();
//		final MeanVarianceAccumulator steps = new MeanVarianceAccumulator();
//		final int Ngames = 100000;
//		for( int i = 0; i < Ngames; ++i ) {
//			final FuelWorldState s0;
//			if( choices ) {
//				s0 = FuelWorldState.createDefaultWithChoices( rng );
//			}
//			else {
//				s0 = FuelWorldState.createDefault( rng );
//			}
//			final FuelWorldSimulator sim = new FuelWorldSimulator( s0 );
//
//			final Episode<FuelWorldState, FuelWorldAction> episode
//				= new Episode<FuelWorldState, FuelWorldAction>( sim, JointPolicy.create( pistar ) );
//			final RewardAccumulator<FuelWorldState, FuelWorldAction> racc
//				= new RewardAccumulator<FuelWorldState, FuelWorldAction>( sim.nagents(), discount );
//			episode.addListener( racc );
//
//			final long tstart = System.nanoTime();
//			episode.run();
//			final long tend = System.nanoTime();
//			final double elapsed_ms = (tend - tstart) * 1e-6;
//
//			ret.add( racc.v()[0] );
//			steps.add( racc.steps() );
//		}
//
//		System.out.println( "****************************************" );
//		System.out.println( "Average return: " + ret.mean() );
//		System.out.println( "Return variance: " + ret.variance() );
//		System.out.println( "Confidence: " + ret.confidence() );
//		System.out.println( "Steps (mean): " + steps.mean() );
//		System.out.println( "Steps (var): " + steps.variance() );
	}

}
