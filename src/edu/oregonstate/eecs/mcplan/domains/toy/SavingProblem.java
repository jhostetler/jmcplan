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
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.abstraction.IndexRepresentation;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.KeyValueStore;

/**
 * @author jhostetler
 *
 */
public class SavingProblem
{
	public static class Parameters
	{
		public final int T;
		public final int price_min;
		public final int price_max;
		public final int maturity_period;
		public final int invest_period;
		public final int loan_period;
		
		public final int Nprices;
		
		
		public Parameters( final int T,
						   final int price_min, final int price_max,
						   final int maturity_period,
						   final int invest_period, final int loan_period )
		{
			this.T = T;
			this.price_min = price_min;
			this.price_max = price_max;
			this.maturity_period = maturity_period;
			assert( this.maturity_period >= 1 );
			this.invest_period = invest_period;
			assert( this.invest_period >= 1 );
			this.loan_period = loan_period;
			assert( this.loan_period >= 1 );
			
			this.Nprices = price_max - price_min + 1;
		}
		
		public Parameters( final KeyValueStore config )
		{
			this( config.getInt( "saving.T" ),
				  config.getInt( "saving.price_min" ),
				  config.getInt( "saving.price_max" ),
				  config.getInt( "saving.maturity_period" ),
				  config.getInt( "saving.invest_period" ),
				  config.getInt( "saving.loan_period" ) );
		}
	}
	
	public static class State implements edu.oregonstate.eecs.mcplan.State
	{
		public final Parameters params;
		
		public int t = 0;
		public int investment = 0;
		public int maturity_t = 0;
		public int invest_t = 0;
		public int loan = 0;
		public int loan_t = 0;
		public int price = 0;
		
		private double r = 0;
		
		public State( final Parameters params )
		{
			this.params = params;
		}
		
		public State( final State that )
		{
			this.params = that.params;
			
			this.t = that.t;
			this.investment = that.investment;
			this.maturity_t = that.maturity_t;
			this.invest_t = that.invest_t;
			this.loan = that.loan;
			this.loan_t = that.loan_t;
		}
		
		@Override
		public void close()
		{ }
		
		@Override
		public boolean isTerminal()
		{
			return t >= params.T;
		}
		
		@Override
		public String toString()
		{
			return "t: " + t + ", price: " + price
				   + ", investment: " + investment + ", maturity_t: " + maturity_t
				   + ", invest_t: " + invest_t + ", loan: " + loan + ", loan_t: " + loan_t;
		}
		
