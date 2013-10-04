/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager.experiments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.random.MersenneTwister;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.DurativeActionGenerator;
import edu.oregonstate.eecs.mcplan.FixedEffortPolicy;
import edu.oregonstate.eecs.mcplan.GroundedPolicy;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.JointPolicy;
import edu.oregonstate.eecs.mcplan.MarginalPolicy;
import edu.oregonstate.eecs.mcplan.Option;
import edu.oregonstate.eecs.mcplan.OptionPolicy;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.PolicyActionGenerator;
import edu.oregonstate.eecs.mcplan.ProductActionGenerator;
import edu.oregonstate.eecs.mcplan.RandomPolicy;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.SingleStepAdapter;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.ControlMctsVisitor;
import edu.oregonstate.eecs.mcplan.domains.voyager.IdentityRepresenter;
import edu.oregonstate.eecs.mcplan.domains.voyager.NullRepresenter;
import edu.oregonstate.eecs.mcplan.domains.voyager.Player;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerActionGenerator;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerInstance;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerParameters;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerState;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerStateToken;
import edu.oregonstate.eecs.mcplan.domains.voyager.policies.BalancedPolicy;
import edu.oregonstate.eecs.mcplan.domains.voyager.policies.VoyagerPolicyFactory;
import edu.oregonstate.eecs.mcplan.experiments.Environment;
import edu.oregonstate.eecs.mcplan.experiments.Experiment;
import edu.oregonstate.eecs.mcplan.experiments.ExperimentalSetup;
import edu.oregonstate.eecs.mcplan.experiments.MultipleInstanceMultipleWorldGenerator;
import edu.oregonstate.eecs.mcplan.search.BackupRule;
import edu.oregonstate.eecs.mcplan.search.BackupRules;
import edu.oregonstate.eecs.mcplan.search.DiscountedRefinementPolicy;
import edu.oregonstate.eecs.mcplan.search.GameTreeFactory;
import edu.oregonstate.eecs.mcplan.search.IterativeDeepeningPolicy;
import edu.oregonstate.eecs.mcplan.search.IterativeRefinementPolicy;
import edu.oregonstate.eecs.mcplan.search.MctsNegamaxVisitor;
import edu.oregonstate.eecs.mcplan.search.MctsVisitor;
import edu.oregonstate.eecs.mcplan.search.NegamaxVisitor;
import edu.oregonstate.eecs.mcplan.search.RolloutPolicy;
import edu.oregonstate.eecs.mcplan.search.SearchPolicy;
import edu.oregonstate.eecs.mcplan.search.StateNode;
import edu.oregonstate.eecs.mcplan.search.UctPolicy;
import edu.oregonstate.eecs.mcplan.search.UctSearch;
import edu.oregonstate.eecs.mcplan.sim.OptionSimulator;
import edu.oregonstate.eecs.mcplan.sim.SequentialJointSimulator;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;

/**
 * @author jhostetler
 *
 */
public class VoyagerExperiments
{
	public static final long NO_LEAF_LOOKAHEAD = 0;
	public static final double NO_LEAF_LOOKAHEAD_PERCENT = 0.0;
	
	public static final double win_margin = 0.2;
	public static final int garrison = 1;
	
	// -----------------------------------------------------------------------

	private static class IterativeDeepeningFactory implements VoyagerPolicyFactory
	{
		private final double leaf_lookahead_percent_;
		
		public IterativeDeepeningFactory( final String[] args )
		{
			leaf_lookahead_percent_ = Double.parseDouble( args[0] );
			assert( 0.0 <= leaf_lookahead_percent_ );
			assert( leaf_lookahead_percent_ <= 1.0 );
		}
		
