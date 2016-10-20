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
import java.util.Arrays;
import java.util.Deque;
import java.util.ListIterator;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.JointPolicy;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.abstraction.ClusterAbstraction;
import edu.oregonstate.eecs.mcplan.sim.Episode;
import edu.oregonstate.eecs.mcplan.sim.ResetAdapter;
import edu.oregonstate.eecs.mcplan.sim.RewardAccumulator;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * @author jhostetler
 *
 */
public class ChainWalk
{
	public final int Ns;
	public final double slip;
	public final int Ni;
	
	public ChainWalk( final int Ns, final double slip, final int Ni )
	{
		this.Ns = Ns;
		this.slip = slip;
		this.Ni = Ni;
	}
	
	public class State implements edu.oregonstate.eecs.mcplan.State
	{
		public int x = 0;
		public int i = 0;
		
		@Override
		public String toString()
		{
			return "(" + Integer.toString( x ) + ", " + Integer.toString( i ) + ")";
		}

		@Override
		public boolean isTerminal()
		{
			return Math.abs( x ) == Ns;
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
		private int x_old_ = 0;
		private int i_old_ = 0;

		@Override
		public void doAction( final RandomGenerator rng, final State s )
		{
			x_old_ = s.x;
			i_old_ = s.i;
			if( rng.nextDouble() > slip ) {
				s.x -= 1;
			}
			else {
				s.x += 1;
			}
			s.i = rng.nextInt( Ni );
			done_ = true;
		}

		@Override
		public boolean isDone()
		{ return done_; }

		@Override
		public void undoAction( final State s )
		{
			s.x = x_old_;
			s.i = i_old_;
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
		private int x_old_ = 0;
		private int i_old_ = 0;

		@Override
		public void doAction( final RandomGenerator rng, final State s )
		{
			x_old_ = s.x;
			i_old_ = s.i;
			if( rng.nextDouble() > slip ) {
				s.x += 1;
			}
			else {
				s.x -= 1;
			}
			s.i = rng.nextInt( Ni );
			done_ = true;
		}

		@Override
		public boolean isDone()
		{ return done_; }

		@Override
		public void undoAction( final State s )
		{
			s.x = x_old_;
			s.i = i_old_;
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
		private final RandomGenerator rng_;
		private final State s_;
		private final Deque<Action> h_ = new ArrayDeque<Action>();
		
		public Simulator( final RandomGenerator rng, final State s )
		{
			rng_ = rng;
			s_ = s;
		}
		
		@Override
		public State state()
		{ return s_; }

		@Override
		public void takeAction( final JointAction<Action> j )
		{
			final Action a = j.get( 0 );
			a.doAction( rng_, s_ );
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
			if( s_.isTerminal() ) {
				return new double[] { 0.0 }; // { 2*Ns };
			}
			else {
				return new double[] { -1.0 };
			}
		}

		@Override
		public boolean isTerminalState( )
		{ return s_.isTerminal(); }

		@Override
		public long horizon()
		{ return Long.MAX_VALUE; }

		@Override
		public String detailString()
		{ return ""; }
	}
	
	public static class IdentityRepresentation extends FactoredRepresentation<State>
	{
		private final float[] phi_;
		
		public IdentityRepresentation( final State s )
		{
			phi_ = new float[] { s.x, s.i };
		}
		
		public static ArrayList<Attribute> attributes()
		{
			final ArrayList<Attribute> attr = new ArrayList<Attribute>();
			attr.add( new Attribute( "x" ) );
			attr.add( new Attribute( "i" ) );
			return attr;
		}
		
		private IdentityRepresentation( final IdentityRepresentation that )
		{
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
			return Arrays.equals( phi_, that.phi_ );
		}

		@Override
		public int hashCode()
		{ return 3 + 5*Arrays.hashCode( phi_ ); }
		
		@Override
		public String toString()
		{
			return Arrays.toString( phi_ );
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
		{ return "ChainWalk.IdentityRepresenter"; }
	}
	
	public static class PiStarRepresenter implements FactoredRepresenter<State, FactoredRepresentation<State>>
	{
		private final int neg_idx_ = -1;
		
		@Override
		public PiStarRepresenter create()
		{
			return new PiStarRepresenter();
		}

		@Override
		public FactoredRepresentation<State> encode( final State s )
		{
			if( s.isTerminal() ) {
				return new ClusterAbstraction<State>( -1 );
			}
			else if( s.x <= 0 ) {
				return new ClusterAbstraction<State>( 0 );
			}
			else {
				return new ClusterAbstraction<State>( 1 );
			}
		}

		@Override
		public ArrayList<Attribute> attributes()
		{
			return ClusterAbstraction.attributes();
		}
	}
	
	public class ActionGen extends ActionGenerator<State, Action>
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
		public Action next()
		{ return itr_.next(); }

		@Override
		public ActionGen create()
		{ return new ActionGen( rng_ ); }

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
	
	public class OptimalPolicy extends Policy<State, Action>
	{
		private State s_ = null;
		
		@Override
		public void setState( final State s, final long t )
		{
			s_ = s;
		}

		@Override
		public Action getAction()
		{
			if( s_.x <= 0 ) {
				return new LeftAction();
			}
			else {
				return new RightAction();
			}
		}

		@Override
		public void actionResult( final State sprime, final double[] r )
		{ }

		@Override
		public String getName()
		{
			return "ChainWalk.OptimalPolicy";
		}

		@Override
		public int hashCode()
		{
			return 7;
		}

		@Override
		public boolean equals( final Object obj )
		{
			return obj != null && obj instanceof ChainWalk.OptimalPolicy;
		}
	}
	
	/**
	 * @param args
	 */
	public static void main( final String[] args )
	{
		final MersenneTwister rng = new MersenneTwister( 42 );
		final ChainWalk domain = new ChainWalk( 4, 0.1, 100 );
		final Simulator sim = domain.new Simulator( rng, domain.new State() );
		final ResetAdapter<State, Action> resetable = new ResetAdapter<State, Action>( sim );
		
		final MeanVarianceAccumulator vbar = new MeanVarianceAccumulator();
		for( int i = 0; i < 100000; ++i ) {
			final Episode<State, Action> episode = new Episode<State, Action>(
				resetable, new JointPolicy<State, Action>( domain.new OptimalPolicy() ) );
			final RewardAccumulator<State, Action> racc = new RewardAccumulator<State, Action>( 1, 1.0 );
			episode.addListener( racc );
			episode.run();
			vbar.add( racc.v()[0] );
			resetable.reset();
		}
		System.out.println( vbar.mean() );
		System.out.println( vbar.variance() );
		
//		final Policy<State, JointAction<Action>> rollout_policy
//			= new RandomPolicy<State, JointAction<Action>>(
//					0 /*Player*/, rng.nextInt(), domain.new ActionGen( rng ) );
//		while( !sim.state().isTerminal() ) {
//			System.out.print( sim.state() );
//			rollout_policy.setState( sim.state(), sim.t() );
//			final JointAction<Action> a = rollout_policy.getAction();
//			System.out.print( " -- " );
//			System.out.print( a );
//			System.out.print( " -> " );
//			sim.takeAction( a );
//			System.out.print( sim.state() );
//			System.out.print( " " );
//			System.out.print( Arrays.toString( sim.reward() ) );
//			System.out.println();
//		}
	}
}
