/**
 * 
 */
package edu.oregonstate.eecs.mcplan.abstraction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.CholeskyDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffSaver;
import weka.core.converters.Saver;
import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.RandomPolicy;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.domains.racetrack.Circuit;
import edu.oregonstate.eecs.mcplan.domains.racetrack.Circuits;
import edu.oregonstate.eecs.mcplan.domains.racetrack.PrimitiveRacetrackRepresenter;
import edu.oregonstate.eecs.mcplan.domains.racetrack.RacetrackAction;
import edu.oregonstate.eecs.mcplan.domains.racetrack.RacetrackActionGenerator;
import edu.oregonstate.eecs.mcplan.domains.racetrack.RacetrackSimulator;
import edu.oregonstate.eecs.mcplan.domains.racetrack.RacetrackState;
import edu.oregonstate.eecs.mcplan.domains.racetrack.RacetrackVisualization;
import edu.oregonstate.eecs.mcplan.domains.racetrack.SectorEvaluator;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.ClusterRepresenter;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.PrimitiveYahtzeeRepresenter;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.ReprWrapper;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.YahtzeeAction;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.YahtzeeActionGenerator;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.YahtzeeSimulator;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.YahtzeeState;
import edu.oregonstate.eecs.mcplan.ml.InformationTheoreticMetricLearner;
import edu.oregonstate.eecs.mcplan.ml.MetricConstrainedKMeans;
import edu.oregonstate.eecs.mcplan.ml.VoronoiClassifier;
import edu.oregonstate.eecs.mcplan.search.BackupRule;
import edu.oregonstate.eecs.mcplan.search.BackupRules;
import edu.oregonstate.eecs.mcplan.search.DefaultMctsVisitor;
import edu.oregonstate.eecs.mcplan.search.EvaluationFunction;
import edu.oregonstate.eecs.mcplan.search.GameTree;
import edu.oregonstate.eecs.mcplan.search.GameTreeFactory;
import edu.oregonstate.eecs.mcplan.search.MctsVisitor;
import edu.oregonstate.eecs.mcplan.search.RolloutEvaluator;
import edu.oregonstate.eecs.mcplan.search.SearchPolicy;
import edu.oregonstate.eecs.mcplan.search.UctSearch;
import edu.oregonstate.eecs.mcplan.sim.Episode;
import edu.oregonstate.eecs.mcplan.sim.EpisodeListener;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.Csv;
import edu.oregonstate.eecs.mcplan.util.Factory;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;
import gnu.trove.list.TIntList;
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
	
	public static class SolvedStateAccumulator<S, X extends FactoredRepresentation<S>,
											   A extends VirtualConstructor<A>>
		implements EpisodeListener<S, A>
	{
		public ArrayList<X> states_ = new ArrayList<X>();
		public ArrayList<RealVector> Phi_ = new ArrayList<RealVector>();
		public ArrayList<A> actions_ = new ArrayList<A>();
		
		private final Representer<S, X> repr_;
		private X x_ = null;
		
		public SolvedStateAccumulator( final Representer<S, X> repr )
		{
			repr_ = repr;
		}
		
		@Override
		public <P extends Policy<S, JointAction<A>>>
		void startState( final S s, final P pi )
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
			Phi_.add( new ArrayRealVector( x_.phi() ) );
			actions_.add( a.get( 0 ).create() );
		}

		@Override
		public void onActionsTaken( final S sprime )
		{ x_ = repr_.encode( sprime ); }

		@Override
		public void endState( final S s )
		{ }
		
	}
	
	
	/**
	 * Gathers unlabeled states. States are taken from the level of the
	 * tree directly below the root.
	 *
	 * @param <S>
	 * @param <X>
	 * @param <A>
	 */
	public static class UnlabeledStateAccumulator<S, X extends FactoredRepresentation<S>,
												  A extends VirtualConstructor<A>>
		extends DefaultMctsVisitor<S, Representation<S>, A>
	{
		public ArrayList<RealVector> Phi_ = new ArrayList<RealVector>();
		private final Representer<S, X> repr_;
		private boolean sample_ = false;
		
		public UnlabeledStateAccumulator( final Representer<S, X> repr )
		{
			repr_ = repr;
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
				Phi_.add( new ArrayRealVector( repr_.encode( sprime ).phi() ) );
			}
		}
	}
	
	// -----------------------------------------------------------------------
	
	private static <A> MetricConstrainedKMeans makeClustering(
		final RandomGenerator rng, final RealMatrix A0, final ArrayList<RealVector> XL,
		final ArrayList<A> y, final ArrayList<RealVector> XU, final boolean with_metric_learning )
	{
		final int K = 4;
		final int d = XL.get( 0 ).getDimension(); //52 + 52;
		final double u = 4.0; //1.0;
		final double ell = 16.0; //2.0;
		final double gamma = 1.0;
		
		final ArrayList<RealVector> X = new ArrayList<RealVector>();
		X.addAll( XL );
		X.addAll( XU );
		
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
			}
			M.put( i, Pair.makePair( m.toArray(), Fn.repeat( 1.0, m.size() ) ) );
			C.put( i, Pair.makePair( c.toArray(), Fn.repeat( 1.0, c.size() ) ) );
		}
		
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
				X, S, D, u, ell, A0, gamma, rng );
			itml.run();
			A = itml.A();
		}
		else {
			A = A0;
		}
		
		final MetricConstrainedKMeans kmeans = new MetricConstrainedKMeans( K, d, X, A, M, C, rng );
		kmeans.run();
		return kmeans;
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
			writer.cell( "cluster" ).cell( "label" ).cell( "x1" ).cell( "x2" ).cell( "x3" ).cell( "Ax1" ).cell( "Ax2" ).cell( "Ax3" ).newline();
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
	
	private static <S, X extends FactoredRepresentation<S>, A extends VirtualConstructor<A>>
	Instances makeTrainingSet( final RandomGenerator rng, final SolvedStateAccumulator<S, X, A> acc,
							   final ArrayList<Attribute> attributes, final int iter )
	{
		final int[] num_instances = new int[2];
		final ArrayList<Instance> negative = new ArrayList<Instance>();
		final ArrayList<Instance> positive = new ArrayList<Instance>();
		final ArrayList<String> nominal = new ArrayList<String>();
		nominal.add( "0" );
		nominal.add( "1" );
		final int d = attributes.size(); // Minus 1 for label
		final ArrayList<Attribute> labeled_attributes = new ArrayList<Attribute>( attributes );
		labeled_attributes.add( new Attribute( "__label__", nominal ) );
		
		for( int i = 0; i < acc.Phi_.size(); ++i ) {
			final double[] phi_i = acc.Phi_.get( i ).toArray();
			for( int j = i + 1; j < acc.Phi_.size(); ++j ) {
				final double[] phi_j = acc.Phi_.get( j ).toArray();
				final double[] phi_labeled = new double[d + 1];
				for( int k = 0; k < d; ++k ) {
					phi_labeled[k] = Math.abs( phi_i[k] - phi_j[k] );
				}
				final int label;
				if( acc.actions_.get( i ).equals( acc.actions_.get( j ) ) ) {
					label = 1;
				}
				else {
					label = 0;
				}
				final double weight = 1.0; // TODO: Weights?
				final String label_string = Integer.toString( label );
				phi_labeled[d] = label; //attributes.get( label_index ).indexOfValue( label_string );
				
				num_instances[label] += 1;
				
				final Instance instance = new DenseInstance( weight, phi_labeled );
				if( label == 0 ) {
					negative.add( instance );
				}
				else {
					positive.add( instance );
				}
			}
		}
		System.out.println( "num_instances = " + Arrays.toString( num_instances ) );
		final int k = Fn.min( num_instances );
		if( positive.size() > k ) {
			Fn.shuffle( rng, positive, k );
		}
		if( negative.size() > k ) {
			Fn.shuffle( rng, negative, k );
		}
		
		final Instances x = new Instances( "train" + iter, labeled_attributes, k + k );
		x.setClassIndex( d );
		for( int i = 0; i < k; ++i ) {
			x.add( negative.get( i ) );
		}
		for( int i = 0; i < k; ++i ) {
			x.add( positive.get( i ) );
		}
		
		return x;
	}
	
	private static Classifier makeClassifier( final Instances train )
	{
		try {
			final FastRandomForest rf = new FastRandomForest();
//			final Classifier rf = new J48();
			rf.buildClassifier( train );
			return rf;
		}
		catch( final Exception ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	private static void writeDataset( final File root, final Instances x )
	{
		final File dataset_file = new File( root, x.relationName() + ".arff" );
		final Saver saver = new ArffSaver();
		try {
			saver.setFile( dataset_file );
			saver.setInstances( x );
			saver.writeBatch();
		}
		catch( final IOException ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	private static <S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, FactoredRepresentation<S>>>
	void runExperiment( final RandomGenerator rng, final String algorithm,
						final Factory<? extends UndoSimulator<S, A>> sim_factory,
						final R base_repr, final ActionGenerator<S, JointAction<A>> action_gen,
						final double uct_c,
						final EvaluationFunction<S, A> evaluator,
						final int Niterations,
						final int Ntrain_games, final int Ntrain_episodes,
						final int Ntest_games, final int Ntest_episodes,
						final File root,
						final EpisodeListener<S, A> listener ) throws Exception
	{
		final int print_interval = 10000;
		
		System.out.println( "****************************************" );
		System.out.println( "game = x ("
							+ Ntrain_games + "(" + Ntrain_episodes + ")"
							+ " / " + Ntest_games + "(" + Ntest_episodes + ")) "
							+ ": " + base_repr );
		final Csv.Writer data_out = new Csv.Writer( new PrintStream( new File( root, "data.csv" ) ) );
		data_out.cell( "abstraction" ).cell( "iteration" )
				.cell( "Ntrain_games" ).cell( "Ntrain_episodes" ).cell( "Ntest_games" ).cell( "Ntest_episodes" )
				.cell( "mean" ).cell( "var" ).cell( "conf" ).newline();

		// Optimistic default value
		final double[] default_value = new double[] { 1.0 };
		Representer<S, Representation<S>> Crepr = new ReprWrapper<S>( base_repr );
		
		for( int iter = 0; iter < Niterations; ++iter ) {
			System.out.println( "Iteration " + iter );
			
			final MctsVisitor<S, Representation<S>, A> train_visitor;
			if( "kmeans".equals( algorithm ) ) {
				train_visitor = new UnlabeledStateAccumulator<S, FactoredRepresentation<S>, A>( base_repr.create() );
			}
			else {
				train_visitor = new DefaultMctsVisitor<S, Representation<S>, A>();
			}
			final BackupRule<Representation<S>, A> train_backup
				= BackupRule.<Representation<S>, A>MaxQ();
			
			// Gather training examples
			System.out.println( "Gathering training examples..." );
			final SolvedStateAccumulator<S, FactoredRepresentation<S>, A> labeled
				= new SolvedStateAccumulator<S, FactoredRepresentation<S>, A>( base_repr );
			for( int i = 0; i < Ntrain_games; ++i ) {
				if( i % print_interval == 0 ) {
					System.out.println( "Episode " + i );
				}
				
				final UndoSimulator<S, A> sim = sim_factory.create();
				
				final GameTreeFactory<
					S, Representation<S>, A
				> factory
					= new UctSearch.Factory<S, Representation<S>, A>(
						sim, Crepr.create(), action_gen.create(),
						uct_c, Ntrain_episodes, rng,
						evaluator, train_backup, default_value );
				
				final SearchPolicy<S, Representation<S>, A>
					search_policy = new SearchPolicy<S, Representation<S>, A>( factory, train_visitor, null ) {
						@Override
						protected JointAction<A> selectAction( final GameTree<Representation<S>, A> tree )
						{ return BackupRules.MaxAction( tree.root() ).a(); }
	
						@Override
						public int hashCode()
						{ return System.identityHashCode( this ); }
	
						@Override
						public boolean equals( final Object that )
						{ return this == that; }
				};
				
				final Episode<S, A> episode
					= new Episode<S, A>( sim, search_policy );
				episode.addListener( labeled );
				if( listener != null ) {
					episode.addListener( listener );
				}
				episode.run();
				System.out.println( "********* Game " + i + " *********" );
				System.out.println( sim.state() );
			}
			
			// Train classifier
			System.out.println( "Training classifier..." );
			// NOTE: In the Blackjack domain, we can easily enumerate all
			// legal states, so I'm punting the issue of how to collect them
			// properly during search. In reality, there's a question of
			// whether we should be weighting them, e.g. by their reachability.
			final boolean with_metric_learning = true;
			if( "kmeans".equals( algorithm ) ) {
				@SuppressWarnings( "unchecked" )
				final UnlabeledStateAccumulator<S, X, A> unlabeled
					= (UnlabeledStateAccumulator<S, X, A>) train_visitor;
				final ArrayList<RealVector> Phi = unlabeled.Phi_;
				RealMatrix A0 = MatrixUtils.createRealIdentityMatrix( Phi.get( 0 ).getDimension() );
				final MetricConstrainedKMeans kmeans = makeClustering( rng, A0, labeled.Phi_, labeled.actions_, Phi,
																	   with_metric_learning );
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
				writeClustering( kmeans, root, iter );
				Crepr = new ReprWrapper<S>( new ClusterRepresenter<S>( classifier, base_repr.create() ) );
			}
			else if( "rf".equals( algorithm ) ) {
				final Instances train = makeTrainingSet( rng, labeled, base_repr.attributes(), iter );
				writeDataset( root, train );
				final Classifier classifier = makeClassifier( train );
				SerializationHelper.write( new File( root, "rf" + iter + ".model" ).getAbsolutePath(), classifier );
				Crepr = new ReprWrapper<S>( new PairwiseClassifierRepresenter<S, FactoredRepresentation<S>>(
					base_repr.create(), new Instances( train ), classifier ) );
			}
			else if( "none".equals( algorithm ) ) {
				Crepr = new ReprWrapper<S>( base_repr.create() );
			}
			
			// Test
			System.out.println( "Testing..." );
			final MctsVisitor<S, Representation<S>, A> test_visitor
				= new DefaultMctsVisitor<S, Representation<S>, A>();
			final BackupRule<Representation<S>, A> test_backup
				= BackupRule.<Representation<S>, A>MaxQ();
			final MeanVarianceAccumulator ret = new MeanVarianceAccumulator();
			for( int i = 0; i < Ntest_games; ++i ) {
				if( i % print_interval == 0 ) {
					System.out.println( "Episode " + i );
				}
				
				final UndoSimulator<S, A> sim = sim_factory.create();
				
				final GameTreeFactory<
					S, Representation<S>, A
				> factory
					= new UctSearch.Factory<S, Representation<S>, A>(
						sim, Crepr.create(), action_gen.create(),
						uct_c, Ntest_episodes, rng,
						evaluator, test_backup, default_value );
				
				final SearchPolicy<S, Representation<S>, A>
					search_policy = new SearchPolicy<S, Representation<S>, A>( factory, test_visitor, null ) {
						@Override
						protected JointAction<A> selectAction( final GameTree<Representation<S>, A> tree )
						{ return BackupRules.MaxAction( tree.root() ).a(); }
	
						@Override
						public int hashCode()
						{ return System.identityHashCode( this ); }
	
						@Override
						public boolean equals( final Object that )
						{ return this == that; }
				};
				
				final Episode<S, A> episode	= new Episode<S, A>( sim, search_policy );
				if( listener != null ) {
					episode.addListener( listener );
				}
				episode.run();
	//			System.out.println( sim.state().token().toString() );
	//			System.out.println( "Reward: " + sim.reward()[0] );
				ret.add( sim.reward()[0] );
			}
			System.out.println( "****************************************" );
			System.out.println( "Average return: " + ret.mean() );
			System.out.println( "Return variance: " + ret.variance() );
			final double conf = 0.975 * ret.variance() / Math.sqrt( Ntest_games );
			System.out.println( "Confidence: " + conf );
			System.out.println();
//			data_out.println( "abstraction,iterations,Ntrain_games,Ntrain_episodes,Ntest_games,Ntest_episodes,mean,var,conf" );
			data_out.cell( base_repr ).cell( iter )
					.cell( Ntrain_games ).cell( Ntrain_episodes ).cell( Ntest_games ).cell( Ntest_episodes )
					.cell( ret.mean() ).cell( ret.variance() ).cell( conf ).newline();
		}
	}
	
	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main( final String[] args ) throws Exception
	{
		final int seed = 42;
//		final int Niterations = 4;
//		final int Ntrain_games = 2;
//		final int Ntest_games = 2;
//		final int Ntrain_episodes = 2048;
//		final int Ntest_episodes = 256;
		final String algorithm = "none";
		final String domain = "yahtzee";
		final File root = new File( "discovery/sandbox", domain );
		
		final RandomGenerator rng = new MersenneTwister( seed );
		
		if( "yahtzee".equals( domain ) ) {
			final double uct_c = 100.0;
			final int Niterations = 1;
			final int Ntrain_games = 128;
			final int Ntest_games = 0;
			final int Ntrain_episodes = (int) Math.pow( 2, 8 ); //16384;
			final int Ntest_episodes = 256;
			final double discount = 1.0;
			final int rollout_width = 1;
			final int rollout_depth = Integer.MAX_VALUE;
			final ActionGenerator<YahtzeeState, JointAction<YahtzeeAction>> action_gen
				= new YahtzeeActionGenerator();
//				= new SmartActionGenerator();
			final Factory<UndoSimulator<YahtzeeState, YahtzeeAction>> sim_factory
				= new Factory<UndoSimulator<YahtzeeState, YahtzeeAction>>() {
	
					@Override
					public UndoSimulator<YahtzeeState, YahtzeeAction> create()
					{
						return new YahtzeeSimulator( rng );
					}
			};
			final PrimitiveYahtzeeRepresenter base_repr = new PrimitiveYahtzeeRepresenter();
			final Policy<YahtzeeState, JointAction<YahtzeeAction>> rollout_policy
				= new RandomPolicy<YahtzeeState, JointAction<YahtzeeAction>>(
					0 /*Player*/, rng.nextInt(), action_gen.create() );
			final EvaluationFunction<YahtzeeState, YahtzeeAction> rollout_evaluator
				= RolloutEvaluator.create( rollout_policy, discount, rollout_width, rollout_depth );
			runExperiment( rng, algorithm, sim_factory, base_repr, action_gen, uct_c, rollout_evaluator,
						   Niterations, Ntrain_games, Ntrain_episodes, Ntest_games, Ntest_episodes, root,
						   null );
		}
		else if( "formula".equals( domain ) ) {
			final double uct_c = 10.0;
			final int Niterations = 1;
			final int Ntrain_games = 1;
			final int Ntest_games = 0;
			final int Ntrain_episodes = (int) Math.pow( 2, 8 );
			final int Ntest_episodes = 0;
			final double discount = 1.0;
			final int rollout_width = 1;
			final int rollout_depth = 256;
			final Circuit circuit = Circuits.PaperClip( 500, 200 );
			final ActionGenerator<RacetrackState, JointAction<RacetrackAction>> action_gen
				= new RacetrackActionGenerator();
			final Factory<RacetrackSimulator> sim_factory
				= new Factory<RacetrackSimulator>() {
					@Override
					public RacetrackSimulator create()
					{
						final RacetrackState state = new RacetrackState( circuit );
						final double control_noise = 0.1;
						final RacetrackSimulator sim = new RacetrackSimulator( rng, state, control_noise );
						return sim;
					}
			};
			final Policy<RacetrackState, JointAction<RacetrackAction>> rollout_policy
				= new RandomPolicy<RacetrackState, JointAction<RacetrackAction>>(
					0 /*Player*/, rng.nextInt(), action_gen.create() );
			// TODO: Cleaner way of obtaining the terminal velocity.
			final RacetrackSimulator dummy_sim = sim_factory.create();
			final EvaluationFunction<RacetrackState, RacetrackAction> rollout_evaluator
				= new SectorEvaluator( dummy_sim.terminal_velocity(), dummy_sim.tstep_ );
//				= RolloutEvaluator.create( rollout_policy, discount, rollout_width, rollout_depth );
			final PrimitiveRacetrackRepresenter base_repr = new PrimitiveRacetrackRepresenter();
			final double scale = 0.5;
			final GridRepresenter<RacetrackState> grid = new GridRepresenter<RacetrackState>(
				base_repr, new double[] { scale*1.0, scale*1.0, Math.PI / 8, 1.0 } );
			final RacetrackVisualization vis = new RacetrackVisualization( circuit, null, 2 );
			runExperiment( rng, algorithm, sim_factory, grid, action_gen, uct_c, rollout_evaluator,
						   Niterations, Ntrain_games, Ntrain_episodes, Ntest_games, Ntest_episodes, root,
						   vis.updater( 0 ) );
		}
	}

}
