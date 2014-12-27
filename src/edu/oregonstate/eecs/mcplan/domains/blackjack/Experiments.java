/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.RandomPolicy;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.domains.cards.Deck;
import edu.oregonstate.eecs.mcplan.domains.cards.InfiniteDeck;
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
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * @author jhostetler
 *
 */
public class Experiments
{

	private static final RandomGenerator rng = new MersenneTwister( 43 );
	
	private static <X extends Representation<BlackjackState>, R extends Representer<BlackjackState, X>>
	void runExperiment( final R repr, final BlackjackParameters params,
						final int Nepisodes, final double p, final int Ngames, final PrintStream data_out )
	{
		System.out.println( "****************************************" );
		System.out.println( "game = " + params.max_score + " x " + Ngames
							+ ": " + repr + " x " + Nepisodes + ", p = " + p );
		
		final MctsVisitor<BlackjackState, BlackjackAction>
			visitor	= new DefaultMctsVisitor<BlackjackState, BlackjackAction>();
		
		final ActionGenerator<BlackjackState, JointAction<BlackjackAction>> action_gen
			= new BlackjackJointActionGenerator( 1 );
		
		final double c = 1.0;
		final int rollout_width = 1;
		final int rollout_depth = Integer.MAX_VALUE;
		final double discount = 1.0;
		// Optimistic default value
		final double[] default_value = new double[] { 1.0 };
		final BackupRule<X, BlackjackAction> backup
			= BackupRule.<X, BlackjackAction>MaxQ();
		final MeanVarianceAccumulator ret = new MeanVarianceAccumulator();
		for( int i = 0; i < Ngames; ++i ) {
			if( i % 100000 == 0 ) {
				System.out.println( "Episode " + i );
			}
			
			final Deck deck = new InfiniteDeck();
			final BlackjackSimulator sim = new BlackjackSimulator( deck, 1, params );
			
			final Policy<BlackjackState, JointAction<BlackjackAction>>
				rollout_policy = new RandomPolicy<BlackjackState, JointAction<BlackjackAction>>(
					0 /*Player*/, rng.nextInt(), action_gen.create() );
			final EvaluationFunction<BlackjackState, BlackjackAction> rollout_evaluator
				= RolloutEvaluator.create( rollout_policy, discount, rollout_width, rollout_depth );
			
			final GameTreeFactory<
				BlackjackState, X, BlackjackAction
			> factory
				= new UctSearch.Factory<BlackjackState, X, BlackjackAction>(
					sim, repr.create(), action_gen, c, Nepisodes, rng,
					rollout_evaluator, backup, default_value );
			
			final SearchPolicy<BlackjackState, X, BlackjackAction>
				search_policy = new SearchPolicy<BlackjackState, X, BlackjackAction>(
					factory, visitor, null ) {
	
						@Override
						protected JointAction<BlackjackAction> selectAction(
								final GameTree<X, BlackjackAction> tree )
						{
							return BackupRules.MaxAction( tree.root() ).a();
						}
	
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
		final double conf = 1.96 * Math.sqrt( ret.variance() ) / Math.sqrt( Ngames );
		System.out.println( "Confidence: " + conf );
		System.out.println();
		data_out.print( repr );
		data_out.print( "," + params.max_score );
		data_out.print( "," + Ngames );
		data_out.print( "," + Nepisodes );
		data_out.print( "," + p );
		data_out.print( "," + ret.mean() );
		data_out.print( "," + ret.variance() );
		data_out.print( "," + conf );
		data_out.println();
	}
	
	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main( final String[] args ) throws FileNotFoundException
	{
		final PrintStream data_out = new PrintStream( "data_r2.csv" );
		data_out.println( "abstraction,game,Ngames,Nepisodes,p,mean,var,conf" );
		final BlackjackParameters params = new BlackjackParameters();
		final BlackjackMdp mdp = new BlackjackMdp( params );
		System.out.println( "Solving MDP" );
		final Pair<String[][], String[][]> soln = mdp.solve();
		final String[][] hard_actions = soln.first;
		final String[][] soft_actions = soln.second;
		
//		for( final int Nepisodes : new int[] { 512, 1024 } ) {
//			runExperiment( new IdentityRepresenter(), Nepisodes, 0.0, Ngames, data_out );
//			runExperiment( new BlackjackAggregator(), Nepisodes, 0.0, Ngames, data_out );
//			runExperiment( new NoisyAStarAggregator( rng, 0.0 ), Nepisodes, 0.0, Ngames, data_out );
//		}
		
//		for( final int Nepisodes : new int[] { 4, 8, 16, 32, 64, 128, 256, 512, 1024 } ) {
//			for( final double p : new double[] { 0.02, 0.04, 0.08, 0.16, 0.32 } ) {
//				runExperiment( new NoisyAStarAggregator( rng, p ), Nepisodes, p, Ngames, data_out );
//			}
//		}
		
		final int Ngames = 100000;
		for( final int Nepisodes : new int[] { 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192 } ) {
//		for( final int Nepisodes : new int[] { 8192 } ) {
			for( final double p : new double[] { 0, 0.3 } ) {
				runExperiment( new NoisyAStarAggregator( rng, p, hard_actions, soft_actions, params ),
							   params, Nepisodes, p, Ngames, data_out );
			}
			
			runExperiment( new BlackjackAggregator(), params, Nepisodes, 0.0, Ngames, data_out );
			runExperiment( new BlackjackPrimitiveRepresenter(), params, Nepisodes, 0.0, Ngames, data_out );
		}
	}
//		final RandomGenerator rng = new MersenneTwister();
//
//		final MctsVisitor<BlackjackState, BlackjackStateToken, BlackjackAction>
//			visitor	= new DefaultMctsVisitor<BlackjackState, BlackjackStateToken, BlackjackAction>();
//
//		final Representer<BlackjackState, BlackjackStateToken> repr = new IdentityRepresenter();
//		final ActionGenerator<BlackjackState, JointAction<BlackjackAction>> action_gen
//			= new BlackjackActionGenerator( 1 );
//		final Policy<BlackjackState, JointAction<BlackjackAction>>
//			rollout_policy = new RandomPolicy<BlackjackState, JointAction<BlackjackAction>>(
//				0 /*Player*/, rng.nextInt(), action_gen.create() );
//
//		final double c = 1.0;
//		final int episode_limit = 32;
//		final int rollout_width = 1;
//		final int rollout_depth = 1;
//		// Optimistic default value
//		final double[] default_value = new double[] { 1.0 };
//		final BackupRule<BlackjackStateToken, BlackjackAction> backup
//			= BackupRule.<BlackjackStateToken, BlackjackAction>MaxQ();
//		final MeanVarianceAccumulator ret = new MeanVarianceAccumulator();
//		final int Nepisodes = 10;
//		for( int i = 0; i < Nepisodes; ++i ) {
//			if( i % 100 == 0 ) {
//				System.out.println( "Episode " + i );
//			}
//
//			final Deck deck = new InfiniteDeck();
//			final BlackjackSimulator sim = new BlackjackSimulator( deck, 1 );
//
//			final GameTreeFactory<
//				BlackjackState, BlackjackStateToken, BlackjackAction
//			> factory
//				= new UctSearch.Factory<BlackjackState, BlackjackStateToken, BlackjackAction>(
//					sim, repr, action_gen, c, episode_limit, rng,
//					rollout_policy, rollout_width, rollout_depth, backup, default_value );
//
//			final SearchPolicy<BlackjackState, BlackjackStateToken, BlackjackAction>
//				search_policy = new SearchPolicy<BlackjackState, BlackjackStateToken, BlackjackAction>(
//					factory, visitor, null ) {
//
//						@Override
//						protected JointAction<BlackjackAction> selectAction(
//								final GameTree<BlackjackStateToken, BlackjackAction> tree )
//						{
//							return BackupRules.MaxAction( tree.root() ).a();
//						}
//
//						@Override
//						public int hashCode()
//						{ return System.identityHashCode( this ); }
//
//						@Override
//						public boolean equals( final Object that )
//						{ return this == that; }
//			};
//
//			final Episode<BlackjackState, BlackjackAction> episode
//				= new Episode<BlackjackState, BlackjackAction>(	sim, search_policy );
//			episode.run();
//			System.out.println( sim.state().token().toString() );
//			System.out.println( "Reward: " + sim.reward()[0] );
//			ret.add( sim.reward()[0] );
//		}
//		System.out.println( "****************************************" );
//		System.out.println( "Average return: " + ret.mean() );
//		System.out.println( "Return variance: " + ret.variance() );
//	}

}
