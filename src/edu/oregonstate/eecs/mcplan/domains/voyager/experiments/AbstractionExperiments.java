package edu.oregonstate.eecs.mcplan.domains.voyager.experiments;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.math3.random.MersenneTwister;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.AnytimePolicy;
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
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.SingleStepAdapter;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.domains.voyager.ControlMctsVisitor;
import edu.oregonstate.eecs.mcplan.domains.voyager.IdentityRepresenter;
import edu.oregonstate.eecs.mcplan.domains.voyager.Player;
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
import edu.oregonstate.eecs.mcplan.search.MaxMinStateNode;
import edu.oregonstate.eecs.mcplan.search.MctsVisitor;
import edu.oregonstate.eecs.mcplan.search.SearchPolicy;
import edu.oregonstate.eecs.mcplan.search.SparseSampleTree;
import edu.oregonstate.eecs.mcplan.search.StateNode;
import edu.oregonstate.eecs.mcplan.sim.Episode;
import edu.oregonstate.eecs.mcplan.sim.OptionSimulator;
import edu.oregonstate.eecs.mcplan.sim.SequentialJointSimulator;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.Tuple.Tuple2;

public class AbstractionExperiments
{
	/*
	private static class UctJointOptionPolicyFactory implements VoyagerPolicyFactory
	{
		private final double c_;
		private final int act_epoch_;
		private final int lookahead_epoch_;
		private final String rollout_options_;
		private final boolean contingent_;
		
		public UctJointOptionPolicyFactory( final String[] args )
		{
			c_ = Double.parseDouble( args[0] );
			act_epoch_ = Integer.parseInt( args[1] );
			lookahead_epoch_ = Integer.parseInt( args[2] );
			if( args.length > 3 ) {
				rollout_options_ = args[3];
			}
			else {
				rollout_options_ = "all";
			}
			if( args.length > 4 ) {
				// 'nc' for non-contingent
				contingent_ = !"nc".equals( args[4] );
			}
			else {
				contingent_ = true;
			}
		}
		
		@Override
		public Policy<VoyagerState, UndoableAction<VoyagerState>> create(
			final Environment env, final VoyagerParameters params, final VoyagerInstance instance, final Player player )
		{
			final JointPolicy.Builder<
				VoyagerState,
				Option<VoyagerState, UndoableAction<VoyagerState>>
			> default_policy_builder
				= new JointPolicy.Builder<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>>();
			final ActionGenerator<VoyagerState, ? extends Policy<VoyagerState, UndoableAction<VoyagerState>>> rollout_gen_min;
			final ActionGenerator<VoyagerState, ? extends Policy<VoyagerState, UndoableAction<VoyagerState>>> rollout_gen_max;
			if( "Balanced".equals( rollout_options_ ) ) {
				rollout_gen_min = new BalancedPolicyGenerator( params, instance, Player.Min );
				rollout_gen_max = new BalancedPolicyGenerator( params, instance, Player.Max );
			}
			else {
				rollout_gen_min = new VoyagerPolicyGenerator( params, instance, Player.Min );
				rollout_gen_max = new VoyagerPolicyGenerator( params, instance, Player.Max );
			}
			// TODO: Not sure how ordering is going to work here.
			default_policy_builder.pi( new RandomPolicy<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>>(
				Player.Min.id, instance.nextSeed(),
				new DurativeActionGenerator<VoyagerState, UndoableAction<VoyagerState>>(
					rollout_gen_min, lookahead_epoch_ ) ) );
			default_policy_builder.pi( new RandomPolicy<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>>(
				Player.Max.id, instance.nextSeed(),
				new DurativeActionGenerator<VoyagerState, UndoableAction<VoyagerState>>(
					rollout_gen_max, lookahead_epoch_ ) ) );
			final JointPolicy<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>> default_policy
				= default_policy_builder.finish();
			
			final MctsVisitor<VoyagerState, JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>> visitor
				= new ControlMctsVisitor<JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>>();
			
			final OptionSimulator<VoyagerState, UndoableAction<VoyagerState>> opt_sim
				= new OptionSimulator<VoyagerState, UndoableAction<VoyagerState>>(
					instance.simulator(), instance.nextSeed(), player.id );
			
			final UndoSimulator<VoyagerState, JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>> joint_sim
				= new SequentialJointSimulator<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>>( 2, opt_sim );
			
			final PrintStream log_stream;
			try {
				log_stream = new PrintStream( new File( env.root_directory, "tree.log" ) );
			}
			catch( final FileNotFoundException ex ) {
				throw new RuntimeException( ex );
			}
			final ArrayList<ActionGenerator<VoyagerState, ? extends Option<VoyagerState, UndoableAction<VoyagerState>>>> gen_list
				= new ArrayList<ActionGenerator<VoyagerState, ? extends Option<VoyagerState, UndoableAction<VoyagerState>>>>();
			gen_list.add( new DurativeActionGenerator<VoyagerState, UndoableAction<VoyagerState>>(
				new VoyagerPolicyGenerator( params, instance, Player.Min ), act_epoch_ ) );
			gen_list.add( new DurativeActionGenerator<VoyagerState, UndoableAction<VoyagerState>>(
				new VoyagerPolicyGenerator( params, instance, Player.Max ), act_epoch_ ) );
			final ProductActionGenerator<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>> pgen
				= new ProductActionGenerator<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>>( gen_list );
			
			final AnytimePolicy<VoyagerState, JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>> policy;
			if( contingent_ ) {
				final BackupRule<Representation<VoyagerState, IdentityRepresenter>,
							 	 JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>> backup
					= BackupRule.<Representation<VoyagerState, IdentityRepresenter>,
								  Option<VoyagerState, UndoableAction<VoyagerState>>>MaxMinQ();
				final GameTreeFactory<
					VoyagerState, IdentityRepresenter,
					JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>
				> factory
					= new UctSearch.Factory<VoyagerState, IdentityRepresenter,
											JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>>(
						joint_sim, new IdentityRepresenter( params.Nplanets * player.competitors, params.max_eta ),
						pgen, c_, new MersenneTwister( instance.nextSeed() ), default_policy, backup );
				policy = new SearchPolicy<VoyagerState, IdentityRepresenter,
										  JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>>(
					factory, visitor, log_stream )
					{
						@Override
						protected JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>> selectAction(
							final StateNode<Representation<VoyagerState, IdentityRepresenter>,
											JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>> root )
						{
							return BackupRules.MaxMinAction( root ).a;
						}
					};
			}
			else {
				final BackupRule<Representation<VoyagerState, NullRepresenter>,
							 	 JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>> backup
					= BackupRule.<Representation<VoyagerState, NullRepresenter>,
								  Option<VoyagerState, UndoableAction<VoyagerState>>>MaxMinQ();
				final GameTreeFactory<
					VoyagerState, NullRepresenter,
					JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>
				> factory
					= new UctSearch.Factory<VoyagerState, NullRepresenter,
											JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>>(
						joint_sim, new NullRepresenter(), pgen,
						c_, new MersenneTwister( instance.nextSeed() ), default_policy,	backup );
				policy = new SearchPolicy<VoyagerState, NullRepresenter,
										  JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>>(
					factory, visitor, log_stream )
					{
						@Override
						protected JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>> selectAction(
							final StateNode<Representation<VoyagerState, NullRepresenter>,
											JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>> root )
						{
							return BackupRules.MaxMinAction( root ).a;
						}
					};
			}
			
			final Policy<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>> fixed
				= new SingleStepAdapter<VoyagerState, UndoableAction<VoyagerState>>(
					new MarginalPolicy<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>>(
						new FixedEffortPolicy<VoyagerState, JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>>(
							policy, params.max_time[player.ordinal()] ),
						0 ) );
			
			return new OptionPolicy<VoyagerState, UndoableAction<VoyagerState>>( fixed, instance.nextSeed() );
		}
	}
	*/
	
