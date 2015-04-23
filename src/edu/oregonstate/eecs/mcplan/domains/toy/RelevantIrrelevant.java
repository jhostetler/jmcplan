/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.toy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.ArrayFactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.TrivialRepresentation;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.KeyValueStore;

/**
 * @author jhostetler
 *
 */
public class RelevantIrrelevant
{
	public static class Parameters
	{
		public final RandomGenerator rng;
		public final int T;
		
		public final int nr;
		public final int ni;

		public Parameters( final RandomGenerator rng, final int T, final int nr, final int ni )
		{
			this.rng = rng;
			this.T = T;
			this.nr = nr;
			this.ni = ni;
		}
		
		public Parameters( final RandomGenerator rng, final KeyValueStore config )
		{
			this( rng,
				  config.getInt( "relevant_irrelevant.T" ),
				  config.getInt( "relevant_irrelevant.nr" ),
				  config.getInt( "relevant_irrelevant.ni" ) );
		}
	}
	
	public static class State implements edu.oregonstate.eecs.mcplan.State
	{
		public final Parameters params;
		
		public int t = 0;
		public int r = 0;
		public int i = 0;
		
		public State( final Parameters params )
		{
			this.params = params;
		}
		
		public State( final State that )
		{
			this.params = that.params;
			
			this.t = that.t;
			this.r = that.r;
			this.i = that.i;
		}
		
		@Override
		public boolean isTerminal()
		{
			return t >= params.T;
		}
		
