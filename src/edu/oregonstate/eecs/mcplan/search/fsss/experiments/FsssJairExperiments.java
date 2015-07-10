/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss.experiments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import ch.qos.logback.classic.Level;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.JointPolicy;
import edu.oregonstate.eecs.mcplan.LoggerManager;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.domains.advising.AdvisingFsssModel;
import edu.oregonstate.eecs.mcplan.domains.advising.AdvisingParameters;
import edu.oregonstate.eecs.mcplan.domains.advising.AdvisingRddlParser;
import edu.oregonstate.eecs.mcplan.domains.inventory.InventoryFsssModel;
import edu.oregonstate.eecs.mcplan.domains.inventory.InventoryProblem;
import edu.oregonstate.eecs.mcplan.domains.racegrid.RacegridCircuits;
import edu.oregonstate.eecs.mcplan.domains.racegrid.RacegridFsssModel;
import edu.oregonstate.eecs.mcplan.domains.racegrid.RacegridState;
import edu.oregonstate.eecs.mcplan.domains.spbj.SpBjFsssModel;
import edu.oregonstate.eecs.mcplan.domains.toy.CliffWorld;
import edu.oregonstate.eecs.mcplan.domains.toy.RallyWorld;
import edu.oregonstate.eecs.mcplan.domains.toy.RelevantIrrelevant;
import edu.oregonstate.eecs.mcplan.domains.toy.SavingProblem;
import edu.oregonstate.eecs.mcplan.domains.toy.WeinsteinLittman;
import edu.oregonstate.eecs.mcplan.search.fsss.AStarIrrelevanceSplitEvaluator;
import edu.oregonstate.eecs.mcplan.search.fsss.Budget;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssAbstractActionNode;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssAbstraction;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssModel;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssParameters;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssPartitionTreeRefinementAbstraction;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssSampleBudget;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssSimulatorAdapter;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssStaticAbstraction;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssTimeBudget;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssTreeStatistics;
import edu.oregonstate.eecs.mcplan.search.fsss.L1SplitEvaluator;
import edu.oregonstate.eecs.mcplan.search.fsss.ParssTreeBuilder;
import edu.oregonstate.eecs.mcplan.search.fsss.PriorityRefinementOrder;
import edu.oregonstate.eecs.mcplan.search.fsss.RandomPartitionRepresenter;
import edu.oregonstate.eecs.mcplan.search.fsss.RefineableRandomPartitionRepresenter;
import edu.oregonstate.eecs.mcplan.search.fsss.SearchAlgorithm;
import edu.oregonstate.eecs.mcplan.search.fsss.SplitEvaluator;
import edu.oregonstate.eecs.mcplan.search.fsss.SubtreeHeuristicBfsRefinementOrder;
import edu.oregonstate.eecs.mcplan.search.fsss.SubtreeRefinementOrder.SplitChooser;
import edu.oregonstate.eecs.mcplan.search.fsss.TrivialRepresenterFsssModelAdapter;
import edu.oregonstate.eecs.mcplan.search.fsss.priority.BreadthFirstPriorityRefinementOrder;
import edu.oregonstate.eecs.mcplan.search.fsss.priority.UniformPriorityRefinementOrder;
import edu.oregonstate.eecs.mcplan.sim.Episode;
import edu.oregonstate.eecs.mcplan.sim.EpisodeListener;
import edu.oregonstate.eecs.mcplan.sim.LoggingEpisodeListener;
import edu.oregonstate.eecs.mcplan.sim.RewardAccumulator;
import edu.oregonstate.eecs.mcplan.sim.Simulator;
import edu.oregonstate.eecs.mcplan.util.Csv;
import edu.oregonstate.eecs.mcplan.util.Csv.Writer;
import edu.oregonstate.eecs.mcplan.util.CsvConfigurationParser;
import edu.oregonstate.eecs.mcplan.util.KeyValueStore;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;
import edu.oregonstate.eecs.mcplan.util.MinMaxAccumulator;

