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
package edu.oregonstate.eecs.mcplan.abstraction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.CholeskyDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.correlation.StorelessCovariance;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import weka.classifiers.Classifier;
import weka.classifiers.meta.LogitBoost;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.JointPolicy;
import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.QFunction;
import edu.oregonstate.eecs.mcplan.QGreedyPolicy;
import edu.oregonstate.eecs.mcplan.RandomPolicy;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.SingleAgentJointActionGenerator;
import edu.oregonstate.eecs.mcplan.SingleAgentPolicyAdapter;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.TrivialRepresenter;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.domains.blackjack.BlackjackAction;
import edu.oregonstate.eecs.mcplan.domains.blackjack.BlackjackActionGenerator;
import edu.oregonstate.eecs.mcplan.domains.blackjack.BlackjackParameters;
import edu.oregonstate.eecs.mcplan.domains.blackjack.BlackjackPrimitiveRepresenter;
import edu.oregonstate.eecs.mcplan.domains.blackjack.BlackjackSimulator;
import edu.oregonstate.eecs.mcplan.domains.blackjack.BlackjackState;
import edu.oregonstate.eecs.mcplan.domains.cards.Deck;
import edu.oregonstate.eecs.mcplan.domains.cards.InfiniteDeck;
import edu.oregonstate.eecs.mcplan.domains.frogger.FroggerAction;
import edu.oregonstate.eecs.mcplan.domains.frogger.FroggerActionGenerator;
import edu.oregonstate.eecs.mcplan.domains.frogger.FroggerParameters;
import edu.oregonstate.eecs.mcplan.domains.frogger.FroggerSimulator;
import edu.oregonstate.eecs.mcplan.domains.frogger.FroggerState;
import edu.oregonstate.eecs.mcplan.domains.frogger.LanesToGoHeuristic;
import edu.oregonstate.eecs.mcplan.domains.frogger.RelativeFroggerRepresenter;
import edu.oregonstate.eecs.mcplan.domains.fuelworld.FuelWorldAction;
import edu.oregonstate.eecs.mcplan.domains.fuelworld.FuelWorldActionGenerator;
import edu.oregonstate.eecs.mcplan.domains.fuelworld.FuelWorldSimulator;
import edu.oregonstate.eecs.mcplan.domains.fuelworld.FuelWorldState;
import edu.oregonstate.eecs.mcplan.domains.fuelworld.PrimitiveFuelWorldRepresenter;
import edu.oregonstate.eecs.mcplan.domains.racegrid.PrimitiveRacegridRepresenter;
import edu.oregonstate.eecs.mcplan.domains.racegrid.RacegridAction;
import edu.oregonstate.eecs.mcplan.domains.racegrid.RacegridActionGenerator;
import edu.oregonstate.eecs.mcplan.domains.racegrid.RacegridCircuits;
import edu.oregonstate.eecs.mcplan.domains.racegrid.RacegridSimulator;
import edu.oregonstate.eecs.mcplan.domains.racegrid.RacegridState;
import edu.oregonstate.eecs.mcplan.domains.racegrid.RacegridVisualization;
import edu.oregonstate.eecs.mcplan.domains.racegrid.ShortestPathHeuristic;
import edu.oregonstate.eecs.mcplan.domains.racegrid.TerrainType;
import edu.oregonstate.eecs.mcplan.domains.racetrack.Circuit;
import edu.oregonstate.eecs.mcplan.domains.racetrack.Circuits;
import edu.oregonstate.eecs.mcplan.domains.racetrack.PrimitiveRacetrackRepresenter;
import edu.oregonstate.eecs.mcplan.domains.racetrack.RacetrackAction;
import edu.oregonstate.eecs.mcplan.domains.racetrack.RacetrackActionGenerator;
import edu.oregonstate.eecs.mcplan.domains.racetrack.RacetrackSimulator;
import edu.oregonstate.eecs.mcplan.domains.racetrack.RacetrackState;
import edu.oregonstate.eecs.mcplan.domains.racetrack.SectorEvaluator;
import edu.oregonstate.eecs.mcplan.domains.tamarisk.IndicatorTamariskRepresenter;
import edu.oregonstate.eecs.mcplan.domains.tamarisk.TamariskAction;
import edu.oregonstate.eecs.mcplan.domains.tamarisk.TamariskActionGenerator;
import edu.oregonstate.eecs.mcplan.domains.tamarisk.TamariskParameters;
import edu.oregonstate.eecs.mcplan.domains.tamarisk.TamariskSimulator;
import edu.oregonstate.eecs.mcplan.domains.tamarisk.TamariskState;
import edu.oregonstate.eecs.mcplan.domains.taxi.PrimitiveTaxiRepresenter;
import edu.oregonstate.eecs.mcplan.domains.taxi.TaxiAction;
import edu.oregonstate.eecs.mcplan.domains.taxi.TaxiActionGenerator;
import edu.oregonstate.eecs.mcplan.domains.taxi.TaxiSimulator;
import edu.oregonstate.eecs.mcplan.domains.taxi.TaxiState;
import edu.oregonstate.eecs.mcplan.domains.taxi.TaxiWorlds;
import edu.oregonstate.eecs.mcplan.domains.toy.ChainWalk;
import edu.oregonstate.eecs.mcplan.domains.toy.CliffWorld;
import edu.oregonstate.eecs.mcplan.domains.toy.Irrelevance;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.PrimitiveYahtzeeRepresenter;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.ReprWrapper;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.YahtzeeAction;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.YahtzeePhasedActionGenerator;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.YahtzeeSimulator;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.YahtzeeState;
import edu.oregonstate.eecs.mcplan.ensemble.MultiAbstractionUctSearch;
import edu.oregonstate.eecs.mcplan.ensemble.VotingPolicyEnsemble;
import edu.oregonstate.eecs.mcplan.ml.HilbertSpace;
import edu.oregonstate.eecs.mcplan.ml.InformationTheoreticMetricLearner;
import edu.oregonstate.eecs.mcplan.ml.MatrixAlgorithms;
import edu.oregonstate.eecs.mcplan.ml.Memorizer;
import edu.oregonstate.eecs.mcplan.ml.MetricConstrainedKMeans;
import edu.oregonstate.eecs.mcplan.ml.SimilarityFunction;
import edu.oregonstate.eecs.mcplan.ml.VoronoiClassifier;
import edu.oregonstate.eecs.mcplan.rddl.RDDLAction;
import edu.oregonstate.eecs.mcplan.rddl.RDDLState;
import edu.oregonstate.eecs.mcplan.rddl.RddlActionGenerator;
import edu.oregonstate.eecs.mcplan.rddl.RddlRepresenter;
import edu.oregonstate.eecs.mcplan.rddl.RddlSimulatorAdapter;
import edu.oregonstate.eecs.mcplan.rddl.RddlSpec;
import edu.oregonstate.eecs.mcplan.search.ActionNode;
import edu.oregonstate.eecs.mcplan.search.AggregatingActionNode;
import edu.oregonstate.eecs.mcplan.search.BackupRules;
import edu.oregonstate.eecs.mcplan.search.DefaultMctsVisitor;
import edu.oregonstate.eecs.mcplan.search.EvaluationFunction;
import edu.oregonstate.eecs.mcplan.search.GameTreeVisitor;
import edu.oregonstate.eecs.mcplan.search.LazyAggregatingActionNode;
import edu.oregonstate.eecs.mcplan.search.MctsVisitor;
import edu.oregonstate.eecs.mcplan.search.MutableActionNode;
import edu.oregonstate.eecs.mcplan.search.RolloutEvaluator;
import edu.oregonstate.eecs.mcplan.search.SimpleMutableActionNode;
import edu.oregonstate.eecs.mcplan.search.StateNode;
import edu.oregonstate.eecs.mcplan.search.TreeStatisticsRecorder;
import edu.oregonstate.eecs.mcplan.search.UTreeSearch;
import edu.oregonstate.eecs.mcplan.search.UctSearch;
import edu.oregonstate.eecs.mcplan.sim.Episode;
import edu.oregonstate.eecs.mcplan.sim.EpisodeListener;
import edu.oregonstate.eecs.mcplan.sim.ResetAdapter;
import edu.oregonstate.eecs.mcplan.sim.ResetSimulator;
import edu.oregonstate.eecs.mcplan.sim.RewardAccumulator;
import edu.oregonstate.eecs.mcplan.sim.Simulator;
import edu.oregonstate.eecs.mcplan.util.Csv;
import edu.oregonstate.eecs.mcplan.util.Csv.Writer;
import edu.oregonstate.eecs.mcplan.util.CsvConfigurationParser;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.KeyValueStore;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;
import edu.oregonstate.eecs.mcplan.util.MinMaxAccumulator;
import edu.oregonstate.eecs.mcplan.util.QuantileAccumulator;
import edu.oregonstate.eecs.mcplan.util.ReservoirSampleAccumulator;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntProcedure;
import hr.irb.fastRandomForest.FastRandomForest;

/**
 * @author jhostetler
 *
 */
public class Experiments
{
	/**
	 * Accumulates states in which the optimal action is "known".
	 * 
	 * TODO: The 'X' parameter seems to be superfluous, as nobody is using
	 * the 'states_' member.
	 *
	 * @param <S>
	 * @param <X>
	 * @param <A>
	 */
	public static class SolvedStateAccumulator<S, X extends FactoredRepresentation<S>,
											   A extends VirtualConstructor<A>>
		implements EpisodeListener<S, A>
	{
		public ArrayList<X> states_ = new ArrayList<X>();
		public ArrayList<double[]> Phi_ = new ArrayList<double[]>();
		public ArrayList<A> actions_ = new ArrayList<A>();
		
		private final Representer<S, X> repr_;
		private X x_ = null;
		
		public SolvedStateAccumulator( final Representer<S, X> repr )
		{
			repr_ = repr;
		}
		
		@Override
		public <P extends Policy<S, JointAction<A>>>
		void startState( final S s, final double[] r, final P pi )
		{
			x_ = repr_.encode( s );
		}

		@Override
		public void preGetAction()
		{ }

		@Override
		public void postGetAction( final JointAction<A> a )
		{
			states_.add( x_ );
			Phi_.add( Arrays.copyOf( x_.phi(), x_.phi().length ) );
			actions_.add( a.get( 0 ).create() );
		}

		@Override
		public void onActionsTaken( final S sprime, final double[] r )
		{ x_ = repr_.encode( sprime ); }

		@Override
		public void endState( final S s )
		{ }
		
	}
	
	public static class SolvedStateGapRecorder<X, A extends VirtualConstructor<A>> implements GameTreeVisitor<X, A>
	{
		public ArrayList<A> labels = new ArrayList<A>();
		public TDoubleArrayList gaps = new TDoubleArrayList();

		@Override
		public void visit( final StateNode<X, A> s )
		{
			labels.add( BackupRules.MaxAction( s ).a( 0 ) );
			gaps.add( calculateGap( s ) );
		}

		@Override
		public void visit( final ActionNode<X, A> a )
		{
			throw new AssertionError();
		}
	}
	
	/**
	 * Collects unlabeled state vectors. These are taken from the first level
	 * of the tree (below the root), so that they are different from the
	 * states collected by SolvedStateAccumulator.
	 *
	 * @param <S>
	 * @param <A>
	 */
	public static class UnlabeledStateAccumulator<S, A extends VirtualConstructor<A>>
		extends DefaultMctsVisitor<S, A>
	{
		private final int max_samples_;
		private final Representer<S, ? extends FactoredRepresentation<S>> base_repr_;
		private boolean sample_ = false;
		
		public final ReservoirSampleAccumulator<double[]> unlabeled;
		
		public UnlabeledStateAccumulator( final RandomGenerator rng, final int max_samples,
										  final Representer<S, ? extends FactoredRepresentation<S>> base_repr )
		{
			max_samples_ = max_samples;
			base_repr_ = base_repr;
			unlabeled = new ReservoirSampleAccumulator<double[]>( rng, max_samples_ );
		}
		
		@Override
		public void startEpisode( final S s, final int nagents, final int[] turn )
		{
			sample_ = true;
		}
		
		@Override
		public void treeAction( final JointAction<A> a, final S sprime, final int[] next_turn )
		{
			if( sample_ ) {
				sample_ = false;
				if( unlabeled.acceptNext() ) {
					unlabeled.addPending( base_repr_.encode( sprime ).phi() );
				}
			}
		}
	}
	
	// -----------------------------------------------------------------------
	
	/**
	 * Constructs an InformationTheoreticMetricLearner from a set of labeled
	 * state vector differences.
	 * 
	 * @param config
	 * @param A0
	 * @param XL A labeled set of state vector differences. The label must be
	 * the last attribute, and it must be 1 if the states are similar and 0
	 * if they are not.
	 * @return
	 */
	private static <A> InformationTheoreticMetricLearner learnMetric(
		final Configuration config, final RealMatrix A0, final Instances XL )
	{
		final int d = XL.numAttributes() - 1; //XL.get( 0 ).getDimension();
		System.out.println( "d = " + d );
		final double u;
		final double ell;
		final double gamma = config.getDouble( "itml.gamma" );
		// We will set 'ell' and 'u' using sample quantiles as described in
		// the ITML paper.
		final QuantileAccumulator qacc = new QuantileAccumulator( 0.05, 0.95 );
		
		final ArrayList<double[]> S = new ArrayList<double[]>();
		final ArrayList<double[]> D = new ArrayList<double[]>();
		for( int i = 0; i < XL.size(); ++i ) {
			final Instance ii = XL.get( i );
			final double diff[] = new double[d];
			for( int j = 0; j < d; ++j ) {
				diff[j] = ii.value( j );
			}
			
			if( ii.classValue() == 0.0 ) {
				D.add( diff );
			}
			else {
				S.add( diff );
			}
			
			qacc.add( Math.sqrt( HilbertSpace.inner_prod( diff, A0, diff ) ) );
		}
		// Set bounds to quantile esimates
		ell = qacc.estimates[0];
		u = qacc.estimates[1];
		System.out.println( "ITML: ell = " + ell );
		System.out.println( "ITML: u = " + u );
		
		final InformationTheoreticMetricLearner itml
			= new InformationTheoreticMetricLearner( S, D, u, ell, A0, gamma, config.rng );
		itml.run();
		return itml;
	}
	
	// -----------------------------------------------------------------------
	
	private static <A> MetricConstrainedKMeans makeClustering(
		final Configuration config, final RealMatrix A0, final Instances instances,
		final ArrayList<RealVector> XU, final boolean with_metric_learning )
	{
		final int K = config.getInt( "cluster.k" );
		final int d = instances.numAttributes(); //XL.get( 0 ).getDimension();
		final double u;
		final double ell;
		final double gamma = config.getDouble( "itml.gamma" );
		// We will set 'ell' and 'u' using sample quantiles as described in
		// the ITML paper.
		final QuantileAccumulator qacc = new QuantileAccumulator( 0.05, 0.95 );
		
		final ArrayList<RealVector> X = new ArrayList<RealVector>();
		X.addAll( XL );
		X.addAll( XU );
		
		// Must-link and Cannot-link constraints in the form that
		// MetricConstrainedKMeans wants them
		final TIntObjectMap<Pair<int[], double[]>> M = new TIntObjectHashMap<Pair<int[], double[]>>();
		final TIntObjectMap<Pair<int[], double[]>> C = new TIntObjectHashMap<Pair<int[], double[]>>();
		
		for( int i = 0; i < XL.size(); ++i ) {
			final TIntList m = new TIntArrayList();
			final TIntList c = new TIntArrayList();
			for( int j = i + 1; j < XL.size(); ++j ) {
				if( y.get( i ).equals( y.get( j ) ) ) {
					m.add( j );
				}
				else {
					c.add( j );
				}
				qacc.add( Math.sqrt( HilbertSpace.inner_prod( XL.get( i ), A0, XL.get( j ) ) ) );
			}
			M.put( i, Pair.makePair( m.toArray(), Fn.repeat( 1.0, m.size() ) ) );
			C.put( i, Pair.makePair( c.toArray(), Fn.repeat( 1.0, c.size() ) ) );
		}
		// Set bounds to quantile esimates
		ell = qacc.estimates[0];
		u = qacc.estimates[1];
		System.out.println( "ITML: ell = " + ell );
		System.out.println( "ITML: u = " + u );
		
		// Similar pairs for MetricLearner
		final ArrayList<int[]> S = new ArrayList<int[]>();
		M.forEachKey( new TIntProcedure() {
			@Override
			public boolean execute( final int i )
			{
				final Pair<int[], double[]> p = M.get( i );
				if( p != null ) {
					for( final int j : p.first ) {
						S.add( new int[] { i, j } );
					}
				}
				return true;
			}
		} );
		
		// Disimilar pairs for MetricLearner
		final ArrayList<int[]> D = new ArrayList<int[]>();
		C.forEachKey( new TIntProcedure() {
			@Override
			public boolean execute( final int i )
			{
				final Pair<int[], double[]> p = C.get( i );
				if( p != null ) {
					for( final int j : p.first ) {
						D.add( new int[] { i, j } );
					}
				}
				return true;
			}
		} );
		
		final RealMatrix A;
		if( with_metric_learning ) {
			final InformationTheoreticMetricLearner itml = new InformationTheoreticMetricLearner(
				X, S, D, u, ell, A0, gamma, config.rng );
			itml.run();
			A = itml.A();
		}
		else {
			A = A0;
		}
		
		final MetricConstrainedKMeans kmeans = new MetricConstrainedKMeans( K, d, X, A, M, C, config.rng );
		kmeans.run();
		return kmeans;
	}
	
