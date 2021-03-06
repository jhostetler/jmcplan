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
package edu.oregonstate.eecs.mcplan.domains.cosmic.experiments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import ch.qos.logback.classic.Level;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;

import edu.oregonstate.eecs.mcplan.ActionSpace;
import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.BudgetPolicy;
import edu.oregonstate.eecs.mcplan.ConsPolicy;
import edu.oregonstate.eecs.mcplan.LoggerManager;
import edu.oregonstate.eecs.mcplan.OverridePolicy;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.ReducedFrequencyPolicy;
import edu.oregonstate.eecs.mcplan.SequencePolicy;
import edu.oregonstate.eecs.mcplan.bandit.CyclicFiniteBandit;
import edu.oregonstate.eecs.mcplan.bandit.EpsilonGreedyBandit;
import edu.oregonstate.eecs.mcplan.bandit.FiniteBandit;
import edu.oregonstate.eecs.mcplan.bandit.UcbBandit;
import edu.oregonstate.eecs.mcplan.bandit.UcbSqrtBandit;
import edu.oregonstate.eecs.mcplan.domains.cosmic.Bus;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicAction;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicGson;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicMatlabInterface;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicNothingAction;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicOptions;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicParameters;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicState;
import edu.oregonstate.eecs.mcplan.domains.cosmic.CosmicTransitionSimulator;
import edu.oregonstate.eecs.mcplan.domains.cosmic.IslandActionSpace;
import edu.oregonstate.eecs.mcplan.domains.cosmic.LoadShedActionSpace;
import edu.oregonstate.eecs.mcplan.domains.cosmic.Machine;
import edu.oregonstate.eecs.mcplan.domains.cosmic.NothingActionSpace;
import edu.oregonstate.eecs.mcplan.domains.cosmic.ShedGlobalActionSpace;
import edu.oregonstate.eecs.mcplan.domains.cosmic.ShedZoneAction;
import edu.oregonstate.eecs.mcplan.domains.cosmic.ShedZoneActionSpace;
import edu.oregonstate.eecs.mcplan.domains.cosmic.Shunt;
import edu.oregonstate.eecs.mcplan.domains.cosmic.TripBranchSetAction;
import edu.oregonstate.eecs.mcplan.domains.cosmic.policy.FeatureFrequency;
import edu.oregonstate.eecs.mcplan.domains.cosmic.policy.FeatureVmag;
import edu.oregonstate.eecs.mcplan.domains.cosmic.policy.HystereticLoadShedding;
import edu.oregonstate.eecs.mcplan.domains.cosmic.policy.IsolatePolicy;
import edu.oregonstate.eecs.mcplan.domains.cosmic.policy.NothingPolicy;
import edu.oregonstate.eecs.mcplan.domains.cosmic.policy.PartitionZonesPolicy;
import edu.oregonstate.eecs.mcplan.domains.cosmic.policy.ShedGlobalPolicy;
import edu.oregonstate.eecs.mcplan.op.PolicyRollout;
import edu.oregonstate.eecs.mcplan.op.PolicySwitching;
import edu.oregonstate.eecs.mcplan.search.fsss.Budget;
import edu.oregonstate.eecs.mcplan.sim.ActionNode;
import edu.oregonstate.eecs.mcplan.sim.SimulationListener;
import edu.oregonstate.eecs.mcplan.sim.StateActionGraphVisitor;
import edu.oregonstate.eecs.mcplan.sim.StateNode;
import edu.oregonstate.eecs.mcplan.sim.TrajectoryBudget;
import edu.oregonstate.eecs.mcplan.sim.TrajectorySimulator;
import edu.oregonstate.eecs.mcplan.sim.TrajectoryTraversal;
import edu.oregonstate.eecs.mcplan.sim.TransitionBudget;
import edu.oregonstate.eecs.mcplan.util.Csv;
import edu.oregonstate.eecs.mcplan.util.CsvConfigurationParser;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.KeyValueStore;

/**
 * @author jhostetler
 *
 */
public class CosmicExperiments
{
	public static final class Configuration extends KeyValueStore
	{
		private final KeyValueStore config_;
		
		// FIXME: Why is 'root_directory' a String?
		public final String root_directory;
		
		public final File data_directory;
		public final String experiment_name;
		
		public final int Tstable;
		public final int Tepisode;
		public final int T;
		
		public final int[] branch_set;
		
		public final boolean cosmic_verbose;
		
		public final CosmicParameters.Version version;
		
		public enum HistoryLoggingMode
		{
			none, small, full
		}
		
		public final HistoryLoggingMode log_history;
		