		public int samplePrice( final RandomGenerator rng )
		{
			final int p = rng.nextInt( params.Nprices );
			return p + params.price_min;
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static abstract class Action extends UndoableAction<State> implements VirtualConstructor<Action>
	{
		public abstract double reward( final State s );
	}
	
	public static class SaveAction extends Action
	{
		public static final double reward = 1;
		
		@Override
		public void undoAction( final State s )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void doAction( final RandomGenerator rng, final State s )
		{ }

		@Override
		public boolean isDone()
		{ return false; }

		@Override
		public SaveAction create()
		{ return new SaveAction(); }
		
		@Override
		public boolean equals( final Object obj )
		{ return obj instanceof SaveAction; }
		
		@Override
		public int hashCode()
		{ return SaveAction.class.hashCode(); }
		
		@Override
		public String toString()
		{ return "SaveAction"; }
		
		@Override
		public double reward( final State s )
		{ return reward; }
	}
	
	public static class InvestAction extends Action
	{
		public static final double reward = 0;
		
		@Override
		public void undoAction( final State s )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void doAction( final RandomGenerator rng, final State s )
		{
			assert( s.investment == 0 );
			s.investment = 1;
			s.maturity_t = s.params.maturity_period;
			s.invest_t = s.params.invest_period;
		}

		@Override
		public boolean isDone()
		{ return false; }

		@Override
		public InvestAction create()
		{ return new InvestAction(); }
		
		@Override
		public boolean equals( final Object obj )
		{ return obj instanceof InvestAction; }
		
		@Override
		public int hashCode()
		{ return InvestAction.class.hashCode(); }
		
		@Override
		public String toString()
		{ return "InvestAction"; }
		
		@Override
		public double reward( final State s )
		{ return reward; }
	}
	
	public static class SellAction extends Action
	{
		@Override
		public void undoAction( final State s )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void doAction( final RandomGenerator rng, final State s )
		{
			assert( s.investment > 0 );
			s.investment -= 1;
			s.invest_t = 0;
		}

		@Override
		public boolean isDone()
		{ return false; }

		@Override
		public SellAction create()
		{ return new SellAction(); }
		
		@Override
		public boolean equals( final Object obj )
		{ return obj instanceof SellAction; }
		
		@Override
		public int hashCode()
		{ return SellAction.class.hashCode(); }
		
		@Override
		public String toString()
		{ return "SellAction"; }
		
		@Override
		public double reward( final State s )
		{ return s.price; }
	}
	
	public static class BorrowAction extends Action
	{
		public static final double reward = 2;
		public static final double repay_reward = -3;
		
		@Override
		public void undoAction( final State s )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void doAction( final RandomGenerator rng, final State s )
		{
			assert( s.loan == 0 );
			s.loan = 1;
			s.loan_t = s.params.loan_period;
		}

		@Override
		public boolean isDone()
		{ return false; }

		@Override
		public BorrowAction create()
		{ return new BorrowAction(); }
		
		@Override
		public boolean equals( final Object obj )
		{ return obj instanceof BorrowAction; }
		
		@Override
		public int hashCode()
		{ return BorrowAction.class.hashCode(); }
		
		@Override
		public String toString()
		{ return "BorrowAction"; }
		
		@Override
		public double reward( final State s )
		{ return reward; }
	}
	
	public static class Actions extends ActionGenerator<State, Action>
	{
		private static final int InvestShift = 0;
		private static final int BorrowShift = 1;
		private static final int SellShift = 2;
		
		private static Action[] special_actions = new Action[] {
			new InvestAction(),
			new BorrowAction(),
			new SellAction()
		};
		
		public static int actionSetIndex( final State s )
		{
			int code = 0;
			if( s.investment == 0 ) {
				// Invest is legal
				code |= 1 << InvestShift;
			}
			if( s.loan == 0 ) {
				// Borrow is legal
				code |= 1 << BorrowShift;
			}
			if( s.investment > 0 && s.maturity_t == 0 ) {
				// Sell is legal
				code |= 1 << SellShift;
			}
			return code;
		}
		
		private final Parameters params;
		private int code = 0;
		private int shift = 0;
		private int Nactions = -1;
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
			code = actionSetIndex( s );
			Nactions = 1;
			for( int i = 0; i < special_actions.length; ++i ) {
				if( (code & (1 << i)) != 0 ) {
					Nactions += 1;
				}
			}
			shift = -1;
		}

		@Override
		public int size()
		{
			return Nactions;
		}

		@Override
		public boolean hasNext()
		{ return n < size(); }

		@Override
		public Action next()
		{
			assert( hasNext() );
			
			Action a = null;
			if( shift < 0 ) {
				a = new SaveAction();
			}
			else  {
				for( int i = shift; i < special_actions.length; ++i ) {
					shift = i;
					if( (code & (1<<i)) != 0 ) {
						a = special_actions[i].create();
						break;
					}
				}
			}
			
			shift += 1;
			n += 1;
			return a;
		}
	}
	
	public static void applyPreDynamics( final State s )
	{
		s.r = 0;
	}
	
	public static void applyPostDynamics( final RandomGenerator rng, final State s )
	{
		if( s.investment > 0 && s.invest_t == 0 ) {
			assert( s.maturity_t == 0 );
			s.investment = 0;
		}
		if( s.loan > 0 && s.loan_t == 0 ) {
			s.loan = 0;
			s.r = BorrowAction.repay_reward;
		}
		
		if( s.investment > 0 ) {
			if( s.maturity_t > 0 ) {
				s.maturity_t -= 1;
			}
			if( s.maturity_t == 0 ) {
				s.invest_t -= 1;
			}
		}
		if( s.loan > 0 ) {
			s.loan_t -= 1;
		}
		s.price = s.samplePrice( rng );
		s.t += 1;
	}
	