		@Override
		public AnytimePolicy<VoyagerState, UndoableAction<VoyagerState>> create(
			final Environment env, final VoyagerParameters params, final VoyagerInstance instance, final Player player )
		{
			final NegamaxVisitor<VoyagerState, UndoableAction<VoyagerState>> visitor
				= new NullHeuristicNegamaxVisitor<UndoableAction<VoyagerState>>( System.out, player );
	
			final AnytimePolicy<VoyagerState, UndoableAction<VoyagerState>> pi
				= new IterativeDeepeningPolicy<VoyagerState, UndoableAction<VoyagerState>>(
					params.max_depth, instance.simulator(),
					PolicyActionGenerator.create( new VoyagerPolicyGenerator( params, instance ) ), visitor,
					GroundedPolicy.create( RandomPolicy.create(
						player.id, instance.nextSeed(), new VoyagerPolicyGenerator( params, instance ) ) ) );
			
			return pi;
		}
	}
	
	private static class IterativeRefinementFactory implements VoyagerPolicyFactory
	{
		@Override
		public AnytimePolicy<VoyagerState, UndoableAction<VoyagerState>> create(
			final Environment env, final VoyagerParameters params, final VoyagerInstance instance, final Player player )
		{
			final NegamaxVisitor<VoyagerState, UndoableAction<VoyagerState>> visitor
				= new NullHeuristicNegamaxVisitor<UndoableAction<VoyagerState>>( System.out, player );
			
			final AnytimePolicy<VoyagerState, UndoableAction<VoyagerState>> pi
				= new IterativeRefinementPolicy<VoyagerState, UndoableAction<VoyagerState>>(
					params.max_depth, params.policy_horizon, instance.simulator(),
					new VoyagerPolicyGenerator( params, instance ), visitor,
					GroundedPolicy.create( RandomPolicy.create(
						player.id, instance.nextSeed(), new VoyagerPolicyGenerator( params, instance ) ) ) );
			return pi;
		}
	}
	
	private static class DiscountedRefinementFactory implements VoyagerPolicyFactory
	{
		private final double discount_;
		
		public DiscountedRefinementFactory( final String[] args )
		{
			discount_ = Double.parseDouble( args[0] );
			assert( 0.0 <= discount_ );
			assert( discount_ <= 1.0 );
		}
		
		@Override
		public AnytimePolicy<VoyagerState> create(
			final Environment env, final VoyagerParameters params, final VoyagerInstance instance, final Player player )
		{
			final NegamaxVisitor<VoyagerState, UndoableAction<VoyagerState>> visitor
				= new NullHeuristicNegamaxVisitor<UndoableAction<VoyagerState>>( System.out, player );
			
			final AnytimePolicy<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>> pi
				= new DiscountedRefinementPolicy<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>>(
					params.max_depth, params.policy_horizon, discount_, instance.simulator(),
					new DurativeActionGenerator<VoyagerState, UndoableAction<VoyagerState>>(
						new VoyagerPolicyGenerator( params, instance ), act_epoch_ ), visitor,
					GroundedPolicy.create( RandomPolicy.create(
						player.id, instance.nextSeed(), new VoyagerPolicyGenerator( params, instance ) ) ) );
			return pi;
			
			final ArrayList<Policy<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>>> default_policies
				= new ArrayList<Policy<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>>>();
			// TODO: Not sure how ordering is going to work here.
			default_policies.add( new RandomPolicy<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>>(
				Player.Min.id, instance.nextSeed(),
				new DurativeActionGenerator<VoyagerState, UndoableAction<VoyagerState>>(
					new VoyagerPolicyGenerator( params, instance ), lookahead_epoch_ ) ) );
			default_policies.add( new RandomPolicy<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>>(
				Player.Max.id, instance.nextSeed(),
				new DurativeActionGenerator<VoyagerState, UndoableAction<VoyagerState>>(
					new VoyagerPolicyGenerator( params, instance ), lookahead_epoch_ ) ) );
			
			final MctsNegamaxVisitor<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>> visitor
				= new ControlMctsVisitor<Option<VoyagerState, UndoableAction<VoyagerState>>>( player );
			
			final OptionSimulator<VoyagerState, UndoableAction<VoyagerState>> opt_sim
				= new OptionSimulator<VoyagerState, UndoableAction<VoyagerState>>(
					instance.simulator(), instance.nextSeed(), player.id );
			
			final UctPolicy<VoyagerState, VoyagerStateToken, Option<VoyagerState, UndoableAction<VoyagerState>>> policy
				= new UctPolicy<VoyagerState, VoyagerStateToken, Option<VoyagerState, UndoableAction<VoyagerState>>>(
					opt_sim,
					new DurativeActionGenerator<VoyagerState, UndoableAction<VoyagerState>>(
						new VoyagerPolicyGenerator( params, instance ), act_epoch_ ),
					default_policies, c_, instance.nextSeed(), visitor );
			
			final Policy<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>> fixed
				= new FixedEffortPolicy<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>>(
					policy, params.max_time[player.ordinal()] );
			
			return new OptionPolicy<VoyagerState, UndoableAction<VoyagerState>>( fixed, instance.nextSeed() );
		}
	}
	