		public Configuration( final String root_directory, final String experiment_name, final KeyValueStore config )
		{
			config_ = config;
			
			this.experiment_name = experiment_name;
			this.root_directory = root_directory;
			
			data_directory = new File( root_directory, experiment_name );
			data_directory.mkdirs();
			
			Tstable = config_.getInt( "Tstable" );
			Tepisode = config_.getInt( "Tepisode" );
			T = Tstable + Tepisode;
			
			final String[] faults = config_.get( "fault" ).split( ";" );
			if( faults.length == 1 && faults[0].isEmpty() ) {
				branch_set = new int[0];
			}
			else {
				branch_set = Fn.mapParseInt( faults );
			}
			
			cosmic_verbose = config_.getBoolean( "cosmic.verbose" );
			
			{
				final String s = config_.get( "cosmic.version" );
				switch( s ) {
				case "v1":
					version = CosmicParameters.Version.take_action;
					break;
				case "v1_iter":
					version = CosmicParameters.Version.take_action_iter;
					break;
				case "v2":
					version = CosmicParameters.Version.take_action2;
					break;
				default:
					throw new IllegalArgumentException( "cosmic.version" );
				}
			}
			
			log_history = HistoryLoggingMode.valueOf( get( "log.history" ) );
		}

		@Override
		public String get( final String key )
		{ return config_.get( key ); }

		@Override
		public Iterable<String> keys()
		{ return config_.keys(); }
		
		private AnytimePolicy<CosmicState, CosmicAction> createPolicyRollout(
				final RandomGenerator rng, final CosmicParameters params, final CosmicTransitionSimulator sim )
		{
			final ActionSpace<CosmicState, CosmicAction> as = getActionSpace();
			final FiniteBandit<Policy<CosmicState, CosmicAction>> bandit = createBandit();
			final ArrayList<Policy<CosmicState, CosmicAction>> Pi = getPolicySet( rng, params );
			final int depth_limit = getInt( "pr.depth" );
			return new PolicyRollout<>( rng, sim, as, bandit, Pi, depth_limit );
		}
		
		/**
		 * Returns Nothing immediate if total power is 0 in the current state,
		 * else delegates to 'pi'.
		 */
		private static class ShortCircuitPolicy extends Policy<CosmicState, CosmicAction>
		{
			private final Policy<CosmicState, CosmicAction> pi;
			private boolean terminal = false;
			
			public ShortCircuitPolicy( final Policy<CosmicState, CosmicAction> pi )
			{
				this.pi = pi;
			}
			
			@Override
			public void setState( final CosmicState s, final long t )
			{
				double P = 0;
				for( final Shunt sh : s.shunts() ) {
					final double cur_p = sh.current_P();
					P += Math.max( cur_p, 0 );
				}
				terminal = (P == 0);
				if( !terminal ) {
					pi.setState( s, t );
				}
			}

			@Override
			public CosmicAction getAction()
			{
				if( terminal ) {
					return new CosmicNothingAction();
				}
				else {
					return pi.getAction();
				}
			}

			@Override
			public void actionResult( final CosmicState sprime, final double[] r )
			{
				if( !terminal ) {
					pi.actionResult( sprime, r );
				}
			}

			@Override
			public String getName()
			{
				return pi.getName();
			}

			@Override
			public int hashCode()
			{
				return pi.hashCode();
			}

			@Override
			public boolean equals( final Object that )
			{
				return that instanceof ShortCircuitPolicy
					   && ((ShortCircuitPolicy) that).pi.equals( pi );
			}
		}
		
		public Policy<CosmicState, CosmicAction> applyEpoch( final Policy<CosmicState, CosmicAction> pi )
		{
			final int epoch = getInt( "epoch" );
			if( epoch <= 0 ) {
				throw new IllegalArgumentException( "epoch" );
			}
			else if( epoch == 1 ) {
				return pi;
			}
			else {
				return new ReducedFrequencyPolicy<>( pi, new CosmicNothingAction(),
													 new ReducedFrequencyPolicy.Skip<CosmicState, CosmicAction>( epoch ) );
			}
		}
		
