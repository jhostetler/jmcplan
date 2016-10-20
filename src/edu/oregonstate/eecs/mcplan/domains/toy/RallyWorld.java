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
public class RallyWorld
{
	public static class Parameters
	{
		public final double breakdown_reward = -30;
		
		public final RandomGenerator rng;
		public final double dmg_slow;
		public final double dmg_fast;
		public final double pfault;
		public final int Nfaults;
		public final double pbreak;
		public final int W;
		public final int Nreckless;
		
		public Parameters( final RandomGenerator rng,
						   final double dmg_slow, final double dmg_fast, final double pfault,
						   final int Nfaults, final double pbreak, final int W,
						   final int Nreckless )
		{
			this.rng = rng;
			this.dmg_slow = dmg_slow;
			this.dmg_fast = dmg_fast;
			this.pfault = pfault;
			this.Nfaults = Nfaults;
			this.pbreak = pbreak;
			this.W = W;
			this.Nreckless = Nreckless;
		}
		
		public Parameters( final RandomGenerator rng, final KeyValueStore config )
		{
			this.rng = rng;
			this.dmg_slow = 0.1;
			this.dmg_fast = 0.9;
			this.pfault = config.getDouble( "rally.pfault" );
			this.Nfaults = config.getInt( "rally.Nfaults" );
			this.pbreak = config.getDouble( "rally.pbreak" );
			this.W = config.getInt( "rally.W" );
			this.Nreckless = 2;
		}
	}
	
	public static class State implements edu.oregonstate.eecs.mcplan.State
	{
		public final Parameters params;
		
		public int w = 0;
		public boolean damage = false;
		public int fault = 0;
		public boolean failure = false;
		public boolean breakdown = false;
		public int reckless = 0;
		
		public State( final Parameters params )
		{
			this.params = params;
		}
		
		public State( final State that )
		{
			this.params = that.params;
			
			this.w = that.w;
			this.damage = that.damage;
			this.fault = that.fault;
			this.failure = that.failure;
			this.breakdown = that.breakdown;
			this.reckless = that.reckless;
		}
		
		@Override
		public boolean isTerminal()
		{
			return false;
		}
		
		@Override
		public String toString()
		{
			return "w: " + w + ", damage: " + damage + ", fault: " + fault
					+ ", failure: " + failure + ", breakdown: " + breakdown + ", reckless: " + reckless;
		}
		
		private double weatherRisk()
		{
			return w / ((double) params.W);
		}
		
		public boolean sampleSlowDamage()
		{
			final double r = params.dmg_slow * weatherRisk();
			return params.rng.nextDouble() < r;
		}
		
		public boolean sampleFastDamage()
		{
			final double r = params.dmg_fast * weatherRisk();
			return params.rng.nextDouble() < r;
		}
		
		public boolean sampleFaultOccurence()
		{
			return params.rng.nextDouble() < params.pfault;
		}
		
		public int sampleFault()
		{
			return 1 + params.rng.nextInt( params.Nfaults );
		}
		
		public boolean sampleBreakdown()
		{
			return params.rng.nextDouble() < params.pbreak;
		}
		
		public int sampleWeather()
		{
			return params.rng.nextInt( params.W );
		}

		@Override
		public void close()
		{ }
	}
	
	// -----------------------------------------------------------------------
	
	public static abstract class Action extends UndoableAction<State> implements VirtualConstructor<Action>
	{
		public abstract double reward();
	}
	
	public static class SlowAction extends Action
	{
		public static final double reward = -2;
		
		@Override
		public void undoAction( final State s )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void doAction( final RandomGenerator rng, final State s )
		{
			s.damage = s.damage || s.sampleSlowDamage();
		}

		@Override
		public boolean isDone()
		{ return false; }

		@Override
		public SlowAction create()
		{ return new SlowAction(); }
		
		@Override
		public boolean equals( final Object obj )
		{ return obj instanceof SlowAction; }
		
		@Override
		public int hashCode()
		{ return SlowAction.class.hashCode(); }
		
		@Override
		public String toString()
		{ return "SlowAction"; }
		
		@Override
		public double reward()
		{ return SlowAction.reward; }
	}
	
	public static class FastAction extends Action
	{
		public static final double reward = -1;
		
		@Override
		public void undoAction( final State s )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void doAction( final RandomGenerator rng, final State s )
		{
			s.damage = s.damage || s.sampleFastDamage();
		}