/**
 * @author jhostetler
 *
 */
public class FsssJairExperiments
{
	public static class Configuration implements KeyValueStore
	{
		private final KeyValueStore config_;
		
		public final String model;
		public final String domain;
		// FIXME: Why is 'root_directory' a String?
		public final String root_directory;
		public final String training_data;
		public final String labels;
		
		public final int Ntest_episodes_order;
		public final int Ntest_episodes;
		public final int Ntest_games;
		public final double discount;
		public final int seed;
		
		public final RandomGenerator rng;
		public final File data_directory;
		public final String experiment_name;
		
		private final Set<String> exclude_ = new HashSet<String>();
		
		public File trainSingleDirectory()
		{
			return new File( root_directory, "train_single" );
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
			
			model = config.get( "model" );
			exclude_.add( "model" );
			
			training_data = config.get( "training_data" );
			exclude_.add( "training_data" );
			
			labels = config.get( "labels" );
			exclude_.add( "labels" );
			
			Ntest_episodes_order = config.getInt( "Ntest_episodes_order" );
			Ntest_episodes = 1 << Ntest_episodes_order; // 2^order
			Ntest_games = config.getInt( "Ntest_games" );
			discount = config.getDouble( "discount" );
			seed = config.getInt( "seed" );
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
	
	private static abstract class Algorithm<S extends State, A extends VirtualConstructor<A>>
	{
		public abstract FsssParameters getParameters();
		public abstract FsssAbstraction<S, A> getAbstraction();
		public abstract Policy<S, A> getControlPolicy( final Configuration config, final FsssModel<S, A> model );
		public abstract void writeStatisticsHeaders( final Csv.Writer csv );
		public abstract void writeStatisticsRecord( final Csv.Writer csv );
	}
	
	private static class ParssAlgorithm<S extends State, A extends VirtualConstructor<A>> extends Algorithm<S, A>
	{
		private final FsssAbstraction<S, A> abstraction;
		private final PriorityRefinementOrder.Factory<S, A> refinement_order_factory;
		
		private final FsssParameters parameters;
		
		private final FsssTreeStatistics<S, A> tree_stats;
		private final MeanVarianceAccumulator num_refinements = new MeanVarianceAccumulator();
		private final MinMaxAccumulator min_max_refinements = new MinMaxAccumulator();
		private final MeanVarianceAccumulator num_lead_changes = new MeanVarianceAccumulator();
		private final MinMaxAccumulator min_max_lead_changes = new MinMaxAccumulator();
		private final MeanVarianceAccumulator elapsed_time = new MeanVarianceAccumulator();
		private final MeanVarianceAccumulator budget = new MeanVarianceAccumulator();
		
		public ParssAlgorithm( final FsssParameters parameters, final FsssAbstraction<S, A> abstraction,
							   final PriorityRefinementOrder.Factory<S, A> refinement_order_factory )
		{
			this.abstraction = abstraction;
			this.refinement_order_factory = refinement_order_factory;
			
			this.parameters = parameters;
			
			tree_stats = new FsssTreeStatistics<S, A>( parameters.depth );
		}
		
		@Override
		public FsssParameters getParameters()
		{
			return parameters;
		}
		
		@Override
		public String toString()
		{
			return "PARSS(" + abstraction + "; " + refinement_order_factory + ")";
		}
		
		@Override
		public FsssAbstraction<S, A> getAbstraction()
		{
			return abstraction;
		}
		
		@Override
		public Policy<S, A> getControlPolicy( final Configuration config, final FsssModel<S, A> model )
		{
			return new Policy<S, A>() {
				
				SearchAlgorithm<S, A> search = null;

				@Override
				public void setState( final S s, final long t )
				{
					model.resetSampleCount();
					
					if( parameters.depth == 0 ) {
						assert( false );
//						assert( !"par".equals( config.get( "ss.abstraction" ) ) );
//						search = new IterativeDeepening<S, A>(
//							parameters, model, abstraction, s, refinement_order_factory );
					}
					else {
						search = new ParssTreeBuilder<S, A>(
							parameters, model, abstraction, s, refinement_order_factory );
					}
					
					if( config.getBoolean( "log.search" ) ) {
						search.enableLogging();
					}
				}

				@Override
				public A getAction()
				{
					final long then = System.nanoTime();
					search.run();
					final long now = System.nanoTime();
					final long elapsed_ms = (now - then) / 1000000L;
					elapsed_time.add( elapsed_ms );
					budget.add( parameters.budget.actualDouble() );
					
					tree_stats.visitRoot( search.root() );
					num_refinements.add( search.numRefinements() );
					min_max_refinements.add( search.numRefinements() );
					num_lead_changes.add( search.numLeadChanges() );
					min_max_lead_changes.add( search.numLeadChanges() );
					
					final ArrayList<FsssAbstractActionNode<S, A>> best = search.root().greatestLowerBound();
					final A a = best.get( config.rng.nextInt( best.size() ) ).a().create();
					return a;
				}

				@Override
				public void actionResult( final S sprime, final double[] r )
				{ }

				@Override
				public String getName()
				{ return "PARSS"; }

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
			csv.cell( "time_mean" ).cell( "time_var" ).cell( "time_conf" );
			
			csv.cell( "budget_mean" ).cell( "budget_var" ).cell( "budget_conf" );
			
			csv.cell( "num_samples_mean" ).cell( "num_samples_var" );
			csv.cell( "min_samples" ).cell( "max_samples" );
			csv.cell( "num_refinements_mean" ).cell( "num_refinements_var" );
			csv.cell( "min_refinements" ).cell( "max_refinements" );
			csv.cell( "num_lead_changes_mean" ).cell( "num_lead_changes_var" );
			csv.cell( "min_lead_changes" ).cell( "max_lead_changes" );
			
			csv.cell( "optimal_abstract_subtree_size_mean" ).cell( "optimal_abstract_subtree_size_var" )
			   .cell( "optimal_ground_subtree_size_mean" ).cell( "optimal_ground_subtree_size_var" )
			   .cell( "optimal_max_depth_mean" ).cell( "optimal_max_depth_var" )
			   .cell( "optimal_mean_depth_mean" ).cell( "optimal_mean_depth_var" )
			   .cell( "optimal_num_leaves_mean" ).cell( "optimal_num_leaves_var" );
			for( int i = 0; i < tree_stats.optimal_subtree.depth_branching.size(); ++i ) {
				csv.cell( "optimal_depth_" + i + "_branching_mean" ).cell( "optimal_depth_" + i + "_branching_var" );
			}
			
			csv.cell( "nonoptimal_abstract_subtree_size_mean" ).cell( "nonoptimal_abstract_subtree_size_var" )
			   .cell( "nonoptimal_ground_subtree_size_mean" ).cell( "nonoptimal_ground_subtree_size_var" )
			   .cell( "nonoptimal_max_depth_mean" ).cell( "nonoptimal_max_depth_var" )
			   .cell( "nonoptimal_mean_depth_mean" ).cell( "nonoptimal_mean_depth_var" )
			   .cell( "nonoptimal_num_leaves_mean" ).cell( "nonoptimal_num_leaves_var" );
			for( int i = 0; i < tree_stats.nonoptimal_subtrees.depth_branching.size(); ++i ) {
				csv.cell( "nonoptimal_depth_" + i + "_branching_mean" ).cell( "nonoptimal_depth_" + i + "_branching_var" );
			}
			
			csv.cell( "samples_per_ms" );
		}

		@Override
		public void writeStatisticsRecord( final Writer csv )
		{
			csv.cell( elapsed_time.mean() ).cell( elapsed_time.variance() ).cell( elapsed_time.confidence() );
			
			csv.cell( budget.mean() ).cell( budget.variance() ).cell( budget.confidence() );
			
			csv.cell( tree_stats.num_samples.mean() ).cell( tree_stats.num_samples.variance() );
			csv.cell( tree_stats.min_max_samples.min() ).cell( tree_stats.min_max_samples.max() );
			csv.cell( num_refinements.mean() ).cell( num_refinements.variance() );
			csv.cell( min_max_refinements.min() ).cell( min_max_refinements.max() );
			csv.cell( num_lead_changes.mean() ).cell( num_lead_changes.variance() );
			csv.cell( min_max_lead_changes.min() ).cell( min_max_lead_changes.max() );
			
			final FsssTreeStatistics.SubtreeStatistics<S, A> opt = tree_stats.optimal_subtree;
			csv.cell( opt.abstract_subtree_size.mean() ).cell( opt.abstract_subtree_size.variance() )
			   .cell( opt.ground_subtree_size.mean() ).cell( opt.ground_subtree_size.variance() )
			   .cell( opt.max_depth.mean() ).cell( opt.max_depth.variance() )
			   .cell( opt.mean_depth.mean() ).cell( opt.mean_depth.variance() )
			   .cell( opt.num_leaves.mean() ).cell( opt.num_leaves.variance() );
			for( int i = 0; i < opt.depth_branching.size(); ++i ) {
				csv.cell( opt.depth_branching.get( i ).mean() ).cell( opt.depth_branching.get( i ).variance() );
			}
			
			final FsssTreeStatistics.SubtreeStatistics<S, A> nonopt = tree_stats.nonoptimal_subtrees;
			csv.cell( nonopt.abstract_subtree_size.mean() ).cell( nonopt.abstract_subtree_size.variance() )
			   .cell( nonopt.ground_subtree_size.mean() ).cell( nonopt.ground_subtree_size.variance() )
			   .cell( nonopt.max_depth.mean() ).cell( nonopt.max_depth.variance() )
			   .cell( nonopt.mean_depth.mean() ).cell( nonopt.mean_depth.variance() )
			   .cell( nonopt.num_leaves.mean() ).cell( nonopt.num_leaves.variance() );
			for( int i = 0; i < nonopt.depth_branching.size(); ++i ) {
				csv.cell( nonopt.depth_branching.get( i ).mean() ).cell( nonopt.depth_branching.get( i ).variance() );
			}
			
			csv.cell( tree_stats.num_samples.mean() / elapsed_time.mean() );
		}
	}
	
	private static <S extends State, X extends FactoredRepresentation<S>,
					A extends VirtualConstructor<A>, R extends FactoredRepresenter<S, X>>
	void runGames( final Configuration config, final FsssModel<S, A> model, final int iter ) throws Exception
	{
		final Algorithm<S, A> algorithm = createAlgorithm( config, model );
		
		// Time limit?
		final String T_str = config.get( config.domain + ".T" );
		final int T = (T_str != null ? Integer.parseInt( T_str ) : Integer.MAX_VALUE);
		
		System.out.println( "****************************************" );
		System.out.println( "game = " + config.Ntest_games
							+ " x " + config.domain
							+ ", "	+ algorithm );
		System.out.println( "SS: width = " + config.getInt( "ss.width" ) + ", depth = " + config.getInt( "ss.depth" )
							+ ", budget = " + algorithm.getParameters().budget );
		
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

			final Policy<S, A> pi = algorithm.getControlPolicy( config, model );
			
			final S s0 = model.initialState();
			final Simulator<S, A> sim = new FsssSimulatorAdapter<S, A>( model, s0 );
			final Episode<S, A> episode	= new Episode<S, A>( sim, new JointPolicy<S, A>( pi ), T );
			final RewardAccumulator<S, A> racc = new RewardAccumulator<S, A>( sim.nagents(), config.discount );
			episode.addListener( racc );
			
			if( config.getBoolean( "log.execution" ) ) {
				final LoggingEpisodeListener<S, A> epi_log = new LoggingEpisodeListener<S, A>();
				episode.addListener( epi_log );
			}
			
//			if( listener != null ) {
//				episode.addListener( listener );
//			}
			if( use_visualization ) {
				// TODO:
				final EpisodeListener<S, A> vis = null; //domain.getVisualization();
				if( vis != null ) {
					episode.addListener( vis );
				}
				else {
					System.out.println( "Warning: No visualization implemented" );
				}
			}
			
			// TODO: Debugging code
//			{
//				final RacegridState rs = (RacegridState) s0;
//				final RacegridVisualization vis = new RacegridVisualization( null, rs.terrain, 10 );
//				episode.addListener( (EpisodeListener<S, A>) vis.updater( 500 ) );
//			}
			
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
		
		// This must happen *after* the statistics object has been populated
		final Csv.Writer data_out = createDataWriter( config, model, algorithm, iter );
		// See: createDataWriter for correct column order
		data_out.cell( config.experiment_name ).cell( model.base_repr() ).cell( algorithm )
				.cell( config.Ntest_episodes ).cell( config.Ntest_games )
				.cell( ret.mean() ).cell( ret.variance() ).cell( ret.confidence() )
				.cell( steps.mean() ).cell( steps.variance() ).cell( steps_minmax.min() ).cell( steps_minmax.max() );
				
		algorithm.writeStatisticsRecord( data_out );
		for( final String k : config.keys() ) {
			data_out.cell( config.get( k ) );
		}
		data_out.newline();
		System.out.println();
	}
	
	private static <S extends State, A extends VirtualConstructor<A>>
	Csv.Writer createDataWriter( final Configuration config, final FsssModel<S, A> model,
								 final Algorithm<S, A> algorithm, final int iter )
	{
		Csv.Writer data_out;
		try {
			data_out = new Csv.Writer( new PrintStream( new File( config.data_directory, "data_" + iter + ".csv" ) ) );
		}
		catch( final FileNotFoundException ex ) {
			throw new RuntimeException( ex );
		}
		data_out.cell( "experiment_name" ).cell( "base_repr" ).cell( "algorithm" )
				.cell( "Nepisodes" ).cell( "Ngames" )
				.cell( "V_mean" ).cell( "V_var" ).cell( "V_conf" )
				.cell( "steps_mean" ).cell( "steps_var" ).cell( "steps_min" ).cell( "steps_max" );
				
		algorithm.writeStatisticsHeaders( data_out );
		for( final String k : config.keys() ) {
			data_out.cell( k );
		}
		data_out.newline();
		
		return data_out;
	}
	
	// -----------------------------------------------------------------------
	// Configuration
	// -----------------------------------------------------------------------
	
	private static <S extends State, A extends VirtualConstructor<A>>
	Budget createBudget( final Configuration config, final FsssModel<S, A> model )
	{
		if( "samples".equals( config.get( "ss.budget_type" ) ) ) {
			final int samples = config.getInt( "ss.budget" );
			return new FsssSampleBudget<S, A>( model, samples );
		}
		else if( "time".equals( config.get( "ss.budget_type" ) ) ) {
			final double ms = config.getDouble( "ss.budget" );
			return new FsssTimeBudget( ms );
		}
		else {
			throw new IllegalArgumentException( "ss.budget_type" );
		}
	}
	
	private static <S extends State, A extends VirtualConstructor<A>>
	Algorithm<S, A> createAlgorithm( final Configuration config, final FsssModel<S, A> model )
	{
		final Budget budget = createBudget( config, model );
		final FsssParameters parameters = new FsssParameters(
			config.getInt( "ss.width" ), config.getInt( "ss.depth" ), budget );
		final FsssAbstraction<S, A> abstraction;
		final PriorityRefinementOrder.Factory<S, A> priority_factory;
		if( "par".equals( config.get( "ss.abstraction" ) ) ) {
			// FIXME: It isn't quite right to divide the algorithm into
			// 'classifier' and 'refinement_order', because some refinement
			// orders assume a particular classifier (e.g. 'heuristic'
			// subtree order assumes 'PartitionTreeRefinementAbstraction').
			if( "decision_tree".equals( config.get( "par.classifier" ) ) ) {
				final SplitChooser<S, A> split_chooser = createSplitChooser( config, parameters, model );
				abstraction = new FsssPartitionTreeRefinementAbstraction<S, A>( model, split_chooser );
			}
			else if( "random_partition".equals( config.get( "par.classifier" ) ) ) {
				abstraction = new RefineableRandomPartitionRepresenter.Abstraction<S, A>( model );
			}
			else {
				throw new IllegalArgumentException( "par.classifier" );
			}
			priority_factory = createPriorityOrderingFactory( config );
		}
		else if( "random_partition".equals( config.get( "ss.abstraction" ) ) ) {
			final int k = config.getInt( "random_partition.k" );
			abstraction = new RandomPartitionRepresenter.Abstraction<S, A>( model, k );
			priority_factory = null;
		}
		else if( "static".equals( config.get( "ss.abstraction" ) ) ) {
			abstraction = new FsssStaticAbstraction<S, A>( model );
			priority_factory = null;
		}
		else if( "trivial".equals( config.get( "ss.abstraction" ) ) ) {
			abstraction = new FsssStaticAbstraction<S, A>( new TrivialRepresenterFsssModelAdapter<S, A>( model ) );
			priority_factory = null;
		}
		else {
			throw new IllegalArgumentException( "ss.abstraction" );
		}
		
		return new ParssAlgorithm<S, A>( parameters, abstraction, priority_factory );
	}
	
	private static <S extends State, A extends VirtualConstructor<A>>
	PriorityRefinementOrder.Factory<S, A> createPriorityOrderingFactory( final Configuration config )
	{
		final String refinement_order = config.get( "par.priority" );
		if( "bf".equals( refinement_order ) ) {
			return new BreadthFirstPriorityRefinementOrder.Factory<S, A>();
		}
		else if( "uniform".equals( refinement_order ) ) {
			return new UniformPriorityRefinementOrder.Factory<S, A>();
		}
		else {
			throw new IllegalArgumentException( "par.priority" );
		}
	}
	
	private static <S extends State, A extends VirtualConstructor<A>>
	SplitChooser<S, A> createSplitChooser(
		final Configuration config, final FsssParameters parameters, final FsssModel<S, A> model )
	{
		final String chooser = config.get( "par.split_chooser" );
		if( "heuristic".equals( chooser ) ) {
			final SplitEvaluator<S, A> split_evaluator = createSplitEvaluator( config );
			return new SubtreeHeuristicBfsRefinementOrder<S, A>( parameters, model, split_evaluator );
		}
//		else if( "random".equals( chooser ) ) {
//			return new SubtreeRandomPartitionBfsRefinementOrder<S, A>( parameters, model );
//		}
		else {
			throw new IllegalArgumentException( "par.split_chooser" );
		}
	}
	
	private static <S extends State, A extends VirtualConstructor<A>>
	SplitEvaluator<S, A> createSplitEvaluator( final Configuration config )
	{
		final String evaluator = config.get( "par.heuristic.split_evaluator" );
		if( "L1".equals( evaluator ) ) {
			final double size_regularization = config.getDouble( "par.split_evaluator.size_regularization" );
			return new L1SplitEvaluator<S, A>( size_regularization );
		}
		else if( "astar".equals( evaluator ) ) {
			final double size_regularization = config.getDouble( "par.split_evaluator.size_regularization" );
			return new AStarIrrelevanceSplitEvaluator<S, A>( size_regularization );
		}
		else {
			throw new IllegalArgumentException( "par.heuristic.split_evaluator" );
		}
	}

	// -----------------------------------------------------------------------
	
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
			
			LoggerManager.getLogger( "log.search" ).setLevel( Level.valueOf( config.get( "log.search") ) );
			
			if( "advising".equals( config.domain ) ) {
				final File domain = new File( config.root_directory, config.get( "rddl.domain" ) + ".rddl" );
				final File instance = new File( config.root_directory, config.get( "rddl.instance" ) + ".rddl" );
				final int max_grade = config.getInt( "advising.max_grade" );
				final int passing_grade = config.getInt( "advising.passing_grade" );
				final AdvisingParameters params = AdvisingRddlParser.parse(
						config.rng, max_grade, passing_grade, domain, instance );
				final AdvisingFsssModel model = new AdvisingFsssModel( params );
				runGames( config, model, expr );
			}
			else if( "cliffworld".equals( config.domain ) ) {
				final CliffWorld.FsssModel model = new CliffWorld.FsssModel( config.rng, config );
				runGames( config, model, expr );
			}
			else if( "inventory".equals( config.domain ) ) {
				final String problem_name = config.get( "inventory.problem" );
				final InventoryProblem problem;
				if( "Dependent".equals( problem_name ) ) {
					problem = InventoryProblem.Dependent();
				}
				else if( "Geometric".equals( problem_name ) ) {
					problem = InventoryProblem.Geometric();
				}
				else if( "Geometric2".equals( problem_name ) ) {
					problem = InventoryProblem.Geometric2();
				}
				else if( "TwoProducts".equals( problem_name ) ) {
					problem = InventoryProblem.TwoProducts();
				}
				else {
					throw new IllegalArgumentException( "inventory.problem" );
				}
				final InventoryFsssModel model = new InventoryFsssModel( config.rng, problem );
				runGames( config, model, expr );
			}
			else if( "racegrid".equals( config.domain ) ) {
				final String circuit = config.get( "racegrid.circuit" );
				final int scale = config.getInt( "racegrid.scale" );
				final int T = config.getInt( "racegrid.T" );
				final RacegridState ex;
				if( "bbs_small".equals( circuit ) ) {
					ex = RacegridCircuits.barto_bradtke_singh_SmallTrack( config.rng, T, scale );
				}
				else if( "bbs_large".equals( circuit ) ) {
					ex = RacegridCircuits.barto_bradtke_singh_LargeTrack( config.rng, T, scale );
				}
				else {
					throw new IllegalArgumentException( "racegrid.circuit" );
				}
				final double slip = config.getDouble( "racegrid.slip" );
				final RacegridFsssModel model = new RacegridFsssModel( config.rng, ex, slip );
				runGames( config, model, expr );
			}
			else if( "rally".equals( config.domain ) ) {
				final RallyWorld.Parameters params = new RallyWorld.Parameters( config.rng, config );
				final RallyWorld.FsssModel model = new RallyWorld.FsssModel( params );
				runGames( config, model, expr );
			}
			else if( "relevant_irrelevant".equals( config.domain ) ) {
				final RelevantIrrelevant.Parameters params = new RelevantIrrelevant.Parameters( config.rng, config );
				final RelevantIrrelevant.FsssModel model = new RelevantIrrelevant.FsssModel( params );
				runGames( config, model, expr );
			}
			else if( "saving".equals( config.domain ) ) {
				final SavingProblem.Parameters params = new SavingProblem.Parameters( config.rng, config );
				final SavingProblem.FsssModel model = new SavingProblem.FsssModel( params );
				runGames( config, model, expr );
			}
			else if( "spbj".equals( config.domain ) ) {
				final SpBjFsssModel model = new SpBjFsssModel( config.rng );
				runGames( config, model, expr );
			}
			else if( "weinstein_littman".equals( config.domain ) ) {
				final WeinsteinLittman.Parameters params = new WeinsteinLittman.Parameters( config.rng, config );
				final WeinsteinLittman.FsssModel model = new WeinsteinLittman.FsssModel( params );
				runGames( config, model, expr );
			}
			else {
				throw new IllegalArgumentException( "domain = " + config.domain );
			}
		}
		
		System.out.println( "Alles gut!" );
	}
}
