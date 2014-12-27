/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

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
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.RandomPolicy;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.abstraction.ClusterAbstraction;
import edu.oregonstate.eecs.mcplan.abstraction.PairwiseSimilarityRepresenter;
import edu.oregonstate.eecs.mcplan.domains.cards.Deck;
import edu.oregonstate.eecs.mcplan.domains.cards.InfiniteDeck;
import edu.oregonstate.eecs.mcplan.ml.InformationTheoreticMetricLearner;
import edu.oregonstate.eecs.mcplan.ml.MetricConstrainedKMeans;
import edu.oregonstate.eecs.mcplan.ml.VoronoiClassifier;
import edu.oregonstate.eecs.mcplan.search.BackupRule;
import edu.oregonstate.eecs.mcplan.search.BackupRules;
import edu.oregonstate.eecs.mcplan.search.DefaultMctsVisitor;
import edu.oregonstate.eecs.mcplan.search.GameTree;
import edu.oregonstate.eecs.mcplan.search.GameTreeFactory;
import edu.oregonstate.eecs.mcplan.search.MctsVisitor;
import edu.oregonstate.eecs.mcplan.search.SearchPolicy;
import edu.oregonstate.eecs.mcplan.search.UctSearch;
import edu.oregonstate.eecs.mcplan.sim.Episode;
import edu.oregonstate.eecs.mcplan.sim.EpisodeListener;
import edu.oregonstate.eecs.mcplan.util.Csv;
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
public class AbstractionDiscovery
{
	public static class IdentityRepresenter implements Representer<BlackjackState, BlackjackStateToken>
	{
		@Override
		public Representer<BlackjackState, BlackjackStateToken> create()
		{ return new IdentityRepresenter(); }

		@Override
		public BlackjackStateToken encode( final BlackjackState s )
		{ return new BlackjackStateToken( s ); }
		
		@Override
		public String toString()
		{ return "flat"; }
	}
	
	public static class SolvedStateAccumulator<X extends FactoredRepresentation<BlackjackState>>
		implements EpisodeListener<BlackjackState, BlackjackAction>
	{
		public ArrayList<X> states_ = new ArrayList<X>();
		public ArrayList<RealVector> Phi_ = new ArrayList<RealVector>();
		public ArrayList<BlackjackAction> actions_ = new ArrayList<BlackjackAction>();
		
		private final Representer<BlackjackState, X> repr_;
		private X x_ = null;
		
		public SolvedStateAccumulator( final Representer<BlackjackState, X> repr )
		{
			repr_ = repr;
		}
		
		@Override
		public <P extends Policy<BlackjackState, JointAction<BlackjackAction>>>
		void startState( final BlackjackState s, final P pi )
		{
			x_ = repr_.encode( s );
		}

		@Override
		public void preGetAction()
		{ }

		@Override
		public
		void postGetAction( final JointAction<BlackjackAction> a )
		{
			states_.add( x_ );
			Phi_.add( new ArrayRealVector( x_.phi() ) );
			actions_.add( a.get( 0 ).create() );
		}

		@Override
		public void onActionsTaken( final BlackjackState sprime )
		{ x_ = repr_.encode( sprime ); }

		@Override
		public void endState( final BlackjackState s )
		{ }
		
	}
	
	public static class UnlabeledStateAccumulator<X extends FactoredRepresentation<BlackjackState>>
		extends DefaultMctsVisitor<BlackjackState, X, BlackjackAction>
	{
		public ArrayList<RealVector> Phi_ = new ArrayList<RealVector>();
		private final Representer<BlackjackState, X> repr_;
		private RealVector s_ = null;
		
		public UnlabeledStateAccumulator( final Representer<BlackjackState, X> repr )
		{
			repr_ = repr;
		}
		
		@Override
		public void treeAction( final JointAction<BlackjackAction> a, final BlackjackState sprime, final int[] next_turn )
		{
			// This has the effect of adding states only if an action was
			// chosen within the tree for that state. It will *not* add the
			// root state, since that is labeled and will be added by
			// SolvedStateAccumulator
			if( s_ != null ) {
				Phi_.add( s_ );
			}
			s_ = new ArrayRealVector( repr_.encode( sprime ).phi() );
		}
	}
	
