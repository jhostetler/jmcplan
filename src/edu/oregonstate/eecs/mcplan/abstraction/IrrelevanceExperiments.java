package edu.oregonstate.eecs.mcplan.abstraction;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.MersenneTwister;

import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.Saver;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FixedEffortPolicy;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.RandomPolicy;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.domains.toy.Irrelevance;
import edu.oregonstate.eecs.mcplan.domains.toy.Irrelevance.Action;
import edu.oregonstate.eecs.mcplan.domains.toy.Irrelevance.IdentityRepresentation;
import edu.oregonstate.eecs.mcplan.domains.toy.Irrelevance.State;
import edu.oregonstate.eecs.mcplan.domains.voyager.Player;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerInstance;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerParameters;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerState;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerStateToken;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerVisualization;
import edu.oregonstate.eecs.mcplan.experiments.Environment;
import edu.oregonstate.eecs.mcplan.experiments.ExecutionTimer;
import edu.oregonstate.eecs.mcplan.experiments.Experiment;
import edu.oregonstate.eecs.mcplan.experiments.ExperimentalSetup;
import edu.oregonstate.eecs.mcplan.experiments.Instance;
import edu.oregonstate.eecs.mcplan.experiments.MultipleInstanceMultipleWorldGenerator;
import edu.oregonstate.eecs.mcplan.ml.GameTreeStateSimilarityDataset;
import edu.oregonstate.eecs.mcplan.search.ActionNode;
import edu.oregonstate.eecs.mcplan.search.BackupRule;
import edu.oregonstate.eecs.mcplan.search.BackupRules;
import edu.oregonstate.eecs.mcplan.search.GameTree;
import edu.oregonstate.eecs.mcplan.search.GameTreeFactory;
import edu.oregonstate.eecs.mcplan.search.MctsVisitor;
import edu.oregonstate.eecs.mcplan.search.SearchPolicy;
import edu.oregonstate.eecs.mcplan.search.SparseSampleTree;
import edu.oregonstate.eecs.mcplan.search.StateNode;
import edu.oregonstate.eecs.mcplan.sim.Episode;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.Tuple.Tuple2;

public class IrrelevanceExperiments
{
	// -----------------------------------------------------------------------
	
	public static class ContextualPiStar<A extends VirtualConstructor<A>>
		extends GameTreeStateSimilarityDataset<VoyagerStateToken, JointAction<A>>
	{
		public final double false_positive_weight;
		
		public ContextualPiStar( final GameTree<VoyagerStateToken, JointAction<A>> tree,
								 final ArrayList<Attribute> attributes,
								 final int player, final double false_positive_weight )
		{
			// TODO: It appears that context = true is the same as context = false ???
			super( tree, attributes, player, 1 /* min_samples to consider a state node */,
				   4 /* max instances of each class */, true /* Use context */ );
			this.false_positive_weight = false_positive_weight;
		}
		
		private ActionNode<VoyagerStateToken, JointAction<A>> getAction( final StateNode<VoyagerStateToken, JointAction<A>> s )
		{
			return BackupRules.MaxMinAction( s );
		}

		@Override
		public Tuple2<Integer, Double> label( final List<ActionNode<VoyagerStateToken, JointAction<A>>> path,
											  final int player,
											  final StateNode<VoyagerStateToken, JointAction<A>> s1,
											  final StateNode<VoyagerStateToken, JointAction<A>> s2 )
		{
			final ActionNode<VoyagerStateToken, JointAction<A>> a1 = getAction( s1 );
			final ActionNode<VoyagerStateToken, JointAction<A>> a2 = getAction( s2 );
			final int label;
			if( a1 != null && a2 != null && a1.a( player ).equals( a2.a( player ) ) ) {
				label = 1;
			}
			else {
				label = 0;
			}
			final double weight = computeInstanceWeight( s1, a1, s2, a2, label, false_positive_weight );
			return Tuple2.of( label, weight );
		}
	}
	
