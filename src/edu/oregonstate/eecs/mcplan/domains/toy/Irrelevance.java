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
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;

/**
 * This is a very simple "irrelevant state variable" test case. It has horizon
 * 2, and two actions. The state contains the action history and also an
 * integer i that is set randomly by every action. The rewards do not depend
 * on i, so the idea is to discover that i is irrelevant.
 */
public class Irrelevance
{
	public final int Ns;
	public final double slip;
	
	public Irrelevance( final int Ns, final double slip )
	{
		this.Ns = Ns;
		this.slip = slip;
	}
	
	public class State implements edu.oregonstate.eecs.mcplan.State
	{
		public String s = "";
		public int i = 0;
		
		@Override
		public String toString()
		{
			return "(" + s + ", " + i + ")";
		}

		@Override
		public boolean isTerminal()
		{
			return s.length() == 2;
		}

		@Override
		public void close()
		{ }
	}
	
	public static abstract class Action extends UndoableAction<State> implements VirtualConstructor<Action>
	{
		@Override
		public abstract Action create();
	}
	
	public class LeftAction extends Action
	{
		private boolean done_ = false;
		private int i_old_ = 0;

		@Override
		public void doAction( final RandomGenerator rng, final State s )
		{
			i_old_ = s.i;
			final UniformIntegerDistribution u = new UniformIntegerDistribution( rng, 0, Ns - 1 );
			s.i = u.sample();
			if( rng.nextDouble() > slip ) {
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
		{ return new LeftAction(); }
		
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
	
	public class RightAction extends Action
	{
		private boolean done_ = false;
		private int i_old_ = 0;
		
		@Override
		public void doAction( final RandomGenerator rng, final State s )
		{
			i_old_ = s.i;
			final UniformIntegerDistribution u = new UniformIntegerDistribution( rng, 0, Ns - 1 );
			s.i = u.sample();
			if( rng.nextDouble() > slip ) {
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
		{ return new RightAction(); }
		
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
	
	public class Simulator implements UndoSimulator<State, Action>
	{
		private final State s_;
		private final Deque<Action> h_ = new ArrayDeque<Action>();
		
		public Simulator( final Irrelevance.State s )
		{
			s_ = s;
		}

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
			if( "LL".equals( s_.s ) || "RR".equals( s_.s ) ) {
				return new double[] { 1.0 };
			}
			else {
				return new double[] { 0.0 };
			}
		}

		@Override
		public boolean isTerminalState( )
		{ return s_.isTerminal(); }

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
		private final float[] phi_;
		
		public IdentityRepresentation( final State s )
		{
			s_ = s.toString();
			
			final String ss = s.s;
			phi_ = new float[8];
			if( "".equals( ss ) ) {
				phi_[0] = 1.0f;
			}
			else if( "L".equals( ss ) ) {
				phi_[1] = 1.0f;
			}
			else if( "R".equals( ss ) ) {
				phi_[2] = 1.0f;
			}
			else if( "LL".equals( ss ) ) {
				phi_[3] = 1.0f;
			}
			else if( "LR".equals( ss ) ) {
				phi_[4] = 1.0f;
			}
			else if( "RL".equals( ss ) ) {
				phi_[5] = 1.0f;
			}
			else if( "RR".equals( ss ) ) {
				phi_[6] = 1.0f;
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
//			attr.add( new Attribute( "label" ) );
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
		public float[] phi()
		{
			return phi_;
		}
	}
	
	public static class IdentityRepresenter implements FactoredRepresenter<State, FactoredRepresentation<State>>
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

		@Override
		public ArrayList<Attribute> attributes()
		{
			return IdentityRepresentation.attributes();
		}
		
		@Override
		public String toString()
		{ return "Irrelevance.IdentityRepresenter"; }
	}
	
	public class ActionGen extends ActionGenerator<State, Action>
	{
		private final ArrayList<Action> as_ = new ArrayList<Action>();
		private ListIterator<Action> itr_ = null;
		
		@Override
		public boolean hasNext()
		{ return itr_.hasNext(); }

		@Override
		public Action next()
		{ return itr_.next(); }

		@Override
		public ActionGen create()
		{ return new ActionGen(); }

		@Override
		public void setState( final State s, final long t )
		{
			as_.clear();
			as_.add( new LeftAction() );
			as_.add( new RightAction() );
			itr_ = as_.listIterator();
		}

		@Override
		public int size()
		{ return as_.size(); }
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