		@Override
		public boolean isDone()
		{ return false; }

		@Override
		public FastAction create()
		{ return new FastAction(); }
		
		@Override
		public boolean equals( final Object obj )
		{ return obj instanceof FastAction; }
		
		@Override
		public int hashCode()
		{ return FastAction.class.hashCode(); }
		
		@Override
		public String toString()
		{ return "FastAction"; }
		
		@Override
		public double reward()
		{ return FastAction.reward; }
	}
	
	public static class RecklessAction extends Action
	{
		public static final double reward = -0.5;
		
		@Override
		public void undoAction( final State s )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void doAction( final RandomGenerator rng, final State s )
		{
			if( s.reckless == 0 ) {
				// +1 since it will be decremented immediate in PostDynamics
				s.reckless = s.params.Nreckless + 1;
			}
		}

		@Override
		public boolean isDone()
		{ return false; }

		@Override
		public RecklessAction create()
		{ return new RecklessAction(); }
		
		@Override
		public boolean equals( final Object obj )
		{ return obj instanceof RecklessAction; }
		
		@Override
		public int hashCode()
		{ return RecklessAction.class.hashCode(); }
		
		@Override
		public String toString()
		{ return "RecklessAction"; }
		
		@Override
		public double reward()
		{ return RecklessAction.reward; }
	}
	
	public static class RepairAction extends Action
	{
		/**
		 * We want this value to be such that
		 * 1 + P(repair) * dV(repair) < 2.
		 * 
		 * The LHS is the cost of Fast + Repair if necessary, and the RHS is
		 * the cost of slow. We have
		 * 	P(repair) = w/W
		 * and
		 * 	dV(repair) = [R(fast) - R(repair)] * E[ discount^(tfault) ]
		 * since we are replacing a Fast action (in the best case) with a
		 * Repair action.
		 * 
		 * For discount = 0.9 and pfault = 1/2,
		 * 	dV(repair) = [R(fast) - R(repair)] * 0.9^(1 / 1/2) = [R(fast) - R(repair)] * 0.81
		 * 
		 * When w = W/2, this gives
		 * 1 + P(repair)V(repair) < 2
		 * => 1 + [R(fast) - R(repair)](1/2)(0.81) < 2
		 * => 0.81[R(fast) - R(repair)] < 2
		 * => [R(fast) - R(repair)] < 2.46
		 * 
		 * So R(repair) should be about -3.5. This value makes it optimal to
		 * go Fast when the weather is below W/2.
		 */
		public static final double reward = -3.5;
		
		public final int i;
		
		public RepairAction( final int i )
		{
			this.i = i;
		}
		
		@Override
		public void undoAction( final State s )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void doAction( final RandomGenerator rng, final State s )
		{
			if( s.fault == i && !s.failure ) {
				s.fault = 0;
				s.damage = false;
			}
		}

		@Override
		public boolean isDone()
		{ return false; }

		@Override
		public RepairAction create()
		{ return new RepairAction( i ); }
		
		@Override
		public boolean equals( final Object obj )
		{
			if( !(obj instanceof RepairAction) ) {
				return false;
			}
			final RepairAction that = (RepairAction) obj;
			return this.i == that.i;
		}
		
		@Override
		public int hashCode()
		{ return 5 * (3 + RepairAction.class.hashCode() * (7 + i)); }
		
		@Override
		public String toString()
		{ return "RepairAction(" + i + ")"; }
		
		@Override
		public double reward()
		{ return RepairAction.reward; }
	}
	
	public static class Actions extends ActionGenerator<State, Action>
	{
		public static int actionSetIndex( final State s )
		{
			return 0;
		}
		
		private int n = 0;
		
		private final Parameters params;
		private final int Nactions;
		private final int Nnon_fault = 3;
		
		public Actions( final Parameters params )
		{
			this.params = params;
			this.Nactions = Nnon_fault + params.Nfaults;
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
			return Nactions;
		}

		@Override
		public boolean hasNext()
		{ return n < size(); }

		@Override
		public Action next()
		{
			assert( hasNext() );
			
			final Action a;
			switch( n ) {
			case 0: a = new SlowAction(); break;
			case 1: a = new FastAction(); break;
			case 2: a = new RecklessAction(); break;
			default:
				a = new RepairAction( n - Nnon_fault + 1 );
				break;
			}
			
			n += 1;
			return a;
		}
	}
	