	static <X extends FactoredRepresentation<?>, A extends VirtualConstructor<A>>
	double computeInstanceWeight(
		final StateNode<X, A> s1,
		final ActionNode<X, A> a1,
		final StateNode<X, A> s2,
		final ActionNode<X, A> a2,
		final int label, final double fp_weight )
	{
		if( label == 1 ) {
			return 1.0;
		}
		else {
			// TODO: Assumes zero-sum game.
			final double qdiff;
			if( a1 == null || a2 == null ) {
				qdiff = 2; // TODO: This is actually 2 * Vmax
			}
			else {
				// Cost of false positive is the largest difference in
				// Q-value from doing the wrong action in s1 or s2.
				final ActionNode<X, A> a1_prime = s1.getActionNode( a2.a() );
				final ActionNode<X, A> a2_prime = s2.getActionNode( a1.a() );
				if( a1_prime == null && a2_prime == null ) {
					System.out.println( "! a1_prime and a2_prime both null" );
				}
				final double d1 = (a1_prime == null ? 0 : Math.abs( a1_prime.q( 0 ) - a1.q( 0 ) ));
				final double d2 = (a2_prime == null ? 0 : Math.abs( a2_prime.q( 0 ) - a2.q( 0 ) ));
				// TODO: 1.0 is to prevent 0 weights; should be parameter?
				qdiff = Math.max( d1, d2 ) + 1.0;
			}
			return fp_weight * qdiff;
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static class IrrelevanceInstance
		extends Instance<IrrelevanceInstance, Irrelevance.State, Irrelevance.Action>
	{
		private final MersenneTwister rng_;
		private final Irrelevance.Simulator sim_ = new Irrelevance.Simulator();
		
		public IrrelevanceInstance( final int seed )
		{
			rng_ = new MersenneTwister( seed );
		}
		
		@Override
		public int nextSeed()
		{
			return rng_.nextInt();
		}

		@Override
		public void writeCsv( final PrintStream out )
		{
			// TODO Auto-generated method stub
		}

		@Override
		public IrrelevanceInstance copy()
		{
			return new IrrelevanceInstance( rng_.nextInt() );
		}

		@Override
		public UndoSimulator<State, Action> simulator()
		{
			return sim_;
		}

		@Override
		public State state()
		{
			return sim_.state();
		}
	}
	
	public static class IrrelevanceDomain extends Experiment<VoyagerParameters, IrrelevanceInstance>
	{
		public static final String log_filename = "log.csv";
		
		private Environment env_ = null;
		private VoyagerParameters params_ = null;
		private IrrelevanceInstance world_ = null;
		
		private final int width_;
		private final int depth_;
		private final int rollout_width_;
		private final int rollout_depth_;
		private final int act_epoch_;
		private final int lookahead_epoch_;
		private final double false_positive_weight_;
		private final double q_tolerance_;

		public ExecutionTimer<VoyagerState, VoyagerAction> timer = null;
		
		private final VoyagerVisualization vis_ = null;
		
		// TODO: This is the player index we're building the abstraction for.
		// Should be a parameter.
		private final int player_ = 0;
		
		public IrrelevanceDomain( final String[] args )
		{
			width_ = Integer.parseInt( args[0] );
			depth_ = Integer.parseInt( args[1] );
			rollout_width_ = Integer.parseInt( args[2] );
			rollout_depth_ = Integer.parseInt( args[3] );
			act_epoch_ = Integer.parseInt( args[4] );
			lookahead_epoch_ = Integer.parseInt( args[5] );
			false_positive_weight_ = Double.parseDouble( args[6] );
			q_tolerance_ = Double.parseDouble( args[7] );
		}
		
		@Override
		public String getFileSystemName()
		{
			return "irrelevance";
		}
		
		@Override
		public void setup( final Environment env, final VoyagerParameters params, final IrrelevanceInstance world )
		{
			env_ = env;
			params_ = params;
			world_ = world;
			
			timer = new ExecutionTimer<VoyagerState, VoyagerAction>();
			
			try {
				final PrintStream pout = new PrintStream( new File( env.root_directory, "parameters.csv" ) );
				params_.writeCsv( pout );
				pout.close();
				
//				final PrintStream iout = new PrintStream( new File( env.root_directory, "instance.csv" ) );
//				world_.writeCsv( iout );
//				iout.close();
			}
			catch( final FileNotFoundException ex ) {
				throw new RuntimeException( ex );
			}
		}
		
		@Override
		public void finish()
		{ }
	
		@Override
		public void run()
		{
			final int offline = 0;
			final int online = 1;
			final int variant = offline;
			final int ntrajectories = 50; // TODO: Make this a parameter
			for( int i = 0; i < ntrajectories; ++i ) {
				System.out.println( "[Trajectory " + i + "]" );
				
				final Irrelevance.IdentityRepresenter base_repr = new Irrelevance.IdentityRepresenter();
				final ArrayList<Attribute> attributes = Irrelevance.IdentityRepresentation.attributes();
				
				// Set up sparse sample tree
				//
				final UndoSimulator<Irrelevance.State, Irrelevance.Action> primitive_sim = new Irrelevance.Simulator();
				final Irrelevance.ActionGen action_gen = new Irrelevance.ActionGen( env_.rng );
				
				final ArrayList<Policy<VoyagerState, VoyagerAction>> policies
					= new ArrayList<Policy<VoyagerState, VoyagerAction>>();
				if( variant == offline ) {
					final Representer<Irrelevance.State, Irrelevance.IdentityRepresentation> repr = base_repr;
					final MctsVisitor<Irrelevance.State, Irrelevance.IdentityRepresentation, Irrelevance.Action> visitor
						= new Irrelevance.Visitor<Irrelevance.IdentityRepresentation, Irrelevance.Action>();
					final Policy<Irrelevance.State, JointAction<Irrelevance.Action>>
						rollout_policy = new RandomPolicy<Irrelevance.State, JointAction<Irrelevance.Action>>(
							0 /*Player*/, env_.rng.nextInt(), action_gen.create() );
				
					final String name = "t" + i;
					final Instances dataset = WekaUtil.createEmptyInstances( name, attributes );
					final File dataset_file = new File( env_.root_directory, name + ".arff" );
					final Saver saver = new ArffSaver();
					try {
						saver.setFile( dataset_file );
					}
					catch( final IOException ex ) {
						throw new RuntimeException( ex );
					}
					
					final PrintStream log_stream;
					try {
						log_stream = new PrintStream( new BufferedOutputStream( new FileOutputStream(
							new File( env_.root_directory, "tree" + i + ".log" ) ) ) );
					}
					catch( final FileNotFoundException ex ) {
						throw new RuntimeException( ex );
					}
					
					final BackupRule<Irrelevance.IdentityRepresentation, Irrelevance.Action> backup
						= BackupRule.<Irrelevance.IdentityRepresentation, Irrelevance.Action>MaxQ();
					final GameTreeFactory<
						Irrelevance.State, Irrelevance.IdentityRepresentation, Irrelevance.Action
					> factory
						= new SparseSampleTree.Factory<
								Irrelevance.State, Irrelevance.IdentityRepresentation, Irrelevance.Action>(
							primitive_sim, repr, action_gen, width_, depth_,
							rollout_policy, rollout_width_, rollout_depth_, backup );
					final int min_samples = 1;
					final int max_instances = 256;
					final AbstractionBuilder<Irrelevance.State, Irrelevance.IdentityRepresentation, Irrelevance.Action>
						abstraction_builder	= new AbstractionBuilder<
								Irrelevance.State, Irrelevance.IdentityRepresentation, Irrelevance.Action>(
							factory, visitor, attributes, Player.Min.ordinal(),
							min_samples, max_instances, false_positive_weight_, q_tolerance_, false, log_stream ) {
								@Override
								public double computeInstanceWeight(
										final StateNode<IdentityRepresentation, Action> s1,
										final ActionNode<IdentityRepresentation, Action> a1,
										final StateNode<IdentityRepresentation, Action> s2,
										final ActionNode<IdentityRepresentation, Action> a2,
										final int label, final double fp_weight )
								{
									return IrrelevanceExperiments.computeInstanceWeight( s1, a1, s2, a2, label, fp_weight );
								}

								@Override
								public ActionNode<IdentityRepresentation, Action> getAction(
										final StateNode<IdentityRepresentation, Action> s )
								{
									return BackupRules.MaxAction( s );
								}
					};
					final Policy<Irrelevance.State, JointAction<Irrelevance.Action>> abstraction_executor
						= new FixedEffortPolicy<Irrelevance.State, JointAction<Irrelevance.Action>>(
							abstraction_builder, params_.max_time[player_] );
					
					final Episode<Irrelevance.State, Irrelevance.Action> episode
						= new Episode<Irrelevance.State, Irrelevance.Action>(
							primitive_sim, abstraction_executor );
					episode.run();
					
					// TODO: Not using context for debugging
					System.out.println( "*** Merging instances" );
					for( final Map.Entry<?, Instances> e : abstraction_builder.instances().entrySet() ) {
						dataset.addAll( e.getValue() );
					}
					saver.setInstances( dataset );
					try {
						saver.writeBatch();
					}
					catch( final IOException ex ) {
						throw new RuntimeException( ex );
					}
				}
				else if( variant == online ) {
					final MctsVisitor<Irrelevance.State, AggregateState<Irrelevance.State>, Irrelevance.Action>
						visitor	= new Irrelevance.Visitor<AggregateState<Irrelevance.State>, Irrelevance.Action>();
					
					final Classifier c;
					try {
						// TODO: Hardcoded path
						final Object[] weka_model = weka.core.SerializationHelper.readAll(
							"C:/Users/jhostetler/osu/rts/galcon/MCPlanning/master_random-forest.model" );
						c = (Classifier) weka_model[0];
					}
					catch( final Exception ex ) {
						throw new RuntimeException( ex );
					}
					final Representer<Irrelevance.State, AggregateState<Irrelevance.State>> repr
						= new Aggregator<Irrelevance.State, Irrelevance.IdentityRepresentation>( base_repr, attributes, c );
					
					final Policy<Irrelevance.State, JointAction<Irrelevance.Action>>
						rollout_policy = new RandomPolicy<Irrelevance.State, JointAction<Irrelevance.Action>>(
							0 /*Player*/, env_.rng.nextInt(), action_gen.create() );
				
					final PrintStream log_stream;
					try {
						log_stream = new PrintStream( new BufferedOutputStream( new FileOutputStream(
							new File( env_.root_directory, "online" + i + ".log" ) ) ) );
					}
					catch( final FileNotFoundException ex ) {
						throw new RuntimeException( ex );
					}
					
					final BackupRule<AggregateState<Irrelevance.State>, Irrelevance.Action> backup
						= BackupRule.<AggregateState<Irrelevance.State>, Irrelevance.Action>MaxMinQ();
					final GameTreeFactory<
						Irrelevance.State, AggregateState<Irrelevance.State>, Irrelevance.Action
					> factory
						= new SparseSampleTree.Factory<Irrelevance.State, AggregateState<Irrelevance.State>, Irrelevance.Action>(
							primitive_sim, repr, action_gen, width_, depth_,
							rollout_policy, rollout_width_, rollout_depth_, backup );
					
					final SearchPolicy<Irrelevance.State, AggregateState<Irrelevance.State>,
									   Irrelevance.Action>
						search_policy = new SearchPolicy<Irrelevance.State, AggregateState<Irrelevance.State>,
									   					 Irrelevance.Action>(
							factory, visitor, log_stream ) {

								@Override
								protected JointAction<Irrelevance.Action> selectAction(
										final GameTree<AggregateState<Irrelevance.State>, Irrelevance.Action> tree )
								{
									return BackupRules.MaxMinAction( tree.root() ).a();
								}

								@Override
								public int hashCode()
								{ return System.identityHashCode( this ); }

								@Override
								public boolean equals( final Object that )
								{ return this == that; }
					};
					final Policy<Irrelevance.State, JointAction<Irrelevance.Action>> min_policy
						= new FixedEffortPolicy<Irrelevance.State, JointAction<Irrelevance.Action>>(
							search_policy, params_.max_time[player_] );
					
					final Episode<Irrelevance.State, Irrelevance.Action> episode
						= new Episode<Irrelevance.State, Irrelevance.Action>(
							primitive_sim, min_policy );
					episode.run();
				}
			}
			
//			int npositive = 0;
//			for( final Instance inst : dataset ) {
//				if( inst.classValue() == 1.0 ) {
//					npositive += 1;
//				}
//			}
//			System.out.println( "========== FULL DATASET ==========" );
//			System.out.println( "ninstances = [" + (dataset.size() - npositive) + ", " + npositive + "]" );
//			System.out.println( "========== ============ ==========" );
//
//			final Classifier classifier = createClassifier();
//			System.out.println( "*** Building classifier" );
//			try {
//				classifier.buildClassifier( dataset );
//			}
//			catch( final Exception ex ) {
//				System.out.println( "! Error in buildClassifier():" );
//				ex.printStackTrace();
//				System.exit( -1 );
//			}
//			System.out.println( classifier );
		}
		
		final Classifier createClassifier()
		{
			final Logistic classifier = new Logistic();
			return classifier;
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static File createDirectory( final String[] args )
	{
		final File r = new File( args[0] );
		r.mkdir();
		final File d = new File( r, "x" + args[1] + "_" + args[2] );
		d.mkdir();
		return d;
	}
	
	private static VoyagerInstance createInstance( final VoyagerParameters params, final Environment env )
	{
		return new VoyagerInstance( params, env.rng.nextLong() );
	}
	
	public static void main( final String[] args )
	{
		System.out.println( args.toString() );
		final String batch_name = args[0];
		final String[] instance_args = args[1].split( "," );
		final String[] abstraction_args = args[2].split( "," );
		
		final File root_directory = createDirectory( args );
		int idx = 0;
		final int Nworlds = Integer.parseInt( instance_args[idx++] );
		final int master_seed = Integer.parseInt( instance_args[idx++] );
		final int[] max_time = new int[Player.competitors];
		for( int p = 0; p < Player.competitors; ++p ) {
			max_time[p] = Integer.parseInt( instance_args[idx++] );
		}
		
		// FIXME: This default_params thing is too error-prone! There's no
		// easy way to know whether you need to set a parameter in
		// 1) default_params
		// 2) an element of ps
		// 3) both places
		final VoyagerParameters default_params = new VoyagerParameters.Builder()
			.master_seed( master_seed ).finish();
		final Environment default_environment = new Environment.Builder()
			.root_directory( root_directory )
			.rng( new MersenneTwister( default_params.master_seed ) )
			.finish();
		
//		final int[] anytime_times = new int[Nanytime];
//		anytime_times[Nanytime - 1] = max_time;
//		for( int i = Nanytime - 2; i >= 0; --i ) {
//			anytime_times[i] = anytime_times[i + 1] / 2;
//		}
		
		final List<VoyagerParameters> ps = new ArrayList<VoyagerParameters>( 1 );
		ps.add( new VoyagerParameters.Builder().max_time( max_time ).finish() );
		
		final List<IrrelevanceInstance> ws = new ArrayList<IrrelevanceInstance>( Nworlds );
		for( int i = 0; i < Nworlds; ++i ) {
			// FIXME: Why default_params and not ps.get( i ) ?
//			ws.add( createInstance( default_params, default_environment ) );
			ws.add( new IrrelevanceInstance( default_environment.rng.nextInt() ) );
		}
		
		final MultipleInstanceMultipleWorldGenerator<VoyagerParameters, IrrelevanceInstance>
			experimental_setups = new MultipleInstanceMultipleWorldGenerator<VoyagerParameters, IrrelevanceInstance>(
				default_environment, ps, ws );
		
		final Experiment<VoyagerParameters, IrrelevanceInstance> experiment
			= new IrrelevanceDomain( abstraction_args ); //new String[] { "1", "100", "16", "16" } );
		
		while( experimental_setups.hasNext() ) {
			final ExperimentalSetup<VoyagerParameters, IrrelevanceInstance> setup = experimental_setups.next();
			experiment.setup( setup.environment, setup.parameters, setup.world );
			experiment.run();
			experiment.finish();
		}
		
		/*
		final List<VoyagerInstance> ws = new ArrayList<VoyagerInstance>( Nworlds );
		for( int i = 0; i < Nworlds; ++i ) {
			// FIXME: Why default_params and not ps.get( i ) ?
			ws.add( createInstance( default_params, default_environment ) );
		}
		
		final MultipleInstanceMultipleWorldGenerator<VoyagerParameters, VoyagerInstance>
			experimental_setups = new MultipleInstanceMultipleWorldGenerator<VoyagerParameters, VoyagerInstance>(
				default_environment, ps, ws );
		
		final Experiment<VoyagerParameters, VoyagerInstance> experiment
			= new VoyagerDomain( abstraction_args ); //new String[] { "1", "100", "16", "16" } );
		
		while( experimental_setups.hasNext() ) {
			final ExperimentalSetup<VoyagerParameters, VoyagerInstance> setup = experimental_setups.next();
			experiment.setup( setup.environment, setup.parameters, setup.world );
			experiment.run();
			experiment.finish();
		}
		*/
		
		System.exit( 0 );
	}
}