	// -----------------------------------------------------------------------
	
	private static MetricConstrainedKMeans makeClustering(
		final RandomGenerator rng, final RealMatrix A0, final ArrayList<RealVector> XL,
		final ArrayList<BlackjackAction> y, final ArrayList<RealVector> XU, final boolean with_metric_learning )
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
			A = MatrixUtils.createRealIdentityMatrix( d );
		}
		
		final MetricConstrainedKMeans kmeans = new MetricConstrainedKMeans( K, d, X, A, M, C, rng );
		kmeans.run();
		return kmeans;
	}
	
	private static ArrayList<RealVector> enumerateStates( final BlackjackParameters params )
	{
		// FIXME: The proliferation of different state representations is
		// alarming. I'm using them here for convenience, but they are too
		// subtly different to be intermixed like this safely!
		
		final ArrayList<RealVector> states = new ArrayList<RealVector>();
		final BlackjackStateSpace ss = new BlackjackStateSpace( params );
		
		for( final BlackjackMdpState s : Fn.in( ss.generator() ) ) {
			if( !s.player_passed ) {
				states.add( new ArrayRealVector(
					HandValueAbstraction.makePhi( params, s.player_value, s.player_high_aces, s.dealer_value ) ) );
			}
		}
		
		return states;
	}
	
	private static void writeClustering( final MetricConstrainedKMeans kmeans, final File root,
										 final int iter, final BlackjackParameters params,
										 final String[][] hard_actions, final String[][] soft_actions )
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
						final int pv = (int) phi.getEntry( 0 );
						final int paces = (int) phi.getEntry( 1 );
						final int dv = (int) phi.getEntry( 2 );
						if( paces > 0 ) {
							writer.cell( soft_actions[pv - params.soft_hand_min][dv - params.dealer_showing_min] );
						}
						else {
							writer.cell( hard_actions[pv - params.hard_hand_min][dv - params.dealer_showing_min] );
						}
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
	
	private static <X extends FactoredRepresentation<BlackjackState>>
	Instances makeTrainingSet( final SolvedStateAccumulator<X> acc, final ArrayList<Attribute> attributes, final int iter )
	{
		final int[] num_instances = new int[2];
		final ArrayList<Instance> negative = new ArrayList<Instance>();
		final ArrayList<Instance> positive = new ArrayList<Instance>();
		final ArrayList<String> nominal = new ArrayList<String>();
		nominal.add( "0" );
		nominal.add( "1" );
		attributes.add( new Attribute( "__label__", nominal ) );
		final int d = attributes.size() - 1; // Minus 1 for label
		
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
		final Instances x = new Instances( "train" + iter, attributes, negative.size() + positive.size() );
		x.setClassIndex( d );
		x.addAll( negative );
		x.addAll( positive );
		
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
	
	private static final RandomGenerator rng = new MersenneTwister( 42 );
	
	private static <X extends FactoredRepresentation<BlackjackState>, R extends Representer<BlackjackState, X>>
	void runExperiment( final BlackjackParameters params, final int Niterations,
						final int Ntrain_games, final int Ntrain_episodes,
						final int Ntest_games, final int Ntest_episodes,
						final File root ) throws Exception
	{
		final BlackjackAggregator repr = new BlackjackAggregator();
		final BlackjackMdp mdp = new BlackjackMdp( params );
		System.out.println( "Solving MDP" );
		final Pair<String[][], String[][]> soln = mdp.solve();
		final String[][] hard_actions = soln.first;
		final String[][] soft_actions = soln.second;
		
		System.out.println( "****************************************" );
		System.out.println( "game = " + params.max_score + " x ("
							+ Ntrain_games + "(" + Ntrain_episodes + ")"
							+ " / " + Ntest_games + "(" + Ntest_episodes + ")) "
							+ ": " + repr );
		final Csv.Writer data_out = new Csv.Writer( new PrintStream( new File( root, "data.csv" ) ) );
		data_out.cell( "abstraction" ).cell( "game" ).cell( "iteration" )
				.cell( "Ntrain_games" ).cell( "Ntrain_episodes" ).cell( "Ntest_games" ).cell( "Ntest_episodes" )
				.cell( "mean" ).cell( "var" ).cell( "conf" ).newline();
		
		final ActionGenerator<BlackjackState, JointAction<BlackjackAction>> action_gen
			= new BlackjackJointActionGenerator( 1 );
		final Policy<BlackjackState, JointAction<BlackjackAction>>
			rollout_policy = new RandomPolicy<BlackjackState, JointAction<BlackjackAction>>(
				0 /*Player*/, rng.nextInt(), action_gen.create() );
		
		final double c = 1.0;
		final int rollout_width = 1;
		final int rollout_depth = 1;
		// Optimistic default value
		final double[] default_value = new double[] { 1.0 };
		Representer<BlackjackState, ClusterAbstraction<BlackjackState>> Crepr
			= new TrivialClusterRepresenter( params, mdp.S() );
		
		// NOTE: In the Blackjack domain, we can easily enumerate all
		// legal states, so I'm punting the issue of how to collect them
		// properly during search. In reality, there's a question of
		// whether we should be weighting them, e.g. by their reachability.
		final ArrayList<RealVector> Phi = enumerateStates( params );
		
		RealMatrix A0 = MatrixUtils.createRealIdentityMatrix( Phi.get( 0 ).getDimension() );
		
		for( int iter = 0; iter < Niterations; ++iter ) {
			System.out.println( "Iteration " + iter );
//			final UnlabeledStateAccumulator<ClusterAbstraction<BlackjackState>> train_visitor
//				= new UnlabeledStateAccumulator<ClusterAbstraction<BlackjackState>>( Crepr.create() );
			final MctsVisitor<
				BlackjackState, ClusterAbstraction<BlackjackState>, BlackjackAction
			> train_visitor = new DefaultMctsVisitor<
					BlackjackState, ClusterAbstraction<BlackjackState>, BlackjackAction
				>();
			final BackupRule<ClusterAbstraction<BlackjackState>, BlackjackAction> train_backup
				= BackupRule.<ClusterAbstraction<BlackjackState>, BlackjackAction>MaxQ();
			
			// Gather training examples
			System.out.println( "Gathering training examples..." );
			final SolvedStateAccumulator<HandValueAbstraction> acc
				= new SolvedStateAccumulator<HandValueAbstraction>( repr );
			for( int i = 0; i < Ntrain_games; ++i ) {
				if( i % 100000 == 0 ) {
					System.out.println( "Episode " + i );
				}
				
				final Deck deck = new InfiniteDeck();
				final BlackjackSimulator sim = new BlackjackSimulator( deck, 1, params );
				
				final GameTreeFactory<
					BlackjackState, ClusterAbstraction<BlackjackState>, BlackjackAction
				> factory
					= new UctSearch.Factory<BlackjackState, ClusterAbstraction<BlackjackState>, BlackjackAction>(
						sim, Crepr.create(), action_gen.create(), c, Ntrain_episodes, rng,
						rollout_policy, rollout_width, rollout_depth, train_backup, default_value );
				
				final SearchPolicy<BlackjackState, ClusterAbstraction<BlackjackState>, BlackjackAction>
					search_policy = new SearchPolicy<BlackjackState, ClusterAbstraction<BlackjackState>, BlackjackAction>( factory, train_visitor, null ) {
						@Override
						protected JointAction<BlackjackAction> selectAction( final GameTree<ClusterAbstraction<BlackjackState>, BlackjackAction> tree )
						{ return BackupRules.MaxAction( tree.root() ).a(); }
	
						@Override
						public int hashCode()
						{ return System.identityHashCode( this ); }
	
						@Override
						public boolean equals( final Object that )
						{ return this == that; }
				};
				
				final Episode<BlackjackState, BlackjackAction> episode
					= new Episode<BlackjackState, BlackjackAction>(	sim, search_policy );
				episode.addListener( acc );
				episode.run();
			}
			
			// Train classifier
			System.out.println( "Training classifier..." );
			final String algorithm = "kmeans"; //"rf";
			final boolean with_metric_learning = true;
			if( "kmeans".equals( algorithm ) ) {
				// final ArrayList<RealVector> Phi = train_visitor.Phi_;
				final MetricConstrainedKMeans kmeans = makeClustering( rng, A0, acc.Phi_, acc.actions_, Phi,
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
				writeClustering( kmeans, root, iter, params, hard_actions, soft_actions );
				Crepr = new ClusterRepresenter( classifier, repr.create() );
			}
			else if( "rf".equals( algorithm ) ) {
				final Instances train = makeTrainingSet( acc, HandValueAbstraction.makeAttributes( params ), iter );
				writeDataset( root, train );
				final Classifier classifier = makeClassifier( train );
				SerializationHelper.write( new File( root, "rf" + iter + ".model" ).getAbsolutePath(), classifier );
				Crepr = new PairwiseSimilarityRepresenter<BlackjackState, HandValueAbstraction>(
					repr.create(), new Instances( train ), classifier );
			}
			
			// Test
			System.out.println( "Testing..." );
			final MctsVisitor<BlackjackState, ClusterAbstraction<BlackjackState>, BlackjackAction> test_visitor
				= new DefaultMctsVisitor<BlackjackState, ClusterAbstraction<BlackjackState>, BlackjackAction>();
			final BackupRule<ClusterAbstraction<BlackjackState>, BlackjackAction> test_backup
				= BackupRule.<ClusterAbstraction<BlackjackState>, BlackjackAction>MaxQ();
			final MeanVarianceAccumulator ret = new MeanVarianceAccumulator();
			for( int i = 0; i < Ntest_games; ++i ) {
				if( i % 10000 == 0 ) {
					System.out.println( "Episode " + i );
				}
				
				final Deck deck = new InfiniteDeck();
				final BlackjackSimulator sim = new BlackjackSimulator( deck, 1, params );
				
				final GameTreeFactory<
					BlackjackState, ClusterAbstraction<BlackjackState>, BlackjackAction
				> factory
					= new UctSearch.Factory<BlackjackState, ClusterAbstraction<BlackjackState>, BlackjackAction>(
						sim, Crepr.create(), action_gen.create(), c, Ntest_episodes, rng,
						rollout_policy, rollout_width, rollout_depth, test_backup, default_value );
				
				final SearchPolicy<BlackjackState, ClusterAbstraction<BlackjackState>, BlackjackAction>
					search_policy = new SearchPolicy<BlackjackState, ClusterAbstraction<BlackjackState>, BlackjackAction>( factory, test_visitor, null ) {
						@Override
						protected JointAction<BlackjackAction> selectAction( final GameTree<ClusterAbstraction<BlackjackState>, BlackjackAction> tree )
						{ return BackupRules.MaxAction( tree.root() ).a(); }
	
						@Override
						public int hashCode()
						{ return System.identityHashCode( this ); }
	
						@Override
						public boolean equals( final Object that )
						{ return this == that; }
				};
				
				final Episode<BlackjackState, BlackjackAction> episode
					= new Episode<BlackjackState, BlackjackAction>(	sim, search_policy );
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
//			data_out.println( "abstraction,game,iterations,Ntrain_games,Ntrain_episodes,Ntest_games,Ntest_episodes,mean,var,conf" );
			data_out.cell( repr ).cell( params.max_score ).cell( iter )
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
		final BlackjackParameters params = new BlackjackParameters();
		final int Niterations = 4;
		final int Ntrain_games = 100;
		final int Ntest_games = 100000;
		final int Ntrain_episodes = 2048;
		final int Ntest_episodes = 256;
		final File root = new File( "discovery/sandbox" );
		
		runExperiment( params, Niterations, Ntrain_games, Ntrain_episodes, Ntest_games, Ntest_episodes, root );
	}

}