		public Policy<CosmicState, CosmicAction> createAgent( final RandomGenerator rng, final CosmicParameters params )
		{
			final CosmicTransitionSimulator sim;
			if( getInt( "seed.agent" ) == 0 ) {
				sim = new CosmicTransitionSimulator( "deterministic", params );
			}
			else {
				sim = new CosmicTransitionSimulator( "agent", params );
			}
			final Budget budget = installBudget( sim );
			
			final AnytimePolicy<CosmicState, CosmicAction> base;
			final String alg = get( "algorithm" );
			switch( alg ) {
			case "ndtest":
				final AnytimePolicy<CosmicState, CosmicAction> pr = createPolicyRollout( rng, params, sim );
				base = new OverridePolicy<CosmicState, CosmicAction>( pr, new NothingPolicy() );
				break;
			case "sztest":
				base = new ConsPolicy<>( new ShedZoneAction( 1, 0.1 ), new NothingPolicy() );
				break;
			case "nothing":
				base = new NothingPolicy();
				break;
			case "ps": {
				final ArrayList<Policy<CosmicState, CosmicAction>> Pi = new ArrayList<>();
				Pi.add( applyEpoch( new HystereticLoadShedding( new FeatureVmag(), params, 0.05, 0.95, 0.98, 5, true ) ) );
				Pi.add( applyEpoch( new HystereticLoadShedding( new FeatureVmag(), params, 0.1, 0.95, 0.98, 5, true ) ) );
				Pi.add( new SequencePolicy<>( new int[] { 1 },
					new IsolatePolicy(),
					applyEpoch( new HystereticLoadShedding( new FeatureVmag(), params, 0.05, 0.95, 0.98, 5, true ) ) ) );
				Pi.add( new SequencePolicy<>( new int[] { 1 },
					new ShedGlobalPolicy( 0.1 ),
					applyEpoch( new HystereticLoadShedding( new FeatureVmag(), params, 0.05, 0.95, 0.98, 5, true ) ) ) );
				Pi.add( new SequencePolicy<>( new int[] { 1 },
					new ShedGlobalPolicy( 0.2 ),
					applyEpoch( new HystereticLoadShedding( new FeatureVmag(), params, 0.05, 0.95, 0.98, 5, true ) ) ) );
				final FiniteBandit<Policy<CosmicState, CosmicAction>> bandit = createBandit();
				final int depth_limit = getInt( "pr.depth" );
				base = new PolicySwitching<>( rng, sim, bandit, Pi, depth_limit );
				break;
			}
			case "psd": {
				final ArrayList<Policy<CosmicState, CosmicAction>> Pi = new ArrayList<>();
				Pi.add( new NothingPolicy() );
				Pi.add( applyEpoch( new HystereticLoadShedding( new FeatureVmag(), params, 0.05, 0.95, 0.98, 5, true ) ) );
				Pi.add( applyEpoch( new HystereticLoadShedding( new FeatureVmag(), params, 0.1, 0.95, 0.98, 5, true ) ) );
				Pi.add( new SequencePolicy<>( new int[] { 1 },
					new IsolatePolicy(), new NothingPolicy() ) );
				Pi.add( new SequencePolicy<>( new int[] { 1 },
					new ShedGlobalPolicy( 0.1 ), new NothingPolicy() ) );
				Pi.add( new SequencePolicy<>( new int[] { 1 },
					new ShedGlobalPolicy( 0.2 ), new NothingPolicy() ) );
				Pi.add( new SequencePolicy<>( new int[] { 1 },
					new IsolatePolicy(),
					applyEpoch( new HystereticLoadShedding( new FeatureVmag(), params, 0.05, 0.95, 0.98, 5, true ) ) ) );
				Pi.add( new SequencePolicy<>( new int[] { 1 },
					new ShedGlobalPolicy( 0.1 ),
					applyEpoch( new HystereticLoadShedding( new FeatureVmag(), params, 0.05, 0.95, 0.98, 5, true ) ) ) );
				Pi.add( new SequencePolicy<>( new int[] { 1 },
					new ShedGlobalPolicy( 0.2 ),
					applyEpoch( new HystereticLoadShedding( new FeatureVmag(), params, 0.05, 0.95, 0.98, 5, true ) ) ) );
				final FiniteBandit<Policy<CosmicState, CosmicAction>> bandit = createBandit();
				final int depth_limit = getInt( "pr.depth" ) + 1;
				base = new PolicySwitching<>( rng, sim, bandit, Pi, depth_limit );
				break;
			}
			case "pr":
				base = createPolicyRollout( rng, params, sim );
				break;
			case "shed_global":
				base = new SequencePolicy<>( new int[] { getInt( "Tstable" ) + 1 },
					new ShedGlobalPolicy( getDouble( "shed_global.p" ) ), new NothingPolicy() );
//				assert( epoch == 1 );
				break;
			case "isolate":
				base = new IsolatePolicy();
//				assert( epoch == 1 );
				break;
			case "partition":
				base = new PartitionZonesPolicy( getDouble( "Tstable" ) );
//				assert( epoch == 1 );
				break;
			case "hls": {
				final String fname = get( "hls.feature" );
				final HystereticLoadShedding.Feature f;
				if( "Vmag".equals( fname ) ) {
					f = new FeatureVmag();
				}
				else if( "frequency".equals( fname ) ) {
					f = new FeatureFrequency();
				}
				else {
					throw new IllegalArgumentException( "hls.feature" );
				}
				base = new HystereticLoadShedding( f, params,
												   getDouble( "hls.amount" ),
												   getDouble( "hls.fault_threshold" ),
												   getDouble( "hls.clear_threshold" ),
												   getDouble( "hls.delay" ),
												   true );
//				assert( epoch == 1 );
				break;
			}
			default:
				throw new IllegalArgumentException( "algorithm" );
			}
			
			final BudgetPolicy<CosmicState, CosmicAction> budget_agent = new BudgetPolicy<>( base, budget );
			final Policy<CosmicState, CosmicAction> agent = applyEpoch( budget_agent );
			
			// ShortCircuitPolicy avoids executing 'agent' if the current state
			// is a total blackout.
//			return new ShortCircuitPolicy( agent );
			return agent;
		}

