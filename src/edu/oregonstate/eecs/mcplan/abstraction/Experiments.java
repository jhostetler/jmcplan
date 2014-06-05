/**
 * 
 */
package edu.oregonstate.eecs.mcplan.abstraction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.RandomPolicy;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.SingleAgentJointActionGenerator;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.domains.blackjack.BlackjackAction;
import edu.oregonstate.eecs.mcplan.domains.blackjack.BlackjackActionGenerator;
import edu.oregonstate.eecs.mcplan.domains.blackjack.BlackjackParameters;
import edu.oregonstate.eecs.mcplan.domains.blackjack.BlackjackPrimitiveRepresenter;
import edu.oregonstate.eecs.mcplan.domains.blackjack.BlackjackSimulator;
import edu.oregonstate.eecs.mcplan.domains.blackjack.BlackjackState;
import edu.oregonstate.eecs.mcplan.domains.blackjack.Deck;
import edu.oregonstate.eecs.mcplan.domains.blackjack.InfiniteDeck;
import edu.oregonstate.eecs.mcplan.domains.frogger.FroggerAction;
import edu.oregonstate.eecs.mcplan.domains.frogger.FroggerActionGenerator;
import edu.oregonstate.eecs.mcplan.domains.frogger.FroggerParameters;
import edu.oregonstate.eecs.mcplan.domains.frogger.FroggerSimulator;
import edu.oregonstate.eecs.mcplan.domains.frogger.FroggerState;
import edu.oregonstate.eecs.mcplan.domains.frogger.LanesToGoHeuristic;
import edu.oregonstate.eecs.mcplan.domains.frogger.RelativeFroggerRepresenter;
import edu.oregonstate.eecs.mcplan.domains.racegrid.PrimitiveRacegridRepresenter;
import edu.oregonstate.eecs.mcplan.domains.racegrid.RacegridAction;
import edu.oregonstate.eecs.mcplan.domains.racegrid.RacegridActionGenerator;
import edu.oregonstate.eecs.mcplan.domains.racegrid.RacegridCircuits;
import edu.oregonstate.eecs.mcplan.domains.racegrid.RacegridSimulator;
import edu.oregonstate.eecs.mcplan.domains.racegrid.RacegridState;
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
import edu.oregonstate.eecs.mcplan.domains.toy.ChainWalk;
import edu.oregonstate.eecs.mcplan.domains.toy.Irrelevance;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.PrimitiveYahtzeeRepresenter;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.ReprWrapper;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.SmartActionGenerator;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.YahtzeeAction;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.YahtzeeSimulator;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.YahtzeeState;
import edu.oregonstate.eecs.mcplan.ml.ClassifierSimilarityFunction;
import edu.oregonstate.eecs.mcplan.ml.HilbertSpace;
import edu.oregonstate.eecs.mcplan.ml.InformationTheoreticMetricLearner;
import edu.oregonstate.eecs.mcplan.ml.MatrixAlgorithms;
import edu.oregonstate.eecs.mcplan.ml.MetricConstrainedKMeans;
import edu.oregonstate.eecs.mcplan.ml.SimilarityFunction;
import edu.oregonstate.eecs.mcplan.ml.VoronoiClassifier;
import edu.oregonstate.eecs.mcplan.search.ActionNode;
import edu.oregonstate.eecs.mcplan.search.BackupRule;
import edu.oregonstate.eecs.mcplan.search.BackupRules;
import edu.oregonstate.eecs.mcplan.search.DefaultMctsVisitor;
import edu.oregonstate.eecs.mcplan.search.EvaluationFunction;
import edu.oregonstate.eecs.mcplan.search.GameTree;
import edu.oregonstate.eecs.mcplan.search.GameTreeFactory;
import edu.oregonstate.eecs.mcplan.search.GameTreeVisitor;
import edu.oregonstate.eecs.mcplan.search.MctsVisitor;
import edu.oregonstate.eecs.mcplan.search.RolloutEvaluator;
import edu.oregonstate.eecs.mcplan.search.SearchPolicy;
import edu.oregonstate.eecs.mcplan.search.StateNode;
import edu.oregonstate.eecs.mcplan.search.TreeStatisticsRecorder;
import edu.oregonstate.eecs.mcplan.search.UctSearch;
import edu.oregonstate.eecs.mcplan.sim.DefaultEpisodeListener;
import edu.oregonstate.eecs.mcplan.sim.Episode;
import edu.oregonstate.eecs.mcplan.sim.EpisodeListener;
import edu.oregonstate.eecs.mcplan.sim.RewardAccumulator;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.Csv;
import edu.oregonstate.eecs.mcplan.util.CsvConfigurationParser;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.KeyValueStore;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;
import edu.oregonstate.eecs.mcplan.util.MinMaxAccumulator;
import edu.oregonstate.eecs.mcplan.util.QuantileAccumulator;
import edu.oregonstate.eecs.mcplan.util.ReservoirSampleAccumulator;
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
		public
		void postGetAction( final JointAction<A> a )
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
		
		public SingleInstanceDataset( final Instances instances,
									  final HashMap<A, Integer> action_to_int,
									  final ArrayList<A> int_to_action )
		{
			this.instances = instances;
			this.action_to_int = action_to_int;
			this.int_to_action = int_to_action;
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
		final int iter )
	{
//		System.out.println( "data.size() = " + data.size() );
		final int[] ii = Fn.range( 0, data.size() );
		Fn.shuffle( config.rng, ii );
		
		final HashMap<A, Integer> action_to_int = new HashMap<A, Integer>();
		final ArrayList<A> int_to_action = new ArrayList<A>();
		
		final TIntArrayList counts = new TIntArrayList();
		final int max_per_label = config.getInt( "training.max_per_label" );
		
		final ArrayList<DenseInstance> instance_list = new ArrayList<DenseInstance>();
		for( final int i : ii ) {
			final A a = labels.get( i );
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
			if( c < max_per_label ) {
//				System.out.println( "Adding " + label );
				final double[] phi = Fn.append( data.get( i ), label );
				final DenseInstance instance = new DenseInstance( 1.0, phi );
				instance_list.add( instance );
				counts.set( label, c + 1 );
			}
		}
		
		final int Nlabels = int_to_action.size();
		final ArrayList<Attribute> labeled_attributes = addLabelToAttributes( attributes, Nlabels );
		
		final Instances instances = new Instances(
			config.trainingName( "single", iter ), labeled_attributes, counts.sum() );
		instances.setClassIndex( instances.numAttributes() - 1 );
		for( final DenseInstance instance : instance_list ) {
			instances.add( instance );
			instance.setDataset( instances );
		}
		
		return new SingleInstanceDataset<A>( instances, action_to_int, int_to_action );
	}
	
	private static <A> void writeActionKey( final Configuration config, final SingleInstanceDataset<A> data, final int iter )
	{
		final File f = new File( config.data_directory, config.trainingName( "single", iter ) + "_action-key.csv" );
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
	
	// -----------------------------------------------------------------------
	// Abstraction algorithms
	// -----------------------------------------------------------------------
	
	private static abstract class AbstractionDiscoveryAlgorithm<S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
	{
		public abstract MctsVisitor<S, A> getTrainingMctsVisitor();
		public abstract SolvedStateGapRecorder<Representation<S>, A> getGapRecorder();
		public abstract EpisodeListener<S, A> getTrainingEpisodeListener();
		public abstract ArrayList<double[]> getTrainingVectors();
		public abstract ArrayList<A> getTrainingLabels();
		public abstract Instances makePairwiseInstances(
			final Instances single, final FactoredRepresenter<S, X> repr );
		public abstract void writeModel( final int iter );
		public abstract Representer<S, Representation<S>> loadModel(
			final File dir, final FactoredRepresenter<S, X> base_repr, final int iter );
		public abstract Representer<S, Representation<S>> trainRepresenter(
			final Dataset<A> train, final FactoredRepresenter<S, X> base_repr, final int iter );
	}
	
	private static class NoneAbstractionDiscovery<S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		extends AbstractionDiscoveryAlgorithm<S, X, A, R>
	{
		public static <S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		NoneAbstractionDiscovery<S, X, A, R> create( final Configuration config, final Domain<S, X, A, R> domain )
		{
			return new NoneAbstractionDiscovery<S, X, A, R>( config, domain );
		}
		
		private final SolvedStateAccumulator<S, X, A> labeled_;
		
		public NoneAbstractionDiscovery( final Configuration config, final Domain<S, X, A, R> domain )
		{
//			config_ = config;
//			base_repr_ = domain.getBaseRepresenter();
			labeled_ = new SolvedStateAccumulator<S, X, A>( domain.getBaseRepresenter().create() );
//			unlabeled_ = new UnlabeledStateAccumulator<S, A>( base_repr_.create() );
		}
		
		@Override
		public MctsVisitor<S, A> getTrainingMctsVisitor()
		{
			return new DefaultMctsVisitor<S, A>();
		}
		
		@Override
		public EpisodeListener<S, A> getTrainingEpisodeListener()
		{
//			return new DefaultEpisodeListener<S, A>();
			return labeled_;
		}
		
		@Override
		public Representer<S, Representation<S>> trainRepresenter(
			final Dataset<A> train, final FactoredRepresenter<S, X> base_repr, final int iter )
		{
			return new ReprWrapper<S>( base_repr.create() );
		}

		@Override
		public SolvedStateGapRecorder<Representation<S>, A> getGapRecorder()
		{
			return null;
		}

		@Override
		public ArrayList<double[]> getTrainingVectors()
		{
//			return new ArrayList<double[]>();
			return labeled_.Phi_;
		}

		@Override
		public ArrayList<A> getTrainingLabels()
		{
//			return new ArrayList<A>();
			return labeled_.actions_;
		}

		@Override
		public Representer<S, Representation<S>> loadModel(
			final File dir, final FactoredRepresenter<S, X> base_repr, final int iter )
		{
			return new ReprWrapper<S>( base_repr.create() );
		}

		@Override
		public void writeModel( final int iter )
		{ }

		@Override
		public Instances makePairwiseInstances(
			final Instances single, final FactoredRepresenter<S, X> repr )
		{
			return null;
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
		
		@Override
		public MctsVisitor<S, A> getTrainingMctsVisitor()
		{
			return new DefaultMctsVisitor<S, A>();
		}
		
		@Override
		public EpisodeListener<S, A> getTrainingEpisodeListener()
		{
			return new DefaultEpisodeListener<S, A>();
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
			return new ReprWrapper<S>( new PairwiseClassifierRepresenter<S, X>(
				base_repr.create(),	new HammingSimilarity(),
				decision_threshold_, max_branching ) );
		}

		@Override
		public SolvedStateGapRecorder<Representation<S>, A> getGapRecorder()
		{
			return null;
		}

		@Override
		public ArrayList<double[]> getTrainingVectors()
		{
			return null;
		}

		@Override
		public ArrayList<A> getTrainingLabels()
		{
			return null;
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
			
			return new ReprWrapper<S>( new PairwiseClassifierRepresenter<S, X>(
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
	
	private static class MetricSimilarityFunction implements SimilarityFunction
	{
		private final RealMatrix metric_;
		
		public MetricSimilarityFunction( final RealMatrix metric )
		{
			metric_ = metric;
		}
		
		@Override
		public double similarity( final double[] a, final double[] b )
		{
			final double eps = 1e-6;
			final double[] diff = Fn.vminus( a, b );
			final double ip = HilbertSpace.inner_prod( diff, metric_, diff );
//			assert( ip >= -eps );
			if( ip < 0 ) {
				if( ip > -eps ) {
					return 0.0;
				}
				else {
					throw new IllegalStateException( "inner_prod = " + ip );
				}
			}
			return -Math.sqrt( ip );
		}
	}
	
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
		public MctsVisitor<S, A> getTrainingMctsVisitor()
		{
//			return unlabeled_;
			return new DefaultMctsVisitor<S, A>();
		}
		
		@Override
		public EpisodeListener<S, A> getTrainingEpisodeListener()
		{
			return labeled_;
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
			
			return new ReprWrapper<S>( new PairwiseClassifierRepresenter<S, X>(
				base_repr.create(),
				new MetricSimilarityFunction( itml.A() ), decision_threshold_, max_branching ) );
		}
		
		private double decisionThreshold( final double u, final double ell )
		{
			return -(u + ell) / 2.0;
		}

		@Override
		public SolvedStateGapRecorder<Representation<S>, A> getGapRecorder()
		{
			return gaps_;
		}

		@Override
		public ArrayList<double[]> getTrainingVectors()
		{
			return labeled_.Phi_;
		}

		@Override
		public ArrayList<A> getTrainingLabels()
		{
			return labeled_.actions_;
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
			
			return new ReprWrapper<S>( new PairwiseClassifierRepresenter<S, X>(
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
		public MctsVisitor<S, A> getTrainingMctsVisitor()
		{
			return new DefaultMctsVisitor<S, A>();
		}
		
		@Override
		public EpisodeListener<S, A> getTrainingEpisodeListener()
		{
			return labeled_;
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
			return new ReprWrapper<S>( new PairwiseClassifierRepresenter<S, X>(
				base_repr.create(),	new MetricSimilarityFunction( M_ ),
				decision_threshold_, max_branching ) );
		}

		@Override
		public SolvedStateGapRecorder<Representation<S>, A> getGapRecorder()
		{
			return gaps_;
		}

		@Override
		public ArrayList<double[]> getTrainingVectors()
		{
			return labeled_.Phi_;
		}

		@Override
		public ArrayList<A> getTrainingLabels()
		{
			return labeled_.actions_;
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
			
			return new ReprWrapper<S>( new PairwiseClassifierRepresenter<S, X>(
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
		public MctsVisitor<S, A> getTrainingMctsVisitor()
		{
//			return unlabeled_;
			return new DefaultMctsVisitor<S, A>();
		}
		
		@Override
		public EpisodeListener<S, A> getTrainingEpisodeListener()
		{
			return labeled_;
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
		public SolvedStateGapRecorder<Representation<S>, A> getGapRecorder()
		{
			return gaps_;
		}

		@Override
		public ArrayList<double[]> getTrainingVectors()
		{
			return labeled_.Phi_;
		}

		@Override
		public ArrayList<A> getTrainingLabels()
		{
			return labeled_.actions_;
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

		@Override
		public Instances makePairwiseInstances( final Instances single, final FactoredRepresenter<S, X> repr )
		{
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	// -----------------------------------------------------------------------
	
	private static class RandomForestAbstractionDiscovery<S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		extends AbstractionDiscoveryAlgorithm<S, X, A, R>
	{
		public static <S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
		RandomForestAbstractionDiscovery<S, X, A, R> create( final Configuration config, final Domain<S, X, A, R> domain )
		{
			return new RandomForestAbstractionDiscovery<S, X, A, R>( config, domain );
		}
		
		private final Configuration config_;
		private final R base_repr_;
		
		private final SolvedStateAccumulator<S, X, A> labeled_;
		private final SolvedStateGapRecorder<Representation<S>, A> gaps_
			= new SolvedStateGapRecorder<Representation<S>, A>();
		
		public RandomForestAbstractionDiscovery( final Configuration config, final Domain<S, X, A, R> domain )
		{
			config_ = config;
			base_repr_ = domain.getBaseRepresenter();
			labeled_ = new SolvedStateAccumulator<S, X, A>( base_repr_.create() );
		}
		
		@Override
		public MctsVisitor<S, A> getTrainingMctsVisitor()
		{
			return new DefaultMctsVisitor<S, A>();
		}
		
		@Override
		public EpisodeListener<S, A> getTrainingEpisodeListener()
		{
			return labeled_;
		}
		
		@Override
		public Representer<S, Representation<S>> trainRepresenter(
			final Instances train, final FactoredRepresenter<S, X> base_repr, final int iter )
		{
			final FastRandomForest classifier;
			try {
				classifier = new FastRandomForest();
				// We want single-threaded, so that we can run multiple
				// experiments on one machine without competing for cores.
				classifier.setNumThreads( 1 );
				classifier.setNumTrees( config_.getInt( "rf.Ntrees" ) );
				classifier.buildClassifier( train );
				SerializationHelper.write(
					new File( config_.data_directory, "rf" + iter + ".model" ).getAbsolutePath(), classifier );
			}
			catch( final Exception ex ) {
				throw new RuntimeException( ex );
			}
			
//			final int Nfeatures = base_repr.attributes().size();
//			for( int i = 0; i < Nfeatures; ++i ) {
//				final RealVector xi = new ArrayRealVector( Nfeatures );
//				xi.setEntry( i, 1.0 );
//				xi.setEntry( Nfeatures - 1, 1.0 );
//				for( int j = 0; j <= i; ++j ) {
//					final RealVector xj = new ArrayRealVector( Nfeatures );
//					xj.setEntry( j, 1.0 );
//					xj.setEntry( Nfeatures - 1, 10.0 );
//					final double[] diff = makePairwiseFeaturesUnlabeled( xi.toArray(), xj.toArray() );
//					System.out.print( WekaUtil.classify( classifier, base_repr.attributes(), diff ) + "\t" );
////					final Instance instance = new DenseInstance( 1.0, diff );
////					final Instances x = new Instances( "eval", base_repr.attributes(), 1 );
////					x.add( instance );
////					instance.setDataset( x );
////					try {
////						System.out.print( classifier.classifyInstance( instance ) + "\t" );
////					}
////					catch( final Exception ex ) {
////						ex.printStackTrace();
////						throw new RuntimeException();
////					}
//				}
//				System.out.println();
//			}
//			for( int i = 0; i < Nfeatures; ++i ) {
//				final RealVector xi = new ArrayRealVector( Nfeatures );
//				xi.setEntry( i, 1.0 );
//				xi.setEntry( Nfeatures - 1, 1.0 );
//				for( int j = 0; j <= i; ++j ) {
//					final RealVector xj = new ArrayRealVector( Nfeatures );
//					xj.setEntry( j, 1.0 );
//					xj.setEntry( Nfeatures - 1, 10.0 );
//					final double[] diff = makePairwiseFeaturesUnlabeled( xi.toArray(), xj.toArray() );
//					System.out.print( Fn.max( WekaUtil.distribution( classifier, base_repr.attributes(), diff ) ) + "\t" );
//				}
//				System.out.println();
//			}
			
			// FIXME: These have to be the same as the spec for ChainWalk, but
			// it's hard to get those values here because we don't actually
			// know that we're using ChainWalk.
//			final int Nvalues = config_.getInt( "chain_walk.Nstates" );
//			final int Nirrelevant = 4;
//			for( int irrelevant_i = 0; irrelevant_i < Nirrelevant; ++irrelevant_i ) {
//				for( int irrelevant_j = 0; irrelevant_j < Nirrelevant; ++irrelevant_j ) {
//					System.out.println( "=== " + irrelevant_i + " x " + irrelevant_j + ":" );
//					for( int i = -Nvalues; i <= Nvalues; ++i ) {
//						final RealVector xi = new ArrayRealVector( base_repr.attributes().size() );
//						xi.setEntry( 0, i );
//						xi.setEntry( 1, irrelevant_i );
//						for( int j = -Nvalues; j <= Nvalues; ++j ) {
//							final RealVector xj = new ArrayRealVector( base_repr.attributes().size() );
//							xj.setEntry( 0, j );
//							xj.setEntry( 1, irrelevant_j );
//							final double[] diff = makePairwiseFeaturesUnlabeled( xi.toArray(), xj.toArray() );
//							System.out.print( WekaUtil.classify( classifier, base_repr.attributes(), diff ) + "\t" );
//						}
//						System.out.println();
//					}
//	//				for( int i = -Nvalues; i <= Nvalues; ++i ) {
//	//					final RealVector xi = new ArrayRealVector( 1 );
//	//					xi.setEntry( 0, i );
//	//					for( int j = -Nvalues; j <= Nvalues; ++j ) {
//	//						final RealVector xj = new ArrayRealVector( 1 );
//	//						xj.setEntry( 0, j );
//	//						final double[] diff = makePairwiseFeaturesUnlabeled( xi.toArray(), xj.toArray() );
//	//						System.out.print( Fn.max( WekaUtil.distribution( classifier, base_repr.attributes(), diff ) ) + "\t" );
//	//					}
//	//					System.out.println();
//	//				}
//				}
//			}
			
			final double decision_threshold = config_.getDouble( "pairwise_classifier.decision_threshold" );
			final int max_branching = config_.getInt( "pairwise_classifier.max_branching" );
			return new ReprWrapper<S>( new PairwiseClassifierRepresenter<S, X>(
				base_repr.create(),
				new ClassifierSimilarityFunction( classifier, new Instances( train ) ) {
					@Override
					public Instance makeFeatures( final double[] phi_i, final double[] phi_j )
					{
						final double[] phi_joint = makePairwiseFeaturesUnlabeled( phi_i, phi_j );
						return new DenseInstance( 1.0, phi_joint );
					}
				},
				decision_threshold, max_branching ) );
		}

		@Override
		public SolvedStateGapRecorder<Representation<S>, A> getGapRecorder()
		{
			return gaps_;
		}

		@Override
		public ArrayList<double[]> getTrainingVectors()
		{
			return labeled_.Phi_;
		}

		@Override
		public ArrayList<A> getTrainingLabels()
		{
			return labeled_.actions_;
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

		@Override
		public Instances makePairwiseInstances( final Instances single, final FactoredRepresenter<S, X> repr )
		{
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static <S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
	AbstractionDiscoveryAlgorithm<S, X, A, R> createAbstractionDiscoveryAlgorithm(
		final Configuration config, final Domain<S, X, A, R> domain )
	{
		if( "metric".equals( config.abstraction ) ) {
			return MetricLearningAbstraction.create( config, domain );
		}
		else if( "mahalanobis".equals( config.abstraction ) ) {
			return MahalanobisDistanceAbstraction.create( config, domain );
		}
		else if( "kmeans".equals( config.abstraction ) ) {
			return KMeansAbstraction.create( config, domain );
		}
		else if( "random_forest".equals( config.abstraction ) ) {
			return RandomForestAbstractionDiscovery.create( config, domain );
		}
		else {
			return NoneAbstractionDiscovery.create( config, domain );
		}
	}
	
	// -----------------------------------------------------------------------
	// Domains
	// -----------------------------------------------------------------------
	
	private static abstract class Domain<S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, ? extends FactoredRepresentation<S>>>
	{
		public abstract UndoSimulator<S, A> createSimulator();
		public abstract GameTreeFactory<S, Representation<S>, A> getUctFactory(
			final UndoSimulator<S, A> sim, final Representer<S, Representation<S>> repr, final int Nepisodes );
		public abstract R getBaseRepresenter();
		public abstract ActionGenerator<S, A> getActionGenerator();
//		public abstract EvaluationFunction<S, A> getEvaluator();
		
		public abstract EpisodeListener<S, A> getVisualization();
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
		public UndoSimulator<Irrelevance.State, Irrelevance.Action> createSimulator()
		{
			return irrelevance_.new Simulator();
		}
		
		@Override
		public GameTreeFactory<Irrelevance.State, Representation<Irrelevance.State>, Irrelevance.Action>
		getUctFactory( final UndoSimulator<Irrelevance.State, Irrelevance.Action> sim,
					   final Representer<Irrelevance.State, Representation<Irrelevance.State>> repr,
					   final int Nepisodes )
		{
			final double uct_c = config_.uct_c;
			// Optimistic default value
			final double[] default_value = new double[] { 1.0 };
			final BackupRule<Representation<Irrelevance.State>, Irrelevance.Action> train_backup
				= BackupRule.<Representation<Irrelevance.State>, Irrelevance.Action>MaxQ();
			final GameTreeFactory<
				Irrelevance.State, Representation<Irrelevance.State>, Irrelevance.Action
			> factory
				= new UctSearch.Factory<Irrelevance.State, Representation<Irrelevance.State>, Irrelevance.Action>(
					sim, repr.create(), SingleAgentJointActionGenerator.create( getActionGenerator() ),
					uct_c, Nepisodes, config_.rng,
					getEvaluator(), train_backup, default_value );
			return factory;
		}

		@Override
		public Irrelevance.IdentityRepresenter getBaseRepresenter()
		{
			return new Irrelevance.IdentityRepresenter();
		}

//		@Override
		public EvaluationFunction<Irrelevance.State, Irrelevance.Action> getEvaluator()
		{
			final int rollout_width = 1;
			final int rollout_depth = Integer.MAX_VALUE;
			final Policy<Irrelevance.State, JointAction<Irrelevance.Action>> rollout_policy
				= new RandomPolicy<Irrelevance.State, JointAction<Irrelevance.Action>>(
					0 /*Player*/, config_.rng.nextInt(),
					SingleAgentJointActionGenerator.create( getActionGenerator() ) );
			final EvaluationFunction<Irrelevance.State, Irrelevance.Action> rollout_evaluator
				= RolloutEvaluator.create( rollout_policy, config_.discount,
										   rollout_width, rollout_depth, new double[] { 0.0 } );
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
		public UndoSimulator<ChainWalk.State, ChainWalk.Action> createSimulator()
		{
			return world_.new Simulator();
		}
		
		@Override
		public GameTreeFactory<ChainWalk.State, Representation<ChainWalk.State>, ChainWalk.Action>
		getUctFactory( final UndoSimulator<ChainWalk.State, ChainWalk.Action> sim,
					   final Representer<ChainWalk.State, Representation<ChainWalk.State>> repr,
					   final int Nepisodes )
		{
			final double uct_c = config_.uct_c;
			// Optimistic default value
			final double[] default_value = new double[] { 1.0 };
			final BackupRule<Representation<ChainWalk.State>, ChainWalk.Action> train_backup
				= BackupRule.<Representation<ChainWalk.State>, ChainWalk.Action>MaxQ();
			final GameTreeFactory<
				ChainWalk.State, Representation<ChainWalk.State>, ChainWalk.Action
			> factory
				= new UctSearch.Factory<ChainWalk.State, Representation<ChainWalk.State>, ChainWalk.Action>(
					sim, repr.create(), SingleAgentJointActionGenerator.create( getActionGenerator() ),
					uct_c, Nepisodes, config_.rng,
					getEvaluator(), train_backup, default_value );
			return factory;
		}

		@Override
		public FactoredRepresenter<ChainWalk.State, FactoredRepresentation<ChainWalk.State>> getBaseRepresenter()
		{
			return new ChainWalk.IdentityRepresenter();
//			return new ChainWalk.PiStarRepresenter();
		}

//		@Override
		public EvaluationFunction<ChainWalk.State, ChainWalk.Action> getEvaluator()
		{
			final int rollout_width = 1;
			final int rollout_depth = Integer.MAX_VALUE;
			final Policy<ChainWalk.State, JointAction<ChainWalk.Action>> rollout_policy
				= new RandomPolicy<ChainWalk.State, JointAction<ChainWalk.Action>>(
					0 /*Player*/, config_.rng.nextInt(),
					SingleAgentJointActionGenerator.create( getActionGenerator() ) );
			final EvaluationFunction<ChainWalk.State, ChainWalk.Action> rollout_evaluator
				= RolloutEvaluator.create( rollout_policy, config_.discount,
										   rollout_width, rollout_depth, new double[] { 0.0 } );
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
		
	}
	
	// -----------------------------------------------------------------------
	
	private static class BlackjackDomain extends Domain<BlackjackState, FactoredRepresentation<BlackjackState>,
														BlackjackAction, BlackjackPrimitiveRepresenter>
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
		public UndoSimulator<BlackjackState, BlackjackAction> createSimulator()
		{
			final Deck deck = new InfiniteDeck( config_.rng.nextInt() );
			return new BlackjackSimulator( deck, nagents_, params_ );
		}

		@Override
		public GameTreeFactory<BlackjackState, Representation<BlackjackState>, BlackjackAction> getUctFactory(
				final UndoSimulator<BlackjackState, BlackjackAction> sim,
				final Representer<BlackjackState, Representation<BlackjackState>> repr,
				final int Nepisodes )
		{
			final double uct_c = config_.uct_c;
			// Optimistic default value
			final double[] default_value = new double[] { 1.0 };
			final BackupRule<Representation<BlackjackState>, BlackjackAction> train_backup
				= BackupRule.<Representation<BlackjackState>, BlackjackAction>MaxQ();
			final GameTreeFactory<
				BlackjackState, Representation<BlackjackState>, BlackjackAction
			> factory
				= new UctSearch.Factory<BlackjackState, Representation<BlackjackState>, BlackjackAction>(
					sim, repr.create(), SingleAgentJointActionGenerator.create( getActionGenerator() ),
					uct_c, Nepisodes, config_.rng,
					getEvaluator(), train_backup, default_value );
			return factory;
		}

		@Override
		public BlackjackPrimitiveRepresenter getBaseRepresenter()
		{
			return new BlackjackPrimitiveRepresenter( nagents_ );
		}

//		@Override
		public EvaluationFunction<BlackjackState, BlackjackAction> getEvaluator()
		{
			final int rollout_width = 1;
			final int rollout_depth = Integer.MAX_VALUE;
			final Policy<BlackjackState, JointAction<BlackjackAction>> rollout_policy
				= new RandomPolicy<BlackjackState, JointAction<BlackjackAction>>(
					0 /*Player*/, config_.rng.nextInt(),
					SingleAgentJointActionGenerator.create( getActionGenerator() ) );
			final EvaluationFunction<BlackjackState, BlackjackAction> rollout_evaluator
				= RolloutEvaluator.create( rollout_policy, config_.discount,
										   rollout_width, rollout_depth, new double[] { 0.0 } );
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
		public UndoSimulator<YahtzeeState, YahtzeeAction> createSimulator()
		{
			return new YahtzeeSimulator( config_.rng );
		}
		
		@Override
		public GameTreeFactory<YahtzeeState, Representation<YahtzeeState>, YahtzeeAction>
		getUctFactory( final UndoSimulator<YahtzeeState, YahtzeeAction> sim,
					   final Representer<YahtzeeState, Representation<YahtzeeState>> repr,
					   final int Nepisodes )
		{
			final double uct_c = config_.uct_c;
			// Optimistic default value
			final double[] default_value = new double[] { 1.0 };
			final BackupRule<Representation<YahtzeeState>, YahtzeeAction> train_backup
				= BackupRule.<Representation<YahtzeeState>, YahtzeeAction>MaxQ();
			final GameTreeFactory<
				YahtzeeState, Representation<YahtzeeState>, YahtzeeAction
			> factory
				= new UctSearch.Factory<YahtzeeState, Representation<YahtzeeState>, YahtzeeAction>(
					sim, repr.create(), SingleAgentJointActionGenerator.create( getActionGenerator() ),
					uct_c, Nepisodes, config_.rng,
					getEvaluator(), train_backup, default_value );
			return factory;
		}

		@Override
		public PrimitiveYahtzeeRepresenter getBaseRepresenter()
		{
			return new PrimitiveYahtzeeRepresenter();
		}

//		@Override
		public EvaluationFunction<YahtzeeState, YahtzeeAction> getEvaluator()
		{
			final int rollout_width = 1;
			final int rollout_depth = Integer.MAX_VALUE;
			final Policy<YahtzeeState, JointAction<YahtzeeAction>> rollout_policy
				= new RandomPolicy<YahtzeeState, JointAction<YahtzeeAction>>(
					0 /*Player*/, config_.rng.nextInt(),
					SingleAgentJointActionGenerator.create( getActionGenerator() ) );
			final EvaluationFunction<YahtzeeState, YahtzeeAction> rollout_evaluator
				= RolloutEvaluator.create( rollout_policy, config_.discount,
										   rollout_width, rollout_depth, new double[] { 0.0 } );
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
			return new SmartActionGenerator();
//			return new YahtzeeActionGenerator();
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
		public UndoSimulator<FroggerState, FroggerAction> createSimulator()
		{
			final FroggerState s = new FroggerState( params_ );
			final FroggerSimulator sim = new FroggerSimulator( config_.rng, s );
			return sim;
		}
		
		@Override
		public GameTreeFactory<FroggerState, Representation<FroggerState>, FroggerAction>
		getUctFactory( final UndoSimulator<FroggerState, FroggerAction> sim,
					   final Representer<FroggerState, Representation<FroggerState>> repr,
					   final int Nepisodes )
		{
			final double uct_c = config_.uct_c;
			// Optimistic default value
			final double[] default_value = new double[] { 1.0 };
			final BackupRule<Representation<FroggerState>, FroggerAction> train_backup
				= BackupRule.<Representation<FroggerState>, FroggerAction>MaxQ();
			final GameTreeFactory<
				FroggerState, Representation<FroggerState>, FroggerAction
			> factory
				= new UctSearch.Factory<FroggerState, Representation<FroggerState>, FroggerAction>(
					sim, repr.create(), SingleAgentJointActionGenerator.create( getActionGenerator() ),
					uct_c, Nepisodes, config_.rng,
					getEvaluator( sim.state() ), train_backup, default_value );
			return factory;
		}

		@Override
		public RelativeFroggerRepresenter getBaseRepresenter()
		{
//			return new PrimitiveFroggerRepresenter( params_ );
			return new RelativeFroggerRepresenter( params_ );
		}

//		@Override
		public EvaluationFunction<FroggerState, FroggerAction> getEvaluator( final FroggerState s )
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
//			g.print();
//			System.exit( 0 );
			return g;
		}
	}
	
	// -----------------------------------------------------------------------
	
	private static class RacegridDomain extends Domain<RacegridState, FactoredRepresentation<RacegridState>,
													  RacegridAction, PrimitiveRacegridRepresenter>
	{
		private final Configuration config_;
		
		private final TerrainType[][] circuit_;
		private ShortestPathHeuristic heuristic_ = null;
		
		public RacegridDomain( final Configuration config )
		{
			config_ = config;
			if( "bbs_small".equals( config.get( "racegrid.circuit" ) ) ) {
				circuit_ = RacegridCircuits.barto_bradke_singh_SmallTrack();
			}
			else if( "bbs_large".equals( config.get( "racegrid.circuit" ) ) ) {
				circuit_ = RacegridCircuits.barto_bradke_singh_LargeTrack();
			}
			else {
				throw new IllegalArgumentException( "racegrid.circuit = " + config.get( "racegrid.circuit" ) );
			}
		}
		
		@Override
		public UndoSimulator<RacegridState, RacegridAction> createSimulator()
		{
			final RacegridState s = new RacegridState( circuit_ );
			heuristic_ = new ShortestPathHeuristic( s, 3 );
			final RacegridSimulator sim = new RacegridSimulator(
				config_.rng, s, config_.getDouble( "racegrid.slip" ) );
			return sim;
		}
		
		@Override
		public GameTreeFactory<RacegridState, Representation<RacegridState>, RacegridAction>
		getUctFactory( final UndoSimulator<RacegridState, RacegridAction> sim,
					   final Representer<RacegridState, Representation<RacegridState>> repr,
					   final int Nepisodes )
		{
			final double uct_c = config_.uct_c;
			// Optimistic default value
			final double[] default_value = new double[] { 1.0 };
			final BackupRule<Representation<RacegridState>, RacegridAction> train_backup
				= BackupRule.<Representation<RacegridState>, RacegridAction>MaxQ();
			final GameTreeFactory<
				RacegridState, Representation<RacegridState>, RacegridAction
			> factory
				= new UctSearch.Factory<RacegridState, Representation<RacegridState>, RacegridAction>(
					sim, repr.create(), SingleAgentJointActionGenerator.create( getActionGenerator() ),
					uct_c, Nepisodes, config_.rng,
					getEvaluator( sim.state() ), train_backup, default_value );
			return factory;
		}

		@Override
		public PrimitiveRacegridRepresenter getBaseRepresenter()
		{
			return new PrimitiveRacegridRepresenter();
		}

//		@Override
		public EvaluationFunction<RacegridState, RacegridAction> getEvaluator( final RacegridState s )
		{
			return heuristic_.create();
		}

		@Override
		public EpisodeListener<RacegridState, RacegridAction> getVisualization()
		{
//			final RacegridVisualization vis = new RacegridVisualization( null, circuit_, 10 );
//			return vis.updater( 500 );
			
			return null;
		}

		@Override
		public ActionGenerator<RacegridState, RacegridAction> getActionGenerator()
		{
			return new RacegridActionGenerator();
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
		public UndoSimulator<RacetrackState, RacetrackAction> createSimulator()
		{
			final RacetrackState s = new RacetrackState( circuit_ );
			final RacetrackSimulator sim = new RacetrackSimulator(
				config_.rng, s, config_.getDouble( "race_car.control_noise" ), 0.0 );
			return sim;
		}
		
		@Override
		public GameTreeFactory<RacetrackState, Representation<RacetrackState>, RacetrackAction>
		getUctFactory( final UndoSimulator<RacetrackState, RacetrackAction> sim,
					   final Representer<RacetrackState, Representation<RacetrackState>> repr,
					   final int Nepisodes )
		{
			final double uct_c = config_.uct_c;
			// Optimistic default value
			final double[] default_value = new double[] { 1.0 };
			final BackupRule<Representation<RacetrackState>, RacetrackAction> train_backup
				= BackupRule.<Representation<RacetrackState>, RacetrackAction>MaxQ();
			final GameTreeFactory<
				RacetrackState, Representation<RacetrackState>, RacetrackAction
			> factory
				= new UctSearch.Factory<RacetrackState, Representation<RacetrackState>, RacetrackAction>(
					sim, repr.create(), SingleAgentJointActionGenerator.create( getActionGenerator() ),
					uct_c, Nepisodes, config_.rng,
					getEvaluator( sim.state() ), train_backup, default_value );
			return factory;
		}

		@Override
		public PrimitiveRacetrackRepresenter getBaseRepresenter()
		{
			return new PrimitiveRacetrackRepresenter();
//			final double scale = 0.5;
//			final GridRepresenter<RacetrackState> grid = new GridRepresenter<RacetrackState>(
//				base_repr, new double[] { scale*1.0, scale*1.0, Math.PI / 8, 1.0 } );
		}

//		@Override
		public EvaluationFunction<RacetrackState, RacetrackAction> getEvaluator( final RacetrackState s )
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
	}
	
	// -----------------------------------------------------------------------
	
	private static class TamariskDomain extends Domain<TamariskState, FactoredRepresentation<TamariskState>,
													  TamariskAction, IndicatorTamariskRepresenter>
	{
		private final Configuration config_;
		private final TamariskParameters params_;
		
		public TamariskDomain( final Configuration config )
		{
			config_ = config;
			params_ = new TamariskParameters(
				config_.rng, config_.getInt( "tamarisk.Nreaches" ), config_.getInt( "tamarisk.Nhabitats" ) );
		}
		
		@Override
		public UndoSimulator<TamariskState, TamariskAction> createSimulator()
		{
			final int branching = config_.getInt( "tamarisk.branching" );
			final DirectedGraph<Integer, DefaultEdge> g = params_.createBalancedGraph( branching );
			final TamariskState s = new TamariskState( config_.rng, params_, g );
			return new TamariskSimulator( s );
		}
		
		@Override
		public GameTreeFactory<TamariskState, Representation<TamariskState>, TamariskAction>
		getUctFactory( final UndoSimulator<TamariskState, TamariskAction> sim,
					   final Representer<TamariskState, Representation<TamariskState>> repr,
					   final int Nepisodes )
		{
			final double uct_c = config_.uct_c;
			// Optimistic default value
			final double[] default_value = new double[] { 1.0 };
			final BackupRule<Representation<TamariskState>, TamariskAction> train_backup
				= BackupRule.<Representation<TamariskState>, TamariskAction>MaxQ();
			final GameTreeFactory<
				TamariskState, Representation<TamariskState>, TamariskAction
			> factory
				= new UctSearch.Factory<TamariskState, Representation<TamariskState>, TamariskAction>(
					sim, repr.create(), SingleAgentJointActionGenerator.create( getActionGenerator() ),
					uct_c, Nepisodes, config_.rng,
					getEvaluator(), train_backup, default_value );
			return factory;
		}

		@Override
		public IndicatorTamariskRepresenter getBaseRepresenter()
		{
			return new IndicatorTamariskRepresenter( params_ );
		}

//		@Override
		public EvaluationFunction<TamariskState, TamariskAction> getEvaluator()
		{
			final int rollout_width = 1;
			final int rollout_depth = 10 ; //Integer.MAX_VALUE;
			final Policy<TamariskState, JointAction<TamariskAction>> rollout_policy
				= new RandomPolicy<TamariskState, JointAction<TamariskAction>>(
					0 /*Player*/, config_.rng.nextInt(),
					SingleAgentJointActionGenerator.create( getActionGenerator() ) );
			final EvaluationFunction<TamariskState, TamariskAction> rollout_evaluator
				= RolloutEvaluator.create( rollout_policy, config_.discount,
										   rollout_width, rollout_depth, new double[] { 0.0 } );
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
	}
	
	// -----------------------------------------------------------------------
	
	private static <S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
	void runExperiment( final Configuration config,
						final Domain<S, X, A, R> domain,
						final AbstractionDiscoveryAlgorithm<S, X, A, R> discovery ) throws Exception
	{
		System.out.println( "****************************************" );
		System.out.println( "game = x ("
							+ config.Ntrain_games + "(" + config.Ntrain_episodes + ")"
							+ " / " + config.Ntest_games
							+ "(" + config.Ntest_episodes_order_min + " - " + config.Ntest_episodes_order_max + ")) "
							+ ": " + domain.getBaseRepresenter() );
		
		final Csv.Writer data_out = createDataWriter( config );
		
		Representer<S, Representation<S>> Crepr = new ReprWrapper<S>( domain.getBaseRepresenter() );
		
		for( int iter = 0; iter < config.Niterations; ++iter ) {
			System.out.println( "Iteration " + iter );
			
			final Dataset<A> training_data = new Dataset<A>();
			if( "create".equals( config.training_data_single ) ) {
				// Gather training examples
				System.out.println( "[Gathering training_data.single]" );
				
				runGames( "train", config, domain, discovery, config.Ntrain_episodes, config.Ntrain_games,
						  discovery.getTrainingMctsVisitor(), discovery.getTrainingEpisodeListener(), Crepr, data_out, iter );
				
				final SingleInstanceDataset<A> single = makeSingleInstanceDataset(
						config, domain.getBaseRepresenter().attributes(),
						discovery.getTrainingVectors(), discovery.getTrainingLabels(), iter );
				training_data.single = single.instances;
				
				WekaUtil.writeDataset( config.data_directory, single.instances );
				writeActionKey( config, single, iter );
			}
			if( "create".equals( config.training_data_pair ) ) {
				if( training_data.single == null ) {
					System.out.println( "[Loading training_data.single]" );
					training_data.single = config.loadSingleInstances();
				}
				System.out.println( "[Constructing training_data.pair]" );
				training_data.pair = discovery.makePairwiseInstances(
					training_data.single, domain.getBaseRepresenter() );
				
				WekaUtil.writeDataset( config.data_directory, training_data.pair );
			}
			
			if( !"none".equals( config.abstraction ) ) {
				if( "create".equals( config.model ) ) {
					if( training_data.single == null ) {
						System.out.println( "[Loading training_data.single]" );
						training_data.single = config.loadSingleInstances();
					}
					if( training_data.pair == null ) {
						System.out.println( "[Loading training_data.pair]" );
						training_data.pair = config.loadPairInstances();
					}
					// Train classifier
					System.out.println( "[Training classifier]" );
					Crepr = discovery.trainRepresenter( training_data, domain.getBaseRepresenter(), iter );
					discovery.writeModel( iter );
				}
				else if( !"none".equals( config.model ) ) {
					Crepr = discovery.loadModel( new File( config.modelDirectory(), config.model ),
												 domain.getBaseRepresenter(), iter );
				}
			}
			
			// Test
			if( config.Ntest_games > 0 ) {
				System.out.println( "[Testing]" );
				final int order_min = config.Ntest_episodes_order_min;
				final int order_max = config.Ntest_episodes_order_max;
				for( int order = order_min; order <= order_max; ++order ) {
					final int Ntest_episodes = (int) Math.pow( 2, order );
					System.out.println( "Ntest_episodes = " + Ntest_episodes );
					final MctsVisitor<S, A> test_visitor = new DefaultMctsVisitor<S, A>();
					
					runGames( "test", config, domain, discovery, Ntest_episodes, config.Ntest_games,
							  test_visitor, null /*no EpisodeListener*/, Crepr, data_out, iter );
				}
			}
		}
	}
	
	private static Csv.Writer createDataWriter( final Configuration config )
	{
		Csv.Writer data_out;
		try {
			data_out = new Csv.Writer( new PrintStream( new File( config.data_directory, "data.csv" ) ) );
		}
		catch( final FileNotFoundException ex ) {
			throw new RuntimeException( ex );
		}
		data_out.cell( "phase" ).cell( "abstraction" ).cell( "actions" ).cell( "iteration" )
				.cell( "Nepisodes" ).cell( "Ngames" )
				.cell( "mean" ).cell( "var" ).cell( "conf" )
				.cell( "state_branching_mean" ).cell( "state_branching_var" )
				.cell( "action_branching_mean" ).cell( "action_branching_var" )
				.cell( "tree_depth_mean" ).cell( "tree_depth_var" )
				.cell( "steps_mean" ).cell( "steps_var" ).cell( "steps_min" ).cell( "steps_max" );
		for( final String k : config.keys() ) {
			data_out.cell( k );
		}
		data_out.newline();
		
		return data_out;
	}
	
	private static <S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
	void runGames( final String phase, final Configuration config,
				   final Domain<S, X, A, R> domain, final AbstractionDiscoveryAlgorithm<S, X, A, R> discovery,
				   final int Nepisodes, final int Ngames,
				   final MctsVisitor<S, A> mcts_visitor, final EpisodeListener<S, A> listener,
				   final Representer<S, Representation<S>> Crepr,
				   final Csv.Writer data_out, final int iter ) throws Exception
	{
		assert( "train".equals( phase ) || "test".equals( phase ) );
		
		final int print_interval = 100;
		
		final MeanVarianceAccumulator ret = new MeanVarianceAccumulator();
		final MeanVarianceAccumulator steps = new MeanVarianceAccumulator();
		final MinMaxAccumulator steps_minmax = new MinMaxAccumulator();
		final TreeStatisticsRecorder<Representation<S>, A> tree_stats
			= new TreeStatisticsRecorder<Representation<S>, A>();
		
		for( int i = 0; i < Ngames; ++i ) {
			if( i % print_interval == 0 ) {
				System.out.println( "Episode " + i );
			}
			
			final UndoSimulator<S, A> sim = domain.createSimulator();
			
			final GameTreeFactory<S, Representation<S>, A> factory
				= domain.getUctFactory( sim, Crepr.create(), Nepisodes );
			
			final SearchPolicy<S, Representation<S>, A>
				search_policy = new SearchPolicy<S, Representation<S>, A>( factory, mcts_visitor, null ) {
					@Override
					protected JointAction<A> selectAction( final GameTree<Representation<S>, A> tree )
					{
						tree.root().accept( tree_stats );
						return BackupRules.MaxAction( tree.root() ).a();
					}

					@Override
					public int hashCode()
					{ return System.identityHashCode( this ); }

					@Override
					public boolean equals( final Object that )
					{ return this == that; }
			};
			
			final Episode<S, A> episode	= new Episode<S, A>( sim, search_policy );
			final RewardAccumulator<S, A> racc = new RewardAccumulator<S, A>( sim.nagents(), config.discount );
			episode.addListener( racc );
			if( listener != null ) {
				episode.addListener( listener );
			}
			final EpisodeListener<S, A> vis = domain.getVisualization();
			if( vis != null ) {
				episode.addListener( vis );
			}
			episode.run();
			
			ret.add( racc.v()[0] );
			steps.add( racc.steps() );
			steps_minmax.add( racc.steps() );
			
//			System.out.println( "Reward: " + racc.v()[0] );
		}
		
		System.out.println( "****************************************" );
		System.out.println( "Average return: " + ret.mean() );
		System.out.println( "Return variance: " + ret.variance() );
		final double conf = config.Ntest_games > 0
							? 1.96 * Math.sqrt( ret.variance() ) / Math.sqrt( config.Ntest_games )
							: 0;
		System.out.println( "Confidence: " + conf );
		System.out.println( "State branching (mean): " + tree_stats.state_branching.mean() );
		System.out.println( "State branching (var): " + tree_stats.state_branching.variance() );
		System.out.println( "Action branching (mean): " + tree_stats.action_branching.mean() );
		System.out.println( "Action branching (var): " + tree_stats.action_branching.variance() );
		System.out.println( "Depth (mean): " + tree_stats.depth.mean() );
		System.out.println( "Depth (var): " + tree_stats.depth.variance() );
		System.out.println( "Steps (mean): " + steps.mean() );
		System.out.println( "Steps (var): " + steps.variance() );
		System.out.println( "Steps (min/max): " + steps_minmax.min() + " -- " + steps_minmax.max() );
		System.out.println();
		// See: createDataWriter for correct column order
		data_out.cell( phase ).cell( domain.getBaseRepresenter() ).cell( domain.getActionGenerator() )
				.cell( iter ).cell( Nepisodes ).cell( Ngames )
				.cell( ret.mean() ).cell( ret.variance() ).cell( conf )
				.cell( tree_stats.state_branching.mean() ).cell( tree_stats.state_branching.variance() )
				.cell( tree_stats.action_branching.mean() ).cell( tree_stats.action_branching.variance() )
				.cell( tree_stats.depth.mean() ).cell( tree_stats.depth.variance() )
				.cell( steps.mean() ).cell( steps.variance() ).cell( steps_minmax.min() ).cell( steps_minmax.max() );
		for( final String k : config.keys() ) {
			data_out.cell( config.get( k ) );
		}
		data_out.newline();
	}

	public static class Configuration implements KeyValueStore
	{
		private final KeyValueStore config_;
		
		public final String abstraction;
		public final String model;
		public final String domain;
		public final String root_directory;
		public final String training_data_single;
		public final String training_data_pair;
		public final String labels;
				
		public final int Ntrain_episodes;
		public final int Ntrain_games;
//		public final int max_training_size;
//		public final int Ntest_episodes;
		public final int Ntest_episodes_order_min;
		public final int Ntest_episodes_order_max;
		public final int Ntest_games;
		public final double uct_c;
		public final double discount;
		public final int seed;
		public final int Niterations;
		
		public final RandomGenerator rng;
//		public final File training_directory;
		public final File data_directory;
		
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
		
		public Instances loadSingleInstances()
		{
			return WekaUtil.readLabeledDataset(
				new File( trainSingleDirectory(), get( "training_data.single" ) ) );
		}
		
		public Instances loadPairInstances()
		{
			return WekaUtil.readLabeledDataset(
				new File( trainPairDirectory(), get( "training_data.pair" ) ) );
		}
		
		public Configuration( final KeyValueStore config )
		{
			config_ = config;
			
			root_directory = config.get( "root_directory" );
			exclude_.add( "root_directory" );
			domain = config.get( "domain" );
			exclude_.add( "domain" );
			abstraction = config.get( "abstraction" );
			exclude_.add( "abstraction" );
			model = config.get( "model" );
			exclude_.add( "model" );
			training_data_single = config.get( "training_data.single" );
			exclude_.add( "training_data.single" );
			training_data_pair = config.get( "training_data.pair" );
			exclude_.add( "training_data.pair" );
			labels = config.get( "labels" );
			exclude_.add( "labels" );
			
			Ntrain_episodes = config.getInt( "Ntrain_episodes" );
			Ntrain_games = config.getInt( "Ntrain_games" );
			Ntest_episodes_order_min = config.getInt( "Ntest_episodes_order_min" );
			Ntest_episodes_order_max = config.getInt( "Ntest_episodes_order_max" );
			Ntest_games = config.getInt( "Ntest_games" );
			uct_c = config.getDouble( "uct_c" );
			discount = config.getDouble( "discount" );
			seed = config.getInt( "seed" );
			Niterations = config.getInt( "Niterations" );
			
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
		
//			training_directory = new File( root_directory + File.separator + domain + File.separator + "train" );
			
			final String file_name = root_directory + File.separator
								   + "results" + File.separator
					  			   + domain + File.separator
					  			   + abstraction + File.separator
					  			   + sb.toString();
			
			rng = new MersenneTwister( seed );
			data_directory = new File( file_name );
			data_directory.mkdirs();
		}
		
		public String trainingName( final String keyword, final int iter )
		{
			final StringBuilder sb = new StringBuilder();
			sb.append( "train_" ).append( keyword ).append( "_" ).append( iter )
			  .append( "_" ).append( Ntrain_episodes )
			  .append( "_" ).append( Ntrain_games )
			  .append( "_" ).append( getInt( "training.max_per_label" ) );
			return sb.toString();
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
		final String experiment = args[0];
		final CsvConfigurationParser csv = new CsvConfigurationParser( new FileReader( experiment ) );
		for( int expr = 0; expr < csv.size(); ++expr ) {
			final KeyValueStore expr_config = csv.get( expr );
			final Configuration config = new Configuration( expr_config );
			
			if( "irrelevance".equals( config.domain ) ) {
				final IrrelevanceDomain domain = new IrrelevanceDomain( config, 10 );
				runExperiment( config, domain,
							   createAbstractionDiscoveryAlgorithm( config, domain ) );
			}
			else if( "chain_walk".equals( config.domain ) ) {
				final ChainWalkDomain domain = new ChainWalkDomain( config );
				runExperiment( config, domain,
							   createAbstractionDiscoveryAlgorithm( config, domain ) );
			}
			else if( "blackjack".equals( config.domain ) ) {
				final BlackjackDomain domain = new BlackjackDomain( config );
				runExperiment( config, domain,
							   createAbstractionDiscoveryAlgorithm( config, domain ) );
			}
			else if( "yahtzee".equals( config.domain ) ) {
				final YahtzeeDomain domain = new YahtzeeDomain( config );
				runExperiment( config, domain,
							   createAbstractionDiscoveryAlgorithm( config, domain ) );
			}
			else if( "frogger".equals( config.domain ) ) {
				final FroggerDomain domain = new FroggerDomain( config );
				runExperiment( config, domain,
							   createAbstractionDiscoveryAlgorithm( config, domain ) );
			}
			else if( "racegrid".equals( config.domain ) ) {
				final RacegridDomain domain = new RacegridDomain( config );
				runExperiment( config, domain,
							   createAbstractionDiscoveryAlgorithm( config, domain ) );
			}
			else if( "race_car".equals( config.domain ) ) {
				final RaceCarDomain domain = new RaceCarDomain( config );
				runExperiment( config, domain,
							   createAbstractionDiscoveryAlgorithm( config, domain ) );
			}
			else if( "tamarisk".equals( config.domain ) ) {
				final TamariskDomain domain = new TamariskDomain( config );
				runExperiment( config, domain,
							   createAbstractionDiscoveryAlgorithm( config, domain ) );
			}
		}
	}

}
