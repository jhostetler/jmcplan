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
import edu.oregonstate.eecs.mcplan.bandit.FiniteBandit;
import edu.oregonstate.eecs.mcplan.bandit.UniformFiniteBandit;
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
import edu.oregonstate.eecs.mcplan.domains.cosmic.NothingActionSpace;
import edu.oregonstate.eecs.mcplan.domains.cosmic.ShedZoneAction;
import edu.oregonstate.eecs.mcplan.domains.cosmic.ShedZoneActionSpace;
import edu.oregonstate.eecs.mcplan.domains.cosmic.TripBranchSetAction;
import edu.oregonstate.eecs.mcplan.domains.cosmic.policy.NothingPolicy;
import edu.oregonstate.eecs.mcplan.op.PolicyRollout;
import edu.oregonstate.eecs.mcplan.search.fsss.Budget;
import edu.oregonstate.eecs.mcplan.sim.ActionNode;
import edu.oregonstate.eecs.mcplan.sim.SimulationListener;
import edu.oregonstate.eecs.mcplan.sim.StateActionGraphVisitor;
import edu.oregonstate.eecs.mcplan.sim.StateNode;
import edu.oregonstate.eecs.mcplan.sim.TrajectoryBudget;
import edu.oregonstate.eecs.mcplan.sim.TrajectorySimulator;
import edu.oregonstate.eecs.mcplan.sim.TrajectoryTraversal;
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
	public static final class Configuration implements KeyValueStore
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
		}

		@Override
		public String get( final String key )
		{ return config_.get( key ); }

		@Override
		public int getInt( final String key )
		{ return config_.getInt( key ); }

		@Override
		public double getDouble( final String key )
		{ return config_.getDouble( key ); }
		
		@Override
		public boolean getBoolean( final String key )
		{ return config_.getBoolean( key ); }

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
		
		public Policy<CosmicState, CosmicAction> createAgent( final RandomGenerator rng, final CosmicParameters params )
		{
			final CosmicTransitionSimulator sim = new CosmicTransitionSimulator( params );
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
			case "pr":
				base = createPolicyRollout( rng, params, sim );
				break;
			default:
				throw new IllegalArgumentException( "algorithm" );
			}
			
			final BudgetPolicy<CosmicState, CosmicAction> budget_agent = new BudgetPolicy<>( base, budget );
			
			final Policy<CosmicState, CosmicAction> agent;
			final int epoch = getInt( "epoch" );
			if( epoch <= 0 ) {
				throw new IllegalArgumentException( "epoch" );
			}
			else if( epoch == 1 ) {
				agent = budget_agent;
			}
			else {
				agent = new ReducedFrequencyPolicy<>( budget_agent, new CosmicNothingAction(),
													  new ReducedFrequencyPolicy.Skip<CosmicState, CosmicAction>( epoch ) );
			}
			
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
				Pi_set.addAll( parsePolicySet( s ) );
			}
			
			final ArrayList<Policy<CosmicState, CosmicAction>> Pi = new ArrayList<>();
			Pi.addAll( Pi_set );
			return Pi;
		}
		
		public CosmicMatlabInterface.Case createCase( final CosmicMatlabInterface cosmic )
		{
			final CosmicOptions jopt = new CosmicOptions.Builder()
				.verbose( cosmic_verbose )
				.simgrid_max_recursion( getInt( "cosmic.simgrid_max_recursion" ) )
				.simgrid_method( CosmicOptions.SimgridMethod.valueOf( get( "cosmic.simgrid_method" ) ) )
				.finish();
			final String name = get( "domain" );
			final CosmicMatlabInterface.Case c;
			switch( name ) {
			case "ieee39":
				c = cosmic.init_case39( T, jopt );
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
				return new UniformFiniteBandit<>();
			default:
				throw new IllegalArgumentException( "pr.bandit" );
			}
		}

		public Budget installBudget( final CosmicTransitionSimulator sim )
		{
			final String name = get( "budget_type" );
			final double amount = getDouble( "budget" );
			switch( name ) {
			case "trajectory":
				final TrajectoryBudget<CosmicState, CosmicAction> b = new TrajectoryBudget<>( (int) amount );
				sim.addSimulationListener( b );
				return b;
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
	
	private static ArrayList<Policy<CosmicState, CosmicAction>> parsePolicySet( final String name )
	{
		final ArrayList<Policy<CosmicState, CosmicAction>> Pi = new ArrayList<>();
		if( name.startsWith( "Nothing" ) ) {
			Pi.add( new NothingPolicy() );
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
		public void reset()
		{
			t = -1;
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
	
	private static class DataOutput implements AutoCloseable, SimulationListener<CosmicState, CosmicAction>
	{
		private final Configuration config;
		private final Csv.Writer csv;
		
		private final boolean log_history;
		private final Gson gson;
		private PrintWriter history_out = null;
		private JsonWriter history_writer = null;
		
		private StateNode<CosmicState, CosmicAction> sprev = null;
		
		public DataOutput( final Configuration config, final CosmicParameters params ) throws FileNotFoundException
		{
			this.config = config;
			this.csv = new Csv.Writer( new PrintStream( new File( config.data_directory, "rewards.csv" ) ) );
			this.log_history = config.getBoolean( "log.history" );
			this.gson = log_history ? config.createGson( params ) : null;
			
			// Initialize output files
			csv.cell( "Tstable" ).cell( "Tepisode" ).cell( "fault" );
			// Rewards are state + previous action, so there's one more reward
			// column than action column
			for( int i = 0; i < config.T; ++i ) {
				csv.cell( "r" + i );
				csv.cell( "a" + i );
			}
			csv.cell( "r" + config.T );
			csv.newline();
		}
		
		public void beginEpisode( final TrajectorySimulator<CosmicState, CosmicAction> world, final int episode ) throws IOException
		{
			if( log_history ) {
				history_out = config.createHistoryPrintStream( episode );
				history_writer = new JsonWriter( history_out );
				history_writer.beginArray();
			}
			
			csv.cell( config.Tstable ).cell( config.Tepisode ).cell( StringUtils.join( config.branch_set, ';' ) );
			
			world.addSimulationListener( this );
		}
		
		public void endEpisode() throws IOException
		{
			if( log_history ) {
				history_writer.endArray();
				history_out.close();
				history_out = null;
			}
			
			csv.newline();
		}
		
		@Override
		public void close()
		{
			csv.close();
			if( history_out != null ) {
				history_out.close();
				history_out = null;
			}
		}

		@Override
		public void onInitialStateSample( final StateNode<CosmicState, CosmicAction> s0 )
		{
			csv.cell( s0.r );
			sprev = s0;
			
			if( log_history ) {
				gson.toJson( sprev.s, sprev.s.getClass(), history_writer );
				gson.toJson( new JsonPrimitive( sprev.r ), history_writer );
			}
		}

		@Override
		public void onTransitionSample( final ActionNode<CosmicState, CosmicAction> trans )
		{
			csv.cell( trans.a );
			double r = trans.r;
			final StateNode<CosmicState, CosmicAction> succ = Fn.head( trans.succ() );
			r += succ.r;
			csv.cell( r );
			
			if( log_history ) {
				if( !(trans.a instanceof CosmicNothingAction) ) {
					if( sprev.s.t > 0 ) {
						// We always log the initial state, so make sure we
						// don't duplicate it.
						gson.toJson( sprev.s, sprev.s.getClass(), history_writer );
						gson.toJson( new JsonPrimitive( sprev.r ), history_writer );
					}
					
					gson.toJson( trans.a, trans.a.getClass(), history_writer );
					gson.toJson( new JsonPrimitive( trans.r ), history_writer );
				}
				
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
			final CosmicMatlabInterface.Case cosmic_case = config.createCase( cosmic );
			final CosmicParameters params = cosmic_case.params;
			LogDomain.info( "params: {}", cosmic_case.params );
			LogDomain.info( "s0: {}", cosmic_case.s0 );
			LogDomain.info( "s0.ps: {}", cosmic_case.s0.ps );
			
			try( final DataOutput data_out = new DataOutput( config, params ) ) {
				
				for( int episode = 0; episode < config.getInt( "Nepisodes" ); ++episode ) {
					final CosmicState s = cosmic_case.s0;
					
					// Simulator
					final RandomGenerator rng = new MersenneTwister( config.getInt( "seed.world" ) );
					final CosmicTransitionSimulator world = new CosmicTransitionSimulator( params );
					world.addSimulationListener( new SimulationListener<CosmicState, CosmicAction>() {
		
						@Override
						public void onInitialStateSample( final StateNode<CosmicState, CosmicAction> s0 )
						{
							LogWorld.info( "world: s  : {}", s0.s );
							LogWorld.info( "world: s.r: {}", s0.r );
						}
		
						@Override
						public void onTransitionSample(
								final ActionNode<CosmicState, CosmicAction> trans )
						{
							LogWorld.info( "world: a  : {}", trans.a );
							LogWorld.info( "world: a.r: {}", trans.r );
							final StateNode<CosmicState, CosmicAction> sprime = Fn.head( trans.succ() );
							LogWorld.debug( "world: s  : {}", sprime.s );
							LogWorld.info( "world: s.r: {}", sprime.r );
//							cosmic.show_memory();
						}
					} );
					
					// Agent
					final Policy<CosmicState, CosmicAction> agent = config.createAgent( rng, params );
					
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
					world.sampleTrajectory( rng, s, pi, config.T );
					data_out.endEpisode();
				} // for each episode
			} // RAII for data_out
		} // RAII for cosmic interface
		
		System.out.println( "Alles gut!" );
	}

}