		public ActionSpace<CosmicState, CosmicAction> getActionSpace()
		{
			final String p = get( "action_space" );
			final String[] sets = p.split( "\\." );
			final List<ActionSpace<CosmicState, CosmicAction>> ass = new ArrayList<>();
			for( final String s : sets ) {
				ass.add( createActionSpace( s ) );
			}
			final ActionSpace<CosmicState, CosmicAction> as = ActionSpace.union( ass );
			LogAgent.info( "action_space: {}", as );
			return as;
		}
		
		private ActionSpace<CosmicState, CosmicAction> createActionSpace( final String name )
		{
			switch( name ) {
			case "Nothing":
				return new NothingActionSpace();
			case "LoadShed":
				return new LoadShedActionSpace();
			case "Island":
				return new IslandActionSpace();
			case "ShedGlobal": {
				final String[] amount_strings = get( "shed_global.amounts" ).split( ";" );
				final double[] amounts = Fn.mapParseDouble( amount_strings );
				return new ShedGlobalActionSpace( amounts );
			}
			case "ShedZone": {
				final String[] amount_strings = get( "shed_zone.amounts" ).split( ";" );
				final double[] amounts = Fn.mapParseDouble( amount_strings );
				return new ShedZoneActionSpace( amounts );
			}
			default:
				throw new IllegalArgumentException( "action_space" );
			}
		}

		public ArrayList<Policy<CosmicState, CosmicAction>>
		getPolicySet( final RandomGenerator rng, final CosmicParameters params )
		{
			final String p = get( "pr.policy_set" );
			final String[] sets = p.split( "\\." );
			
			final Set<Policy<CosmicState, CosmicAction>> Pi_set = new HashSet<>();
			
			for( final String s : sets ) {
				Pi_set.addAll( parsePolicySet( s, params ) );
			}
			
			final ArrayList<Policy<CosmicState, CosmicAction>> Pi = new ArrayList<>();
			Pi.addAll( Pi_set );
			return Pi;
		}
		
		private ArrayList<Policy<CosmicState, CosmicAction>> parsePolicySet( final String name, final CosmicParameters params )
		{
			final ArrayList<Policy<CosmicState, CosmicAction>> Pi = new ArrayList<>();
			if( name.startsWith( "Nothing" ) ) {
				Pi.add( new NothingPolicy() );
			}
			else if( name.startsWith( "HLS" ) ) {
				Pi.add( new HystereticLoadShedding( new FeatureVmag(), params, 0.05, 0.95, 0.98, 5, true ) );
			}
			else if( name.startsWith( "ShedGlobal" ) ) {
				Pi.add( applyEpoch( new ShedGlobalPolicy( 0.1 ) ) );
			}
			/*
			else if( name.startsWith( "LS" ) ) {
				if( name.startsWith( "LS-H" ) ) {
					final HystereticLoadShedding.Feature feature = new FeatureVmag();
					final double fault_threshold = 0.9;
					final double clear_threshold = 0.95;
					final double delay = 3.0;
					final HystereticLoadShedding ls = new HystereticLoadShedding(
							new MersenneTwister( rng.nextInt() ), feature, params,
							fault_threshold, clear_threshold, delay );
					Pi.add( ls );
				}
			}
			*/
			else {
				throw new IllegalArgumentException( "policy '" + name + "'" );
			}
			return Pi;
		}
		