		@Override
		public String toString()
		{
			return "{t: " + t + ", r: " + r + ", i: " + i + "}";
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static class Action implements UndoableAction<State>, VirtualConstructor<Action>
	{
		public final int r;
		
		public Action( final int r )
		{
			this.r = r;
		}
		
		public double reward( final State s )
		{
			return (r == s.r ? 1.0 : 0.0);
		}
		
		@Override
		public void undoAction( final State s )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void doAction( final State s )
		{
			s.t += 1;
			s.r = s.params.rng.nextInt( s.params.nr );
			s.i = s.params.rng.nextInt( s.params.ni );
		}

		@Override
		public boolean isDone()
		{ return false; }

		@Override
		public Action create()
		{ return new Action( r ); }
		
		@Override
		public boolean equals( final Object obj )
		{
			if( !(obj instanceof Action) ) {
				return false;
			}
			final Action that = (Action) obj;
			return r == that.r;
		}
		
		@Override
		public int hashCode()
		{ return Action.class.hashCode() ^ r; }
		
		@Override
		public String toString()
		{ return "Action[" + r + "]"; }
	}
	
	public static class Actions extends ActionGenerator<State, Action>
	{
		private final Parameters params;
		
		private int n = 0;
		
		public Actions( final Parameters params )
		{
			this.params = params;
		}
		
		@Override
		public Actions create()
		{ return new Actions( params ); }

		@Override
		public void setState( final State s, final long t )
		{
			n = 0;
		}

		@Override
		public int size()
		{
			return params.nr;
		}

		@Override
		public boolean hasNext()
		{ return n < size(); }

		@Override
		public Action next()
		{
			assert( hasNext() );
			return new Action( n++ );
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static class FsssModel extends edu.oregonstate.eecs.mcplan.search.fsss.FsssModel<State, Action>
	{
		private final Parameters params;
		
		private final PrimitiveRepresenter base_repr = new PrimitiveRepresenter();
		private final ActionSetRepresenter action_repr = new ActionSetRepresenter();
		
		private int sample_count = 0;
		
		public FsssModel( final Parameters params )
		{
			this.params = params;
		}
		
		@Override
		public RandomGenerator rng()
		{
			return params.rng;
		}
		
		@Override
		public double Vmin( final State s )
		{ return 0; }

		@Override
		public double Vmax( final State s )
//		{ return params.T; }
		{ return params.T - s.t; }
		
		@Override
		public double Vmin( final State s, final Action a )
		{ return reward( s, a ) + 0; }

		@Override
		public double Vmax( final State s, final Action a )
		{
			// t - 1 for next state
			return reward( s, a ) + (params.T - s.t - 1);
		}
		
		@Override
		public double heuristic( final State s )
		{ return 0; }

		@Override
		public double discount()
		{ return 1.0; }

		@Override
		public FactoredRepresenter<State, ? extends FactoredRepresentation<State>> base_repr()
		{ return base_repr; }

		@Override
		public Representer<State, ? extends Representation<State>> action_repr()
		{ return action_repr; }
		
		@Override
		public State initialState()
		{
			final State s0 = new State( params );
			s0.r = params.rng.nextInt( params.nr );
			s0.i = params.rng.nextInt( params.ni );
			return s0;
		}

		@Override
		public Iterable<Action> actions( final State s )
		{
			final Actions actions = new Actions( s.params );
			actions.setState( s, 0L );
			return Fn.in( actions );
		}

		@Override
		public State sampleTransition( final State s, final Action a )
		{
			sample_count += 1;
			
			final State copy = new State( s );
			a.create().doAction( copy );
			
			return copy;
		}

		@Override
		public double reward( final State s )
		{
			return 0;
		}

		@Override
		public double reward( final State s, final Action a )
		{
			return a.reward( s );
		}

		@Override
		public int sampleCount()
		{ return sample_count; }

		@Override
		public void resetSampleCount()
		{ sample_count = 0; }
	}
	
	// -----------------------------------------------------------------------
	
	public static class PrimitiveRepresenter implements FactoredRepresenter<State, FactoredRepresentation<State>>
	{
		private static ArrayList<Attribute> attributes;
		static {
			attributes = new ArrayList<Attribute>();
			attributes.add( new Attribute( "t" ) );
			attributes.add( new Attribute( "r" ) );
			attributes.add( new Attribute( "i" ) );
		}
		
		@Override
		public FactoredRepresenter<State, FactoredRepresentation<State>> create()
		{ return new PrimitiveRepresenter(); }

		@Override
		public FactoredRepresentation<State> encode( final State s )
		{
			final double[] phi = new double[attributes.size()];
			int idx = 0;
			phi[idx++] = s.t;
			phi[idx++] = s.r;
			phi[idx++] = s.i;
			return new ArrayFactoredRepresentation<State>( phi );
		}

		@Override
		public ArrayList<Attribute> attributes()
		{
			return attributes;
		}
	}
	
	public static class ActionSetRepresenter implements Representer<State, Representation<State>>
	{
		@Override
		public Representer<State, Representation<State>> create()
		{
			return new ActionSetRepresenter();
		}
	
		@Override
		public Representation<State> encode( final State s )
		{
			return new TrivialRepresentation<State>();
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] argv ) throws NumberFormatException, IOException
	{
		final RandomGenerator rng = new MersenneTwister( 42 );
		final int T = 30;
		final int nr = 3;
		final int ni = 3;
		
		final Parameters params = new Parameters( rng, T, nr, ni );
		final Actions actions = new Actions( params );
		final FsssModel model = new FsssModel( params );
		
		State s = model.initialState();
		while( !s.isTerminal() ) {
			System.out.println( s );
			System.out.println( "R(s): " + model.reward( s ) );
			actions.setState( s, 0 );
			final ArrayList<Action> action_list = Fn.takeAll( actions );
			for( int i = 0; i < action_list.size(); ++i ) {
				System.out.println( i + ": " + action_list.get( i ) );
			}
			System.out.print( ">>> " );
			final BufferedReader cin = new BufferedReader( new InputStreamReader( System.in ) );
			final int choice = Integer.parseInt( cin.readLine() );
			final Action a = action_list.get( choice );
			System.out.println( "R(s, a): " + model.reward( s, a ) );
			s = model.sampleTransition( s, a );
		}
		
//		// Estimate the value of a "good" policy.
//		// Note: The "good" policy is to Invest when you can, and Sell if the
//		// price is >= 2. This is not necessarily optimal because:
//		// 	1. You should Borrow once the episode will end before the loan must be repaid
//		//	2. For some values of invest_period, you should pass on a low price
//		//	   early in the period to try to get a better one later.
//		final int Ngames = 10000;
//		double V = 0;
//		int Ninvest = 0;
//		for( int i = 0; i < Ngames; ++i ) {
//			State s = model.initialState();
//			double Vi = model.reward( s );
//			while( !s.isTerminal() ) {
//				final Action a;
//
//				// "Good" policy
//				if( s.investment == 0 ) {
//					a = new InvestAction();
//					Ninvest += 1;
//				}
//				else if( s.investment > 0 && s.price >= 2 ) {
//					if( s.invest_t < (params.invest_period - 1) || s.price > 2 ) {
//						a = new SellAction();
//					}
//					else {
//						a = new SaveAction();
//					}
////					a = new SellAction();
//				}
//				else {
//					a = new SaveAction();
//				}
//
//				// "Borrow" policy
////				if( s.loan == 0 ) {
////					a = new BorrowAction();
////				}
////				else {
////					a = new SaveAction();
////				}
//
//				final double ra = model.reward( s, a );
//				s = model.sampleTransition( s, a );
//				Vi += ra + model.reward( s );
//			}
//			V += Vi;
//		}
//
//		final double Vavg = V / Ngames;
//		final double Navg = (Ninvest / ((double) Ngames));
//		System.out.println( "Avg. value: " + Vavg );
//		System.out.println( "Avg. Invest actions: " + Navg );
//		System.out.println( "V(Invest) ~= " + ( 1 + (Vavg - params.T)/Navg ) );
	}
}