	private static void writeMetric( final File root, final RealMatrix M, final int iter )
	{
		try {
			Csv.write( new PrintStream( new File( root, "M" + iter + ".csv" ) ), M );
		}
		catch( final FileNotFoundException ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	private static void writeClustering( final MetricConstrainedKMeans kmeans, final File root,
										 final int iter )
												 throws FileNotFoundException
	{
		Csv.write( new PrintStream( new File( root, "M" + iter + ".csv" ) ), kmeans.metric );
		{
			final Csv.Writer writer = new Csv.Writer( new PrintStream( new File( root, "mu" + iter + ".csv" ) ) );
			for( int i = 0; i < kmeans.d; ++i ) {
				for( int j = 0; j < kmeans.k; ++j ) {
					writer.cell( kmeans.mu()[j].getEntry( i ) );
				}
				writer.newline();
			}
		}
		// Lt.operate( x ) maps x to the space defined by the metric
		final RealMatrix Lt = new CholeskyDecomposition( kmeans.metric ).getLT();
		{
			final Csv.Writer writer = new Csv.Writer( new PrintStream( new File( root,  "X" + iter + ".csv" ) ) );
			writer.cell( "cluster" ).cell( "label" );
			for( int i = 0; i < kmeans.metric.getColumnDimension(); ++i ) {
				writer.cell( "x" + i );
			}
			for( int i = 0; i < kmeans.metric.getColumnDimension(); ++i ) {
				writer.cell( "Ax" + i );
			}
			writer.newline();
			for( int cluster = 0; cluster < kmeans.k; ++cluster ) {
				for( int i = 0; i < kmeans.N; ++i ) {
					if( kmeans.assignments()[i] == cluster ) {
						writer.cell( cluster );
						final RealVector phi = kmeans.X_.get( i ); //Phi.get( i );
						writer.cell( "?" ); // TODO: write label
						for( int j = 0; j < phi.getDimension(); ++j ) {
							writer.cell( phi.getEntry( j ) );
						}
						final RealVector trans = Lt.operate( phi );
						for( int j = 0; j < trans.getDimension(); ++j ) {
							writer.cell( trans.getEntry( j ) );
						}
						writer.newline();
					}
				}
			}
		}
	}
	
	private static class InstanceGap implements Comparable<InstanceGap>
	{
		public final double gap;
		public final Instance instance;
		
		public InstanceGap( final double gap, final Instance instance )
		{
			this.gap = gap;
			this.instance = instance;
		}

		@Override
		public int compareTo( final InstanceGap that )
		{
			// Order smallest gap first, so that it can be removed from the
			// priority queue efficiently.
			return (int) Math.signum( this.gap - that.gap );
		}
	}
	
	public static <S, A extends VirtualConstructor<A>>
	double calculateGap( final StateNode<S, A> sn )
	{
		double first_value = -Double.MAX_VALUE;
		double second_value = -Double.MAX_VALUE;
		
		for( final ActionNode<S, A> an : Fn.in( sn.successors() ) ) {
			final double q = an.q( 0 );
			if( q > first_value ) {
				second_value = first_value;
				first_value = q;
			}
			else if( q > second_value ) {
				second_value = q;
			}
		}
		
		return first_value - second_value;
	}
	
	public static ArrayList<Attribute> addLabelToAttributes( final ArrayList<Attribute> attr, final int Nlabels )
	{
		final ArrayList<Attribute> result = new ArrayList<Attribute>();
		for( final Attribute a : attr ) {
			result.add( a );
		}
		final ArrayList<String> nominal = new ArrayList<String>();
		for( int i = 0; i < Nlabels; ++i ) {
			nominal.add( Integer.toString( i ) );
		}
		result.add( new Attribute( "__label__", nominal ) );
		return result;
	}
	
	// -----------------------------------------------------------------------
	
	/**
	 * Contains an Instances object containing factored state representations
	 * labeled with an integer action index, and mappings in both directions
	 * between the action and the action index.
	 * @param <A>
	 */
	private static class SingleInstanceDataset<A>
	{
		public final Instances instances;
		public final HashMap<A, Integer> action_to_int;
		public final ArrayList<A> int_to_action;
		public final ArrayList<Pair<ArrayList<A>, TDoubleList>> qtable;
		
		public SingleInstanceDataset( final Instances instances,
									  final HashMap<A, Integer> action_to_int,
									  final ArrayList<A> int_to_action,
									  final ArrayList<Pair<ArrayList<A>, TDoubleList>> qtable )
		{
			this.instances = instances;
			this.action_to_int = action_to_int;
			this.int_to_action = int_to_action;
			this.qtable = qtable;
		}
	}
	
	private static class Dataset<A>
	{
		public Instances single = null;
		public Instances pair = null;
		
		public Dataset()
		{ }
		
		public Dataset( final Instances single, final Instances pair )
		{
			this.single = single;
			this.pair = pair;
		}
	}
	
	// -----------------------------------------------------------------------
	
	/**
	 * Creates a labeled dataset of states pair with optimal actions. Action
	 * labels are represented as indexes into an array list. Mappings in both
	 * directions are also returned.
	 * @param config
	 * @param attributes
	 * @param data
	 * @param labels
	 * @param iter
	 * @return
	 */
	private static <A extends VirtualConstructor<A>>
	SingleInstanceDataset<A> makeSingleInstanceDataset(
		final Configuration config,
		final ArrayList<Attribute> attributes, final ArrayList<double[]> data, final ArrayList<A> labels,
		final ArrayList<Pair<ArrayList<A>, TDoubleList>> qtable, final int iter )
	{
//		System.out.println( "data.size() = " + data.size() );
		final int[] ii = Fn.range( 0, data.size() );
		Fn.shuffle( config.rng, ii );
		
		final HashMap<A, Integer> action_to_int = new HashMap<A, Integer>();
		final ArrayList<A> int_to_action = new ArrayList<A>();
		final ArrayList<Pair<ArrayList<A>, TDoubleList>> abridged_qtable
			= (qtable != null ? new ArrayList<Pair<ArrayList<A>, TDoubleList>>() : null);
		
		final TIntArrayList counts = new TIntArrayList();
		final int max_per_label = config.getInt( "training.max_per_label" );
		final int max_instances = config.getInt( "training.max_single" );
		
		final ArrayList<DenseInstance> instance_list = new ArrayList<DenseInstance>();
		for( int i = 0; i < Math.min( data.size(), max_instances ); ++i ) {
			final int idx = ii[i];
			final A a = labels.get( idx );
			final Integer idx_obj = action_to_int.get( a );
			final int label;
			if( idx_obj == null ) {
//				System.out.println( "\tNew action: " + a );
				label = int_to_action.size();
				int_to_action.add( a );
				action_to_int.put( a, label );
				counts.add( 0 );
			}
			else {
//				System.out.println( "\tRepeat action: " + a );
				label = idx_obj;
			}
			
			final int c = counts.get( label );
			if( max_per_label <= 0 || c < max_per_label ) {
//				System.out.println( "Adding " + label );
				final double[] phi = Fn.append( data.get( idx ), label );
				final DenseInstance instance = new DenseInstance( 1.0, phi );
				instance_list.add( instance );
				counts.set( label, c + 1 );
				if( qtable != null ) {
					abridged_qtable.add( qtable.get( idx ) );
				}
			}
		}
		
		final int Nlabels = int_to_action.size();
		final ArrayList<Attribute> labeled_attributes = addLabelToAttributes( attributes, Nlabels );
		
		final Instances instances = new Instances(
			deriveDatasetName( config.training_data_single, iter ), labeled_attributes, counts.sum() );
		instances.setClassIndex( instances.numAttributes() - 1 );
		for( final DenseInstance instance : instance_list ) {
			instances.add( instance );
			instance.setDataset( instances );
		}
		
		return new SingleInstanceDataset<A>( instances, action_to_int, int_to_action, abridged_qtable );
	}
	
	private static <A> void writeActionKey( final Configuration config, final SingleInstanceDataset<A> data, final int iter )
	{
		final File f = new File( config.data_directory, data.instances.relationName() + "_action-key.csv" );
		try {
			final Csv.Writer writer = new Csv.Writer( new PrintStream( f ) );
			writer.cell( "key" ).cell( "action" ).newline();
			for( int i = 0; i < data.int_to_action.size(); ++i ) {
				writer.cell( i ).cell( data.int_to_action.get( i ) ).newline();
			}
		}
		catch( final FileNotFoundException ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	private static <A> void writeQTable( final Configuration config, final SingleInstanceDataset<A> data, final int iter )
	{
		final DecimalFormat df = new DecimalFormat( "#.####" );
		final File f = new File( config.data_directory, data.instances.relationName() + "_qvalues.csv" );
		try {
			final Csv.Writer writer = new Csv.Writer( new PrintStream( f ) );
			for( final Pair<ArrayList<A>, TDoubleList> q : data.qtable ) {
				for( int i = 0; i < q.first.size(); ++i ) {
					final A a = q.first.get( i );
					final double v = q.second.get( i );
					writer.cell( data.action_to_int.get( a ) + ":" + df.format( v ) );
				}
				writer.newline();
			}
		}
		catch( final FileNotFoundException ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	private static <A> SingleInstanceDataset<A> combineInstances( final RandomGenerator rng,
		final SingleInstanceDataset<A> running, final SingleInstanceDataset<A> new_, final double discount )
	{
//		final int[] idx = Fn.range( 0, running.size() );
//		Fn.shuffle( rng, idx );
//
//		for( int i = 0; i < running.size(); ++i ) {
//			final double p = rng.nextDouble();
//			if( p < discount ) {
//				running.set( i, new_.get( idx[i] ) );
//			}
//		}
//
//		return running;
		
		// TODO: This is the "right" thing to do, but the dataset might get
		// unmanageable quickly
		
		for( final Instance inst : new_.instances ) {
			inst.setValue( inst.classIndex(),
						   running.action_to_int.get( new_.int_to_action.get( (int) inst.classValue() ) ) );
			running.instances.add( inst );
		}
		return running;
	}
	
	// -----------------------------------------------------------------------
	// Abstraction algorithms
	// -----------------------------------------------------------------------
	
	private static abstract class AbstractionDiscoveryAlgorithm<S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
	{
		public abstract void writeModel( final int iter );
		public abstract void loadModel(
			final FactoredRepresenter<S, X> base_repr, final int iter );
		public abstract void trainRepresenter(
			final Dataset<A> train, final FactoredRepresenter<S, X> base_repr, final int iter );
		
		public abstract Policy<S, A> getControlPolicy( final Configuration config, final Domain<S, X, A, R> domain );
		
		public ArrayList<Pair<ArrayList<A>, TDoubleList>> getQTable()
		{ return null; }
		
		public abstract void writeStatisticsHeaders( final Csv.Writer csv );
		public abstract void writeStatisticsRecord( final Csv.Writer csv );
	}
	
	private static class PolicyEvaluator<S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		extends AbstractionDiscoveryAlgorithm<S, X, A, R>
	{
		@Override
		public void writeModel( final int iter )
		{ }

		@Override
		public void loadModel( final FactoredRepresenter<S, X> base_repr, final int iter )
		{ }

		@Override
		public void trainRepresenter( final Dataset<A> train, final FactoredRepresenter<S, X> base_repr, final int iter )
		{
			final Memorizer memorizer = new Memorizer();
			memorizer.buildClassifier( train.single );
			classifier_ = memorizer;
		}

		@Override
		public Policy<S, A> getControlPolicy( final Configuration config,
				final Domain<S, X, A, R> domain )
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void writeStatisticsHeaders( final Writer csv )
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void writeStatisticsRecord( final Writer csv )
		{
			// TODO Auto-generated method stub
			
		}
	}
	
	private static abstract class QDiscovery<S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		extends AbstractionDiscoveryAlgorithm<S, X, A, R>
	{
		public final ArrayList<Pair<ArrayList<A>, TDoubleList>> qtable
			= new ArrayList<Pair<ArrayList<A>, TDoubleList>>();
		
		@Override
		public Policy<S, A> getControlPolicy( final Configuration config, final Domain<S, X, A, R> domain )
		{
			final QFunction<S, A> qfunction = createQFunctionEstimator( config, domain );
			final Policy<S, A> pi = new QGreedyPolicy<S, A>( qfunction ) {
				@Override
				protected void onQFunctionCalculate( final S s, final Pair<ArrayList<A>, TDoubleList> q )
				{
					qtable.add( q );
				}
			};
			return pi;
		}
		
		@Override
		public ArrayList<Pair<ArrayList<A>, TDoubleList>> getQTable()
		{
			return qtable;
		}
		
		protected abstract QFunction<S, A> createQFunctionEstimator(
			final Configuration config, final Domain<S, X, A, R> domain );
		
//		public UctSearch.Factory<S, A> getUctFactory(
//			final Configuration config, final Domain<S, X, A, R> domain, final UndoSimulator<S, A> sim, final int Nepisodes )
//		{
//			final UctSearch.Factory<S, A> f = createUctFactory( config, domain, sim, Nepisodes );
//			if( config.getInt( "max_action_visits" ) > 0 ) {
//				f.setMaxActionVisits( config.getInt( "max_action_visits" ) );
//			}
//			if( config.getInt( "uct.max_depth" ) > 0 ) {
//				f.setMaxDepth( config.getInt( "uct.max_depth" ) );
//			}
//			return f;
//		}
//
//		protected abstract UctSearch.Factory<S, A> createUctFactory(
//			final Configuration config, final Domain<S, X, A, R> domain, final UndoSimulator<S, A> sim, final int Nepisodes );
	}
	
	private abstract static class UctAbstractionDiscovery<S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		extends QDiscovery<S, X, A, R>
	{
		private final TreeStatisticsRecorder<S, A> tree_stats = new TreeStatisticsRecorder<S, A>();
		private final MeanVarianceAccumulator elapsed_time = new MeanVarianceAccumulator();
		
		protected abstract MutableActionNode<S, A> createRootActionNode(
			final Configuration config, final Domain<S, X, A, R> domain );
		
		protected abstract Representer<S, ? extends Representation<S>>
		getRepresenter( final Configuration config, final Domain<S, X, A, R> domain );
		
		@Override
		protected final QFunction<S, A> createQFunctionEstimator(
			final Configuration config, final Domain<S, X, A, R> domain )
		{
			return new QFunction<S, A>() {

				Pair<ArrayList<A>, TDoubleList> q = null;
				
				@Override
				public void calculate( final S s )
				{
					final UctSearch<S, A> search = new UctSearch<S, A>(
						domain.createSimulator( s ), getRepresenter( config, domain ),
						SingleAgentJointActionGenerator.create( domain.getActionGenerator() ),
						config.uct_c, config.Ntest_episodes,
						config.rng, domain.getEvaluator(), new DefaultMctsVisitor<S, A>(),
						createRootActionNode( config, domain ) );
					
					final long tstart = System.nanoTime();
					search.run();
					final long tend = System.nanoTime();
					final double elapsed_ms = (tend - tstart) * 1e-6;
					elapsed_time.add( elapsed_ms );
					
					search.root().accept( tree_stats );
					q = Pair.makePair( new ArrayList<A>(), (TDoubleList) new TDoubleArrayList() );
					qtable.add( q );
					for( final ActionNode<S, A> an : Fn.in( search.root().successors() ) ) {
						// FIXME: This doesn't work for >1 agent
						q.first.add( an.a().get( 0 ) );
						q.second.add( an.q( 0 ) );
					}
					
//					System.out.println( "****************************************" );
//					System.out.println( "****************************************" );
//					search.root().accept( new TreePrinter<S, A>( 4 ) );
//					System.out.println( "----------------------------------------" );
				}

				@Override
				public Pair<ArrayList<A>, TDoubleList> get()
				{
					return q;
				}
			};
		}
		
		@Override
		public void writeStatisticsHeaders( final Writer csv )
		{
			csv.cell( "time_mean" ).cell( "time_var" ).cell( "time_conf" )
			   .cell( "state_branching_mean" ).cell( "state_branching_var" )
			   .cell( "action_branching_mean" ).cell( "action_branching_var" )
			   .cell( "tree_depth_mean" ).cell( "tree_depth_var" )
			   .cell( "tree_avg_depth_mean" ).cell( "tree_avg_depth_var" )
			   .cell( "action_visits_mean" ).cell( "action_visits_var" ).cell( "action_visits_conf" )
			   .cell( "action_visits_per_ms" );
		}
		
		@Override
		public void writeStatisticsRecord( final Writer csv )
		{
			csv.cell( elapsed_time.mean() ).cell( elapsed_time.variance() ).cell( elapsed_time.confidence() )
			   .cell( tree_stats.state_branching.mean() ).cell( tree_stats.state_branching.variance() )
			   .cell( tree_stats.action_branching.mean() ).cell( tree_stats.action_branching.variance() )
			   .cell( tree_stats.depth.mean() ).cell( tree_stats.depth.variance() )
			   .cell( tree_stats.avg_depth.mean() ).cell( tree_stats.avg_depth.variance() )
			   .cell( tree_stats.action_visits.mean() ).cell( tree_stats.action_visits.variance() ).cell( tree_stats.action_visits.confidence() )
			   .cell( tree_stats.action_visits.mean() / elapsed_time.mean() );
			
			System.out.println( "Time (ms) (mean): " + elapsed_time.mean() );
			System.out.println( "Time (ms) (var): " + elapsed_time.variance() );
			System.out.println( "Time (ms) (conf): " + elapsed_time.confidence() );
			
			System.out.println( "State branching (mean): " + tree_stats.state_branching.mean() );
			System.out.println( "State branching (var): " + tree_stats.state_branching.variance() );
			System.out.println( "Action branching (mean): " + tree_stats.action_branching.mean() );
			System.out.println( "Action branching (var): " + tree_stats.action_branching.variance() );
			System.out.println( "Depth (mean): " + tree_stats.depth.mean() );
			System.out.println( "Depth (var): " + tree_stats.depth.variance() );
			System.out.println( "Avg. Depth (mean): " + tree_stats.avg_depth.mean() );
			System.out.println( "Avg. Depth (var): " + tree_stats.avg_depth.variance() );
			
			System.out.println( "Action visits (mean): " + tree_stats.action_visits.mean() );
			System.out.println( "Action visits (var): " + tree_stats.action_visits.variance() );
			System.out.println( "Action visits (conf): " + tree_stats.action_visits.confidence() );
			System.out.println( "Action visits per millisecond: " + tree_stats.action_visits.mean() / elapsed_time.mean() );
		}
	}
	
	private static class UTreeAbstraction<S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		extends QDiscovery<S, X, A, R>
	{
		private final TreeStatisticsRecorder<S, A> tree_stats = new TreeStatisticsRecorder<S, A>();
		private final MeanVarianceAccumulator elapsed_time = new MeanVarianceAccumulator();
		
		private final MeanVarianceAccumulator Nsplits_accepted = new MeanVarianceAccumulator();
		
		@Override
		protected final QFunction<S, A> createQFunctionEstimator(
			final Configuration config, final Domain<S, X, A, R> domain )
		{
			return new QFunction<S, A>() {

				Pair<ArrayList<A>, TDoubleList> q = null;
				
				@Override
				public void calculate( final S s )
				{
//					final double split_threshold = config.getDouble( "utree.split_threshold" );
//					assert( split_threshold < 1.0 );
//					assert( split_threshold > 0.0 );
//					final int Nsplit_threshold = (int) Math.ceil( config.Ntest_episodes * split_threshold );
					
					final int Nsplit_threshold = config.getInt( "utree.split_threshold" );
					final int Ntop_actions = config.getInt( "utree.Ntop_actions" );
					final double size_regularization = config.getDouble( "utree.size_regularization" );
					
					final UTreeSearch<S, A> search = new UTreeSearch<S, A>(
						domain.createSimulator( s ), domain.getBaseRepresenter(),
						SingleAgentJointActionGenerator.create( domain.getActionGenerator() ),
						config.uct_c, config.Ntest_episodes,
						domain.getEvaluator(), new DefaultMctsVisitor<S, A>(),
						Nsplit_threshold, Ntop_actions, size_regularization );
					
					final long tstart = System.nanoTime();
					search.run();
					final long tend = System.nanoTime();
					final double elapsed_ms = (tend - tstart) * 1e-6;
					elapsed_time.add( elapsed_ms );
					Nsplits_accepted.add( search.Nsplits_accepted() );
					
					search.root().accept( tree_stats );
					q = Pair.makePair( new ArrayList<A>(), (TDoubleList) new TDoubleArrayList() );
					qtable.add( q );
					for( final ActionNode<S, A> an : Fn.in( search.root().successors() ) ) {
						// FIXME: This doesn't work for >1 agent
						q.first.add( an.a().get( 0 ) );
						q.second.add( an.q( 0 ) );
					}
					
//					System.out.println( "****************************************" );
//					System.out.println( "****************************************" );
//					search.root().accept( new TreePrinter<S, A>( 4 ) );
//					System.out.println( "----------------------------------------" );
				}

				@Override
				public Pair<ArrayList<A>, TDoubleList> get()
				{
					return q;
				}
			};
		}
		
		@Override
		public void writeStatisticsHeaders( final Writer csv )
		{
			csv.cell( "time_mean" ).cell( "time_var" ).cell( "time_conf" )
			   .cell( "state_branching_mean" ).cell( "state_branching_var" )
			   .cell( "action_branching_mean" ).cell( "action_branching_var" )
			   .cell( "tree_depth_mean" ).cell( "tree_depth_var" )
			   .cell( "tree_avg_depth_mean" ).cell( "tree_avg_depth_var" )
			   .cell( "action_visits_mean" ).cell( "action_visits_var" ).cell( "action_visits_conf" )
			   .cell( "action_visits_per_ms" )
			   .cell( "Nsplits_accepted_mean" ).cell( "Nsplits_accepted_var" ).cell( "Nsplits_accepted_conf" );
		}
		
		@Override
		public void writeStatisticsRecord( final Writer csv )
		{
			csv.cell( elapsed_time.mean() ).cell( elapsed_time.variance() ).cell( elapsed_time.confidence() )
			   .cell( tree_stats.state_branching.mean() ).cell( tree_stats.state_branching.variance() )
			   .cell( tree_stats.action_branching.mean() ).cell( tree_stats.action_branching.variance() )
			   .cell( tree_stats.depth.mean() ).cell( tree_stats.depth.variance() )
			   .cell( tree_stats.avg_depth.mean() ).cell( tree_stats.avg_depth.variance() )
			   .cell( tree_stats.action_visits.mean() ).cell( tree_stats.action_visits.variance() ).cell( tree_stats.action_visits.confidence() )
			   .cell( tree_stats.action_visits.mean() / elapsed_time.mean() )
			   .cell( Nsplits_accepted.mean() ).cell( Nsplits_accepted.variance() ).cell( Nsplits_accepted.confidence() );
			
			System.out.println( "Time (ms) (mean): " + elapsed_time.mean() );
			System.out.println( "Time (ms) (var): " + elapsed_time.variance() );
			System.out.println( "Time (ms) (conf): " + elapsed_time.confidence() );
			
			System.out.println( "State branching (mean): " + tree_stats.state_branching.mean() );
			System.out.println( "State branching (var): " + tree_stats.state_branching.variance() );
			System.out.println( "Action branching (mean): " + tree_stats.action_branching.mean() );
			System.out.println( "Action branching (var): " + tree_stats.action_branching.variance() );
			System.out.println( "Depth (mean): " + tree_stats.depth.mean() );
			System.out.println( "Depth (var): " + tree_stats.depth.variance() );
			System.out.println( "Avg. Depth (mean): " + tree_stats.avg_depth.mean() );
			System.out.println( "Avg. Depth (var): " + tree_stats.avg_depth.variance() );
			
			System.out.println( "Action visits (mean): " + tree_stats.action_visits.mean() );
			System.out.println( "Action visits (var): " + tree_stats.action_visits.variance() );
			System.out.println( "Action visits (conf): " + tree_stats.action_visits.confidence() );
			System.out.println( "Action visits per millisecond: " + tree_stats.action_visits.mean() / elapsed_time.mean() );
			
			System.out.println( "Nsplits_accepted (mean): " + Nsplits_accepted.mean() );
			System.out.println( "Nsplits_accepted (var): " + Nsplits_accepted.variance() );
			System.out.println( "Nsplits_accepted (conf): " + Nsplits_accepted.confidence() );
		}

		@Override
		public void writeModel( final int iter )
		{ }

		@Override
		public void loadModel( final FactoredRepresenter<S, X> base_repr, final int iter )
		{ }

		@Override
		public void trainRepresenter( final Dataset<A> train,
				final FactoredRepresenter<S, X> base_repr, final int iter )
		{ }
	}
	
	private static class EnsembleUct<S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		extends AbstractionDiscoveryAlgorithm<S, X, A, R>
	{
		private final TreeStatisticsRecorder<S, A> tree_stats = new TreeStatisticsRecorder<S, A>();
		private final MeanVarianceAccumulator elapsed_time = new MeanVarianceAccumulator();
		
		private final ArrayList<Representer<S, ? extends Representation<S>>> reprs
			= new ArrayList<Representer<S, ? extends Representation<S>>>();

		public EnsembleUct( final Configuration config, final Domain<S, X, A, R> domain )
		{
			final int Nreprs = config.getInt( "ensemble.N" );
			final int Nvars = config.getInt( "pdb.Nvars" );
			
			final ArrayList<Attribute> attributes = domain.getBaseRepresenter().attributes();
			final int[] idx = Fn.range( 0, attributes.size() );
			for( int i = 0; i < Nreprs; ++i ) {
				Fn.shuffle( config.rng, idx );
				final int[] indices = Arrays.copyOf( idx, Nvars );
				System.out.println( "Pattern " + i + ": " + Arrays.toString( indices ) );
				reprs.add( new ProjectionRepresenter<S>( domain.getBaseRepresenter(), indices ) );
			}
		}
		
		@Override
		public Policy<S, A> getControlPolicy( final Configuration config, final Domain<S, X, A, R> domain )
		{
			final ArrayList<Policy<S, A>> Pi = new ArrayList<Policy<S, A>>();
			for( final Representer<S, ? extends Representation<S>> repr : reprs ) {
				Pi.add( new SingleAgentPolicyAdapter<S, A>( 0, new Policy<S, JointAction<A>>() {
					private S s_ = null;
					@Override
					public void setState( final S s, final long t )
					{ s_ = s; }
					
					@Override
					public JointAction<A> getAction()
					{
						final UctSearch<S, A> search = new UctSearch<S, A>(
							domain.createSimulator( s_ ), repr.create(),
							SingleAgentJointActionGenerator.create( domain.getActionGenerator() ),
							config.uct_c, config.Ntest_episodes,
							config.rng, domain.getEvaluator(), new DefaultMctsVisitor<S, A>(),
							new SimpleMutableActionNode<S, A>( null, 0, repr.create() ) );
						
						final long tstart = System.nanoTime();
						search.run();
						final long tend = System.nanoTime();
						final double elapsed_ms = (tend - tstart) * 1e-6;
						elapsed_time.add( elapsed_ms );
						
						search.root().accept( tree_stats );
						return BackupRules.MaxAction( search.root() ).a();
					}

					@Override
					public void actionResult( final S sprime, final double[] r )
					{ }

					@Override
					public String getName()
					{ return "EnsembleComponent"; }
	
					@Override
					public int hashCode()
					{ return System.identityHashCode( this ); }
	
					@Override
					public boolean equals( final Object that )
					{ return this == that; }
				} ) );
			}
			
			final Policy<S, A> pi_ensemble = new VotingPolicyEnsemble<S, A>( config.rng, Pi );
			return pi_ensemble;
		}
		
		@Override
		public void writeStatisticsHeaders( final Writer csv )
		{
			csv.cell( "time_mean" ).cell( "time_var" ).cell( "time_conf" )
			   .cell( "state_branching_mean" ).cell( "state_branching_var" )
			   .cell( "action_branching_mean" ).cell( "action_branching_var" )
			   .cell( "tree_depth_mean" ).cell( "tree_depth_var" )
			   .cell( "tree_avg_depth_mean" ).cell( "tree_avg_depth_var" )
			   .cell( "action_visits_mean" ).cell( "action_visits_var" ).cell( "action_visits_conf" )
			   .cell( "action_visits_per_ms" );
		}
		
		@Override
		public void writeStatisticsRecord( final Writer csv )
		{
			csv.cell( elapsed_time.mean() ).cell( elapsed_time.variance() ).cell( elapsed_time.confidence() )
			   .cell( tree_stats.state_branching.mean() ).cell( tree_stats.state_branching.variance() )
			   .cell( tree_stats.action_branching.mean() ).cell( tree_stats.action_branching.variance() )
			   .cell( tree_stats.depth.mean() ).cell( tree_stats.depth.variance() )
			   .cell( tree_stats.avg_depth.mean() ).cell( tree_stats.avg_depth.variance() )
			   .cell( tree_stats.action_visits.mean() ).cell( tree_stats.action_visits.variance() ).cell( tree_stats.action_visits.confidence() )
			   .cell( tree_stats.action_visits.mean() / elapsed_time.mean() );
			
			System.out.println( "Time (ms) (mean): " + elapsed_time.mean() );
			System.out.println( "Time (ms) (var): " + elapsed_time.variance() );
			System.out.println( "Time (ms) (conf): " + elapsed_time.confidence() );
			
			System.out.println( "State branching (mean): " + tree_stats.state_branching.mean() );
			System.out.println( "State branching (var): " + tree_stats.state_branching.variance() );
			System.out.println( "Action branching (mean): " + tree_stats.action_branching.mean() );
			System.out.println( "Action branching (var): " + tree_stats.action_branching.variance() );
			System.out.println( "Depth (mean): " + tree_stats.depth.mean() );
			System.out.println( "Depth (var): " + tree_stats.depth.variance() );
			System.out.println( "Avg. Depth (mean): " + tree_stats.avg_depth.mean() );
			System.out.println( "Avg. Depth (var): " + tree_stats.avg_depth.variance() );
			
			System.out.println( "Action visits (mean): " + tree_stats.action_visits.mean() );
			System.out.println( "Action visits (var): " + tree_stats.action_visits.variance() );
			System.out.println( "Action visits (conf): " + tree_stats.action_visits.confidence() );
			System.out.println( "Action visits per millisecond: " + tree_stats.action_visits.mean() / elapsed_time.mean() );
		}

		@Override
		public void writeModel( final int iter )
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void loadModel( final FactoredRepresenter<S, X> base_repr, final int iter )
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void trainRepresenter( final Dataset<A> train,
				final FactoredRepresenter<S, X> base_repr, final int iter )
		{ }
	}
	
	private static class MultiAbstractionUct<S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		extends AbstractionDiscoveryAlgorithm<S, X, A, R>
	{
		private final TreeStatisticsRecorder<S, A> tree_stats = new TreeStatisticsRecorder<S, A>();
		private final MeanVarianceAccumulator elapsed_time = new MeanVarianceAccumulator();
		
		private final ArrayList<Representer<S, ? extends Representation<S>>> reprs
			= new ArrayList<Representer<S, ? extends Representation<S>>>();

		public MultiAbstractionUct( final Configuration config, final Domain<S, X, A, R> domain )
		{
			final int Nreprs = config.getInt( "ensemble.N" );
			final int Nvars = config.getInt( "pdb.Nvars" );
			
			final ArrayList<Attribute> attributes = domain.getBaseRepresenter().attributes();
			final ArrayList<double[]> ranges = domain.getVariableRanges();
			final int[] idx = Fn.range( 0, attributes.size() );
			for( int i = 0; i < Nreprs; ++i ) {
				Fn.shuffle( config.rng, idx );
				final int[] indices = Arrays.copyOf( idx, Nvars );
				System.out.println( "Pattern " + i + ": " + Arrays.toString( indices ) );
				final double[][] quantiles = new double[Nvars][];
				for( int j = 0; j < Nvars; ++j ) {
					final int jprime = indices[j];
					final double[] r = ranges.get( jprime );
					if( r != null ) {
						final double q = r[0] + config.rng.nextDouble()*(r[1] - r[0]);
						quantiles[j] = new double[] { q };
					}
					else {
						quantiles[j] = new double[] { };
					}
					System.out.println( "\tQ" + j + " = " + Arrays.toString( quantiles[j] ) );
				}
				reprs.add( new QuantizedRepresenter<S>(
					new ProjectionRepresenter<S>( domain.getBaseRepresenter(), indices ), quantiles ) );
			}
		}
		
		@Override
		public Policy<S, A> getControlPolicy( final Configuration config, final Domain<S, X, A, R> domain )
		{
			return new Policy<S, A>() {
				private S s_ = null;
				@Override
				public void setState( final S s, final long t )
				{ s_ = s; }
				
				@Override
				public A getAction()
				{
					final MultiAbstractionUctSearch<S, A> search = new MultiAbstractionUctSearch<S, A>(
						domain.createSimulator( s_ ), reprs,
						SingleAgentJointActionGenerator.create( domain.getActionGenerator() ),
						config.uct_c, config.Ntest_episodes,
						domain.getEvaluator(), new DefaultMctsVisitor<S, A>() );
					
					final long tstart = System.nanoTime();
					search.run();
					final long tend = System.nanoTime();
					final double elapsed_ms = (tend - tstart) * 1e-6;
					elapsed_time.add( elapsed_ms );
					
					// TODO: Record tree stats
//					for( final Map.Entry<JointAction<A>, UcbBandit<MutableActionNode<S, A>>> e
//							: search.action_abstractions.entrySet() ) {
//						System.out.println( e.getKey() );
//						for( int i = 0; i < e.getValue().arms.size(); ++i ) {
//							System.out.println( "\tArm " + i + ": " + e.getValue().counts[i] );
//							e.getValue().arms.get( i ).accept( new TreePrinter<S, A>() );
//						}
//					}
//					System.out.println( "*****" );
					
					// TODO: Generalize to multiagent
					return search.astar().get( 0 );
				}

				@Override
				public void actionResult( final S sprime, final double[] r )
				{ }

				@Override
				public String getName()
				{ return "MultiAbstractionUct"; }

				@Override
				public int hashCode()
				{ return System.identityHashCode( this ); }

				@Override
				public boolean equals( final Object that )
				{ return this == that; }
			};
		}
		
		@Override
		public void writeStatisticsHeaders( final Writer csv )
		{
			csv.cell( "time_mean" ).cell( "time_var" ).cell( "time_conf" )
			   .cell( "state_branching_mean" ).cell( "state_branching_var" )
			   .cell( "action_branching_mean" ).cell( "action_branching_var" )
			   .cell( "tree_depth_mean" ).cell( "tree_depth_var" )
			   .cell( "tree_avg_depth_mean" ).cell( "tree_avg_depth_var" )
			   .cell( "action_visits_mean" ).cell( "action_visits_var" ).cell( "action_visits_conf" )
			   .cell( "action_visits_per_ms" );
		}
		
		@Override
		public void writeStatisticsRecord( final Writer csv )
		{
			csv.cell( elapsed_time.mean() ).cell( elapsed_time.variance() ).cell( elapsed_time.confidence() )
			   .cell( tree_stats.state_branching.mean() ).cell( tree_stats.state_branching.variance() )
			   .cell( tree_stats.action_branching.mean() ).cell( tree_stats.action_branching.variance() )
			   .cell( tree_stats.depth.mean() ).cell( tree_stats.depth.variance() )
			   .cell( tree_stats.avg_depth.mean() ).cell( tree_stats.avg_depth.variance() )
			   .cell( tree_stats.action_visits.mean() ).cell( tree_stats.action_visits.variance() ).cell( tree_stats.action_visits.confidence() )
			   .cell( tree_stats.action_visits.mean() / elapsed_time.mean() );
			
			System.out.println( "Time (ms) (mean): " + elapsed_time.mean() );
			System.out.println( "Time (ms) (var): " + elapsed_time.variance() );
			System.out.println( "Time (ms) (conf): " + elapsed_time.confidence() );
			
			System.out.println( "State branching (mean): " + tree_stats.state_branching.mean() );
			System.out.println( "State branching (var): " + tree_stats.state_branching.variance() );
			System.out.println( "Action branching (mean): " + tree_stats.action_branching.mean() );
			System.out.println( "Action branching (var): " + tree_stats.action_branching.variance() );
			System.out.println( "Depth (mean): " + tree_stats.depth.mean() );
			System.out.println( "Depth (var): " + tree_stats.depth.variance() );
			System.out.println( "Avg. Depth (mean): " + tree_stats.avg_depth.mean() );
			System.out.println( "Avg. Depth (var): " + tree_stats.avg_depth.variance() );
			
			System.out.println( "Action visits (mean): " + tree_stats.action_visits.mean() );
			System.out.println( "Action visits (var): " + tree_stats.action_visits.variance() );
			System.out.println( "Action visits (conf): " + tree_stats.action_visits.confidence() );
			System.out.println( "Action visits per millisecond: " + tree_stats.action_visits.mean() / elapsed_time.mean() );
		}

		@Override
		public void writeModel( final int iter )
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void loadModel( final FactoredRepresenter<S, X> base_repr, final int iter )
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void trainRepresenter( final Dataset<A> train,
				final FactoredRepresenter<S, X> base_repr, final int iter )
		{ }
	}
	
	// -----------------------------------------------------------------------
	
	private static class NoneAbstractionDiscovery<S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		extends UctAbstractionDiscovery<S, X, A, R>
	{
		public static <S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		NoneAbstractionDiscovery<S, X, A, R> create( final Configuration config, final Domain<S, X, A, R> domain )
		{
			return new NoneAbstractionDiscovery<S, X, A, R>( config, domain );
		}
		
		public NoneAbstractionDiscovery( final Configuration config, final Domain<S, X, A, R> domain )
		{ }
		
		@Override
		public void trainRepresenter( final Dataset<A> train, final FactoredRepresenter<S, X> base_repr, final int iter )
		{ }

		@Override
		public void loadModel( final FactoredRepresenter<S, X> base_repr, final int iter )
		{ }

		@Override
		public void writeModel( final int iter )
		{ }

		@Override
		protected MutableActionNode<S, A> createRootActionNode(
			final Configuration config, final Domain<S, X, A, R> domain )
		{
			return new SimpleMutableActionNode<S, A>( null, 0, getRepresenter( config, domain ) );
		}

		@Override
		protected Representer<S, ? extends Representation<S>> getRepresenter( final Configuration config, final Domain<S, X, A, R> domain )
		{
			return domain.getBaseRepresenter().create();
		}
	}
	
	private static class TrivialAbstraction<S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		extends UctAbstractionDiscovery<S, X, A, R>
	{
		@Override
		public void writeModel( final int iter )
		{ }

		@Override
		public void loadModel( final FactoredRepresenter<S, X> base_repr, final int iter )
		{ }

		@Override
		public void trainRepresenter( final Dataset<A> train, final FactoredRepresenter<S, X> base_repr, final int iter )
		{ }

		@Override
		protected MutableActionNode<S, A> createRootActionNode(
			final Configuration config, final Domain<S, X, A, R> domain )
		{
			return new SimpleMutableActionNode<S, A>( null, 0, getRepresenter( config, domain ) );
		}

		@Override
		protected TrivialRepresenter<S> getRepresenter( final Configuration config, final Domain<S, X, A, R> domain )
		{
			return new TrivialRepresenter<S>();
		}
		
//		@Override
//		public UctSearch.Factory<S, A> createUctFactory( final Configuration config,
//				final Domain<S, X, A, R> domain, final UndoSimulator<S, A> sim,
//				final int Nepisodes )
//		{
////			System.out.println( "Trivial.createUctFactory()" );
//			return new UctSearch.Factory<S, A>(
//				sim, repr_.create(),
//				SingleAgentJointActionGenerator.create( domain.getActionGenerator() ),
//				config.uct_c, Nepisodes, config.rng, domain.getEvaluator(),
//				new SimpleMutableActionNode<S, A>( null, 0, repr_.create() ) );
//		}
	}
	
	// -----------------------------------------------------------------------
	
	private static class RandomClusterAbstraction<S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		extends UctAbstractionDiscovery<S, X, A, R>
	{
		@Override
		protected MutableActionNode<S, A> createRootActionNode(
				final Configuration config, final Domain<S, X, A, R> domain )
		{
			return new SimpleMutableActionNode<S, A>( null, 0, getRepresenter( config, domain ) );
		}

		@Override
		protected Representer<S, ? extends Representation<S>> getRepresenter(
				final Configuration config, final Domain<S, X, A, R> domain )
		{
			return new RandomClusterRepresenter<S>(
				config.rng, config.getInt( "random.branching" ) );
		}

		@Override
		public void writeModel( final int iter )
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void loadModel( final FactoredRepresenter<S, X> base_repr, final int iter )
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void trainRepresenter( final Dataset<A> train,
				final FactoredRepresenter<S, X> base_repr, final int iter )
		{
			// TODO Auto-generated method stub
			
		}


	}
	
	// -----------------------------------------------------------------------
	
	private static class HammingDistanceAbstraction<S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		extends AbstractionDiscoveryAlgorithm<S, X, A, R>
	{
		public static <S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		HammingDistanceAbstraction<S, X, A, R> create( final Configuration config, final Domain<S, X, A, R> domain )
		{
			return new HammingDistanceAbstraction<S, X, A, R>( config, domain );
		}
		
		private final Configuration config_;
		private final R base_repr_;
		
		private double decision_threshold_ = 0.0;
		
		public HammingDistanceAbstraction( final Configuration config, final Domain<S, X, A, R> domain )
		{
			config_ = config;
			base_repr_ = domain.getBaseRepresenter();
		}
		
		private int hammingDistance( final double[] diff )
		{
			int distance = 0;
			for( final double d : diff ) {
				if( d != 0 ) {
					distance += 1;
				}
			}
			return distance;
		}
		
		private class HammingSimilarity implements SimilarityFunction
		{
			@Override
			public double similarity( final double[] a, final double[] b )
			{
				assert( a.length == b.length );
				int distance = 0;
				for( int i = 0; i < a.length; ++i ) {
					if( a[i] != b[i] ) {
						distance += 1;
					}
				}
				return distance;
			}
		}
		
		@Override
		public Representer<S, Representation<S>> trainRepresenter(
			final Dataset<A> train, final FactoredRepresenter<S, X> base_repr, final int iter )
		{
			final int max_branching = config_.getInt( "pairwise_classifier.max_branching" );
			final QuantileAccumulator q = new QuantileAccumulator( 1.0 / max_branching );
			for( final Instance i : train.pair ) {
				final double[] phi = i.toDoubleArray();
				q.add( hammingDistance( phi ) );
			}
			decision_threshold_ = -q.estimates[0];
			return new ReprWrapper<S>( new PairwiseSimilarityRepresenter<S, X>(
				base_repr.create(),	new HammingSimilarity(),
				decision_threshold_, max_branching ) );
		}

		@Override
		public Representer<S, Representation<S>> loadModel(
			final File dir, final FactoredRepresenter<S, X> base_repr, final int iter )
		{
			final Map<String, String> params = Csv.readKeyValue( new File( dir, "params" + iter + ".csv" ), true );
			decision_threshold_ = Double.parseDouble( params.get( "decision_threshold" ) );
			
			// FIXME: This class requires that pair instances are available
			// in order to calculate the decision threshold. This is different
			// from other implementations.
			final Instances pair = config_.loadPairInstances();
			final int max_branching = config_.getInt( "pairwise_classifier.max_branching" );
			final QuantileAccumulator q = new QuantileAccumulator( 1.0 / max_branching );
			for( final Instance i : pair ) {
				final double[] phi = i.toDoubleArray();
				q.add( hammingDistance( phi ) );
			}
			decision_threshold_ = -q.estimates[0];
			System.out.println( "decision_threshold = " + decision_threshold_ );
			
			return new ReprWrapper<S>( new PairwiseSimilarityRepresenter<S, X>(
				base_repr.create(), new HammingSimilarity(),
				decision_threshold_,
				config_.getInt( "pairwise_classifier.max_branching" ) ) );
		}

		@Override
		public void writeModel( final int iter )
		{
			final Map<String, String> m = new HashMap<String, String>();
			m.put( "decision_threshold", Double.toString( decision_threshold_ ) );
			try {
				Csv.write( new PrintStream( new File( config_.data_directory, "params" + iter + ".csv" ) ), m );
			}
			catch( final FileNotFoundException ex ) {
				throw new RuntimeException( ex );
			}
		}

		@Override
		public Instances makePairwiseInstances(
			final Instances single, final FactoredRepresenter<S, X> repr )
		{
			final int max_pairwise_instances = config_.getInt( "training.max_pairwise" );
			final PairDataset.InstanceCombiner combiner = new PairDataset.DifferenceFeatures( repr.attributes() );
			return PairDataset.makePairDataset(
				config_.rng, max_pairwise_instances, single, combiner );
		}
	}
	
	// -----------------------------------------------------------------------
	
	private static class MetricLearningAbstraction<S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		extends AbstractionDiscoveryAlgorithm<S, X, A, R>
	{
		public static <S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		MetricLearningAbstraction<S, X, A, R> create( final Configuration config, final Domain<S, X, A, R> domain )
		{
			return new MetricLearningAbstraction<S, X, A, R>( config, domain );
		}
		
		private final Configuration config_;
		private final R base_repr_;
		
		private final SolvedStateAccumulator<S, X, A> labeled_;
//		private final UnlabeledStateAccumulator<S, A> unlabeled_;
		private final SolvedStateGapRecorder<Representation<S>, A> gaps_
			= new SolvedStateGapRecorder<Representation<S>, A>();
		
		private RealMatrix M_ = null;
		private double decision_threshold_ = 0.0;
		private final double u_ = 0;
		private final double ell_ = 0;
		
		public MetricLearningAbstraction( final Configuration config, final Domain<S, X, A, R> domain )
		{
			config_ = config;
			base_repr_ = domain.getBaseRepresenter();
			labeled_ = new SolvedStateAccumulator<S, X, A>( base_repr_.create() );
//			unlabeled_ = new UnlabeledStateAccumulator<S, A>( base_repr_.create() );
		}
		
		@Override
		public Representer<S, Representation<S>> trainRepresenter(
			final Dataset<A> train, final FactoredRepresenter<S, X> base_repr, final int iter )
		{
//			WekaUtil.writeDataset( config_.data_directory, train );
//			final ArrayList<RealVector> unlabeled = new ArrayList<RealVector>(); //unlabeled_.unlabeled;
//			final ArrayList<Attribute> attributes = WekaUtil.extractAttributes( train );
//			final Instances XL = makePairDataset(
//				config_, train, new DifferenceFeatures( attributes ), iter );
			
			final RealMatrix A0 = MatrixUtils.createRealIdentityMatrix( base_repr.attributes().size() );
			final InformationTheoreticMetricLearner itml = learnMetric( config_, A0, train.pair );
			M_ = itml.A();
			
			final int max_branching = config_.getInt( "pairwise_classifier.max_branching" );
			final QuantileAccumulator q = new QuantileAccumulator( 1.0 / max_branching );
			for( final Instance i : train.pair ) {
				final double[] phi = i.toDoubleArray();
				q.add( Math.sqrt( HilbertSpace.inner_prod( phi, M_, phi ) ) );
			}
			decision_threshold_ = -q.estimates[0];
			
//			u_ = itml.u;
//			ell_ = itml.ell;
//			final double decision_threshold = decisionThreshold( itml.u, itml.ell );
			
			return new ReprWrapper<S>( new PairwiseSimilarityRepresenter<S, X>(
				base_repr.create(),
				new MetricSimilarityFunction( itml.A() ), decision_threshold_, max_branching ) );
		}
		
		private double decisionThreshold( final double u, final double ell )
		{
			return -(u + ell) / 2.0;
		}

		@Override
		public Representer<S, Representation<S>> loadModel(
			final File dir, final FactoredRepresenter<S, X> base_repr, final int iter )
		{
			M_ = Csv.readMatrix( new File( dir, "M" + iter + ".csv" ) );
			
//			final Map<String, String> params = Csv.readKeyValue( new File( dir, "params" + iter + ".csv" ), true );
//			decision_threshold_ = Double.parseDouble( params.get( "decision_threshold" ) );
//			u_ = Double.parseDouble( params.get( "u" ) );
//			ell_ = Double.parseDouble( params.get( "ell" ) );
//			final double decision_threshold = decisionThreshold( u_, ell_ );
			
			// FIXME: This class requires that pair instances are available
			// in order to calculate the decision threshold. This is different
			// from other implementations.
			final Instances pair = config_.loadPairInstances();
			final int max_branching = config_.getInt( "pairwise_classifier.max_branching" );
			final QuantileAccumulator q = new QuantileAccumulator( 1.0 / max_branching );
			for( final Instance i : pair ) {
				final double[] phi = i.toDoubleArray();
				q.add( Math.sqrt( HilbertSpace.inner_prod( phi, M_, phi ) ) );
			}
			decision_threshold_ = -q.estimates[0];
			System.out.println( "decision_threshold = " + decision_threshold_ );
			
			return new ReprWrapper<S>( new PairwiseSimilarityRepresenter<S, X>(
				base_repr.create(), new MetricSimilarityFunction( M_ ),
				decision_threshold_, config_.getInt( "pairwise_classifier.max_branching" ) ) );
		}

		@Override
		public void writeModel( final int iter )
		{
			writeMetric( config_.data_directory, M_, iter );
			final Map<String, String> m = new HashMap<String, String>();
			m.put( "decision_threshold", Double.toString( decision_threshold_ ) );
			m.put( "u", Double.toString( u_ ) );
			m.put( "ell", Double.toString( ell_ ) );
			try {
				Csv.write( new PrintStream( new File( config_.data_directory, "params" + iter + ".csv" ) ), m );
			}
			catch( final FileNotFoundException ex ) {
				throw new RuntimeException( ex );
			}
		}

		@Override
		public Instances makePairwiseInstances(
			final Instances single, final FactoredRepresenter<S, X> repr )
		{
			final int max_pairwise_instances = config_.getInt( "training.max_pairwise" );
			final PairDataset.InstanceCombiner combiner = new PairDataset.DifferenceFeatures( repr.attributes() );
			return PairDataset.makePairDataset(
				config_.rng, max_pairwise_instances, single, combiner );
		}
	}
	
	// -----------------------------------------------------------------------
	
	private static class MahalanobisDistanceAbstraction<S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		extends AbstractionDiscoveryAlgorithm<S, X, A, R>
	{
		public static <S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		MahalanobisDistanceAbstraction<S, X, A, R> create( final Configuration config, final Domain<S, X, A, R> domain )
		{
			return new MahalanobisDistanceAbstraction<S, X, A, R>( config, domain );
		}
		
		private final Configuration config_;
		private final R base_repr_;
		
		private final SolvedStateAccumulator<S, X, A> labeled_;
		private final SolvedStateGapRecorder<Representation<S>, A> gaps_
			= new SolvedStateGapRecorder<Representation<S>, A>();
		
		private RealMatrix M_ = null;
		private double decision_threshold_ = 0.0;
		
		public MahalanobisDistanceAbstraction( final Configuration config, final Domain<S, X, A, R> domain )
		{
			config_ = config;
			base_repr_ = domain.getBaseRepresenter();
			labeled_ = new SolvedStateAccumulator<S, X, A>( base_repr_.create() );
		}
		
		@Override
		public Representer<S, Representation<S>> trainRepresenter(
			final Dataset<A> train, final FactoredRepresenter<S, X> base_repr, final int iter )
		{
			final int dim = base_repr.attributes().size();
			final StorelessCovariance cov = new StorelessCovariance( dim );
			for( final Instance i : train.single ) {
				final double[] phi = WekaUtil.unlabeledFeatures( i );
				cov.increment( phi );
			}
			
			final RealMatrix S = cov.getCovarianceMatrix();
//			writeMetric( new File( config_.root_directory ), S, iter ); // TODO: Debugging
			M_ = MatrixAlgorithms.robustInversePSD( S, 1e-2 );
			
			final int max_branching = config_.getInt( "pairwise_classifier.max_branching" );
			final QuantileAccumulator q = new QuantileAccumulator( 1.0 / max_branching );
			for( final Instance i : train.pair ) {
				final double[] phi = i.toDoubleArray();
				q.add( Math.sqrt( HilbertSpace.inner_prod( phi, M_, phi ) ) );
			}
			decision_threshold_ = -q.estimates[0];
			return new ReprWrapper<S>( new PairwiseSimilarityRepresenter<S, X>(
				base_repr.create(),	new MetricSimilarityFunction( M_ ),
				decision_threshold_, max_branching ) );
		}

		@Override
		public Representer<S, Representation<S>> loadModel(
			final File dir, final FactoredRepresenter<S, X> base_repr, final int iter )
		{
			M_ = Csv.readMatrix( new File( dir, "M" + iter + ".csv" ) );
			
//			final Map<String, String> params = Csv.readKeyValue( new File( dir, "params" + iter + ".csv" ), true );
//			decision_threshold_ = Double.parseDouble( params.get( "decision_threshold" ) );
			
			// FIXME: This class requires that pair instances are available
			// in order to calculate the decision threshold. This is different
			// from other implementations.
			final Instances pair = config_.loadPairInstances();
			final int max_branching = config_.getInt( "pairwise_classifier.max_branching" );
			final QuantileAccumulator q = new QuantileAccumulator( 1.0 / max_branching );
			for( final Instance i : pair ) {
				final double[] phi = i.toDoubleArray();
				q.add( Math.sqrt( HilbertSpace.inner_prod( phi, M_, phi ) ) );
			}
			decision_threshold_ = -q.estimates[0];
			System.out.println( "decision_threshold = " + decision_threshold_ );
			
			return new ReprWrapper<S>( new PairwiseSimilarityRepresenter<S, X>(
				base_repr.create(), new MetricSimilarityFunction( M_ ),
				decision_threshold_,
				config_.getInt( "pairwise_classifier.max_branching" ) ) );
		}

		@Override
		public void writeModel( final int iter )
		{
			writeMetric( config_.data_directory, M_, iter );
			final Map<String, String> m = new HashMap<String, String>();
			m.put( "decision_threshold", Double.toString( decision_threshold_ ) );
			try {
				Csv.write( new PrintStream( new File( config_.data_directory, "params" + iter + ".csv" ) ), m );
			}
			catch( final FileNotFoundException ex ) {
				throw new RuntimeException( ex );
			}
		}

		@Override
		public Instances makePairwiseInstances(
			final Instances single, final FactoredRepresenter<S, X> repr )
		{
			final int max_pairwise_instances = config_.getInt( "training.max_pairwise" );
			final PairDataset.InstanceCombiner combiner = new PairDataset.DifferenceFeatures( repr.attributes() );
			return PairDataset.makePairDataset(
				config_.rng, max_pairwise_instances, single, combiner );
		}
	}
	
	// -----------------------------------------------------------------------
	
	private static class MulticlassAbstraction<S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		extends UctAbstractionDiscovery<S, X, A, R>
	{
		public static <S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		MulticlassAbstraction<S, X, A, R> create( final Configuration config, final Domain<S, X, A, R> domain )
		{
			return new MulticlassAbstraction<S, X, A, R>( config, domain );
		}
		
		private final Configuration config_;
		private final R base_repr_;
		
		private final SolvedStateAccumulator<S, X, A> labeled_;
		private final SolvedStateGapRecorder<Representation<S>, A> gaps_
			= new SolvedStateGapRecorder<Representation<S>, A>();
		
		private Classifier classifier_ = null;
		private final int Nclasses;
		
		private MulticlassRepresenter<S> abstract_repr_ = null;
		
		public MulticlassAbstraction( final Configuration config, final Domain<S, X, A, R> domain )
		{
			config_ = config;
			Nclasses = domain.getActionGenerator().size();
			base_repr_ = domain.getBaseRepresenter();
			labeled_ = new SolvedStateAccumulator<S, X, A>( base_repr_.create() );
		}
		
		@Override
		public void trainRepresenter(
			final Dataset<A> train, final FactoredRepresenter<S, X> base_repr, final int iter )
		{
			try {
				final String algorithm = config_.get( "multiclass.classifier" );
				if( "random_forest".equals( algorithm ) ) {
					final FastRandomForest rf = new FastRandomForest();
					rf.setNumTrees( config_.getInt( "multiclass.random_forest.Ntrees" ) );
					rf.setMaxDepth( config_.getInt( "multiclass.random_forest.max_depth" ) );
					rf.setNumThreads( 1 );
					rf.buildClassifier( train.single );
					classifier_ = rf;
				}
				else if( "decision_tree".equals( algorithm ) ) {
					final J48 dt = new J48();
					dt.buildClassifier( train.single );
					classifier_ = dt;
				}
				else if( "memorize".equals( algorithm ) ) {
					final Memorizer memorizer = new Memorizer();
					memorizer.buildClassifier( train.single );
					classifier_ = memorizer;
				}
				else {
					throw new IllegalArgumentException( "multiclass.classifier = " + algorithm );
				}
				abstract_repr_ = new MulticlassRepresenter<S>(
					classifier_, train.single.numClasses(), base_repr );
			}
			catch( final RuntimeException ex ) {
				throw ex;
			}
			catch( final Exception ex ) {
				throw new RuntimeException( ex );
			}
		}

		private String modelFilename()
		{
			final String algorithm = config_.get( "multiclass.classifier" );
			return "multiclass." + algorithm + ".model";
		}
		
		@Override
		public void loadModel( final FactoredRepresenter<S, X> base_repr, final int iter )
		{
			try {
//				classifier_ = (Classifier) SerializationHelper.read(
//					new File( config_.data_directory, deriveDatasetName( modelFilename(), iter ) ).getPath() );
				classifier_ = (Classifier) SerializationHelper.read(
					new File( config_.root_directory, config_.model ).getPath() );
				abstract_repr_ = new MulticlassRepresenter<S>( classifier_, Nclasses, base_repr );
			}
			catch( final RuntimeException ex ) {
				throw ex;
			}
			catch( final Exception ex ) {
				throw new RuntimeException( ex );
			}
		}

		@Override
		public void writeModel( final int iter )
		{
			try {
				SerializationHelper.write(
					new File( config_.data_directory, deriveDatasetName( modelFilename(), iter ) ).getPath(), classifier_ );
			}
			catch( final RuntimeException ex ) {
				throw ex;
			}
			catch( final Exception ex ) {
				throw new RuntimeException( ex );
			}
		}
		
//		@Override
//		public UctSearch.Factory<S, A> createUctFactory(
//				final Configuration config, final Domain<S, X, A, R> domain,
//				final UndoSimulator<S, A> sim, final int Nepisodes )
//		{
//			final int lazy_threshold = config.getInt( "lazy.threshold" );
//			return new UctSearch.Factory<S, A>(
//				sim, domain.getBaseRepresenter(),
//				SingleAgentJointActionGenerator.create( domain.getActionGenerator() ),
//				config.uct_c, Nepisodes, config.rng, domain.getEvaluator(),
//				new LazyAggregatingActionNode<S, A>( null, 0, domain.getBaseRepresenter().create(),
//												 abstract_repr_.create(), lazy_threshold ) );
//		}

		@Override
		protected MutableActionNode<S, A> createRootActionNode(
				final Configuration config, final Domain<S, X, A, R> domain )
		{
			final int lazy_threshold = config.getInt( "lazy.threshold" );
			final MutableActionNode<S, A> an;
			if( lazy_threshold <= 1 ) {
				an = new AggregatingActionNode<S, A>(
					null, 0, domain.getBaseRepresenter().create(), abstract_repr_.create() );
			}
			else {
				an = new LazyAggregatingActionNode<S, A>(
					null, 0, domain.getBaseRepresenter().create(), abstract_repr_.create(), lazy_threshold );
			}
			return an;
		}

		@Override
		protected Representer<S, ? extends Representation<S>> getRepresenter(
				final Configuration config, final Domain<S, X, A, R> domain )
		{
			return domain.getBaseRepresenter();
		}
	}
	
	// -----------------------------------------------------------------------
	
	private static class PairwiseClassifierAbstraction<S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		extends UctAbstractionDiscovery<S, X, A, R>
	{
		public static <S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		PairwiseClassifierAbstraction<S, X, A, R> create( final Configuration config, final Domain<S, X, A, R> domain )
		{
			return new PairwiseClassifierAbstraction<S, X, A, R>( config, domain );
		}
		
		private final Configuration config_;
		private final R base_repr_;
		
		private final SolvedStateAccumulator<S, X, A> labeled_;
		private final SolvedStateGapRecorder<Representation<S>, A> gaps_
			= new SolvedStateGapRecorder<Representation<S>, A>();
		
		private final PairDataset.InstanceCombiner combiner_;
		
		private Classifier classifier_ = null;
		private final int Nclasses;
		
		private PairwiseClassifierRepresenter<S> abstract_repr_ = null;
		
		public PairwiseClassifierAbstraction( final Configuration config, final Domain<S, X, A, R> domain )
		{
			config_ = config;
			Nclasses = domain.getActionGenerator().size();
			base_repr_ = domain.getBaseRepresenter();
			labeled_ = new SolvedStateAccumulator<S, X, A>( base_repr_.create() );
			if( "tamarisk".equals( config.domain ) ) {
				combiner_ = new IndicatorTamariskRepresenter.SmartPairFeatures(
					new TamariskParameters( null, config.getInt( "tamarisk.Nreaches" ), config.getInt( "tamarisk.Nhabitats" ) ) );
			}
			else {
				combiner_ = new PairDataset.SymmetricFeatures( base_repr_.attributes() );
			}
		}
		
		public PairDataset prepareInstances( final Instances test )
		{
			final int positive_pairs = config_.getInt( "training.positive_pairs" );
			final int negative_pairs = config_.getInt( "training.negative_pairs" );
			final PairDataset p = PairDataset.makeBalancedPairDataset(
				config_.rng, negative_pairs, positive_pairs, test, combiner_ );
			final double negative_weight = config_.getDouble( "pair.negative_weight" );
			for( final Instance i : p.instances ) {
				if( (int) i.classValue() == 0 ) {
					i.setWeight( negative_weight );
				}
			}
			return p;
		}
		
		@Override
		public void trainRepresenter(
			final Dataset<A> train, final FactoredRepresenter<S, X> base_repr, final int iter )
		{
			try {
				final String algorithm = config_.get( "pair.algorithm" );
				final PairDataset p = prepareInstances( train.single );
				if( "decision_tree".equals( algorithm ) ) {
					final J48 dt = new J48();
					dt.buildClassifier( p.instances );
					classifier_ = dt;
				}
				else if( "logit_boost".equals( algorithm ) ) {
					final LogitBoost lb = new LogitBoost();
//					final M5P base = new M5P();
					final REPTree base = new REPTree();
//					base.setMaxDepth( 8 );
					lb.setSeed( config_.rng.nextInt() );
					lb.setClassifier( base );
					lb.setNumIterations( 50 );
					lb.buildClassifier( p.instances );
					classifier_ = lb;
				}
				else if( "random_forest".equals( algorithm ) ) {
					final RandomForest rf = new RandomForest();
					final int Ntrees = config_.getInt( "pair.random_forest.Ntrees" );
					rf.setNumTrees( Ntrees );
					rf.buildClassifier( p.instances );
					classifier_ = rf;
				}
				else {
					throw new IllegalArgumentException( "pair.algorithm = " + algorithm );
				}
				abstract_repr_ = new PairwiseClassifierRepresenter<S>( p.combiner, classifier_ );
			}
			catch( final RuntimeException ex ) {
				throw ex;
			}
			catch( final Exception ex ) {
				throw new RuntimeException( ex );
			}
		}

		private String modelFilename()
		{
			final String algorithm = config_.get( "pair.algorithm" );
			return "pair." + algorithm + ".model";
		}
		
		@Override
		public void loadModel( final FactoredRepresenter<S, X> base_repr, final int iter )
		{
			try {
//				classifier_ = (Classifier) SerializationHelper.read(
//					new File( config_.data_directory, deriveDatasetName( modelFilename(), iter ) ).getPath() );
				classifier_ = (Classifier) SerializationHelper.read(
					new File( config_.root_directory, config_.model ).getPath() );
				abstract_repr_ = new PairwiseClassifierRepresenter<S>( combiner_, classifier_ );
			}
			catch( final RuntimeException ex ) {
				throw ex;
			}
			catch( final Exception ex ) {
				throw new RuntimeException( ex );
			}
		}

		@Override
		public void writeModel( final int iter )
		{
			try {
				SerializationHelper.write(
					new File( config_.data_directory, deriveDatasetName( modelFilename(), iter ) ).getPath(), classifier_ );
			}
			catch( final RuntimeException ex ) {
				throw ex;
			}
			catch( final Exception ex ) {
				throw new RuntimeException( ex );
			}
		}
		
//		@Override
//		public UctSearch.Factory<S, A> createUctFactory(
//				final Configuration config, final Domain<S, X, A, R> domain,
//				final UndoSimulator<S, A> sim, final int Nepisodes )
//		{
//			final int lazy_threshold = config.getInt( "lazy.threshold" );
//			final MutableActionNode<S, A> an;
//			if( lazy_threshold <= 1 ) {
//				an = new AggregatingActionNode<S, A>(
//					null, 0, domain.getBaseRepresenter().create(), abstract_repr_.create() );
//			}
//			else {
//				an = new LazyAggregatingActionNode<S, A>(
//					null, 0, domain.getBaseRepresenter().create(), abstract_repr_.create(), lazy_threshold );
//			}
//			return new UctSearch.Factory<S, A>(
//				sim, domain.getBaseRepresenter(),
//				SingleAgentJointActionGenerator.create( domain.getActionGenerator() ),
//				config.uct_c, Nepisodes, config.rng, domain.getEvaluator(), an );
//		}

		@Override
		protected MutableActionNode<S, A> createRootActionNode(
				final Configuration config, final Domain<S, X, A, R> domain )
		{
			final int lazy_threshold = config.getInt( "lazy.threshold" );
			final MutableActionNode<S, A> an;
			if( lazy_threshold <= 1 ) {
				an = new AggregatingActionNode<S, A>(
					null, 0, domain.getBaseRepresenter().create(), abstract_repr_.create() );
			}
			else {
				an = new LazyAggregatingActionNode<S, A>(
					null, 0, domain.getBaseRepresenter().create(), abstract_repr_.create(), lazy_threshold );
			}
			return an;
		}

		@Override
		protected Representer<S, ? extends Representation<S>> getRepresenter(
				final Configuration config, final Domain<S, X, A, R> domain )
		{
			return domain.getBaseRepresenter();
		}
	}
	
	// -----------------------------------------------------------------------
	
	private static class KMeansAbstraction<S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		extends AbstractionDiscoveryAlgorithm<S, X, A, R>
	{
		public static <S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		KMeansAbstraction<S, X, A, R> create( final Configuration config, final Domain<S, X, A, R> domain )
		{
			return new KMeansAbstraction<S, X, A, R>( config, domain );
		}
		
		private final Configuration config_;
		private final R base_repr_;
		
		// TODO: Make this a parameter
		private final boolean with_metric_learning_ = true;
		
		private final SolvedStateAccumulator<S, X, A> labeled_;
//		private final UnlabeledStateAccumulator<S, A> unlabeled_;
		private final SolvedStateGapRecorder<Representation<S>, A> gaps_
			= new SolvedStateGapRecorder<Representation<S>, A>();
		
		public KMeansAbstraction( final Configuration config, final Domain<S, X, A, R> domain )
		{
			config_ = config;
			base_repr_ = domain.getBaseRepresenter();
			labeled_ = new SolvedStateAccumulator<S, X, A>( base_repr_.create() );
//			unlabeled_ = new UnlabeledStateAccumulator<S, A>( base_repr_.create() );
		}
		
		@Override
		public Representer<S, Representation<S>> trainRepresenter(
			final Instances train, final FactoredRepresenter<S, X> base_repr, final int iter )
		{
			final ArrayList<RealVector> unlabeled = new ArrayList<RealVector>(); //unlabeled_.unlabeled;
			RealMatrix A0 = MatrixUtils.createRealIdentityMatrix( base_repr.attributes().size() );
			final MetricConstrainedKMeans kmeans = makeClustering(
				config_, A0, train, unlabeled, with_metric_learning_ );
			
			final VoronoiClassifier classifier = new VoronoiClassifier( kmeans.mu() ) {
				@Override
				protected double distance( final RealVector x1, final RealVector x2 )
				{
					return kmeans.distance( x1, x2 );
				}
			};
			// Update reference matrix. This has the effect of keeping some of
			// the information from previous training episodes.
			A0 = kmeans.metric.copy();
			try {
				writeClustering( kmeans, config_.data_directory, iter );
			}
			catch( final FileNotFoundException ex ) {
				throw new RuntimeException( ex );
			}
			
			final int Nfeatures = base_repr.attributes().size();
			for( int i = 0; i < Nfeatures; ++i ) {
				final RealVector xi = new ArrayRealVector( Nfeatures );
				xi.setEntry( i, 1.0 );
				xi.setEntry( Nfeatures - 1, 1.0 );
				for( int j = 0; j <= i; ++j ) {
					final RealVector xj = new ArrayRealVector( Nfeatures );
					xj.setEntry( j, 1.0 );
					xj.setEntry( Nfeatures - 1, 10.0 );
					System.out.print( kmeans.distance( xi, xj ) + "\t" );
				}
				System.out.println();
			}
			
			return new ReprWrapper<S>( new ClusterRepresenter<S>( classifier, base_repr.create() ) );
		}

		@Override
		public Representer<S, Representation<S>> loadModel(
			final File dir, final FactoredRepresenter<S, X> base_repr, final int iter )
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void writeModel( final int iter )
		{
			// TODO Auto-generated method stub
			
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static <S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
	AbstractionDiscoveryAlgorithm<S, X, A, R> createAbstractionDiscoveryAlgorithm(
		final String name, final Configuration config, final Domain<S, X, A, R> domain )
	{
		if( "none".equals( name ) ) {
			return NoneAbstractionDiscovery.create( config, domain );
		}
		else if( "random".equals( name ) ) {
			return new RandomClusterAbstraction<S, X, A, R>();
		}
		else if( "trivial".equals( name ) ) {
			return new TrivialAbstraction<S, X, A, R>();
		}
		else if( "metric".equals( name ) ) {
			return MetricLearningAbstraction.create( config, domain );
		}
		else if( "mahalanobis".equals( name ) ) {
			return MahalanobisDistanceAbstraction.create( config, domain );
		}
		else if( "multiclass".equals( name ) ) {
			return MulticlassAbstraction.create( config, domain );
		}
		else if( "pair".equals( name ) ) {
			return new PairwiseClassifierAbstraction<S, X, A, R>( config, domain );
		}
		else if( "kmeans".equals( name ) ) {
			return KMeansAbstraction.create( config, domain );
		}
		else if( "ensemble_vote".equals( name ) ) {
			return new EnsembleUct<S, X, A, R>( config, domain );
		}
		else if( "multi_abstraction".equals( name ) ) {
			return new MultiAbstractionUct<S, X, A, R>( config, domain );
		}
		else if( "utree".equals( name ) ) {
			return new UTreeAbstraction<S, X, A, R>();
		}
		else {
			throw new IllegalArgumentException( "name = " + name );
		}
	}
	
	// -----------------------------------------------------------------------
	// Domains
	// -----------------------------------------------------------------------
	
	private static abstract class Domain<S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends Representer<S, ? extends Representation<S>>>
	{
		public abstract S initialState();
		public abstract ResetSimulator<S, A> createSimulator( final S s );
		public abstract R getBaseRepresenter();
		public abstract ActionGenerator<S, A> getActionGenerator();
		public abstract EvaluationFunction<S, A> getEvaluator();
		public abstract EpisodeListener<S, A> getVisualization();
		public abstract String name();
		
		public ArrayList<double[]> getVariableRanges()
		{
			return null;
		}
	}
	
	// -----------------------------------------------------------------------
	
	private static class IrrelevanceDomain extends Domain<Irrelevance.State, FactoredRepresentation<Irrelevance.State>,
													  Irrelevance.Action, Irrelevance.IdentityRepresenter>
	{
		private final Configuration config_;
		private final Irrelevance irrelevance_;
		
		public IrrelevanceDomain( final Configuration config, final int Ndummy )
		{
			config_ = config;
			irrelevance_ = new Irrelevance( Ndummy, 0.2 );
		}
		
		@Override
		public ResetSimulator<Irrelevance.State, Irrelevance.Action> createSimulator( final Irrelevance.State s )
		{
			return ResetAdapter.of( irrelevance_.new Simulator( s ) );
		}

		@Override
		public Irrelevance.IdentityRepresenter getBaseRepresenter()
		{
			return new Irrelevance.IdentityRepresenter();
		}

		@Override
		public EvaluationFunction<Irrelevance.State, Irrelevance.Action> getEvaluator()
		{
			final int rollout_width = 1;
			final int rollout_depth = Integer.MAX_VALUE;
			final Policy<Irrelevance.State, JointAction<Irrelevance.Action>> rollout_policy
				= new RandomPolicy<Irrelevance.State, JointAction<Irrelevance.Action>>(
					0 /*Player*/, config_.rng.nextInt(),
					SingleAgentJointActionGenerator.create( getActionGenerator() ) );
			final EvaluationFunction<Irrelevance.State, Irrelevance.Action> heuristic
				= new EvaluationFunction<Irrelevance.State, Irrelevance.Action>() {
				@Override
				public double[] evaluate( final Simulator<Irrelevance.State, Irrelevance.Action> sim )
				{
					return new double[] { 0.0 };
				}
			};
			final EvaluationFunction<Irrelevance.State, Irrelevance.Action> rollout_evaluator
				= RolloutEvaluator.create( rollout_policy, config_.discount,
										   rollout_width, rollout_depth, heuristic );
			return rollout_evaluator;
		}

		@Override
		public EpisodeListener<Irrelevance.State, Irrelevance.Action> getVisualization()
		{
			return null;
		}

		@Override
		public ActionGenerator<Irrelevance.State, Irrelevance.Action> getActionGenerator()
		{
			return irrelevance_.new ActionGen( config_.rng );
		}

		@Override
		public String name()
		{
			return "irrelevance_" + irrelevance_;
		}

		@Override
		public Irrelevance.State initialState()
		{
			return irrelevance_.new State();
		}
	}
	
	// -----------------------------------------------------------------------
	
	private static class ChainWalkDomain extends Domain<
		ChainWalk.State, FactoredRepresentation<ChainWalk.State>,
		ChainWalk.Action, FactoredRepresenter<ChainWalk.State, FactoredRepresentation<ChainWalk.State>>>
	{
		private final Configuration config_;
		private final ChainWalk world_;
		
		public ChainWalkDomain( final Configuration config )
		{
			config_ = config;
			final int Nstates = config.getInt( "chain_walk.Nstates" );
			final double slip = config.getDouble( "chain_walk.slip" );
			final int Nirrelevant = config.getInt( "chain_walk.Nirrelevant" );
			world_ = new ChainWalk( Nstates, slip, Nirrelevant );
		}
		
		@Override
		public ResetSimulator<ChainWalk.State, ChainWalk.Action> createSimulator( final ChainWalk.State s )
		{
			return ResetAdapter.of(  world_.new Simulator( s ) );
		}

		@Override
		public FactoredRepresenter<ChainWalk.State, FactoredRepresentation<ChainWalk.State>> getBaseRepresenter()
		{
			return new ChainWalk.IdentityRepresenter();
//			return new ChainWalk.PiStarRepresenter();
		}

//		@Override
		@Override
		public EvaluationFunction<ChainWalk.State, ChainWalk.Action> getEvaluator()
		{
			final int rollout_width = 1;
			final int rollout_depth = Integer.MAX_VALUE;
			final Policy<ChainWalk.State, JointAction<ChainWalk.Action>> rollout_policy
				= new RandomPolicy<ChainWalk.State, JointAction<ChainWalk.Action>>(
					0 /*Player*/, config_.rng.nextInt(),
					SingleAgentJointActionGenerator.create( getActionGenerator() ) );
			final EvaluationFunction<ChainWalk.State, ChainWalk.Action> heuristic
				= new EvaluationFunction<ChainWalk.State, ChainWalk.Action>() {
				@Override
				public double[] evaluate( final Simulator<ChainWalk.State, ChainWalk.Action> sim )
				{
					return new double[] { 0.0 };
				}
			};
			final EvaluationFunction<ChainWalk.State, ChainWalk.Action> rollout_evaluator
				= RolloutEvaluator.create( rollout_policy, config_.discount,
										   rollout_width, rollout_depth, heuristic );
			return rollout_evaluator;
		}

		@Override
		public EpisodeListener<ChainWalk.State, ChainWalk.Action> getVisualization()
		{
			return null;
		}

		@Override
		public ActionGenerator<ChainWalk.State, ChainWalk.Action> getActionGenerator()
		{
			return world_.new ActionGen( config_.rng );
		}

		@Override
		public String name()
		{
			return "chain_walk_" + world_;
		}

		@Override
		public ChainWalk.State initialState()
		{
			return world_.new State();
		}
		
	}
	
	// -----------------------------------------------------------------------
	
	private static class BlackjackDomain extends Domain<BlackjackState, FactoredRepresentation<BlackjackState>,
														BlackjackAction, FactoredRepresenter<BlackjackState, FactoredRepresentation<BlackjackState>>>
	{
		private final Configuration config_;
		private final BlackjackParameters params_;
		
		private final int nagents_ = 1;
		
		public BlackjackDomain( final Configuration config )
		{
			config_ = config;
			params_ = new BlackjackParameters();
		}
		
		@Override
		public BlackjackState initialState()
		{
			final Deck deck = new InfiniteDeck( config_.rng.nextInt() );
			return new BlackjackState( deck, nagents_, params_ );
		}
		
		@Override
		public ResetSimulator<BlackjackState, BlackjackAction> createSimulator( final BlackjackState s )
		{
			return ResetAdapter.of( new BlackjackSimulator( s ) );
		}

		@Override
		public FactoredRepresenter<BlackjackState, FactoredRepresentation<BlackjackState>> getBaseRepresenter()
		{
			return new BlackjackPrimitiveRepresenter( nagents_ );
//			return new BlackjackAggregator();
		}

		@Override
		public EvaluationFunction<BlackjackState, BlackjackAction> getEvaluator()
		{
			final int rollout_width = 1;
			final int rollout_depth = Integer.MAX_VALUE;
			final Policy<BlackjackState, JointAction<BlackjackAction>> rollout_policy
				= new RandomPolicy<BlackjackState, JointAction<BlackjackAction>>(
					0 /*Player*/, config_.rng.nextInt(),
					SingleAgentJointActionGenerator.create( getActionGenerator() ) );
			final EvaluationFunction<BlackjackState, BlackjackAction> heuristic
				= new EvaluationFunction<BlackjackState, BlackjackAction>() {
				@Override
				public double[] evaluate( final Simulator<BlackjackState, BlackjackAction> sim )
				{
					return new double[] { 0.0 };
				}
			};
			final EvaluationFunction<BlackjackState, BlackjackAction> rollout_evaluator
				= RolloutEvaluator.create( rollout_policy, config_.discount,
										   rollout_width, rollout_depth, heuristic );
			return rollout_evaluator;
		}

		@Override
		public EpisodeListener<BlackjackState, BlackjackAction> getVisualization()
		{
			return null;
		}

		@Override
		public ActionGenerator<BlackjackState, BlackjackAction> getActionGenerator()
		{
			return new BlackjackActionGenerator();
		}

		@Override
		public String name()
		{
			return "blackjack_" + params_.max_score;
		}
	}
	
	// -----------------------------------------------------------------------
	
	private static class TaxiDomain extends Domain<TaxiState, FactoredRepresentation<TaxiState>,
														TaxiAction, PrimitiveTaxiRepresenter>
	{
		private final Configuration config_;
		private final TaxiState exemplar_state_;
		
		private final int nagents_ = 1;
		
		private final int Nother_taxis;
		private final double slip;
		
		public TaxiDomain( final Configuration config )
		{
			config_ = config;
			Nother_taxis = config_.getInt( "taxi.Nother_taxis" );
			slip = config_.getDouble( "taxi.slip" );
			exemplar_state_ = initialState();
		}
		
		@Override
		public TaxiState initialState()
		{
			return TaxiWorlds.dietterich2000( config_.rng, Nother_taxis, slip );
		}
		
		@Override
		public ResetSimulator<TaxiState, TaxiAction> createSimulator( final TaxiState s )
		{
			final int T = config_.getInt( "taxi.T" );
			final TaxiSimulator sim = new TaxiSimulator( config_.rng, s, slip, T );
			return ResetAdapter.of( sim );
		}

		@Override
		public PrimitiveTaxiRepresenter getBaseRepresenter()
		{
			return new PrimitiveTaxiRepresenter( exemplar_state_.Nother_taxis, exemplar_state_.locations.size() );
		}

		@Override
		public EvaluationFunction<TaxiState, TaxiAction> getEvaluator()
		{
			final int rollout_width = 1;
			final int rollout_depth = 10; //Integer.MAX_VALUE;
			final Policy<TaxiState, JointAction<TaxiAction>> rollout_policy
				= new RandomPolicy<TaxiState, JointAction<TaxiAction>>(
					0 /*Player*/, config_.rng.nextInt(),
					SingleAgentJointActionGenerator.create( getActionGenerator() ) );
			final EvaluationFunction<TaxiState, TaxiAction> heuristic = new EvaluationFunction<TaxiState, TaxiAction>() {
				@Override
				public double[] evaluate( final Simulator<TaxiState, TaxiAction> sim )
				{
					final TaxiState s = sim.state();
					double d = 0.0;
					final int[] p;
					if( s.passenger != TaxiState.IN_TAXI ) {
						p = s.locations.get( s.passenger );
						d += Fn.distance_l1( s.taxi, p );
					}
					else {
						p = s.taxi;
					}
					d += Fn.distance_l1( p, s.locations.get( s.destination ) );
					return new double[] { -d };
				}
			};
			final EvaluationFunction<TaxiState, TaxiAction> rollout_evaluator
				= RolloutEvaluator.create( rollout_policy, config_.discount,
										   rollout_width, rollout_depth, heuristic );
			return rollout_evaluator;
		}

		@Override
		public EpisodeListener<TaxiState, TaxiAction> getVisualization()
		{
//			final int scale = 20;
//			final TaxiVisualization vis = new TaxiVisualization(
//				null, exemplar_state_.topology, exemplar_state_.locations, scale );
//			return vis.updater( 10 );
			
			return null;
		}

		@Override
		public ActionGenerator<TaxiState, TaxiAction> getActionGenerator()
		{
			return new TaxiActionGenerator();
		}

		@Override
		public String name()
		{
			return "taxi_" + config_.getInt( "taxi.Nother_taxis" );
		}
	}
	
	// -----------------------------------------------------------------------
	
	private static class YahtzeeDomain extends Domain<YahtzeeState, FactoredRepresentation<YahtzeeState>,
													  YahtzeeAction, PrimitiveYahtzeeRepresenter>
	{
		private final Configuration config_;
		
		public YahtzeeDomain( final Configuration config )
		{
			config_ = config;
		}
		
		@Override
		public YahtzeeState initialState()
		{
			return new YahtzeeState( config_.rng );
		}
		
		@Override
		public ResetSimulator<YahtzeeState, YahtzeeAction> createSimulator( final YahtzeeState s )
		{
			return ResetAdapter.of( new YahtzeeSimulator( s ) );
		}

		@Override
		public PrimitiveYahtzeeRepresenter getBaseRepresenter()
		{
			return new PrimitiveYahtzeeRepresenter();
		}

		@Override
		public EvaluationFunction<YahtzeeState, YahtzeeAction> getEvaluator()
		{
			final int rollout_width = 1;
			final int rollout_depth = Integer.MAX_VALUE;
			final Policy<YahtzeeState, JointAction<YahtzeeAction>> rollout_policy
				= new RandomPolicy<YahtzeeState, JointAction<YahtzeeAction>>(
					0 /*Player*/, config_.rng.nextInt(),
					SingleAgentJointActionGenerator.create( getActionGenerator() ) );
			final EvaluationFunction<YahtzeeState, YahtzeeAction> heuristic
				= new EvaluationFunction<YahtzeeState, YahtzeeAction>() {
				@Override
				public double[] evaluate( final Simulator<YahtzeeState, YahtzeeAction> sim )
				{
					return new double[] { 0.0 };
				}
			};
			final EvaluationFunction<YahtzeeState, YahtzeeAction> rollout_evaluator
				= RolloutEvaluator.create( rollout_policy, config_.discount,
										   rollout_width, rollout_depth, heuristic );
			return rollout_evaluator;
		}

		@Override
		public EpisodeListener<YahtzeeState, YahtzeeAction> getVisualization()
		{
			return null;
		}

		@Override
		public ActionGenerator<YahtzeeState, YahtzeeAction> getActionGenerator()
		{
//			return new CategoryActionGenerator();
//			return new SmartActionGenerator();
//			return new YahtzeeActionGenerator();
			return new YahtzeePhasedActionGenerator();
		}

		@Override
		public String name()
		{
			return "yahtzee_" + getActionGenerator();
		}
	}
	
	// -----------------------------------------------------------------------
	
	private static class FroggerDomain extends Domain<FroggerState, FactoredRepresentation<FroggerState>,
													  FroggerAction, RelativeFroggerRepresenter>
	{
		private final Configuration config_;
		
		private final FroggerParameters params_ = new FroggerParameters();
		private final LanesToGoHeuristic heuristic_ = new LanesToGoHeuristic();
		
		public FroggerDomain( final Configuration config )
		{
			config_ = config;
		}
		
		@Override
		public FroggerState initialState()
		{
			return new FroggerState( params_ );
		}
		
		@Override
		public ResetSimulator<FroggerState, FroggerAction> createSimulator( final FroggerState s )
		{
			return ResetAdapter.of( new FroggerSimulator( config_.rng, s ) );
		}

		@Override
		public RelativeFroggerRepresenter getBaseRepresenter()
		{
//			return new PrimitiveFroggerRepresenter( params_ );
			// FIXME: Make 'vision' a parameter
			return new RelativeFroggerRepresenter( params_, 3 );
		}

		@Override
		public EvaluationFunction<FroggerState, FroggerAction> getEvaluator()
		{
			return heuristic_;
		}

		@Override
		public EpisodeListener<FroggerState, FroggerAction> getVisualization()
		{
//			final FroggerVisualization vis = new FroggerVisualization( null, params_, 20 );
//			return vis.updater( 1000 );
			
			return null;
		}

		@Override
		public ActionGenerator<FroggerState, FroggerAction> getActionGenerator()
		{
			final FroggerActionGenerator g = new FroggerActionGenerator();
			return g;
		}
		
		@Override
		public String name()
		{
			return "frogger_" + getBaseRepresenter();
		}
	}
	
	// -----------------------------------------------------------------------
	
	private static class RacegridDomain extends Domain<RacegridState, FactoredRepresentation<RacegridState>,
													  RacegridAction, PrimitiveRacegridRepresenter>
	{
		private final Configuration config_;
		
		private final String circuit_name_;
		private final TerrainType[][] circuit_;
		private ShortestPathHeuristic heuristic_ = null;
		
		public RacegridDomain( final Configuration config )
		{
			config_ = config;
			circuit_name_ = config.get( "racegrid.circuit" );
			
			final RacegridState s = initialState();
			circuit_ = s.terrain;
			heuristic_ = new ShortestPathHeuristic( s, 2*config_.getInt( "racegrid.scale" ) );
		}
		
		@Override
		public RacegridState initialState()
		{
			final int scale = config_.getInt( "racegrid.scale" );
			if( "bbs_small".equals( circuit_name_ ) ) {
				return RacegridCircuits.barto_bradtke_singh_SmallTrack( config_.rng, scale );
			}
			else if( "bbs_large".equals( circuit_name_ ) ) {
				return RacegridCircuits.barto_bradtke_singh_LargeTrack( config_.rng, scale );
			}
			else {
				throw new IllegalArgumentException( "racegrid.circuit = " + circuit_name_ );
			}
		}
		
		@Override
		public ResetSimulator<RacegridState, RacegridAction> createSimulator( final RacegridState s )
		{
			final RacegridSimulator sim = new RacegridSimulator(
				config_.rng, s, config_.getDouble( "racegrid.slip" ) );
			return ResetAdapter.of( sim );
		}

		@Override
		public PrimitiveRacegridRepresenter getBaseRepresenter()
		{
			return new PrimitiveRacegridRepresenter();
		}

		@Override
		public EvaluationFunction<RacegridState, RacegridAction> getEvaluator()
		{
			return heuristic_.create();
		}

		@Override
		public EpisodeListener<RacegridState, RacegridAction> getVisualization()
		{
			final RacegridVisualization vis = new RacegridVisualization( null, circuit_, 10 );
			return vis.updater( 500 );
		}

		@Override
		public ActionGenerator<RacegridState, RacegridAction> getActionGenerator()
		{
			return new RacegridActionGenerator();
		}

		@Override
		public String name()
		{
			return "racegrid_" + circuit_name_;
		}
	}
	
	// -----------------------------------------------------------------------
	
	private static class RaceCarDomain extends Domain<RacetrackState, FactoredRepresentation<RacetrackState>,
													  RacetrackAction, PrimitiveRacetrackRepresenter>
	{
		private final Configuration config_;
		
		private final Circuit circuit_;
		
		public RaceCarDomain( final Configuration config )
		{
			config_ = config;
			circuit_ = Circuits.PaperClip( 400, 150 );
		}
		
		@Override
		public ResetSimulator<RacetrackState, RacetrackAction> createSimulator()
		{
			final RacetrackState s = new RacetrackState( circuit_ );
			final RacetrackSimulator sim = new RacetrackSimulator(
				config_.rng, s, config_.getDouble( "race_car.control_noise" ), 0.0 );
			return ResetAdapter.of( sim );
		}

		@Override
		public PrimitiveRacetrackRepresenter getBaseRepresenter()
		{
			return new PrimitiveRacetrackRepresenter();
//			final double scale = 0.5;
//			final GridRepresenter<RacetrackState> grid = new GridRepresenter<RacetrackState>(
//				base_repr, new double[] { scale*1.0, scale*1.0, Math.PI / 8, 1.0 } );
		}

		@Override
		public EvaluationFunction<RacetrackState, RacetrackAction> getEvaluator()
		{
			return new SectorEvaluator( s.terminal_velocity() );
		}

		@Override
		public EpisodeListener<RacetrackState, RacetrackAction> getVisualization()
		{
//			final RacetrackVisualization vis = new RacetrackVisualization( circuit_, null, 2 );
//			return vis.updater( 0 );
			
			return null;
		}

		@Override
		public ActionGenerator<RacetrackState, RacetrackAction> getActionGenerator()
		{
			return new RacetrackActionGenerator();
		}

		@Override
		public String name()
		{
			return "racecar";
		}
	}
	
	// -----------------------------------------------------------------------
	
	private static class TamariskDomain extends Domain<TamariskState, FactoredRepresentation<TamariskState>,
													  TamariskAction, IndicatorTamariskRepresenter>
	{
		private final Configuration config_;
		private final TamariskParameters params_;
		private final int branching_;
		
		public TamariskDomain( final Configuration config )
		{
			config_ = config;
			params_ = new TamariskParameters(
				config_.rng, config_.getInt( "tamarisk.Nreaches" ), config_.getInt( "tamarisk.Nhabitats" ) );
			branching_ = config_.getInt( "tamarisk.branching" );
		}
		
		@Override
		public TamariskState initialState()
		{
			final DirectedGraph<Integer, DefaultEdge> g = params_.createBalancedGraph( branching_ );
			return new TamariskState( config_.rng, params_, g );
		}
		
		@Override
		public ResetSimulator<TamariskState, TamariskAction> createSimulator( final TamariskState s )
		{
			return ResetAdapter.of( new TamariskSimulator( s ) );
		}

		@Override
		public IndicatorTamariskRepresenter getBaseRepresenter()
		{
			return new IndicatorTamariskRepresenter( params_ );
		}

		@Override
		public EvaluationFunction<TamariskState, TamariskAction> getEvaluator()
		{
			final int rollout_width = 1;
			final int rollout_depth = 10 ; //Integer.MAX_VALUE;
			final Policy<TamariskState, JointAction<TamariskAction>> rollout_policy
				= new RandomPolicy<TamariskState, JointAction<TamariskAction>>(
					0 /*Player*/, config_.rng.nextInt(),
					SingleAgentJointActionGenerator.create( getActionGenerator() ) );
			final EvaluationFunction<TamariskState, TamariskAction> heuristic
				= new EvaluationFunction<TamariskState, TamariskAction>() {
				@Override
				public double[] evaluate( final Simulator<TamariskState, TamariskAction> sim )
				{
					return new double[] { 0.0 };
				}
			};
			final EvaluationFunction<TamariskState, TamariskAction> rollout_evaluator
				= RolloutEvaluator.create( rollout_policy, config_.discount,
										   rollout_width, rollout_depth, heuristic );
			return rollout_evaluator;
		}

		@Override
		public EpisodeListener<TamariskState, TamariskAction> getVisualization()
		{
			return null;
		}

		@Override
		public ActionGenerator<TamariskState, TamariskAction> getActionGenerator()
		{
			return new TamariskActionGenerator();
		}

		@Override
		public String name()
		{
			return "tamarisk_" + params_.Nreaches + "_" + params_.Nhabitats + "_" + branching_;
		}
	}
	
	// -----------------------------------------------------------------------
	
	private static class FuelWorldDomain extends Domain<FuelWorldState, FactoredRepresentation<FuelWorldState>,
													  FuelWorldAction, PrimitiveFuelWorldRepresenter>
	{
		private final Configuration config_;
		
		public FuelWorldDomain( final Configuration config )
		{
			config_ = config;
		}
		
		@Override
		public FuelWorldState initialState()
		{
			if( "default".equals( config_.get( "fuelworld.variant" ) ) ) {
				return FuelWorldState.createDefault( config_.rng );
			}
			else if( "choices".equals( config_.get( "fuelworld.variant" ) ) ) {
				return FuelWorldState.createDefaultWithChoices( config_.rng );
			}
			else {
				throw new IllegalArgumentException( "fuelworld.variant = " + config_.get( "fuelworld.variant" ) );
			}
		}
		
		@Override
		public ResetSimulator<FuelWorldState, FuelWorldAction> createSimulator( final FuelWorldState s )
		{
			return ResetAdapter.of( new FuelWorldSimulator( s ) );
		}

		@Override
		public PrimitiveFuelWorldRepresenter getBaseRepresenter()
		{
			return new PrimitiveFuelWorldRepresenter();
		}
		
		@Override
		public ArrayList<double[]> getVariableRanges()
		{
			final ArrayList<double[]> ranges = new ArrayList<double[]>();
			ranges.add( null );
			ranges.add( new double[] { 0, FuelWorldState.fuel_capacity } );
			ranges.add( null );
			return ranges;
		}

		@Override
		public EvaluationFunction<FuelWorldState, FuelWorldAction> getEvaluator()
		{
			final int rollout_width = 1;
			final int rollout_depth = Integer.MAX_VALUE;
			final Policy<FuelWorldState, JointAction<FuelWorldAction>> rollout_policy
				= new RandomPolicy<FuelWorldState, JointAction<FuelWorldAction>>(
					0 /*Player*/, config_.rng.nextInt(),
					SingleAgentJointActionGenerator.create( getActionGenerator() ) );
			final EvaluationFunction<FuelWorldState, FuelWorldAction> heuristic
				= new EvaluationFunction<FuelWorldState, FuelWorldAction>() {
				@Override
				public double[] evaluate( final Simulator<FuelWorldState, FuelWorldAction> sim )
				{
					return new double[] { 0.0 };
				}
			};
			final EvaluationFunction<FuelWorldState, FuelWorldAction> rollout_evaluator
				= RolloutEvaluator.create( rollout_policy, config_.discount,
										   rollout_width, rollout_depth, heuristic );
			return rollout_evaluator;
		}

		@Override
		public EpisodeListener<FuelWorldState, FuelWorldAction> getVisualization()
		{
			return null;
		}

		@Override
		public ActionGenerator<FuelWorldState, FuelWorldAction> getActionGenerator()
		{
			return new FuelWorldActionGenerator();
		}

		@Override
		public String name()
		{
			return "fuelworld";
		}
	}
	
	// -----------------------------------------------------------------------
	
	private static class CliffWorldDomain extends Domain<CliffWorld.State, FactoredRepresentation<CliffWorld.State>,
													  CliffWorld.Action, CliffWorld.PrimitiveRepresenter>
	{
		private final Configuration config_;
		
		private final int L;
		private final int W;
		private final int F;
		
		public CliffWorldDomain( final Configuration config )
		{
			config_ = config;
			L = config_.getInt( "cliffworld.L" );
			W = config_.getInt( "cliffworld.W" );
			F = config_.getInt( "cliffworld.F" );
		}
		
		@Override
		public CliffWorld.State initialState()
		{
			return new CliffWorld.State( config_.rng, L, W, F );
		}
		
		@Override
		public ResetSimulator<CliffWorld.State, CliffWorld.Action> createSimulator( final CliffWorld.State s )
		{
			return ResetAdapter.of( new CliffWorld.Simulator( s, config_.rng ) );
		}

		@Override
		public CliffWorld.PrimitiveRepresenter getBaseRepresenter()
		{
			return new CliffWorld.PrimitiveRepresenter();
		}
		
		@Override
		public ArrayList<double[]> getVariableRanges()
		{
			final ArrayList<double[]> ranges = new ArrayList<double[]>();
			ranges.add( null );
			ranges.add( null );
			ranges.add( new double[] { 0, W - 1 } );
			return ranges;
		}

		@Override
		public EvaluationFunction<CliffWorld.State, CliffWorld.Action> getEvaluator()
		{
			final int rollout_width = 1;
			final int rollout_depth = Integer.MAX_VALUE;
			final Policy<CliffWorld.State, JointAction<CliffWorld.Action>> rollout_policy
				= new RandomPolicy<CliffWorld.State, JointAction<CliffWorld.Action>>(
					config_.rng, SingleAgentJointActionGenerator.create( getActionGenerator() ) );
			final EvaluationFunction<CliffWorld.State, CliffWorld.Action> heuristic
				= new EvaluationFunction<CliffWorld.State, CliffWorld.Action>() {
				@Override
				public double[] evaluate( final Simulator<CliffWorld.State, CliffWorld.Action> sim )
				{
					return new double[] { 0.0 };
				}
			};
			final EvaluationFunction<CliffWorld.State, CliffWorld.Action> rollout_evaluator
				= RolloutEvaluator.create( rollout_policy, config_.discount,
										   rollout_width, rollout_depth, heuristic );
			return rollout_evaluator;
		}

		@Override
		public EpisodeListener<CliffWorld.State, CliffWorld.Action> getVisualization()
		{
			return null;
		}

		@Override
		public ActionGenerator<CliffWorld.State, CliffWorld.Action> getActionGenerator()
		{
			return new CliffWorld.Actions( config_.rng );
		}

		@Override
		public String name()
		{
			return "cliffworld";
		}
	}
	
	// -----------------------------------------------------------------------
	
	private static class RddlDomain extends Domain<RDDLState, FactoredRepresentation<RDDLState>,
												   RDDLAction, RddlRepresenter>
	{
		private final Configuration config_;
		
		private final RddlSpec rddl_spec;
		
		public RddlDomain( final Configuration config )
		{
			config_ = config;
			
			final File domain = new File( config.root_directory, config.get( "rddl.domain" ) + ".rddl" );
			final File instance = new File( config.root_directory, config.get( "rddl.instance" ) + ".rddl" );
			final int nagents = 1;
			final int seed = config.rng.nextInt();
			final String name = config.get( "rddl.instance" );
			
			rddl_spec = new RddlSpec( domain, instance, nagents, seed, name );
		}
		
		@Override
		public RDDLState initialState()
		{
			return rddl_spec.createInitialState();
		}
		
		@Override
		public ResetSimulator<RDDLState, RDDLAction> createSimulator( final RDDLState s )
		{
			return new RddlSimulatorAdapter( rddl_spec, s );
		}

		@Override
		public RddlRepresenter getBaseRepresenter()
		{
			return new RddlRepresenter( rddl_spec );
		}
		
		@Override
		public ArrayList<double[]> getVariableRanges()
		{
			// FIXME:
			return null;
		}

		@Override
		public EvaluationFunction<RDDLState, RDDLAction> getEvaluator()
		{
			final int rollout_width = 1;
			final int rollout_depth = config_.getInt( "rddl.rollout.depth" ); // 1; //Integer.MAX_VALUE;
			final Policy<RDDLState, JointAction<RDDLAction>> rollout_policy
				= new RandomPolicy<RDDLState, JointAction<RDDLAction>>(
					0 /*Player*/, config_.rng.nextInt(),
					SingleAgentJointActionGenerator.create( getActionGenerator() ) );
			final EvaluationFunction<RDDLState, RDDLAction> heuristic
				= new EvaluationFunction<RDDLState, RDDLAction>() {
				@Override
				public double[] evaluate( final Simulator<RDDLState, RDDLAction> sim )
				{
					return new double[] { 0.0 };
				}
			};
			final EvaluationFunction<RDDLState, RDDLAction> rollout_evaluator
				= RolloutEvaluator.create( rollout_policy, config_.discount,
										   rollout_width, rollout_depth, heuristic );
			return rollout_evaluator;
		}

		@Override
		public EpisodeListener<RDDLState, RDDLAction> getVisualization()
		{
			return null;
		}

		@Override
		public ActionGenerator<RDDLState, RDDLAction> getActionGenerator()
		{
			return new RddlActionGenerator( rddl_spec );
		}

		@Override
		public String name()
		{
			return "rddl." + config_.get( "rddl.domain" );
		}
	}
	
	// -----------------------------------------------------------------------
	
	private static String deriveDatasetName( final String base, final int iter )
	{
		if( iter == -1 ) {
			return base;
		}
		
		final String basename = FilenameUtils.getBaseName( base );
		final String ext = FilenameUtils.getExtension( base );
		return basename + "_" + iter + (ext.isEmpty() ? "" : FilenameUtils.EXTENSION_SEPARATOR + ext);
	}
	
	private static <S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
	void runExperiment( final Configuration config,	final Domain<S, X, A, R> domain ) throws Exception
	{
		
		final AbstractionDiscoveryAlgorithm<S, X, A, R> bootstrap
			= createAbstractionDiscoveryAlgorithm( config.get( "abstraction.bootstrap" ), config, domain );
		final AbstractionDiscoveryAlgorithm<S, X, A, R> discovery
			= createAbstractionDiscoveryAlgorithm( config.get( "abstraction.discovery" ), config, domain );
		
		System.out.println( "****************************************" );
		System.out.println( "game = " + config.Ntest_episodes
							+ " x " + config.get( "abstraction.bootstrap" ) + " -> "
							+ config.get( "abstraction.discovery" ) + "." + config.get( "multiclass.classifier" )
							+ " / " + domain.getBaseRepresenter() );
		System.out.println( "UCT: max_depth = " + config.getInt( "uct.max_depth" )
							+ ", max_action_visits = " + config.getInt( "max_action_visits" ) );

		final Representer<S, Representation<S>> Crepr = new ReprWrapper<S>( domain.getBaseRepresenter() );
		
		SingleInstanceDataset<A> running_dataset = null;
		
		for( int iter = 0; iter < config.Niterations; ++iter ) {
			System.out.println( "Iteration " + iter );
			
			final AbstractionDiscoveryAlgorithm<S, X, A, R> algorithm;
			if( "create".equals( config.model ) ) {
				if( iter > 0 ) {
					final String f = deriveDatasetName( config.training_data_single, iter - 1 );
					System.out.println( "[Loading '" + f + "']" );
					final Dataset<A> training_data = new Dataset<A>();
					training_data.single = WekaUtil.readLabeledDataset(
						new File( config.data_directory, f + ".arff" ) );
					
					// Train classifier
					System.out.println( "[Training classifier]" );
					discovery.trainRepresenter( training_data, domain.getBaseRepresenter(), iter );
					discovery.writeModel( iter );
					algorithm = discovery;
				}
				else if( iter == 0 ) {
					if( !"none".equals( config.training_data_single ) ) {
						System.out.println( "[Loading '" + config.training_data_single + "']" );
						final Dataset<A> training_data = new Dataset<A>();
						training_data.single = WekaUtil.readLabeledDataset(
							new File( config.root_directory, config.training_data_single + ".arff" ) );
						
						// Train classifier
						System.out.println( "[Training classifier]" );
						bootstrap.trainRepresenter( training_data, domain.getBaseRepresenter(), iter );
						bootstrap.writeModel( iter );
					}
					// Use flat reprentation
					algorithm = bootstrap;
				}
				else {
					throw new IllegalStateException(
						"Missing training data '" + deriveDatasetName( config.training_data_single, iter - 1 ) + "'" );
				}
				
//						if( !"none".equals( config.training_data_pair ) ) {
//							final String f = config.training_data_pair;
//							System.out.println( "[Loading '" + f + "']" );
//							training_data.pair = WekaUtil.readLabeledDataset( new File( config.data_directory, f ) );
//						}
			}
			else if( !"none".equals( config.model ) ) {
				System.out.println( "[Loading model '" + config.model + "']" );
				assert( config.Niterations == 1 );
				// FIXME: Is this right, given the new bootstrap/discovery distinction?
				bootstrap.loadModel( domain.getBaseRepresenter(), iter );
				algorithm = bootstrap;
			}
			else {
				algorithm = bootstrap;
			}
			
			final Csv.Writer data_out = createDataWriter( config, algorithm, iter );
			
			final SolvedStateAccumulator<S, X, A> labeled_states
				= new SolvedStateAccumulator<S, X, A>( domain.getBaseRepresenter() );
			
			// Test
			System.out.println( "[Running tree search]" );
//			final int order_min = config.Ntest_episodes_order_min;
//			final int order_max = config.Ntest_episodes_order_max;
//			for( int order = order_min; order <= order_max; ++order ) {
//				final int Ntest_episodes = (int) Math.pow( 2, order );
//				System.out.println( "Ntest_episodes = " + Ntest_episodes );
			
				final MctsVisitor<S, A> test_visitor = new DefaultMctsVisitor<S, A>();
				
				final ArrayList<Pair<ArrayList<A>, TDoubleList>> qtable =
					runGames( config, domain, algorithm, test_visitor, labeled_states, Crepr, data_out, iter );
				
				// Gather training examples
				// TODO: Should we have a switch to disable this?
				System.out.println( "[Gathering training_data.single]" );

				final SingleInstanceDataset<A> single = makeSingleInstanceDataset(
						config, domain.getBaseRepresenter().attributes(),
						labeled_states.Phi_, labeled_states.actions_, qtable, iter );
				
				if( running_dataset == null ) {
					System.out.println( "[Storing initial training data]" );
					running_dataset = single;
				}
				else {
					System.out.println( "[Merging new training data]" );
					final double training_instance_discount = 0.5; // TODO: Make it a parameter
					combineInstances( config.rng, running_dataset, single, training_instance_discount );
					running_dataset.instances.setRelationName( single.instances.relationName() );
					System.out.println( "New dataset size = " + running_dataset.instances.size() );
				}
				
				if( config.getBoolean( "output.labels" ) ) {
					WekaUtil.writeDataset( config.data_directory, running_dataset.instances );
					writeActionKey( config, running_dataset, iter );
					if( running_dataset.qtable != null ) {
						writeQTable( config, running_dataset, iter );
					}
				}
				
//				WekaUtil.writeDataset( config.data_directory, single.instances );
//				writeActionKey( config, single, iter );
				
//			}
			
							
//				runGames( "train", config, domain, discovery, config.Ntrain_episodes, config.Ntrain_games,
//						  discovery.getTrainingMctsVisitor(), discovery.getTrainingEpisodeListener(), Crepr, data_out, iter );
				
				
			if( "create".equals( config.training_data_pair ) ) {
				throw new UnsupportedOperationException( "Look for regressions in this code before using it" );
//				if( training_data.single == null ) {
//					System.out.println( "[Loading training_data.single]" );
//					training_data.single = config.loadSingleInstances();
//				}
//				System.out.println( "[Constructing training_data.pair]" );
//				training_data.pair = discovery.makePairwiseInstances(
//					training_data.single, domain.getBaseRepresenter() );
//
//				WekaUtil.writeDataset( config.data_directory, training_data.pair );
			}
			
			
		}
	}
	
	private static <S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
	ArrayList<Pair<ArrayList<A>, TDoubleList>> runGames(
		final Configuration config, final Domain<S, X, A, R> domain,
		final AbstractionDiscoveryAlgorithm<S, X, A, R> discovery,
		final MctsVisitor<S, A> mcts_visitor, final EpisodeListener<S, A> listener,
		final Representer<S, Representation<S>> Crepr,
		final Csv.Writer data_out, final int iter ) throws Exception
	{
//		assert( "train".equals( phase ) || "test".equals( phase ) );
		
		final int print_interval = 1000;
		
		final MeanVarianceAccumulator ret = new MeanVarianceAccumulator();
		final MeanVarianceAccumulator steps = new MeanVarianceAccumulator();
		final MinMaxAccumulator steps_minmax = new MinMaxAccumulator();
		final boolean use_visualization = config.getBoolean( "log.visualization" );
		
//		final ArrayList<ArrayList<Pair<A, Double>>> qtable = new ArrayList<ArrayList<Pair<A, Double>>>();
		
		for( int i = 0; i < config.Ntest_games; ++i ) {
			if( i % print_interval == 0 ) {
				System.out.println( "Episode " + i );
			}

			final Policy<S, A> pi = discovery.getControlPolicy( config, domain );
			
			final S s0 = domain.initialState();
			final ResetSimulator<S, A> sim = domain.createSimulator( s0 );
			final Episode<S, A> episode	= new Episode<S, A>( sim, new JointPolicy<S, A>( pi ) );
			final RewardAccumulator<S, A> racc = new RewardAccumulator<S, A>( sim.nagents(), config.discount );
			episode.addListener( racc );
			
//			final LoggingEpisodeListener<S, A> epi_log = new LoggingEpisodeListener<S, A>();
//			episode.addListener( epi_log );
			
			if( listener != null ) {
				episode.addListener( listener );
			}
			if( use_visualization ) {
				final EpisodeListener<S, A> vis = domain.getVisualization();
				if( vis != null ) {
					episode.addListener( vis );
				}
				else {
					System.out.println( "Warning: No visualization implemented" );
				}
			}
			
			episode.run();
			
			ret.add( racc.v()[0] );
			steps.add( racc.steps() );
			steps_minmax.add( racc.steps() );
			
//			System.out.println( "Reward: " + racc.v()[0] );
		}
		
//		for( final ArrayList<Pair<A, Double>> q : qtable ) {
//			System.out.println( q );
//		}
		
		System.out.println( "****************************************" );
		System.out.println( "Average return: " + ret.mean() );
		System.out.println( "Return variance: " + ret.variance() );
		System.out.println( "Confidence: " + ret.confidence() );
		
		System.out.println( "Steps (mean): " + steps.mean() );
		System.out.println( "Steps (var): " + steps.variance() );
		System.out.println( "Steps (min/max): " + steps_minmax.min() + " -- " + steps_minmax.max() );
		
		// See: createDataWriter for correct column order
		data_out.cell( config.experiment_name ).cell( domain.getBaseRepresenter() ).cell( domain.getActionGenerator() )
				.cell( iter ).cell( config.Ntest_episodes ).cell( config.Ntest_games )
				.cell( ret.mean() ).cell( ret.variance() ).cell( ret.confidence() )
				.cell( steps.mean() ).cell( steps.variance() ).cell( steps_minmax.min() ).cell( steps_minmax.max() );
				
		discovery.writeStatisticsRecord( data_out );
		for( final String k : config.keys() ) {
			data_out.cell( config.get( k ) );
		}
		data_out.newline();
		System.out.println();
		
		return discovery.getQTable();
	}
	
	private static <S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
	Csv.Writer createDataWriter(
		final Configuration config, final AbstractionDiscoveryAlgorithm<S, X, A, R> discovery, final int iter )
	{
		Csv.Writer data_out;
		try {
			data_out = new Csv.Writer( new PrintStream( new File( config.data_directory, "data_" + iter + ".csv" ) ) );
		}
		catch( final FileNotFoundException ex ) {
			throw new RuntimeException( ex );
		}
		data_out.cell( "experiment_name" ).cell( "abstraction" ).cell( "actions" ).cell( "iteration" )
				.cell( "Nepisodes" ).cell( "Ngames" )
				.cell( "mean" ).cell( "var" ).cell( "conf" )
				.cell( "steps_mean" ).cell( "steps_var" ).cell( "steps_min" ).cell( "steps_max" );
				
		discovery.writeStatisticsHeaders( data_out );
		for( final String k : config.keys() ) {
			data_out.cell( k );
		}
		data_out.newline();
		
		return data_out;
	}
	
	// -----------------------------------------------------------------------

	public static class Configuration implements KeyValueStore
	{
		private final KeyValueStore config_;
		
//		public final String abstraction;
		public final String model;
		public final String domain;
		// FIXME: Why is 'root_directory' a String?
		public final String root_directory;
		public final String training_data_single;
		public final String training_data_pair;
		public final String labels;
				
//		public final int Ntrain_episodes;
//		public final int Ntrain_games;
//		public final int max_training_size;
		public final int Ntest_episodes_order;
		public final int Ntest_episodes;
//		public final int Ntest_episodes_order_min;
//		public final int Ntest_episodes_order_max;
		public final int Ntest_games;
		public final double uct_c;
		public final double discount;
		public final int seed;
		public final int Niterations;
		
		public final RandomGenerator rng;
//		public final File training_directory;
		public final File data_directory;
		public final String experiment_name;
		
		private final Set<String> exclude_ = new HashSet<String>();
		
		public File trainSingleDirectory()
		{
			return new File( root_directory, "train_single" );
		}
		
		public File trainPairDirectory()
		{
			return new File( root_directory, "train_pair" );
		}
		
		public File modelDirectory()
		{
			return new File( root_directory, "model" );
		}
		
		public File testDirectory()
		{
			return new File( root_directory, "test" );
		}
		
		public Configuration( final String root_directory, final String experiment_name, final KeyValueStore config )
		{
			config_ = config;
			
			this.experiment_name = experiment_name;
			this.root_directory = root_directory;
			exclude_.add( "root_directory" );
			domain = config.get( "domain" );
			exclude_.add( "domain" );
//			abstraction = config.get( "abstraction" );
			exclude_.add( "abstraction" );
			model = config.get( "model" );
			exclude_.add( "model" );
			training_data_single = config.get( "training_data.single" );
			exclude_.add( "training_data.single" );
			training_data_pair = config.get( "training_data.pair" );
			exclude_.add( "training_data.pair" );
			labels = config.get( "labels" );
			exclude_.add( "labels" );
			
//			Ntrain_episodes = config.getInt( "Ntrain_episodes" );
//			Ntrain_games = config.getInt( "Ntrain_games" );
//			Ntest_episodes_order_min = config.getInt( "Ntest_episodes_order_min" );
//			Ntest_episodes_order_max = config.getInt( "Ntest_episodes_order_max" );
			Ntest_episodes_order = config.getInt( "Ntest_episodes_order" );
			Ntest_episodes = 1 << Ntest_episodes_order; // 2^order
			Ntest_games = config.getInt( "Ntest_games" );
			uct_c = config.getDouble( "uct_c" );
			discount = config.getDouble( "discount" );
			seed = config.getInt( "seed" );
			Niterations = config.getInt( "Niterations" );
			rng = new MersenneTwister( seed );
			
			final StringBuilder sb = new StringBuilder();
			int count = 0;
			for( final String key : config.keys() ) {
				if( exclude_.contains( key ) ) {
					continue;
				}
				
				if( count++ == 0 ) {
					sb.append( "x" );
				}
				else {
					sb.append( "," );
				}
				sb.append( config.get( key ) );
			}
			
			data_directory = new File( root_directory, experiment_name );
			data_directory.mkdirs();
		}

		@Override
		public String get( final String key )
		{
			return config_.get( key );
		}

		@Override
		public int getInt( final String key )
		{
			return config_.getInt( key );
		}

		@Override
		public double getDouble( final String key )
		{
			return config_.getDouble( key );
		}
		
		@Override
		public boolean getBoolean( final String key )
		{
			return config_.getBoolean( key );
		}

		@Override
		public Iterable<String> keys()
		{
			return config_.keys();
		}
	}
	
	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main( final String[] args ) throws Exception
	{
		final String experiment_file = args[0];
		final File root_directory;
		if( args.length > 1 ) {
			root_directory = new File( args[1] );
		}
		else {
			root_directory = new File( "." );
		}
		final CsvConfigurationParser csv_config = new CsvConfigurationParser( new FileReader( experiment_file ) );
		final String experiment_name = FilenameUtils.getBaseName( experiment_file );
		
		final File expr_directory = new File( root_directory, experiment_name );
		expr_directory.mkdirs();
		
		for( int expr = 0; expr < csv_config.size(); ++expr ) {
			final KeyValueStore expr_config = csv_config.get( expr );
			final Configuration config = new Configuration(
					root_directory.getPath(), experiment_name, expr_config );
			
			if( "irrelevance".equals( config.domain ) ) {
				final IrrelevanceDomain domain = new IrrelevanceDomain( config, 10 );
				runExperiment( config, domain );
			}
			else if( "chain_walk".equals( config.domain ) ) {
				final ChainWalkDomain domain = new ChainWalkDomain( config );
				runExperiment( config, domain );
			}
			else if( "blackjack".equals( config.domain ) ) {
				final BlackjackDomain domain = new BlackjackDomain( config );
				runExperiment( config, domain );
			}
			else if( "taxi".equals( config.domain ) ) {
				final TaxiDomain domain = new TaxiDomain( config );
				runExperiment( config, domain );
			}
			else if( "yahtzee".equals( config.domain ) ) {
				final YahtzeeDomain domain = new YahtzeeDomain( config );
				runExperiment( config, domain );
			}
			else if( "frogger".equals( config.domain ) ) {
				final FroggerDomain domain = new FroggerDomain( config );
				runExperiment( config, domain );
			}
			else if( "racegrid".equals( config.domain ) ) {
				final RacegridDomain domain = new RacegridDomain( config );
				runExperiment( config, domain );
			}
			else if( "race_car".equals( config.domain ) ) {
				final RaceCarDomain domain = new RaceCarDomain( config );
				runExperiment( config, domain );
			}
			else if( "tamarisk".equals( config.domain ) ) {
				final TamariskDomain domain = new TamariskDomain( config );
				runExperiment( config, domain );
			}
			else if( "fuelworld".equals( config.domain ) ) {
				final FuelWorldDomain domain = new FuelWorldDomain( config );
				runExperiment( config, domain );
			}
			else if( "cliffworld".equals( config.domain ) ) {
				final CliffWorldDomain domain = new CliffWorldDomain( config );
				runExperiment( config, domain );
			}
			else if( "rddl".equals( config.domain ) ) {
				final RddlDomain domain = new RddlDomain( config );
				runExperiment( config, domain );
			}
			else {
				throw new IllegalArgumentException( "domain = " + config.domain );
			}
		}
	}

}