		public CosmicMatlabInterface.Problem createCase( final CosmicMatlabInterface cosmic )
		{
			final CosmicOptions jopt = new CosmicOptions.Builder()
				.verbose( cosmic_verbose )
				.simgrid_max_recursion( getInt( "cosmic.simgrid_max_recursion" ) )
				.simgrid_method( CosmicOptions.SimgridMethod.valueOf( get( "cosmic.simgrid_method" ) ) )
				.random_generator( "agent", getInt( "seed.agent" ) )
				.random_generator( "world", getInt( "seed.world" ) )
				.random_loads( getBoolean( "cosmic.random.loads" ) )
				.random_load_min( getDouble( "cosmic.random.load_min" ) )
				.random_load_max( getDouble( "cosmic.random.load_max" ) )
				.random_load_sigma( getDouble( "cosmic.random.load_sigma" ) )
				.random_relays( getBoolean( "cosmic.random.relays" ) )
				.random_relay_mu( getDouble( "cosmic.random.relay_mu" ) )
				.finish();
			final String name = get( "domain" );
			final CosmicMatlabInterface.Problem c;
			switch( name ) {
			case "ieee39":
				c = cosmic.init_case39( T, jopt );
				break;
			case "rts96":
				c = cosmic.init_rts96( T, jopt );
				break;
			case "poland":
				c = cosmic.init_poland( T, jopt );
				break;
			default:
				throw new IllegalArgumentException( "domain" );
			}
			// FIXME: This is temporary
			c.params.setCosmicVersion( version );
			return c;
		}

		public FiniteBandit<Policy<CosmicState, CosmicAction>> createBandit()
		{
			final String name = get( "pr.bandit" );
			switch( name ) {
			case "cyclic":
				return new CyclicFiniteBandit<>();
			case "uniform":
				return new EpsilonGreedyBandit<>( EpsilonGreedyBandit.uniform );
			case "ucb":
				return new UcbBandit<>( getDouble( "pr.bandit.ucb.c" ) );
			case "ucb-sqrt":
				return new UcbSqrtBandit<>( getDouble( "pr.bandit.ucb.c" ) );
			case "epsilon-greedy":
				return new EpsilonGreedyBandit<>( getDouble( "pr.bandit.greedy.epsilon" ) );
			default:
				throw new IllegalArgumentException( "pr.bandit" );
			}
		}

		public Budget installBudget( final CosmicTransitionSimulator sim )
		{
			final String name = get( "budget_type" );
			final double amount = getDouble( "budget" );
			switch( name ) {
			case "trajectory": {
				final TrajectoryBudget<CosmicState, CosmicAction> b = new TrajectoryBudget<>( (int) amount );
				sim.addSimulationListener( b );
				return b;
			}
			case "transition": {
				final TransitionBudget<CosmicState, CosmicAction> b = new TransitionBudget<>( (int) amount );
				sim.addSimulationListener( b );
				return b;
			}
			default:
				throw new IllegalArgumentException( "budget_type" );
			}
		}
		
		private Gson createGson( final CosmicParameters params )
		{
			final GsonBuilder gson_builder = new GsonBuilder();
			if( getBoolean( "log.history.pretty" ) ) {
				gson_builder.setPrettyPrinting();
			}
			
			return CosmicGson.createGson( params, gson_builder );
		}
		
		public PrintWriter createHistoryPrintStream( final int episode )
		{
			final PrintWriter out;
			try {
				out = new PrintWriter( new File( data_directory, "history_e" + episode + ".json" ) );
			}
			catch( final FileNotFoundException ex ) {
				throw new RuntimeException( ex );
			}
			return out;
		}
		
		public void installLoggers( final TrajectorySimulator<CosmicState, CosmicAction> sim, final int episode )
		{
			final SimulationListener<CosmicState, CosmicAction> csv_logger
				= new SimulationListener<CosmicState, CosmicAction>() {

				@Override
				public void onInitialStateSample(
						final StateNode<CosmicState, CosmicAction> s0 )
				{
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onTransitionSample(
						final ActionNode<CosmicState, CosmicAction> trans )
				{
					// TODO Auto-generated method stub
					
				}
				
			};
		}
		
		public void writeEpisodeHistory( final Gson gson, final int episode, final StateNode<CosmicState, CosmicAction> s0 )
		{
			try( final PrintWriter out = createHistoryPrintStream( episode ) ) {
				final JsonWriter writer = new JsonWriter( out );
				
				writer.beginArray();
				// To save disk space, we only record the (s, a) pair if
				// 1) 'a' is a non-default action, or
				// 2) 's' is the first or last state
				final StateActionGraphVisitor<CosmicState, CosmicAction> tr_writer
						= new StateActionGraphVisitor<CosmicState, CosmicAction>() {
					StateNode<CosmicState, CosmicAction> sprev = null;
					
					@Override
					public void visitActionNode( final ActionNode<CosmicState, CosmicAction> an )
					{
						if( !(an.a instanceof CosmicNothingAction) ) {
							gson.toJson( sprev.s, sprev.s.getClass(), writer );
							gson.toJson( new JsonPrimitive( sprev.r ), writer );
							
							gson.toJson( an.a, an.a.getClass(), writer );
							gson.toJson( new JsonPrimitive( an.r ), writer );
						}
						else if( sprev.s.t == 0 || sprev.s.t == T ) {
							gson.toJson( sprev.s, sprev.s.getClass(), writer );
							gson.toJson( new JsonPrimitive( sprev.r ), writer );
						}
					}
					
					@Override
					public void visitStateNode( final StateNode<CosmicState, CosmicAction> sn )
					{
						sprev = sn;
					}
				};
				new TrajectoryTraversal<CosmicState, CosmicAction>( s0, tr_writer ).run();
				writer.endArray();
			}
			catch( final IOException ex ) {
				throw new RuntimeException( ex );
			}
		}
	}
	