	private static class UctPrimitiveFactory implements VoyagerPolicyFactory
	{
		private final double c_;
		
		public UctPrimitiveFactory( final String[] args )
		{
			c_ = Double.parseDouble( args[0] );
		}
		
		@Override
		public Policy<VoyagerState, UndoableAction<VoyagerState>> create(
			final Environment env, final VoyagerParameters params, final VoyagerInstance instance, final Player player )
		{
			final ArrayList<Policy<VoyagerState, UndoableAction<VoyagerState>>> default_policies
				= new ArrayList<Policy<VoyagerState, UndoableAction<VoyagerState>>>();
			// TODO: Not sure how ordering is going to work here.
			default_policies.add( new RandomPolicy<VoyagerState, UndoableAction<VoyagerState>>(
				Player.Min.id, instance.nextSeed(),	new VoyagerActionGenerator() ) );
			default_policies.add( new RandomPolicy<VoyagerState, UndoableAction<VoyagerState>>(
				Player.Max.id, instance.nextSeed(), new VoyagerActionGenerator() ) );
//			default_policies.add( new BalancedPolicy( Player.Min, instance.nextSeed(), 0.8, 2.0, 0.1 ) );
//			default_policies.add( new BalancedPolicy( Player.Max, instance.nextSeed(), 0.8, 2.0, 0.1 ) );
//			default_policies.add( new RandomPolicy<VoyagerState, UndoableAction<VoyagerState>>(
//				Player.Min.id, instance.nextSeed(),
//				PolicyActionGenerator.create( new VoyagerPolicyGenerator( params, instance ) ) ) );
//			default_policies.add( new RandomPolicy<VoyagerState, UndoableAction<VoyagerState>>(
//				Player.Max.id, instance.nextSeed(),
//				PolicyActionGenerator.create( new VoyagerPolicyGenerator( params, instance ) ) ) );
			
			final MctsNegamaxVisitor<VoyagerState, UndoableAction<VoyagerState>> visitor
				= new ControlMctsVisitor<UndoableAction<VoyagerState>>( player );
			
			final UctPolicy<VoyagerState, VoyagerStateToken, UndoableAction<VoyagerState>> policy
				= new UctPolicy<VoyagerState, VoyagerStateToken, UndoableAction<VoyagerState>>(
					instance.simulator(), new VoyagerActionGenerator(),
					default_policies, c_, instance.nextSeed(), visitor );
			
			final Policy<VoyagerState, UndoableAction<VoyagerState>> fixed
				= new FixedEffortPolicy<VoyagerState, UndoableAction<VoyagerState>>(
					policy, params.max_time[player.ordinal()] );
			
			return fixed;
		}
	}
	
	private static class UctFactory implements VoyagerPolicyFactory
	{
		private final double c_;
		
		public UctFactory( final String[] args )
		{
			c_ = Double.parseDouble( args[0] );
		}
		