	// -----------------------------------------------------------------------
	
	public static class FsssModel extends edu.oregonstate.eecs.mcplan.search.fsss.FsssModel<State, Action>
	{
		private final Parameters params;
		private final RandomGenerator rng;
		
		private final PrimitiveRepresenter base_repr = new PrimitiveRepresenter();
		private final ActionSetRepresenter action_repr = new ActionSetRepresenter();
		
		private int sample_count = 0;
		
		public FsssModel( final RandomGenerator rng, final Parameters params )
		{
			this.params = params;
			this.rng = rng;
		}
		
		@Override
		public FsssModel create( final RandomGenerator rng )
		{
			return new FsssModel( rng, params );
		}
		
		@Override
		public RandomGenerator rng()
		{
			return rng;
		}
		
		@Override
		public double Vmin( final State s )
		{
			return reward( s ) + (s.params.T - s.t)*BorrowAction.repay_reward;
		}

		@Override
		public double Vmax( final State s )
		{
			return reward( s ) + (s.params.T - s.t)*BorrowAction.reward;
		}
		
		@Override
		public double Vmin( final State s, final Action a )
		{
			return reward( s, a ) + ((s.params.T - s.t) - 1)*BorrowAction.repay_reward;
		}

		@Override
		public double Vmax( final State s, final Action a )
		{
			return reward( s, a ) + ((s.params.T - s.t) - 1)*BorrowAction.reward;
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
			s0.price = s0.samplePrice( rng );
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
			applyPreDynamics( copy );
			a.create().doAction( rng, copy );
			applyPostDynamics( rng, copy );
			
			return copy;
		}

		@Override
		public double reward( final State s )
		{
//			double r = 0;
//			if( s.loan > 0 && s.loan_t == 0 ) {
//				r += BorrowAction.repay_reward;
//			}
//			if( s.investment > 0 && s.invest_t == 0 ) {
//				r += s.price;
//			}
//			return r;
			
			return s.r;
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
			attributes.add( new Attribute( "price" ) );
			attributes.add( new Attribute( "investment" ) );
			attributes.add( new Attribute( "maturity_t" ) );
			attributes.add( new Attribute( "invest_t" ) );
			attributes.add( new Attribute( "loan" ) );
			attributes.add( new Attribute( "loan_t" ) );
		}
		
		@Override
		public FactoredRepresenter<State, FactoredRepresentation<State>> create()
		{ return new PrimitiveRepresenter(); }

		@Override
		public FactoredRepresentation<State> encode( final State s )
		{
			final float[] phi = new float[attributes.size()];
			int idx = 0;
			phi[idx++] = s.t;
			phi[idx++] = s.price;
			phi[idx++] = s.investment;
			phi[idx++] = s.maturity_t;
			phi[idx++] = s.invest_t;
			phi[idx++] = s.loan;
			phi[idx++] = s.loan_t;
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
			return new IndexRepresentation<State>( Actions.actionSetIndex( s ) );
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] argv ) throws NumberFormatException, IOException
	{
		final RandomGenerator rng = new MersenneTwister( 42 );
		final int T = 30;
//		final int price_min = -8;
//		final int price_max = 4;
//		final int invest_period = 6;
//		final int loan_period = 4;
		
		final int price_min = -4;
		final int price_max = 4;
		final int maturity_period = 3;
		final int invest_period = 4;
		final int loan_period = 4;
		
		final Parameters params = new Parameters(
			T, price_min, price_max, maturity_period, invest_period, loan_period );
		final Actions actions = new Actions( params );
		final FsssModel model = new FsssModel( rng, params );
		
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
		
		// Estimate the value of a "good" policy.
		// Note: The "good" policy is to Invest when you can, and Sell if the
		// price is >= 2. This is not necessarily optimal because:
		// 	1. You should Borrow once the episode will end before the loan must be repaid
		//	2. For some values of invest_period, you should pass on a low price
		//	   early in the period to try to get a better one later.
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