	private static class FaultPolicy extends Policy<CosmicState, CosmicAction>
	{
		private int t = -1;
		
		private final CosmicAction fault_action;
		private final int Tstable;
		
		public FaultPolicy( final CosmicAction fault_action, final int Tstable )
		{
			this.fault_action = fault_action;
			this.Tstable = Tstable;
		}

		@Override
		public void setState( final CosmicState s, final long t )
		{
			this.t += 1;
		}

		@Override
		public CosmicAction getAction()
		{
			if( t == Tstable ) {
				return fault_action.create();
			}
			else {
				return new CosmicNothingAction();
			}
		}

		@Override
		public void actionResult( final CosmicState sprime, final double[] r )
		{ }

		@Override
		public String getName()
		{
			return "Fault@" + Tstable + "[" + fault_action + "]";
		}

		@Override
		public int hashCode()
		{
			final HashCodeBuilder hb = new HashCodeBuilder();
			hb.append( getClass() ).append( Tstable ).append( fault_action );
			return hb.toHashCode();
		}

		@Override
		public boolean equals( final Object obj )
		{
			if( !(obj instanceof FaultPolicy) ) {
				return false;
			}
			final FaultPolicy that = (FaultPolicy) obj;
			return fault_action.equals( that.fault_action ) && Tstable == that.Tstable;
		}
		
	}
	
	/**
	 * This class is responsible for extracting necessary information from the
	 * "real world" trajectory and freeing the CosmicState objects.
	 */
	private static class WorldTrajectoryConsumer implements AutoCloseable, SimulationListener<CosmicState, CosmicAction>
	{
		private final Configuration config;
		private final CosmicParameters params;
		private final Csv.Writer rewards;
		private Csv.Writer trajectory = null;
		
		private final Gson gson;
		private PrintWriter history_out = null;
		private JsonWriter history_writer = null;
		
		private StateNode<CosmicState, CosmicAction> sprev = null;
		
		public WorldTrajectoryConsumer( final Configuration config, final CosmicParameters params ) throws FileNotFoundException
		{
			this.config = config;
			this.params = params;
			this.rewards = new Csv.Writer( new PrintStream( new File( config.data_directory, "rewards.csv" ) ) );
			this.gson = config.log_history != Configuration.HistoryLoggingMode.none
						? config.createGson( params ) : null;
			
			// Initialize output files
			rewards.cell( "Tstable" ).cell( "Tepisode" ).cell( "fault" );
			// Rewards are state + previous action, so there's one more reward
			// column than action column
			for( int i = 0; i < config.T; ++i ) {
				rewards.cell( "r" + i );
				rewards.cell( "a" + i );
			}
			rewards.cell( "r" + config.T );
			rewards.newline();
		}
		
		public void beginEpisode( final TrajectorySimulator<CosmicState, CosmicAction> world, final int episode ) throws IOException
		{
			if( config.log_history != Configuration.HistoryLoggingMode.none  ) {
				history_out = config.createHistoryPrintStream( episode );
				history_writer = new JsonWriter( history_out );
				history_writer.beginArray();
			}
			
			rewards.cell( config.Tstable ).cell( config.Tepisode ).cell( StringUtils.join( config.branch_set, ';' ) );
			
			trajectory = new Csv.Writer( new PrintStream( new File(
					config.data_directory, "trajectory_e" + episode + ".csv" ) ) );
			
			world.addSimulationListener( this );
		}
		
		public void endEpisode() throws IOException
		{
			if( config.log_history != Configuration.HistoryLoggingMode.none  ) {
				history_writer.endArray();
				history_out.close();
				history_out = null;
			}
			
			rewards.newline();
			
			trajectory.close();
			
			if( sprev != null ) {
				sprev.s.close();
			}
		}
		
		@Override
		public void close()
		{
			rewards.close();
			if( history_out != null ) {
				history_out.close();
				history_out = null;
			}
		}
		