		@Override
		public Policy<VoyagerState, UndoableAction<VoyagerState>> create(
			final Environment env, final VoyagerParameters params, final VoyagerInstance instance, final Player player )
		{
			final ArrayList<Policy<VoyagerState, UndoableAction<VoyagerState>>> default_policies
				= new ArrayList<Policy<VoyagerState, UndoableAction<VoyagerState>>>();
			// TODO: Not sure how ordering is going to work here.
			default_policies.add( new RandomPolicy<VoyagerState, UndoableAction<VoyagerState>>(
				Player.Min.id, instance.nextSeed(),
				PolicyActionGenerator.create( new BalancedPolicyGenerator( params, instance ) ) ) );
			default_policies.add( new RandomPolicy<VoyagerState, UndoableAction<VoyagerState>>(
				Player.Max.id, instance.nextSeed(),
				PolicyActionGenerator.create( new BalancedPolicyGenerator( params, instance ) ) ) );
//			default_policies.add( new BalancedPolicy( Player.Min, instance.nextSeed(), 0.8, 2.0, 0.1 ) );
//			default_policies.add( new BalancedPolicy( Player.Max, instance.nextSeed(), 0.8, 2.0, 0.1 ) );
//			default_policies.add( new RandomPolicy<VoyagerState, UndoableAction<VoyagerState>>(
//				Player.Min.id, instance.nextSeed(),
//				PolicyActionGenerator.create( new VoyagerPolicyGenerator( params, instance ) ) ) );
//			default_policies.add( new RandomPolicy<VoyagerState, UndoableAction<VoyagerState>>(
//				Player.Max.id, instance.nextSeed(),
//				PolicyActionGenerator.create( new VoyagerPolicyGenerator( params, instance ) ) ) );
			
			final MctsNegamaxVisitor<VoyagerState, UndoableAction<VoyagerState>> visitor
				= new ControlMctsVisitor<UndoableAction<VoyagerState>>( player );
			
			final UctPolicy<VoyagerState, VoyagerStateToken, UndoableAction<VoyagerState>> policy
				= new UctPolicy<VoyagerState, VoyagerStateToken, UndoableAction<VoyagerState>>(
					instance.simulator(), PolicyActionGenerator.create( new VoyagerPolicyGenerator( params, instance ) ),
					default_policies, c_, instance.nextSeed(), visitor );
			
			return policy;
		}
	}
	
	private static class UctOptionFactory implements VoyagerPolicyFactory
	{
		private final double c_;
		private final int act_epoch_;
		private final int lookahead_epoch_;
		private final String rollout_options_;
		private final boolean contingent_;
		
		public UctOptionFactory( final String[] args )
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
			final ArrayList<Policy<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>>> default_policies
				= new ArrayList<Policy<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>>>();
			final ActionGenerator<VoyagerState, ? extends Policy<VoyagerState, UndoableAction<VoyagerState>>> rollout_gen;
			if( "Balanced".equals( rollout_options_ ) ) {
				rollout_gen = new BalancedPolicyGenerator( params, instance );
			}
			else {
				rollout_gen = new VoyagerPolicyGenerator( params, instance );
			}
			// TODO: Not sure how ordering is going to work here.
			default_policies.add( new RandomPolicy<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>>(
				Player.Min.id, instance.nextSeed(),
				new DurativeActionGenerator<VoyagerState, UndoableAction<VoyagerState>>(
					rollout_gen.create(), lookahead_epoch_ ) ) );
			default_policies.add( new RandomPolicy<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>>(
				Player.Max.id, instance.nextSeed(),
				new DurativeActionGenerator<VoyagerState, UndoableAction<VoyagerState>>(
					rollout_gen.create(), lookahead_epoch_ ) ) );
			
			final MctsNegamaxVisitor<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>> visitor
				= new ControlMctsVisitor<Option<VoyagerState, UndoableAction<VoyagerState>>>( player );
			
			final OptionSimulator<VoyagerState, UndoableAction<VoyagerState>> opt_sim
				= new OptionSimulator<VoyagerState, UndoableAction<VoyagerState>>(
					instance.simulator(), instance.nextSeed(), player.id );
			
