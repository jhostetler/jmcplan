package edu.oregonstate.eecs.mcplan.domains.voyager.experiments;

import java.awt.Dimension;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.math3.random.MersenneTwister;

import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.Saver;
import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.DurativeActionGenerator;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FixedEffortPolicy;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.JointPolicy;
import edu.oregonstate.eecs.mcplan.MarginalPolicy;
import edu.oregonstate.eecs.mcplan.Option;
import edu.oregonstate.eecs.mcplan.OptionPolicy;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.ProductActionGenerator;
import edu.oregonstate.eecs.mcplan.RandomPolicy;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.SingleStepAdapter;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.abstraction.AbstractionBuilder;
import edu.oregonstate.eecs.mcplan.abstraction.AggregateState;
import edu.oregonstate.eecs.mcplan.abstraction.Aggregator;
import edu.oregonstate.eecs.mcplan.abstraction.WekaUtil;
import edu.oregonstate.eecs.mcplan.domains.toy.Irrelevance;
import edu.oregonstate.eecs.mcplan.domains.toy.Irrelevance.Action;
import edu.oregonstate.eecs.mcplan.domains.toy.Irrelevance.IdentityRepresentation;
import edu.oregonstate.eecs.mcplan.domains.voyager.ControlMctsVisitor;
import edu.oregonstate.eecs.mcplan.domains.voyager.IdentityRepresenter;
import edu.oregonstate.eecs.mcplan.domains.voyager.Player;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerInstance;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerParameters;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerState;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerStateToken;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerVisualization;
import edu.oregonstate.eecs.mcplan.domains.voyager.policies.BalancedPolicy;
import edu.oregonstate.eecs.mcplan.experiments.Copyable;
import edu.oregonstate.eecs.mcplan.experiments.Environment;
import edu.oregonstate.eecs.mcplan.experiments.ExecutionTimer;
import edu.oregonstate.eecs.mcplan.experiments.Experiment;
import edu.oregonstate.eecs.mcplan.experiments.ExperimentalSetup;
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
import edu.oregonstate.eecs.mcplan.sim.OptionSimulator;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.Tuple.Tuple2;