		private String[] trajectoryColumns( final CosmicState s )
		{
			// Things to log:
			// 0. time
			// 1. Bus
			//		A. Vmag
			// 2. Machine
			// 		A. omega
			// 3. Shunt
			//		A. P
			//		B. Q
			//		C. current_P
			//		D. current_Q
			//		E. factor
			
			final int N = 1 + params.Nbus + params.Nmachine + 5*params.Nshunt;
			final String[] v = new String[N];
			
			int idx = 0;
			// Time
			v[idx++] = "t";
			// Bus fields
			for( final Bus bu : s.buses() ) {
				final String bus_string = "bus" + bu.id();
				v[idx] = bus_string + "_Vmag";
				idx += 1;
			}
			// Machine fields
			for( final Machine ma : s.machines() ) {
				v[idx] = "mac" + ma.id() + "_omega";
				idx += 1;
			}
			// Shunt fields
			for( final Shunt sh : s.shunts() ) {
				final String shunt_string = "sh" + sh.id();
				v[idx]					 = shunt_string + "_P";
				v[idx + 1*params.Nshunt] = shunt_string + "_Q";
				v[idx + 2*params.Nshunt] = shunt_string + "_current_P";
				v[idx + 3*params.Nshunt] = shunt_string + "_current_Q";
				v[idx + 4*params.Nshunt] = shunt_string + "_factor";
				idx += 1;
			}
			idx += 4*params.Nshunt;
			
			assert( idx == N );
			return v;
		}
		
		private String[] trajectoryState( final CosmicState s )
		{
			// Things to log:
			// 0. time
			// 1. Bus
			//		A. Vmag
			// 2. Machine
			// 		A. omega
			// 3. Shunt
			//		A. P
			//		B. Q
			//		C. current_P
			//		D. current_Q
			//		E. factor
			
			final int N = 1 + params.Nbus + params.Nmachine + 5*params.Nshunt;
			final double[] v = new double[N];
			
			int idx = 0;
			// Time
			v[idx++] = s.t;
			// Bus fields
			for( final Bus bu : s.buses() ) {
				v[idx] = bu.Vmag();
				idx += 1;
			}
			// Machine fields
			for( final Machine ma : s.machines() ) {
				v[idx] = ma.omega();
				idx += 1;
			}
			// Shunt fields
			for( final Shunt sh : s.shunts() ) {
				v[idx]					 = sh.P();
				v[idx + 1*params.Nshunt] = sh.Q();
				v[idx + 2*params.Nshunt] = sh.current_P();
				v[idx + 3*params.Nshunt] = sh.current_Q();
				v[idx + 4*params.Nshunt] = sh.factor();
				idx += 1;
			}
			idx += 4*params.Nshunt;
			
			assert( idx == N );
			
			final String[] vs = new String[N];
			final int significant_figures = 5;
			for( int i = 0; i < N; ++i ) {
				final BigDecimal bd = new BigDecimal( v[i], new MathContext(significant_figures) );
				vs[i] = bd.toPlainString();
			}
			return vs;
		}

		@Override
		public void onInitialStateSample( final StateNode<CosmicState, CosmicAction> s0 )
		{
			// We have to wait until now to initialize trajectory log because
			// we need to iterate over elements of the state.
			for( final String tcol : trajectoryColumns( s0.s ) ) {
				trajectory.cell( tcol );
			}
			trajectory.newline();
			trajectory.row( trajectoryState( s0.s ) );
			
			rewards.cell( s0.r );
			sprev = s0;
			
			if( config.log_history != Configuration.HistoryLoggingMode.none  ) {
				gson.toJson( sprev.s, sprev.s.getClass(), history_writer );
				gson.toJson( new JsonPrimitive( sprev.r ), history_writer );
			}
		}

		@Override
		public void onTransitionSample( final ActionNode<CosmicState, CosmicAction> trans )
		{
			rewards.cell( trans.a );
			double r = trans.r;
			final StateNode<CosmicState, CosmicAction> succ = Iterables.getOnlyElement( trans.successors() );
			r += succ.r;
			rewards.cell( r );
			trajectory.row( trajectoryState( succ.s ) );
			
			if( config.log_history != Configuration.HistoryLoggingMode.none  ) {
				if( config.log_history == Configuration.HistoryLoggingMode.full
					|| (config.log_history == Configuration.HistoryLoggingMode.small
						&& (sprev.s.t % config.getInt( "epoch" )) == 0) ) {
//						&& !(trans.a instanceof CosmicNothingAction)) ) {
					if( sprev.s.t > 0 ) {
						// We always log the initial state, so make sure we
						// don't duplicate it.
						gson.toJson( sprev.s, sprev.s.getClass(), history_writer );
						gson.toJson( new JsonPrimitive( sprev.r ), history_writer );
					}
				}
				
				gson.toJson( trans.a, trans.a.getClass(), history_writer );
				gson.toJson( new JsonPrimitive( trans.r ), history_writer );
				
				if( succ.s.t == config.T ) {
					// Always log the final state. We know it's not a length-0
					// trajectory because we're in onTransitionSample().
					gson.toJson( succ.s, succ.s.getClass(), history_writer );
					gson.toJson( new JsonPrimitive( succ.r ), history_writer );
				}
			}
			
			sprev.s.close();
			sprev = succ;
		}
	}
	