			final PrintStream log_stream;
			try {
				log_stream = new PrintStream( new File( env.root_directory, "tree.log" ) );
			}
			catch( final FileNotFoundException ex ) {
				throw new RuntimeException( ex );
			}
			final AnytimePolicy<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>> policy;
			if( contingent_ ) {
				policy = new UctPolicy<VoyagerState, IdentityRepresenter, Option<VoyagerState, UndoableAction<VoyagerState>>>(
					opt_sim, new IdentityRepresenter(),
					new DurativeActionGenerator<VoyagerState, UndoableAction<VoyagerState>>(
						new VoyagerPolicyGenerator( params, instance ), act_epoch_ ),
					default_policies, c_, instance.nextSeed(), visitor, log_stream );
			}
			else {
				policy = new UctPolicy<VoyagerState, NullRepresenter, Option<VoyagerState, UndoableAction<VoyagerState>>>(
					opt_sim, new NullRepresenter(),
					new DurativeActionGenerator<VoyagerState, UndoableAction<VoyagerState>>(
						new VoyagerPolicyGenerator( params, instance ), act_epoch_ ),
					default_policies, c_, instance.nextSeed(), visitor, log_stream );
			}
			
			final Policy<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>> fixed
				= new SingleStepAdapter<VoyagerState, UndoableAction<VoyagerState>>(
					new FixedEffortPolicy<VoyagerState, Option<VoyagerState, UndoableAction<VoyagerState>>>(
						policy, params.max_time[player.ordinal()] ) );
			