public class AbstractionExperiments
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
	
	public static Policy<VoyagerState, VoyagerAction>
	createFixedPolicy( final VoyagerInstance instance, final Player player )
	{
		return new BalancedPolicy( player, instance.nextSeed(), 0.75, 1.25, 0.2 );
//		final ArrayList<Policy<VoyagerState, VoyagerAction>> Pi
//			= new ArrayList<Policy<VoyagerState, VoyagerAction>>();
//		Pi.add( new ExpansionPolicy( player,
//			new int[] { PlanetId.Natural_2nd( player ), PlanetId.Natural_3rd( player ) }, 2 ) );
//		Pi.add( new FortifyPolicy( player, new int[] { PlanetId.Center }, 2, 2 ) );
//		Pi.add( new AggressivePolicy( player, 2 ) );
//		return PhasedPolicy.create( Pi, new int[] { 0, 80, 140 } );
	}
	
	public static class FixedVoyagerPolicyGenerator
		extends ActionGenerator<VoyagerState, Policy<VoyagerState, VoyagerAction>>
	{
		private final VoyagerInstance instance_;
		private final Player player_;
		private boolean done_ = false;
		
		public FixedVoyagerPolicyGenerator( final VoyagerInstance instance, final Player player )
		{
			instance_ = instance;
			player_ = player;
		}
		
		@Override
		public ActionGenerator<VoyagerState, Policy<VoyagerState, VoyagerAction>> create()
		{
			return new FixedVoyagerPolicyGenerator( instance_, player_ );
		}

		@Override
		public void setState( final VoyagerState s, final long t, final int[] turn )
		{ done_ = false; }

		@Override
		public int size()
		{ return 1; }

		@Override
		public boolean hasNext()
		{ return !done_; }

		@Override
		public Policy<VoyagerState, VoyagerAction> next()
		{
			if( !done_ ) {
				done_ = true;
				return createFixedPolicy( instance_, player_ );
			}
			else {
				throw new NoSuchElementException();
			}
		}
		
	}
	
	public static class AbstractionInstance implements Copyable<AbstractionInstance>
	{
		private final VoyagerParameters params_;
		private final int seed_;
		
		public AbstractionInstance( final VoyagerParameters params, final int seed )
		{
			params_ = params;
			seed_ = seed;
		}
		
		public VoyagerInstance nextWorld()
		{
//			return new VoyagerInstance( params_, seed_ );
			return VoyagerInstance.createDesignedInstance( params_, seed_ );
		}
		
		@Override
		public AbstractionInstance copy()
		{
			return new AbstractionInstance( params_, seed_ );
		}
		
	}
		
	public static class VoyagerDomain extends Experiment<VoyagerParameters, AbstractionInstance>
	{
		public static final String log_filename = "log.csv";
		
		private Environment env_ = null;
		private VoyagerParameters params_ = null;
		private AbstractionInstance world_ = null;
		
		private final int width_;
		private final int depth_;
		private final int rollout_width_;
		private final int rollout_depth_;
		private final int act_epoch_;
		private final int lookahead_epoch_;
		private final double false_positive_weight_;
		private final double q_tolerance_;
		
		public EndScoreRecorder end_state = null;
		public ExecutionTimer<VoyagerState, VoyagerAction> timer = null;
		
		private final VoyagerVisualization vis_ = null;
		
		// TODO: This is the player index we're building the abstraction for.
		// Should be a parameter.
		private final int player_ = 0;
		
		public VoyagerDomain( final String[] args )
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
			return "abstraction_SS";
		}
		
		@Override
		public void setup( final Environment env, final VoyagerParameters params, final AbstractionInstance world )
		{
			env_ = env;
			params_ = params;
			world_ = world;
			
			end_state = new EndScoreRecorder();
			timer = new ExecutionTimer<VoyagerState, VoyagerAction>( params_.Nplayers );
			
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
			final VoyagerVisualization<VoyagerAction> vis;
			if( true || params_.use_monitor ) {
				final int wait = 0;
				vis = new VoyagerVisualization<VoyagerAction>(
					params_, new Dimension( 720, 720 ), wait );
			}
			else {
				vis = null;
			}
			
			final int offline = 0;
			final int online = 1;
			final int variant = online;
			final int ntrajectories = 50; // TODO: Make this a parameter
			for( int i = 0; i < ntrajectories; ++i ) {
				System.out.println( "[Trajectory " + i + "]" );
				
				final VoyagerInstance game = world_.nextWorld();
				// TODO: Where should these quantities come from?
				final int Nplanets = game.state().planets.length;
				final int max_eta = params_.max_eta;
				final IdentityRepresenter base_repr = new IdentityRepresenter( Nplanets, max_eta );
				final ArrayList<Attribute> attributes = VoyagerStateToken.attributes( Nplanets, max_eta );
				
				// Set up sparse sample tree
				//
				final UndoSimulator<VoyagerState, VoyagerAction> primitive_sim = game.simulator();
				
				final OptionSimulator<VoyagerState, VoyagerAction> opt_sim
					= new OptionSimulator<VoyagerState, VoyagerAction>(
						primitive_sim, game.nextSeed() );
				
				final ArrayList<ActionGenerator<VoyagerState, ? extends Option<VoyagerState, VoyagerAction>>> gen_list
					= new ArrayList<ActionGenerator<VoyagerState, ? extends Option<VoyagerState, VoyagerAction>>>();
		//			gen_list.add( new DurativeActionGenerator<VoyagerState, VoyagerAction>(
		//				new VoyagerPolicyGenerator( params_, world_, Player.Min ), act_epoch_ ) );
		//			gen_list.add( new DurativeActionGenerator<VoyagerState, VoyagerAction>(
		//				new VoyagerPolicyGenerator( params_, world_, Player.Max ), act_epoch_ ) );
				gen_list.add( new DurativeActionGenerator<VoyagerState, VoyagerAction>(
					new SpecialistPolicyGenerator( game, Player.Min ), act_epoch_ ) );
//					new BalancedPolicyGenerator( params_, game, Player.Min ), act_epoch_ ) );
		//			gen_list.add( new DurativeActionGenerator<VoyagerState, VoyagerAction>(
		//				new BalancedPolicyGenerator( params_, world_, Player.Max ), act_epoch_ ) );
				gen_list.add( new DurativeActionGenerator<VoyagerState, VoyagerAction>(
					new FixedVoyagerPolicyGenerator( game, Player.Max ), act_epoch_ ) );
				final ProductActionGenerator<VoyagerState, Option<VoyagerState, VoyagerAction>> pgen
					= new ProductActionGenerator<VoyagerState, Option<VoyagerState, VoyagerAction>>( gen_list );
				
				final ArrayList<Policy<VoyagerState, VoyagerAction>> policies
					= new ArrayList<Policy<VoyagerState, VoyagerAction>>();
				if( variant == offline ) {
					final Representer<VoyagerState, VoyagerStateToken> repr = base_repr;
					final MctsVisitor<VoyagerState, VoyagerStateToken, Option<VoyagerState, VoyagerAction>> visitor
						= new ControlMctsVisitor<VoyagerStateToken, Option<VoyagerState, VoyagerAction>>();
					final Policy<VoyagerState, JointAction<Option<VoyagerState, VoyagerAction>>>
						rollout_policy = new RandomPolicy<VoyagerState, JointAction<Option<VoyagerState, VoyagerAction>>>(
							0 /*Player*/, game.nextSeed(), pgen.create() );
				
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
					
					final BackupRule<VoyagerStateToken,
								 	 Option<VoyagerState, VoyagerAction>> backup
						= BackupRule.<VoyagerStateToken,
									  Option<VoyagerState, VoyagerAction>>MaxMinQ();
					final GameTreeFactory<
						VoyagerState, VoyagerStateToken,
						Option<VoyagerState, VoyagerAction>
					> factory
						= new SparseSampleTree.Factory<VoyagerState, VoyagerStateToken,
													   Option<VoyagerState, VoyagerAction>>(
							opt_sim, repr, pgen, width_, depth_,
							rollout_policy, rollout_width_, rollout_depth_, backup );
					final int min_samples = 1;
					final int max_instances = 256;
					final AbstractionBuilder<VoyagerState, VoyagerStateToken,
											 Option<VoyagerState, VoyagerAction>>
						abstraction_builder	= new AbstractionBuilder<VoyagerState, VoyagerStateToken,
																	 Option<VoyagerState, VoyagerAction>>(
							factory, visitor, attributes, Player.Min.ordinal(),
							min_samples, max_instances, false_positive_weight_, q_tolerance_, false, log_stream ) {
								@Override
								public double computeInstanceWeight(
										final StateNode<VoyagerStateToken, Option<VoyagerState, VoyagerAction>> s1,
										final ActionNode<VoyagerStateToken, Option<VoyagerState, VoyagerAction>> a1,
										final StateNode<VoyagerStateToken, Option<VoyagerState, VoyagerAction>> s2,
										final ActionNode<VoyagerStateToken, Option<VoyagerState, VoyagerAction>> a2,
										final int label, final double fp_weight )
								{
									return AbstractionExperiments.computeInstanceWeight( s1, a1, s2, a2, label, fp_weight );
								}
					};
					final Policy<VoyagerState, VoyagerAction> abstraction_executor
						= new OptionPolicy<VoyagerState, VoyagerAction>(
							new SingleStepAdapter<VoyagerState, VoyagerAction>(
								new MarginalPolicy<VoyagerState, Option<VoyagerState, VoyagerAction>>(
									new FixedEffortPolicy<VoyagerState, JointAction<Option<VoyagerState, VoyagerAction>>>(
										abstraction_builder, params_.max_time[player_] ),
									0 ) ) );
					
					policies.add( abstraction_executor );
					policies.add( createFixedPolicy( game, Player.Max ) );
					final JointPolicy<VoyagerState, VoyagerAction> joint_policy
						= new JointPolicy<VoyagerState, VoyagerAction>( policies );
					
					final Episode<VoyagerState, VoyagerAction> episode
						= new Episode<VoyagerState, VoyagerAction>( primitive_sim, joint_policy );
					if( vis != null ) {
						vis.attach( episode );
					}
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
					final MctsVisitor<VoyagerState, AggregateState<VoyagerState>, Option<VoyagerState, VoyagerAction>> visitor
						= new ControlMctsVisitor<AggregateState<VoyagerState>, Option<VoyagerState, VoyagerAction>>();
					
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
					final Representer<VoyagerState, AggregateState<VoyagerState>> repr
						= new Aggregator<VoyagerState, VoyagerStateToken>( base_repr, attributes, c );
					
					final Policy<VoyagerState, JointAction<Option<VoyagerState, VoyagerAction>>>
						rollout_policy = new RandomPolicy<VoyagerState, JointAction<Option<VoyagerState, VoyagerAction>>>(
							0 /*Player*/, game.nextSeed(), pgen.create() );
				
					final PrintStream log_stream;
					try {
						log_stream = new PrintStream( new BufferedOutputStream( new FileOutputStream(
							new File( env_.root_directory, "online" + i + ".log" ) ) ) );
					}
					catch( final FileNotFoundException ex ) {
						throw new RuntimeException( ex );
					}
					
					final BackupRule<AggregateState<VoyagerState>,
								 	 Option<VoyagerState, VoyagerAction>> backup
						= BackupRule.<AggregateState<VoyagerState>,
									  Option<VoyagerState, VoyagerAction>>MaxMinQ();
					final GameTreeFactory<
						VoyagerState, AggregateState<VoyagerState>, Option<VoyagerState, VoyagerAction>
					> factory
						= new SparseSampleTree.Factory<VoyagerState, AggregateState<VoyagerState>,
													   Option<VoyagerState, VoyagerAction>>(
							opt_sim, repr, pgen, width_, depth_,
							rollout_policy, rollout_width_, rollout_depth_, backup );
					
					final SearchPolicy<VoyagerState, AggregateState<VoyagerState>,
									   Option<VoyagerState, VoyagerAction>>
						search_policy = new SearchPolicy<VoyagerState, AggregateState<VoyagerState>,
									   					 Option<VoyagerState, VoyagerAction>>(
							factory, visitor, log_stream ) {

								@Override
								protected JointAction<Option<VoyagerState, VoyagerAction>> selectAction(
										final GameTree<AggregateState<VoyagerState>, Option<VoyagerState, VoyagerAction>> tree )
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
					final Policy<VoyagerState, VoyagerAction> min_policy
						= new OptionPolicy<VoyagerState, VoyagerAction>(
							new SingleStepAdapter<VoyagerState, VoyagerAction>(
								new MarginalPolicy<VoyagerState, Option<VoyagerState, VoyagerAction>>(
									new FixedEffortPolicy<VoyagerState, JointAction<Option<VoyagerState, VoyagerAction>>>(
										search_policy, params_.max_time[player_] ),
									0 ) ) );
					
					policies.add( min_policy );
					policies.add( createFixedPolicy( game, Player.Max ) );
					final JointPolicy<VoyagerState, VoyagerAction> joint_policy
						= new JointPolicy<VoyagerState, VoyagerAction>( policies );
					
					final Episode<VoyagerState, VoyagerAction> episode
						= new Episode<VoyagerState, VoyagerAction>( primitive_sim, joint_policy );
					if( vis != null ) {
						vis.attach( episode );
					}
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
	
	public static class IrrelevanceInstance
	{
		
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
		
		public EndScoreRecorder end_state = null;
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
			
			end_state = new EndScoreRecorder();
			timer = new ExecutionTimer<VoyagerState, VoyagerAction>( params_.Nplayers );
			
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
			final int variant = online;
			final int ntrajectories = 50; // TODO: Make this a parameter
			for( int i = 0; i < ntrajectories; ++i ) {
				System.out.println( "[Trajectory " + i + "]" );
				
				// TODO: Where should these quantities come from?
				final Irrelevance.IdentityRepresenter base_repr = new Irrelevance.IdentityRepresenter();
				final ArrayList<Attribute> attributes = Irrelevance.attributes();
				
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
									return AbstractionExperiments.computeInstanceWeight( s1, a1, s2, a2, label, fp_weight );
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
		final int Nplanets = Integer.parseInt( instance_args[idx++] );
		final int Nworlds = Integer.parseInt( instance_args[idx++] );
//		final int Nanytime = Integer.parseInt( instance_args[idx++] );
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
			.Nplanets( Nplanets ).master_seed( master_seed ).finish();
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
		ps.add( new VoyagerParameters.Builder().max_time( max_time ).Nplanets( Nplanets ).finish() );
		
		final List<AbstractionInstance> ws = new ArrayList<AbstractionInstance>( Nworlds );
		for( int i = 0; i < Nworlds; ++i ) {
			// FIXME: Why default_params and not ps.get( i ) ?
//			ws.add( createInstance( default_params, default_environment ) );
			ws.add( new AbstractionInstance( default_params, default_environment.rng.nextInt() ) );
		}
		
		final MultipleInstanceMultipleWorldGenerator<VoyagerParameters, AbstractionInstance>
			experimental_setups = new MultipleInstanceMultipleWorldGenerator<VoyagerParameters, AbstractionInstance>(
				default_environment, ps, ws );
		
		final Experiment<VoyagerParameters, AbstractionInstance> experiment
			= new VoyagerDomain( abstraction_args ); //new String[] { "1", "100", "16", "16" } );
		
		while( experimental_setups.hasNext() ) {
			final ExperimentalSetup<VoyagerParameters, AbstractionInstance> setup = experimental_setups.next();
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
