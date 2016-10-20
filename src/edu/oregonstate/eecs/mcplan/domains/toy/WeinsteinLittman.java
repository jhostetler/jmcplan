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
 * This is the counterexample domain from
 * 	@inproceedings{weinstein2012bandit,
 * 		title={Bandit-Based Planning and Learning in Continuous-Action Markov Decision Processes},
 * 		author={Weinstein, Ari and Littman, Michael L},
 * 		booktitle={Proceedings of the International Conference on Automated Planning and Scheduling ({ICAPS})},
 * 		year={2012}
 * 	}
 * designed so that open-loop planning is not optimal.
 * 
 * Note: There is an error in the domain as described in the paper. Only
 * *one* of s5 and s6 should have r = 2, and the other should have r = -2. I
 * have chosen arbitrarily to make *s6* have r = 2, because it kind of looks
 * like there's an extra space in "r= 2" for s5, suggesting a missing minus
 * sign.
 * 
 * @author jhostetler
 *
 */
public class WeinsteinLittman
{
	public static class Parameters
	{
		public final int nirrelevant;

		public Parameters( final int nirrelevant )
		{
			this.nirrelevant = nirrelevant;
		}
		
		public Parameters( final KeyValueStore config )
		{
			this.nirrelevant = config.getInt( "weinstein_littman.nirrelevant" );
		}
	}
	
	public static class State implements edu.oregonstate.eecs.mcplan.State
	{
		public final Parameters params;
		private final RandomGenerator rng;
		
		public int i = 0;
		
		public int irrelevant = 0;
		
		public State( final RandomGenerator rng, final Parameters params )
		{
			this.params = params;
			this.rng = rng;
		}
		
		public State( final State that )
		{
			this.params = that.params;
			this.rng = that.rng;
			this.i = that.i;
			this.irrelevant = that.irrelevant;
		}
		
		@Override
		public void close()
		{ }
		
		@Override
		public boolean isTerminal()
		{
			return i == 4 || i == 5 || i == 6;
		}
		
		@Override
		public String toString()
		{
			return "{i: " + i + ", irr: " + irrelevant + "}";
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static class Action extends UndoableAction<State> implements VirtualConstructor<Action>
	{
		public final int i;
		
		public Action( final int i )
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
			switch( s.i ) {
			case 0:
				if( this.i == 0 ) {
					s.i = 1;
				}
				else {
					if( s.rng.nextBoolean() ) {
						s.i = 2;
					}
					else {
						s.i = 3;
					}
				}
				break;
			case 1:
				s.i = 4;
				break;
			case 2:
				if( this.i == 0 ) {
					s.i = 5;
				}
				else {
					s.i = 6;
				}
				break;
			case 3:
				if( this.i == 0 ) {
					s.i = 6;
				}
				else {
					s.i = 5;
				}
				break;
			default:
				throw new IllegalStateException( "Acting in state " + s );
			}
			
			s.irrelevant = s.rng.nextInt( s.params.nirrelevant );
		}

		@Override
		public boolean isDone()
		{ return false; }

		@Override
		public Action create()
		{ return new Action( i ); }
		
		@Override
		public boolean equals( final Object obj )
		{
			if( !(obj instanceof Action) ) {
				return false;
			}
			final Action that = (Action) obj;
			return i == that.i;
		}
		
		@Override
		public int hashCode()
		{ return Action.class.hashCode() ^ i; }
		
		@Override
		public String toString()
		{ return "Action[" + i + "]"; }
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
			return 2;
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
		{ return -2; }

		@Override
		public double Vmax( final State s )
		{ return 2; }
		
		@Override
		public double Vmin( final State s, final Action a )
		{ return -2; }

		@Override
		public double Vmax( final State s, final Action a )
		{ return 2; }
		
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
			final State s0 = new State( rng, params );
			s0.irrelevant = rng.nextInt( params.nirrelevant );
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
			a.create().doAction( rng, copy );
			
			return copy;
		}

		/**
		 * Note: See comments in WeinsteinLittman class docs.
		 * @see edu.oregonstate.eecs.mcplan.search.fsss.FsssModel#reward(edu.oregonstate.eecs.mcplan.State)
		 */
		@Override
		public double reward( final State s )
		{
			if( s.i == 4 ) {
				return 1;
			}
			else if( s.i == 5 ) {
				return -2;
			}
			else if( s.i == 6 ) {
				return 2;
			}
			else {
				return 0;
			}
		}

		@Override
		public double reward( final State s, final Action a )
		{
			return 0;
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
			attributes.add( new Attribute( "i" ) );
			attributes.add( new Attribute( "irrelevant" ) );
		}
		
		@Override
		public FactoredRepresenter<State, FactoredRepresentation<State>> create()
		{ return new PrimitiveRepresenter(); }

		@Override
		public FactoredRepresentation<State> encode( final State s )
		{
			final float[] phi = new float[attributes.size()];
			int idx = 0;
			phi[idx++] = s.i;
			phi[idx++] = s.irrelevant;
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
			if( s.isTerminal() ) {
				return new IndexRepresentation<>( 0 );
			}
			else {
				return new IndexRepresentation<>( 1 );
			}
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] argv ) throws NumberFormatException, IOException
	{
		final RandomGenerator rng = new MersenneTwister( 42 );
		final int nirrelevant = 2;
		
		while( true ) {
			final Parameters params = new Parameters( nirrelevant );
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
			System.out.println( "Terminal: " + s );
			System.out.println( "R(s): " + model.reward( s ) );
			System.out.println( "********************" );
		}
	}
}