			return new OptionPolicy<VoyagerState, UndoableAction<VoyagerState>>( fixed, instance.nextSeed() );
		}
	}
	
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
						player.ordinal() ) );
			
			return new OptionPolicy<VoyagerState, UndoableAction<VoyagerState>>( fixed, instance.nextSeed() );
		}
	}
	
	// -----------------------------------------------------------------------
	
	private static class RolloutFactory implements VoyagerPolicyFactory
	{
		private final double c_;
		
		public RolloutFactory( final String[] args )
		{
			c_ = Double.parseDouble( args[0] );
		}
		
		@Override
		public Policy<VoyagerState, UndoableAction<VoyagerState>> create(
			final Environment env, final VoyagerParameters params, final VoyagerInstance instance, final Player player )
		{
			final List<Policy<VoyagerState, UndoableAction<VoyagerState>>> rollout_policies
				= new ArrayList<Policy<VoyagerState, UndoableAction<VoyagerState>>>();
			// TODO: Not sure how ordering is going to work here.
//			rollout_policies.add( new BalancedPolicy( Player.Min, instance.nextSeed(), 0.8, 2.0, 0.1 ) );
//			rollout_policies.add( new BalancedPolicy( Player.Max, instance.nextSeed(), 0.8, 2.0, 0.1 ) );
			rollout_policies.add( new RandomPolicy<VoyagerState, UndoableAction<VoyagerState>>(
				Player.Min.id, instance.nextSeed(),
				PolicyActionGenerator.create( new VoyagerPolicyGenerator( params, instance ) ) ) );
			rollout_policies.add( new RandomPolicy<VoyagerState, UndoableAction<VoyagerState>>(
				Player.Max.id, instance.nextSeed(),
				PolicyActionGenerator.create( new VoyagerPolicyGenerator( params, instance ) ) ) );
			
			final MctsNegamaxVisitor<VoyagerState, UndoableAction<VoyagerState>> visitor
				= new ControlMctsVisitor<UndoableAction<VoyagerState>>( player );
			
			final RolloutPolicy<VoyagerState, VoyagerStateToken, UndoableAction<VoyagerState>> policy
				= new RolloutPolicy<VoyagerState, VoyagerStateToken, UndoableAction<VoyagerState>>(
					instance.simulator(), PolicyActionGenerator.create( new VoyagerPolicyGenerator( params, instance ) ),
					c_, rollout_policies, visitor );
			
			final Policy<VoyagerState, UndoableAction<VoyagerState>> fixed
				= new FixedEffortPolicy<VoyagerState, UndoableAction<VoyagerState>>(
					policy, params.max_time[player.ordinal()] );
			
			return fixed;
		}
	}
	
	private static class RandomFactory implements VoyagerPolicyFactory
	{
		@Override
		public Policy<VoyagerState, UndoableAction<VoyagerState>> create(
				final Environment env, final VoyagerParameters params, final VoyagerInstance instance, final Player player )
		{
			return new RandomPolicy<VoyagerState, UndoableAction<VoyagerState>>(
				player.id, instance.nextSeed(),
				PolicyActionGenerator.create( new VoyagerPolicyGenerator( params, instance ) ) );
		}
	}
	
	private static class FixedFactory implements VoyagerPolicyFactory
	{
		private final String[] pi_;
		public FixedFactory( final String[] pi )
		{
			pi_ = pi;
		}
		
		@Override
		public Policy<VoyagerState, UndoableAction<VoyagerState>> create(
				final Environment env, final VoyagerParameters params, final VoyagerInstance instance, final Player player )
		{
			// TODO: Other policies
			return new BalancedPolicy( player, instance.nextSeed(), pi_ );
		}
	}
	
	private static VoyagerPolicyFactory createPolicy( final String[] args )
	{
		final String policy_name = args[0];
		final String[] policy_args = Arrays.copyOfRange( args, 1, args.length );
		if( "FixedPolicy".equals( policy_name ) ) {
			return new FixedFactory( policy_args );
		}
		else if( "RandomPolicy".equals( policy_name ) ) {
			return new RandomFactory();
		}
		else if( "IterativeDeepening".equals( policy_name ) ) {
			return new IterativeDeepeningFactory( policy_args );
		}
		else if( "IterativeRefinement".equals( policy_name ) ) {
			return new IterativeRefinementFactory();
		}
		else if( "DiscountedRefinement".equals( policy_name ) ) {
			return new DiscountedRefinementFactory( policy_args );
		}
		else if( "RolloutPolicy".equals( policy_name ) ) {
			return new RolloutFactory( policy_args );
		}
		else if( "UctPrimitive".equals( policy_name ) ) {
			return new UctPrimitiveFactory( policy_args );
		}
		else if( "UctPolicy".equals( policy_name ) ) {
			return new UctFactory( policy_args );
		}
		else if( "UctOptionPolicy".equals( policy_name ) ) {
			return new UctOptionFactory( policy_args );
		}
		else if( "UctJointOptionPolicy".equals( policy_name ) ) {
			return new UctJointOptionPolicyFactory( policy_args );
		}
		else {
			throw new IllegalArgumentException();
		}
	}
	
	public static File createDirectory( final String[] args )
	{
		final File r = new File( args[0] );
		r.mkdir();
		final File d = new File( r, "x" + args[1] + "_" + args[2] + "_" + args[3] );
		d.mkdir();
		return d;
	}
	
	private static VoyagerInstance createInstance( final VoyagerParameters params, final Environment env )
	{
		return new VoyagerInstance( params, env.rng.nextLong() );
	}
	
	/**
	 * @param args
	 */
	public static void main( final String[] args )
	{
		System.out.println( args.toString() );
		final String batch_name = args[0];
		final String[] instance_args = args[1].split( "," );
		final String[] pi_args = args[2].split( "," );
		final String[] phi_args = args[3].split( "," );
		
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
		
		final List<VoyagerInstance> ws = new ArrayList<VoyagerInstance>( Nworlds );
		for( int i = 0; i < Nworlds; ++i ) {
			// FIXME: Why default_params and not ps.get( i ) ?
			ws.add( createInstance( default_params, default_environment ) );
		}
		
		final MultipleInstanceMultipleWorldGenerator<VoyagerParameters, VoyagerInstance>
			experimental_setups = new MultipleInstanceMultipleWorldGenerator<VoyagerParameters, VoyagerInstance>(
				default_environment, ps, ws );
		
		final Experiment<VoyagerParameters, VoyagerInstance> experiment
			= new VoyagerPolicyComparison<UndoableAction<VoyagerState>>(
				createPolicy( pi_args ), createPolicy( phi_args ) );
		
		while( experimental_setups.hasNext() ) {
			final ExperimentalSetup<VoyagerParameters, VoyagerInstance> setup = experimental_setups.next();
			experiment.setup( setup.environment, setup.parameters, setup.world );
			experiment.run();
			experiment.finish();
		}
		
		System.exit( 0 );
	}

}
