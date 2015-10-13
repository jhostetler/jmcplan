/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.advising;

import java.io.File;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.JointPolicy;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssSimulatorAdapter;
import edu.oregonstate.eecs.mcplan.sim.Episode;
import edu.oregonstate.eecs.mcplan.sim.HistoryRecorder;
import edu.oregonstate.eecs.mcplan.sim.RewardAccumulator;
import edu.oregonstate.eecs.mcplan.sim.Simulator;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;
import edu.oregonstate.eecs.mcplan.util.MinMaxAccumulator;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * @author jhostetler
 *
 */
public class AdvisingTest
{

	private static class OnlyRequirementsPolicy extends Policy<AdvisingState, TakeCourseAction>
	{
		private AdvisingState s = null;
		
		@Override
		public void setState( final AdvisingState s, final long t )
		{
			this.s = s;
		}

		@Override
		public TakeCourseAction getAction()
		{
			if( s.grade[2] < s.params.passing_grade ) {
				return new TakeCourseAction( new int[] { 2 } );
			}
			else if( s.grade[3] < s.params.passing_grade ) {
				return new TakeCourseAction( new int[] { 3 } );
			}
			else if( s.grade[6] < s.params.passing_grade ) {
				return new TakeCourseAction( new int[] { 6 } );
			}
			else {
				return null;
			}
		}

		@Override
		public void actionResult( final AdvisingState sprime, final double[] r )
		{ }

		@Override
		public String getName()
		{ return getClass().getName(); }

		@Override
		public int hashCode()
		{ return System.identityHashCode( this ); }

		@Override
		public boolean equals( final Object that )
		{ return that instanceof OnlyRequirementsPolicy; }
	}
	
	private static class PrereqsFirstPolicy extends Policy<AdvisingState, TakeCourseAction>
	{
		private AdvisingState s = null;
		
		@Override
		public void setState( final AdvisingState s, final long t )
		{
			this.s = s;
		}

		@Override
		public TakeCourseAction getAction()
		{
			if( s.grade[0] < 1 ) { // s.params.passing_grade ) {
				return new TakeCourseAction( new int[] { 0 } );
			}
			else if( s.grade[1] < 1 ) { //< s.params.passing_grade ) {
				return new TakeCourseAction( new int[] { 1 } );
			}
			else if( s.grade[2] < s.params.passing_grade ) {
				return new TakeCourseAction( new int[] { 2 } );
			}
			else if( s.grade[3] < s.params.passing_grade ) {
				return new TakeCourseAction( new int[] { 3 } );
			}
			else if( s.grade[6] < s.params.passing_grade ) {
				return new TakeCourseAction( new int[] { 6 } );
			}
			else {
				return null;
			}
		}

		@Override
		public void actionResult( final AdvisingState sprime, final double[] r )
		{ }

		@Override
		public String getName()
		{ return getClass().getName(); }

		@Override
		public int hashCode()
		{ return System.identityHashCode( this ); }

		@Override
		public boolean equals( final Object that )
		{ return that instanceof PrereqsFirstPolicy; }
	}
	
	/**
	 * @param args
	 */
	public static void main( final String[] args )
	{
		final RandomGenerator rng = new MersenneTwister( 42 );
		final File domain = new File( ".", "rddl/academic_advising_mdp.rddl" );
		final File instance = new File( ".", "rddl/academic_advising_inst_mdp__1.rddl" );
		final int max_grade = 4;
		final int passing_grade = 2;
		final AdvisingParameters params = AdvisingRddlParser.parse(	max_grade, passing_grade, domain, instance );
//		final Policy<AdvisingState, TakeCourseAction> pi = new OnlyRequirementsPolicy();
		final Policy<AdvisingState, TakeCourseAction> pi = new PrereqsFirstPolicy();
		
		System.out.println( pi );
		
		final MeanVarianceAccumulator ret = new MeanVarianceAccumulator();
		final MeanVarianceAccumulator steps = new MeanVarianceAccumulator();
		final MinMaxAccumulator steps_minmax = new MinMaxAccumulator();
		final TObjectIntMap<TakeCourseAction> action_histogram = new TObjectIntHashMap<>();
		
		for( int i = 0; i < 2000; ++i ) {
			final AdvisingFsssModel episode_model = new AdvisingFsssModel( rng, params );
			final AdvisingState s0 = episode_model.initialState();
			final Simulator<AdvisingState, TakeCourseAction> sim = new FsssSimulatorAdapter<>( episode_model, s0 );
			final Episode<AdvisingState, TakeCourseAction> episode	= new Episode<>( sim, new JointPolicy<>( pi ), params.T );
			final RewardAccumulator<AdvisingState, TakeCourseAction> racc = new RewardAccumulator<>( sim.nagents(), 1.0 );
			episode.addListener( racc );
			final HistoryRecorder<
				AdvisingState, ? extends Representer<AdvisingState, ? extends Representation<AdvisingState>>, TakeCourseAction
			> hacc = new HistoryRecorder<>( episode_model.base_repr() );
			episode.addListener( hacc );
			
			// Do the work
			episode.run();
			
			// Episode statistics
			ret.add( racc.v()[0] );
			steps.add( racc.steps() );
			steps_minmax.add( racc.steps() );
			for( final JointAction<TakeCourseAction> j : hacc.actions ) {
				action_histogram.adjustOrPutValue( j.get( 0 ), 1, 1 );
			}
		}
		
		System.out.println( "****************************************" );
		System.out.println( "Average return: " + ret.mean() );
		System.out.println( "Return variance: " + ret.variance() );
		System.out.println( "Confidence: " + ret.confidence() );
		
		System.out.println( "Steps (mean): " + steps.mean() );
		System.out.println( "Steps (var): " + steps.variance() );
		System.out.println( "Steps (min/max): " + steps_minmax.min() + " -- " + steps_minmax.max() );
		
		System.out.println( "Action histogram:" );
		final TObjectIntIterator<TakeCourseAction> ahist_itr = action_histogram.iterator();
		int total_actions = 0;
		while( ahist_itr.hasNext() ) {
			ahist_itr.advance();
			System.out.println( "" + ahist_itr.key() + ": " + ahist_itr.value() );
			total_actions += ahist_itr.value();
		}
		System.out.println( "total_actions: " + total_actions );
	}

}