	private static final ch.qos.logback.classic.Logger LogAgent = LoggerManager.getLogger( "log.agent" );
	private static final ch.qos.logback.classic.Logger LogDomain = LoggerManager.getLogger( "log.domain" );
	private static final ch.qos.logback.classic.Logger LogWorld = LoggerManager.getLogger( "log.world" );

	/**
	 * @param args
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void main( final String[] args ) throws FileNotFoundException, IOException
	{
		System.out.println( "[Es beginnt!]" );
		
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
		
		final KeyValueStore expr_config = csv_config.get( 0 );
		final Configuration config = new Configuration(
				root_directory.getPath(), experiment_name, expr_config );
		
		// Configure loggers
		LogAgent.setLevel( Level.valueOf( config.get( "log.agent" ) ) );
		LogDomain.setLevel( Level.valueOf( config.get( "log.domain" ) ) );
		LogWorld.setLevel( Level.valueOf( config.get( "log.world" ) ) );
		
		LogAgent.warn( "log.agent working" );
		LogDomain.warn( "log.domain working" );
		LogWorld.warn( "log.world working" );
			
		try( final CosmicMatlabInterface cosmic = new CosmicMatlabInterface() ) {
			System.out.println( "[Matlab is alive]" );
			
			// Initialize Cosmic
			final CosmicMatlabInterface.Problem cosmic_case = config.createCase( cosmic );
			final CosmicParameters params = cosmic_case.params;
			LogDomain.info( "params: {}", cosmic_case.params );
			LogDomain.info( "s0: {}", cosmic_case.s0 );
			LogDomain.info( "s0.ps: {}", cosmic_case.s0.ps );
			
			try( final WorldTrajectoryConsumer data_out = new WorldTrajectoryConsumer( config, params ) ) {
				
				for( int episode = 0; episode < config.getInt( "Nepisodes" ); ++episode ) {
					final CosmicState s = cosmic_case.s0.copy();
					
					// Simulator
					final RandomGenerator world_rng = new MersenneTwister( config.getInt( "seed.world" ) );
					final CosmicTransitionSimulator world = new CosmicTransitionSimulator( "world", params );
					world.addSimulationListener( new SimulationListener<CosmicState, CosmicAction>() {
		
						int t = 0;
						
						@Override
						public void onInitialStateSample( final StateNode<CosmicState, CosmicAction> s0 )
						{
							t = 0;
							LogWorld.info( "world: t  : {}", t );
							LogWorld.info( "world: s  : {}", s0.s );
							LogWorld.info( "world: s.r: {}", s0.r );
						}
		
						@Override
						public void onTransitionSample(
								final ActionNode<CosmicState, CosmicAction> trans )
						{
							LogWorld.info( "world: a  : {}", trans.a );
							LogWorld.info( "world: a.r: {}", trans.r );
							final StateNode<CosmicState, CosmicAction> sprime = Fn.head( trans.successors() );
							
							t += 1;
							LogWorld.info( "world: t  : {}", t );
							LogWorld.debug( "world: s  : {}", sprime.s );
							LogWorld.info( "world: s.r: {}", sprime.r );
							// Note: show_memory() will cause a crash on the cluster!
//							cosmic.show_memory();
						}
					} );
					
					// Agent
					final RandomGenerator agent_rng = new MersenneTwister( config.getInt( "seed.agent" ) );
					final Policy<CosmicState, CosmicAction> agent = config.createAgent( agent_rng, params );
					
					// Fault scenario
					final CosmicAction fault_action;
					if( config.branch_set.length > 0 ) {
						fault_action = new TripBranchSetAction( config.branch_set );
					}
					else {
						fault_action = new CosmicNothingAction();
					}
					// Note: 'Tstable - 1' because we want the agent to start
					// acting at t = Tstable.
					final Policy<CosmicState, CosmicAction> fault = new FaultPolicy( fault_action, config.Tstable - 1 );
					
					// Top-level control policy
					final List<Policy<CosmicState, CosmicAction>> seq = new ArrayList<>();
					seq.add( fault );
					seq.add( agent );
					final int[] switch_times = new int[] { config.Tstable };
					final Policy<CosmicState, CosmicAction> pi = new SequencePolicy<>( seq, switch_times );
					
					// Do episode
					data_out.beginEpisode( world, episode );
					world.sampleTrajectory( world_rng, s, pi, config.T );
					data_out.endEpisode();
					
					System.out.println( "[Episode " + episode + " ok]" );
				} // for each episode
			} // RAII for data_out
		} // RAII for cosmic interface
		
		System.out.println( "[Alles gut!]" );
	}

}
