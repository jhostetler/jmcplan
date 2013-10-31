/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.toy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.ListIterator;

import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.search.DefaultMctsVisitor;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;

/**
 * This is a very simple "irrelevant state variable" test case. It has horizon
 * 2, and two actions. The state contains the action history and also an
 * integer i that is set randomly by every action. The rewards do not depend
 * on i, so the idea is to discover that i is irrelevant.
 */
public class Irrelevance
{
	public static final int Ns = 10;
	public static final double slip = 0.2;
	
	public static class State
	{
		public String s = "";
		public int i = 0;
		
		@Override
		public String toString()
		{
			return "(" + s + ", " + i + ")";
		}
	}
	
	public static abstract class Action implements UndoableAction<State>, VirtualConstructor<Action>
	{
		@Override
		public abstract Action create();
	}
	
	public static class LeftAction extends Action
	{
		private final RandomGenerator rng_;
		private boolean done_ = false;
		private int i_old_ = 0;
		
		public LeftAction( final RandomGenerator rng )
		{
			rng_ = rng;
		}
		
		@Override
		public void doAction( final State s )
		{
			i_old_ = s.i;
			final UniformIntegerDistribution u = new UniformIntegerDistribution( rng_, 0, Ns - 1 );
			s.i = u.sample();
			if( rng_.nextDouble() > slip ) {
				s.s += "L";
			}
			else {
				s.s += "R";
			}
			done_ = true;
		}

		@Override
		public boolean isDone()
		{ return done_; }

		@Override
		public void undoAction( final State s )
		{
			s.i = i_old_;
			s.s = s.s.substring( 0, s.s.length() - 1 );
			done_ = false;
		}

		@Override
		public LeftAction create()
		{ return new LeftAction( rng_ ); }
		
		@Override
		public int hashCode()
		{ return 37; }
		
		@Override
		public boolean equals( final Object obj )
		{
			return obj != null && obj instanceof LeftAction;
		}
		
		@Override
		public String toString()
		{ return "L"; }
	}
	
	public static class RightAction extends Action
	{
		private final RandomGenerator rng_;
		private boolean done_ = false;
		private int i_old_ = 0;
		
		public RightAction( final RandomGenerator rng )
		{
			rng_ = rng;
		}
		
		@Override
		public void doAction( final State s )
		{
			i_old_ = s.i;
			final UniformIntegerDistribution u = new UniformIntegerDistribution( rng_, 0, Ns - 1 );
			s.i = u.sample();
			if( rng_.nextDouble() > slip ) {
				s.s += "R";
			}
			else {
				s.s += "L";
			}
			done_ = true;
		}

		@Override
		public boolean isDone()
		{ return done_; }

		@Override
		public void undoAction( final State s )
		{
			s.i = i_old_;
			s.s = s.s.substring( 0, s.s.length() - 1 );
			done_ = false;
		}

		@Override
		public RightAction create()
		{ return new RightAction( rng_ ); }
		
		@Override
		public int hashCode()
		{ return 41; }
		
		@Override
		public boolean equals( final Object obj )
		{
			return obj != null && obj instanceof RightAction;
		}
		
		@Override
		public String toString()
		{ return "R"; }
	}
	
	public static class Simulator implements UndoSimulator<State, Action>
	{
		private final State s_ = new State();
		private final Deque<Action> h_ = new ArrayDeque<Action>();
		
		@Override
		public State state()
		{ return s_; }

		@Override
		public void takeAction( final JointAction<Action> j )
		{
			final Action a = j.get( 0 );
			a.doAction( s_ );
			h_.push( a );
		}

		@Override
		public void untakeLastAction()
		{
			final Action a = h_.pop();
			a.undoAction( s_ );
		}

		@Override
		public long depth()
		{ return h_.size(); }

		@Override
		public long t()
		{ return depth(); }

		@Override
		public int nagents()
		{ return 1; }

		@Override
		public int[] turn()
		{ return new int[] { 0 }; }

		@Override
		public double[] reward()
		{
			if( "LL".equals( s_.s ) || "RL".equals( s_.s ) ) {
				return new double[] { 1.0 };
			}
			else {
				return new double[] { 0.0 };
			}
		}

		@Override
		public boolean isTerminalState( )
		{ return s_.s.length() == 2; }

		@Override
		public long horizon()
		{ return 2 - s_.s.length(); }

		@Override
		public String detailString()
		{ return ""; }
	}
	
	public static class IdentityRepresentation extends FactoredRepresentation<State>
	{
		private final String s_;
		private final double[] phi_;
		
		public IdentityRepresentation( final State s )
		{
			s_ = s.toString();
			
			final String ss = s.s;
			phi_ = new double[8];
			if( "".equals( ss ) ) {
				phi_[0] = 1.0;
			}
			else if( "L".equals( ss ) ) {
				phi_[1] = 1.0;
			}
			else if( "R".equals( ss ) ) {
				phi_[2] = 1.0;
			}
			else if( "LL".equals( ss ) ) {
				phi_[3] = 1.0;
			}
			else if( "LR".equals( ss ) ) {
				phi_[4] = 1.0;
			}
			else if( "RL".equals( ss ) ) {
				phi_[5] = 1.0;
			}
			else if( "RR".equals( ss ) ) {
				phi_[6] = 1.0;
			}
			phi_[7] = s.i;
		}
		