	// -----------------------------------------------------------------------
	
	public static class ContextualPiStar<A extends VirtualConstructor<A>>
		extends GameTreeStateSimilarityDataset<VoyagerStateToken, JointAction<A>>
	{
		public final double false_positive_weight;
		
		public ContextualPiStar( final GameTree<VoyagerStateToken, JointAction<A>> tree,
								 final ArrayList<Attribute> attributes,
								 final int player, final double false_positive_weight )
		{
			super( tree, attributes, player, 1 /* min_samples to consider a state node */,
				   262144 /*512^2 instances*/, true /* Use context */ );
			System.out.println( "false_positive_weight = " + false_positive_weight );
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
			if( a1 != null && a2 != null && a1.a.get( player ).equals( a2.a.get( player ) ) ) {
				label = 1;
			}
			else {
				label = 0;
			}
			final double weight;
			if( label == 1 ) {
				weight = 1.0;
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
					final ActionNode<VoyagerStateToken, JointAction<A>> a1_prime = s1.getActionNode( a2.a );
					final ActionNode<VoyagerStateToken, JointAction<A>> a2_prime = s2.getActionNode( a1.a );
					if( a1_prime == null && a2_prime == null ) {
						System.out.println( "! a1_prime and a2_prime both null" );
					}
					final double d1 = (a1_prime == null ? 0 : Math.abs( a1_prime.q( 0 ) - a1.q( 0 ) ));
					final double d2 = (a2_prime == null ? 0 : Math.abs( a2_prime.q( 0 ) - a2.q( 0 ) ));
					// TODO: 1.0 is to prevent 0 weights; should be parameter?
					qdiff = Math.max( d1, d2 ) + 1.0;
				}
				System.out.println( "qdiff = " + qdiff );
				weight = false_positive_weight * qdiff;
				System.out.println( "0 weight = " + weight );
			}
			return Tuple2.of( label, weight );
		}
	}
	
	// -----------------------------------------------------------------------
	
	/**
	 * An AggregateState consists of a set of primitive states. It has
	 * *reference semantics*, which is different from typical usage for
	 * Representation types. We can get away with this because AggregateState
	 * instances are only created by the Aggregator class.
	 */
	public static class AggregateState extends Representation<VoyagerState> implements Iterable<Representation<VoyagerState>>
	{
		private final ArrayList<Representation<VoyagerState>> xs_
			= new ArrayList<Representation<VoyagerState>>();
		
//		private final HashCodeBuilder hash_builder_ = new HashCodeBuilder( 139, 149 );
		
		public void add( final Representation<VoyagerState> x )
		{
			xs_.add( x );
//			hash_builder_.append( x );
		}
		
		@Override
		public Representation<VoyagerState> copy()
		{
//			System.out.println( "AggregateState.copy()" );
			final AggregateState cp = new AggregateState();
			for( final Representation<VoyagerState> x : xs_ ) {
				cp.add( x );
			}
			return cp;
		}

		@Override
		public boolean equals( final Object obj )
		{
//			if( obj == null || !(obj instanceof AggregateState) ) {
//				return false;
//			}
//			final AggregateState that = (AggregateState) obj;
//			if( xs_.size() != that.xs_.size() ) {
//				return false;
//			}
//			for( int i = 0; i < xs_.size(); ++i ) {
//				if( !xs_.get( i ).equals( that.xs_.get( i ) ) ) {
//					return false;
//				}
//			}
//			return true;
			return this == obj;
		}

		@Override
		public int hashCode()
		{
//			return hash_builder_.toHashCode();
			return System.identityHashCode( this );
		}

		@Override
		public Iterator<Representation<VoyagerState>> iterator()
		{
			return xs_.iterator();
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static final class Aggregator<X extends FactoredRepresentation<VoyagerState>>
		implements Representer<VoyagerState, AggregateState>
	{
		private final Representer<VoyagerState, X> repr_;
		private final Classifier classifier_;
		
		private final ArrayList<AggregateState> clusters_ = new ArrayList<AggregateState>();
		private final ArrayList<X> exemplars_ = new ArrayList<X>();
		
		private final HashMap<X, AggregateState> cluster_map_ = new HashMap<X, AggregateState>();
		
		// Need an Instances object to add Instance objects to before
		// classification.
		private final Instances dataset_;
		
		public Aggregator( final Representer<VoyagerState, X> repr,
						   final ArrayList<Attribute> attributes,
						   final Classifier classifier )
		{
			repr_ = repr;
			classifier_ = classifier;
			dataset_ = new Instances( "runtime", attributes, 0 );
		}
		
		public AggregateState clusterState( final X x )
		{
			try {
				// TODO: How to do this step is a big design decision. We might
				// eventually like something formally justified, e.g. the
				// "Chinese restaurant process" approach.
				for( int i = 0; i < clusters_.size(); ++i ) {
					final X ex = exemplars_.get( i );
					final Instance instance = features( x, ex );
					dataset_.add( instance );
					final double label = classifier_.classifyInstance( instance );
					dataset_.remove( 0 );
					// TODO: Is there a more generic way to find the right label?
					if( 1.0 == label ) {
						final AggregateState c = clusters_.get( i );
						c.add( x );
						// TODO: Adjust exemplar element?
						return c;
					}
				}
				final AggregateState c = new AggregateState();
				c.add( x );
				clusters_.add( c );
				exemplars_.add( x );
				return c;
			}
			catch( final Exception ex ) {
				throw new RuntimeException( ex );
			}
		}
		
		private Instance features( final X xi, final X xj )
		{
			final double[] phi_i = xi.phi();
			final double[] phi_j = xj.phi();
			assert( phi_i.length == phi_j.length );
			// Feature vector is absolute difference of the two state
			// feature vectors.
			final double[] phi = new double[phi_i.length];
			for( int k = 0; k < phi.length; ++k ) {
				phi[k] = Math.abs( phi_i[k] - phi_j[k] );
			}
			return new DenseInstance( 1.0, phi );
		}
		
		@Override
		public AggregateState encode( final VoyagerState s )
		{
			final X x = repr_.encode( s );
			AggregateState c = cluster_map_.get( x );
			if( c == null ) {
				c = clusterState( x );
				cluster_map_.put( x, c );
			}
			return c;
		}
	}
	
	public static final class VoyagerAggregateSparseSampleTree<
			X extends FactoredRepresentation<VoyagerState>, A extends VirtualConstructor<A>>
		extends SparseSampleTree<VoyagerState, AggregateState, JointAction<A>>
	{
		private final Aggregator<X> aggregator_;
		
		public VoyagerAggregateSparseSampleTree(
						  final UndoSimulator<VoyagerState, JointAction<A>> sim,
						  final Aggregator<X> repr,
						  final ActionGenerator<VoyagerState, ? extends JointAction<A>> actions,
						  final int width, final int depth,
						  final Policy<VoyagerState, JointAction<A>> rollout_policy,
						  final int rollout_width, final int rollout_depth,
						  final MctsVisitor<VoyagerState, JointAction<A>> visitor )
		{
			super( sim, repr, actions, width, depth, rollout_policy, rollout_width, rollout_depth, visitor );
			aggregator_ = repr;
		}
		
		@Override
		protected StateNode<AggregateState, JointAction<A>> createStateNode(
			final ActionNode<AggregateState, JointAction<A>> an, final AggregateState x,
			final int nagents, final int turn )
		{
			return new MaxMinStateNode<AggregateState, A>( x, nagents, turn );
		}
		
		@Override
		protected StateNode<AggregateState, JointAction<A>> fetchStateNode(
			final ActionNode<AggregateState, JointAction<A>> an, final AggregateState x,
			final int nagents, final int turn )
		{
			for( final StateNode<AggregateState, JointAction<A>> agg : Fn.in( an.successors() ) ) {
				if( agg.token.equals( x ) ) {
					return agg;
				}
			}
			return null;
		}
	}
	
	public static class AbstractionBuilder<A extends VirtualConstructor<A>>
		extends SearchPolicy<VoyagerState, VoyagerStateToken, JointAction<A>>
	{
		private final ArrayList<Attribute> attributes_;
		private final int player_;
		private final int min_samples_;
		private final int max_instances_;
		private final double false_positive_weight_;
		private final boolean use_action_context_;
		
		public AbstractionBuilder(
				final GameTreeFactory<VoyagerState, VoyagerStateToken, JointAction<A>> factory,
				final MctsVisitor<VoyagerState, JointAction<A>> visitor,
				final ArrayList<Attribute> attributes,
			    final int player, final int min_samples, final int max_instances,
			    final double false_positive_weight, final boolean use_action_context,
				final PrintStream log_stream )
		{
			super( factory, visitor, log_stream );
			attributes_ = attributes;
			player_ = player;
			min_samples_ = min_samples;
			max_instances_ = max_instances;
			false_positive_weight_ = false_positive_weight;
			use_action_context_ = use_action_context;
		}

		@Override
		protected JointAction<A> selectAction( final GameTree<VoyagerStateToken, JointAction<A>> tree )
		{
			// TODO: Building the dataset adds significant computation to
			// getAction(), although the 'control' value still applies only
			// to time spent constructing the tree.
			final GameTreeStateSimilarityDataset<VoyagerStateToken, JointAction<A>> dataset
				= new ContextualPiStar<A>( tree, attributes_, player_, false_positive_weight_ );
			dataset.run();
			// TODO: It should be possible to get the backup rule from StateNode
			return BackupRules.MaxMinAction( tree.root() ).a;
		}

		@Override
		public int hashCode()
		{
			return System.identityHashCode( this );
		}

		@Override
		public boolean equals( final Object that )
		{
			return this == that;
		}
		
	}
	
	// -----------------------------------------------------------------------
	
	public static Policy<VoyagerState, UndoableAction<VoyagerState>>
	createFixedPolicy( final VoyagerInstance instance, final Player player )
	{
		return new BalancedPolicy( player, instance.nextSeed(), 0.75, 1.25, 0.2 );
	}
	
	public static class FixedVoyagerPolicyGenerator
		extends ActionGenerator<VoyagerState, Policy<VoyagerState, UndoableAction<VoyagerState>>>
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
		public ActionGenerator<VoyagerState, Policy<VoyagerState, UndoableAction<VoyagerState>>> create()
		{
			return new FixedVoyagerPolicyGenerator( instance_, player_ );
		}

		@Override
		public void setState( final VoyagerState s, final long t, final int turn )
		{ done_ = false; }

		@Override
		public int size()
		{ return 1; }

		@Override
		public boolean hasNext()
		{ return !done_; }

		@Override
		public Policy<VoyagerState, UndoableAction<VoyagerState>> next()
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
			return new VoyagerInstance( params_, seed_ );
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
		
		public EndScoreRecorder end_state = null;
		public ExecutionTimer<VoyagerState, UndoableAction<VoyagerState>> timer = null;
		
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
			timer = new ExecutionTimer<VoyagerState, UndoableAction<VoyagerState>>( params_.Nplayers );
			
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
		{
			
		}
	
		@Override
		public void run()
		{
			final VoyagerInstance game = world_.nextWorld();
			
			final int ntrajectories = 1; // TODO: Make this a parameter
			
			for( int i = 0; i < ntrajectories; ++i ) {
				System.out.println( "[Trajectory " + i + "]" );
				
				// Set up sparse sample tree
				//
				final MctsVisitor<VoyagerState, JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>> visitor
					= new ControlMctsVisitor<JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>>();
				
				final UndoSimulator<VoyagerState, UndoableAction<VoyagerState>> primitive_sim = game.simulator();
				final UndoSimulator<VoyagerState, JointAction<UndoableAction<VoyagerState>>> joint_primitive_sim
					= new SequentialJointSimulator<VoyagerState, UndoableAction<VoyagerState>>( 2, primitive_sim );
				
				final OptionSimulator<VoyagerState, UndoableAction<VoyagerState>> opt_sim
					= new OptionSimulator<VoyagerState, UndoableAction<VoyagerState>>(
						primitive_sim, game.nextSeed(), player_ );
				final UndoSimulator<VoyagerState, JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>> joint_opt_sim
					= new SequentialJointSimulator<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>>( 2, opt_sim );
				
				final PrintStream log_stream;
				try {
					log_stream = new PrintStream( new File( env_.root_directory, "tree.log" ) );
				}
				catch( final FileNotFoundException ex ) {
					throw new RuntimeException( ex );
				}
				final ArrayList<ActionGenerator<VoyagerState, ? extends Option<VoyagerState, UndoableAction<VoyagerState>>>> gen_list
					= new ArrayList<ActionGenerator<VoyagerState, ? extends Option<VoyagerState, UndoableAction<VoyagerState>>>>();
		//			gen_list.add( new DurativeActionGenerator<VoyagerState, UndoableAction<VoyagerState>>(
		//				new VoyagerPolicyGenerator( params_, world_, Player.Min ), act_epoch_ ) );
		//			gen_list.add( new DurativeActionGenerator<VoyagerState, UndoableAction<VoyagerState>>(
		//				new VoyagerPolicyGenerator( params_, world_, Player.Max ), act_epoch_ ) );
				gen_list.add( new DurativeActionGenerator<VoyagerState, UndoableAction<VoyagerState>>(
					new BalancedPolicyGenerator( params_, game, Player.Min ), act_epoch_ ) );
		//			gen_list.add( new DurativeActionGenerator<VoyagerState, UndoableAction<VoyagerState>>(
		//				new BalancedPolicyGenerator( params_, world_, Player.Max ), act_epoch_ ) );
				gen_list.add( new DurativeActionGenerator<VoyagerState, UndoableAction<VoyagerState>>(
					new FixedVoyagerPolicyGenerator( game, Player.Max ), act_epoch_ ) );
				final ProductActionGenerator<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>> pgen
					= new ProductActionGenerator<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>>( gen_list );
				
		//			final Policy<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>> pi_rand
		//				= new
		//			final ArrayList<ActionGenerator<VoyagerState, ? extends Option<VoyagerState, UndoableAction<VoyagerState>>>> rand_gen_list
		//				= new ArrayList<ActionGenerator<VoyagerState, ? extends Option<VoyagerState, UndoableAction<VoyagerState>>>>();
		//			rand_gen_list.add( DurativeActionGenerator.create(
		//						new ConstantActionGenerator<VoyagerState,
		//							? extends VirtualConstructor<Policy<VoyagerState, UndoableAction<VoyagerState>>>>(
		//							Arrays.asList( a ) ),
		//						act_epoch_ ) );
		//			final ActionGenerator<VoyagerState, JointAction<? extends Option<VoyagerState, UndoableAction<VoyagerState>>>> random_gen
		//				= ProductActionGenerator.create( Arrays.asList(
		//						new ActionGenerator<VoyagerState, ? extends Option<VoyagerState, UndoableAction<VoyagerState>>>[] {
		//					DurativeActionGenerator.create(
		//						new ConstantActionGenerator<VoyagerState, Policy<VoyagerState, UndoableAction<VoyagerState>>>(
		//							Arrays.asList( a ) ),
		//						act_epoch_ ),
		//					DurativeActionGenerator.create(
		//						new ConstantActionGenerator<VoyagerState, Policy<VoyagerState, UndoableAction<VoyagerState>>>(
		//							Arrays.asList( a ) ),
		//						act_epoch_ ) } ) );
				
				final int Nplanets = params_.Nplanets * Player.competitors;
				final int max_eta = params_.max_eta;
				final ArrayList<Attribute> attributes = VoyagerStateToken.attributes( Nplanets, max_eta );
				final IdentityRepresenter repr = new IdentityRepresenter( Nplanets, max_eta );
				
//				final Classifier state_classifier = new Classifier() {
//					@Override
//					public void buildClassifier( final Instances data ) throws Exception
//					{ }
//
//					@Override
//					public double classifyInstance( final Instance instance )
//							throws Exception
//					{ return 1.0; }
//
//					@Override
//					public double[] distributionForInstance( final Instance instance )
//							throws Exception
//					{ throw new Exception(); }
//
//					@Override
//					public Capabilities getCapabilities()
//					{ return null; }
//				};
//				final Aggregator<VoyagerStateToken> aggregator
//					= new Aggregator<VoyagerStateToken>( repr, attributes, state_classifier );
				final Policy<VoyagerState, JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>>
					rollout_policy = new RandomPolicy<VoyagerState, JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>>(
						0 /*Player*/, game.nextSeed(), pgen.create() );
				
				final BackupRule<VoyagerStateToken,
							 	 JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>> backup
					= BackupRule.<VoyagerStateToken,
								  Option<VoyagerState, UndoableAction<VoyagerState>>>MaxMinQ();
				final GameTreeFactory<
					VoyagerState, VoyagerStateToken,
					JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>
				> factory
					= new SparseSampleTree.Factory<VoyagerState, VoyagerStateToken,
												   JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>>(
						joint_opt_sim, repr, pgen, width_, depth_,
						rollout_policy, rollout_width_, rollout_depth_, backup );
				final int min_samples = 1;
				final int max_instances = 256;
				final AnytimePolicy<VoyagerState, JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>>
					abstraction_builder	= new AbstractionBuilder<Option<VoyagerState, UndoableAction<VoyagerState>>>(
						factory, visitor, attributes, Player.Min.ordinal(),
						min_samples, max_instances, false_positive_weight_, false, System.out );
				final Policy<VoyagerState, UndoableAction<VoyagerState>> abstraction_executor
					= new OptionPolicy<VoyagerState, UndoableAction<VoyagerState>>(
						new SingleStepAdapter<VoyagerState, UndoableAction<VoyagerState>>(
							new MarginalPolicy<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>>(
								new FixedEffortPolicy<VoyagerState, JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>>(
									abstraction_builder, params_.max_time[player_] ),
								0 ) ) );
				final ArrayList<Policy<VoyagerState, UndoableAction<VoyagerState>>> policies
					= new ArrayList<Policy<VoyagerState, UndoableAction<VoyagerState>>>();
				policies.add( abstraction_executor );
				policies.add( createFixedPolicy( game, Player.Max ) );
				final JointPolicy<VoyagerState, UndoableAction<VoyagerState>> joint_policy
					= new JointPolicy<VoyagerState, UndoableAction<VoyagerState>>( policies );
				
				final Episode<VoyagerState, JointAction<UndoableAction<VoyagerState>>> episode
					= new Episode<VoyagerState, JointAction<UndoableAction<VoyagerState>>>(
						joint_primitive_sim, joint_policy );
				if( true || params_.use_monitor ) {
					final int wait = 0;
					final VoyagerVisualization<JointAction<UndoableAction<VoyagerState>>>
						vis = new VoyagerVisualization<JointAction<UndoableAction<VoyagerState>>>(
							params_, new Dimension( 720, 720 ), wait );
					vis.attach( episode );
				}
				episode.run();
				
		//			final GameTree<VoyagerStateToken, JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>>
		//				ss_tree = factory.create( visitor );
				
//				final GameTree<AggregateState, JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>>
//					ss_tree = new VoyagerAggregateSparseSampleTree<
//						VoyagerStateToken, Option<VoyagerState, UndoableAction<VoyagerState>>
//					>( joint_opt_sim, aggregator, pgen, width_, depth_,
//					   rollout_policy, rollout_width_, rollout_depth_, visitor );
//				ss_tree.run();
				
//				System.out.println( "***** ss_tree built" );
//				ss_tree.root().accept( TreePrinter.create( ss_tree ) );
				
				/*
				final GameTreeStateSimilarityDataset<VoyagerStateToken, JointAction<Option<VoyagerState, UndoableAction<VoyagerState>>>>
					dataset = new ContextualPiStar<Option<VoyagerState, UndoableAction<VoyagerState>>>(
						ss_tree, attributes,
						Player.Min.ordinal(), false_positive_weight_ );
				dataset.run();
				
				// TODO: Not using context for debugging
				final Instances instances = dataset.getInstances( null );
				final Classifier classifier = createClassifier();
				System.out.println( "*** Building classifier" );
				try {
					classifier.buildClassifier( instances );
				}
				catch( final Exception ex ) {
					System.out.println( "! Error in buildClassifier():" );
					ex.printStackTrace();
					System.exit( -1 );
				}
				System.out.println( classifier );
				*/
			}
		}
		
		final Classifier createClassifier()
		{
			final J48 classifier = new J48();
			
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
