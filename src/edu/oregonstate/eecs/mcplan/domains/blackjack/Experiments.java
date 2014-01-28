/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.RandomPolicy;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.search.BackupRule;
import edu.oregonstate.eecs.mcplan.search.BackupRules;
import edu.oregonstate.eecs.mcplan.search.DefaultMctsVisitor;
import edu.oregonstate.eecs.mcplan.search.GameTree;
import edu.oregonstate.eecs.mcplan.search.GameTreeFactory;
import edu.oregonstate.eecs.mcplan.search.MctsVisitor;
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

	public static class IdentityRepresenter implements Representer<BlackjackState, BlackjackStateToken>
	{
		@Override
		public Representer<BlackjackState, BlackjackStateToken> create()
		{
			return new IdentityRepresenter();
		}

		@Override
		public BlackjackStateToken encode( final BlackjackState s )
		{
			return s.token();
		}
	}
	
	private static <X extends Representation<BlackjackState>, R extends Representer<BlackjackState, X>>
	void runExperiment( final R repr )
	{
		final RandomGenerator rng = new MersenneTwister();
		
		final MctsVisitor<BlackjackState, X, BlackjackAction>
			visitor	= new DefaultMctsVisitor<BlackjackState, X, BlackjackAction>();
		
		final ActionGenerator<BlackjackState, JointAction<BlackjackAction>> action_gen
			= new BlackjackActionGenerator( 1 );
		final Policy<BlackjackState, JointAction<BlackjackAction>>
			rollout_policy = new RandomPolicy<BlackjackState, JointAction<BlackjackAction>>(
				0 /*Player*/, rng.nextInt(), action_gen.create() );
		
		final double c = 1.0;
		final int episode_limit = 32;
		final int rollout_width = 1;
		final int rollout_depth = 1;
		// Optimistic default value
		final double[] default_value = new double[] { 1.0 };
		final BackupRule<X, BlackjackAction> backup
			= BackupRule.<X, BlackjackAction>MaxQ();
		final MeanVarianceAccumulator ret = new MeanVarianceAccumulator();
		final int Nepisodes = 100000;
		for( int i = 0; i < Nepisodes; ++i ) {
			if( i % 1000 == 0 ) {
				System.out.println( "Episode " + i );
			}
			
			final Deck deck = new InfiniteDeck();
			final BlackjackSimulator sim = new BlackjackSimulator( deck, 1 );
			
			final GameTreeFactory<
				BlackjackState, X, BlackjackAction
			> factory
				= new UctSearch.Factory<BlackjackState, X, BlackjackAction>(
					sim, repr, action_gen, c, episode_limit, rng,
					rollout_policy, rollout_width, rollout_depth, backup, default_value );
			
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
		System.out.println( "Confidence: " + 0.975 * ret.variance() / Math.sqrt( Nepisodes ) );
	}
	
	/**
	 * @param args
	 */
	public static void main( final String[] args )
	{
//		runExperiment( new IdentityRepresenter() );
//		runExperiment( new BlackjackAggregator() );
		runExperiment( new AStarAggregator() );
		
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