		public static ArrayList<Attribute> attributes()
		{
			final ArrayList<Attribute> attr = new ArrayList<Attribute>();
			attr.add( new Attribute( "--" ) );
			attr.add( new Attribute( "L-" ) );
			attr.add( new Attribute( "R-" ) );
			attr.add( new Attribute( "LL" ) );
			attr.add( new Attribute( "LR" ) );
			attr.add( new Attribute( "RL" ) );
			attr.add( new Attribute( "RR" ) );
			attr.add( new Attribute( "i" ) );
			attr.add( new Attribute( "label" ) );
			return attr;
		}
		
		private IdentityRepresentation( final IdentityRepresentation that )
		{
			s_ = that.s_;
			phi_ = that.phi_;
		}
		
		@Override
		public FactoredRepresentation<State> copy()
		{ return new IdentityRepresentation( this ); }

		@Override
		public boolean equals( final Object obj )
		{
			if( obj == null || !(obj instanceof IdentityRepresentation) ) {
				return false;
			}
			final IdentityRepresentation that = (IdentityRepresentation) obj;
			return s_.equals( that.s_ );
		}

		@Override
		public int hashCode()
		{ return s_.hashCode(); }
		
		@Override
		public String toString()
		{
			return s_;
		}

		@Override
		public double[] phi()
		{
			return phi_;
		}
	}
	
	public static class IdentityRepresenter implements Representer<State, IdentityRepresentation>
	{
		@Override
		public IdentityRepresentation encode( final State s )
		{
			return new IdentityRepresentation( s );
		}

		@Override
		public IdentityRepresenter create()
		{
			return new IdentityRepresenter();
		}
	}
	
	public static class ActionGen extends ActionGenerator<State, JointAction<Action>>
	{
		private final RandomGenerator rng_;
		private final ArrayList<Action> as_ = new ArrayList<Action>();
		private ListIterator<Action> itr_ = null;
		
		public ActionGen( final RandomGenerator rng )
		{
			rng_ = rng;
		}
		
		@Override
		public boolean hasNext()
		{ return itr_.hasNext(); }

		@Override
		public JointAction<Action> next()
		{ return new JointAction<Action>( itr_.next() ); }

		@Override
		public ActionGen create()
		{ return new ActionGen( rng_ ); }

		@Override
		public void setState( final State s, final long t, final int[] turn )
		{
			as_.clear();
			as_.add( new LeftAction( rng_ ) );
			as_.add( new RightAction( rng_ ) );
			itr_ = as_.listIterator();
		}

		@Override
		public int size()
		{ return as_.size(); }
	}
	
	public static class Visitor<X extends Representation<State>, A extends VirtualConstructor<A>>
		extends DefaultMctsVisitor<State, X, A>
	{
		@Override
		public double[] terminal( final State s, final int[] turn )
		{
			if( "LL".equals( s.s ) || "RR".equals( s.s ) ) {
				return new double[] { 1.0 };
			}
			else {
				return new double[] { 0.0 };
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main( final String[] args )
	{
//		final MersenneTwister rng = new MersenneTwister( 42 );
//		final Simulator sim = new Simulator();
//		final double c = 1.0;
//		final ArrayList<Policy<State, UndoableAction<State>>> rollout_policies
//			= new ArrayList<Policy<State, UndoableAction<State>>>();
//		rollout_policies.add(
//			RandomPolicy.create( 0, rng.nextInt(), new ActionGen( rng ) ) );
//		rollout_policies.add(
//			RandomPolicy.create( 1, rng.nextInt(), new ActionGen( rng ) ) );
//
//		final UctSearch<State, Representation<State>, UndoableAction<State>> uct
//			= new UctSearch<State, IdentityRepresenter, UndoableAction<State>>(
//				sim, new IdentityRepresenter(), new ActionGen( rng ),
//				c, rng,	rollout_policies,
//				TimeLimitMctsVisitor.create( new Visitor<UndoableAction<State>>(), new Countdown( 1000 ) ) )
//			{
//				@Override
//				public double[] backup( final StateNode<Representation<State, IdentityRepresenter>, UndoableAction<State>> s )
//				{
//					double max_q = -Double.MAX_VALUE;
//					for( final ActionNode<Representation<State, IdentityRepresenter>, UndoableAction<State>> an : Fn.in( s.successors() ) ) {
//						if( an.q( 0 ) > max_q ) {
//							max_q = an.q( 0 );
//						}
//					}
//					return new double[] { max_q };
//				}
//			};
//		uct.run();
//		uct.printTree( System.out );
//		uct.cluster2ndLevel();
	}

}