	public static void applyPreDynamics( final State s )
	{
		if( s.breakdown ) {
			s.breakdown = false;
			s.failure = false;
			s.fault = 0;
			s.damage = false;
			s.reckless = 0;
		}
	}
	
	public static void applyPostDynamics( final State s )
	{
		if( s.reckless > 0 ) {
			s.reckless -= 1;
			if( s.reckless == 0 ) {
				s.breakdown = true;
			}
		}
		else if( s.failure ) {
			if( s.sampleBreakdown() ) {
				s.breakdown = true;
			}
		}
		else if( s.fault != 0 ) {
			s.failure = true;
		}
		else if( s.damage ) {
			if( s.sampleFaultOccurence() ) {
				s.fault = s.sampleFault();
			}
		}
		
		s.w = s.sampleWeather();
	}
	
	// -----------------------------------------------------------------------
	
	public static class FsssModel extends edu.oregonstate.eecs.mcplan.search.fsss.FsssModel<State, Action>
	{
		private final Parameters params;
		private final double Vmin;
		private final double Vmax;
		
		private final PrimitiveRepresenter base_repr = new PrimitiveRepresenter();
		private final ActionSetRepresenter action_repr = new ActionSetRepresenter();
		
		private int sample_count = 0;
		
		public FsssModel( final Parameters params )
		{
			this.params = params;
			Vmin = calculateVmin();
			Vmax = calculateVmax();
		}
		
		@Override
		public edu.oregonstate.eecs.mcplan.search.fsss.FsssModel<State, Action> create(
				final RandomGenerator rng )
		{
			return new FsssModel( params );
		}
		
		@Override
		public RandomGenerator rng()
		{
			return params.rng;
		}
		
		private double calculateVmin()
		{
			// Divide breakdown_reward by 2 since we can only break down
			// every second step in the worst case.
			return (1.0 / (1.0 - discount())) * (FastAction.reward + params.breakdown_reward/2.0);
		}
		
		private double calculateVmax()
		{
			return (1.0 / (1.0 - discount())) * RecklessAction.reward;
		}
		
		@Override
		public double Vmin( final State s )
		{ return Vmin; }

		@Override
		public double Vmax( final State s )
		{ return Vmax; }
		
		@Override
		public double Vmin( final State s, final Action a )
		{
			return reward( s, a ) + discount() * Vmin;
		}

		@Override
		public double Vmax( final State s, final Action a )
		{
			return reward( s, a ) + discount() * Vmax;
		}
		
		@Override
		public double heuristic( final State s )
		{ return Vmax; }

		@Override
		public double discount()
		{ return 0.9; }

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
			s0.w = s0.sampleWeather();
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
			a.create().doAction( copy );
			applyPostDynamics( copy );
			
			return copy;
		}

		@Override
		public double reward( final State s )
		{
			if( s.breakdown ) {
				return s.params.breakdown_reward;
			}
			else {
				return 0;
			}
		}

		@Override
		public double reward( final State s, final Action a )
		{
			return a.reward();
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
			attributes.add( new Attribute( "w" ) );
			attributes.add( new Attribute( "damage" ) );
			attributes.add( new Attribute( "fault" ) );
			attributes.add( new Attribute( "failure" ) );
			attributes.add( new Attribute( "breakdown" ) );
		}
		
		@Override
		public FactoredRepresenter<State, FactoredRepresentation<State>> create()
		{ return new PrimitiveRepresenter(); }

		@Override
		public FactoredRepresentation<State> encode( final State s )
		{
			final float[] phi = new float[5];
			int idx = 0;
			phi[idx++] = s.w;
			phi[idx++] = s.damage ? 1 : 0;
			phi[idx++] = s.fault;
			phi[idx++] = s.failure ? 1 : 0;
			phi[idx++] = s.breakdown ? 1 : 0;
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
		final double dmg_slow = 0.1;
		final double dmg_fast = 0.9;
		final double pfault = 1.0;
		final int Nfaults = 2;
		final double pbreak = 1.0;
		final int W = 5;
		final int Nreckless = 3;
		
		final Parameters params = new Parameters( rng, dmg_slow, dmg_fast, pfault, Nfaults, pbreak, W, Nreckless );
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
	}
}