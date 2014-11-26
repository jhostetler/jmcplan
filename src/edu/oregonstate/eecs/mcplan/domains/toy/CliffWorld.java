/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.toy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class CliffWorld
{
	public static enum Path
	{
		Start,
		Road,
		Cliff,
		Dead
	}
	
	public static class State implements edu.oregonstate.eecs.mcplan.State
	{
		public final int L;
		public final int W;
		public final int F;
		
		public Path path = Path.Start;
		public int location = 0;
		public int wind = 0;
		public int slip = 0;
		
		public State( final int L, final int W, final int F )
		{
			this.L = L;
			this.W = W;
			this.F = F;
		}
		
		@Override
		public boolean isTerminal()
		{
			switch( path ) {
			case Start:
				return false;
			case Road:
				return location == 3*L;
			case Cliff:
				return location == L;
			case Dead:
				return true;
			default:
				throw new AssertionError( "unreachable" );
			}
		}
		
		@Override
		public String toString()
		{
			return "path: " + path + ", location: " + location + ", wind: " + wind + ", slip: " + slip;
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static abstract class Action implements UndoableAction<State>, VirtualConstructor<Action>
	{ }
	
	public static class TakeRoadAction extends Action
	{
		private boolean done = false;
		
		@Override
		public void undoAction( final State s )
		{
			assert( done );
			assert( s.path == Path.Road );
			s.path = Path.Start;
			done = false;
		}

		@Override
		public void doAction( final State s )
		{
			assert( !done );
			assert( s.path == Path.Start );
			s.path = Path.Road;
			done = true;
		}

		@Override
		public boolean isDone()
		{ return done; }

		@Override
		public TakeRoadAction create()
		{ return new TakeRoadAction(); }
		
		@Override
		public boolean equals( final Object obj )
		{ return obj instanceof TakeRoadAction; }
		
		@Override
		public int hashCode()
		{ return TakeRoadAction.class.hashCode(); }
		
		@Override
		public String toString()
		{ return "TakeRoadAction"; }
	}
	
	public static class TakeCliffAction extends Action
	{
		private boolean done = false;
		
		@Override
		public void undoAction( final State s )
		{
			assert( done );
			assert( s.path == Path.Cliff );
			s.path = Path.Start;
			done = false;
		}

		@Override
		public void doAction( final State s )
		{
			assert( !done );
			assert( s.path == Path.Start );
			s.path = Path.Cliff;
			done = true;
		}

		@Override
		public boolean isDone()
		{ return done; }

		@Override
		public TakeCliffAction create()
		{ return new TakeCliffAction(); }
		
		@Override
		public boolean equals( final Object obj )
		{ return obj instanceof TakeCliffAction; }
		
		@Override
		public int hashCode()
		{ return TakeCliffAction.class.hashCode(); }
		
		@Override
		public String toString()
		{ return "TakeCliffAction"; }
	}
	
	public static class CautiousAction extends Action
	{
		private boolean done = false;
		private int old_location = -1;
		private int old_slip = 0;
		
		@Override
		public void undoAction( final State s )
		{
			assert( done );
			assert( s.location > 0 );
			s.location = old_location;
			s.slip = old_slip;
			done = false;
		}

		@Override
		public void doAction( final State s )
		{
			assert( !done );
			old_location = s.location;
			old_slip = s.slip;
			
			if( s.slip == 0 ) {
				s.location += 1;
			}
			else {
				s.slip += 2;
			}
			
			done = true;
		}

		@Override
		public boolean isDone()
		{ return done; }

		@Override
		public CautiousAction create()
		{ return new CautiousAction(); }
		
		@Override
		public boolean equals( final Object obj )
		{ return obj instanceof CautiousAction; }
		
		@Override
		public int hashCode()
		{ return CautiousAction.class.hashCode(); }
		
		@Override
		public String toString()
		{ return "CautiousAction"; }
	}
	
	public static class FastAction extends Action
	{
		private boolean done = false;
		private int old_location = -1;
		private int old_slip = 0;
		
		private final RandomGenerator rng;
		
		public FastAction( final RandomGenerator rng )
		{
			this.rng = rng;
		}
		
		@Override
		public void undoAction( final State s )
		{
			assert( done );
			assert( s.location > 0 || s.path == Path.Dead );
			s.location = old_location;
			s.slip = old_slip;
			done = false;
		}

		@Override
		public void doAction( final State s )
		{
			assert( !done );
			old_location = s.location;
			old_slip = s.slip;
			
			if( s.slip == 0 ) {
				s.location += 1;
				
				if( s.path == Path.Cliff ) {
					final double Pslip = s.wind / ((double) s.W);
					if( rng.nextDouble() < Pslip ) {
						s.slip += 1;
					}
				}
			}
			else {
				s.slip += 2;
			}
			
			done = true;
		}

		@Override
		public boolean isDone()
		{ return done; }

		@Override
		public FastAction create()
		{ return new FastAction( rng ); }
		
		@Override
		public boolean equals( final Object obj )
		{ return obj instanceof FastAction; }
		
		@Override
		public int hashCode()
		{ return FastAction.class.hashCode(); }
		
		@Override
		public String toString()
		{ return "FastAction"; }
	}
	
	public static class SteadyAction extends Action
	{
		private boolean done = false;
		private int old_slip = 0;
		
		@Override
		public void undoAction( final State s )
		{
			assert( done );
			s.slip = old_slip;
			done = false;
		}

		@Override
		public void doAction( final State s )
		{
			assert( !done );
			old_slip = s.slip;
			if( s.slip > 0 ) {
				s.slip -= 1;
			}
			if( s.slip > 0 ) {
				s.slip += 2;
			}
			done = true;
		}

		@Override
		public boolean isDone()
		{ return done; }

		@Override
		public SteadyAction create()
		{ return new SteadyAction(); }
		
		@Override
		public boolean equals( final Object obj )
		{ return obj instanceof SteadyAction; }
		
		@Override
		public int hashCode()
		{ return SteadyAction.class.hashCode(); }
		
		@Override
		public String toString()
		{ return "SteadyAction"; }
	}
	
	public static class Actions extends ActionGenerator<State, Action>
	{
		private Path path = Path.Dead;
		private int n = 0;
		
		private final RandomGenerator rng;
		private final int Nactions = 3;
		
		public Actions( final RandomGenerator rng )
		{
			this.rng = rng;
		}
		
		@Override
		public Actions create()
		{ return new Actions( rng ); }

		@Override
		public void setState( final State s, final long t, final int[] turn )
		{
			path = s.path;
			n = 0;
		}

		@Override
		public int size()
		{
			switch( path ) {
			case Dead: return 0;
			case Start: return 2;
			default: return Nactions;
			}
		}

		@Override
		public boolean hasNext()
		{ return n < size(); }

		@Override
		public Action next()
		{
			assert( hasNext() );
			
			final Action a;
			if( path == Path.Start ) {
				if( n == 0 ) {
					a = new TakeRoadAction();
				}
				else {
					a = new TakeCliffAction();
				}
			}
			else {
				if( n == 0 ) {
					a = new FastAction( rng );
				}
				else if( n == 1 ) {
					a = new CautiousAction();
				}
				else {
					a = new SteadyAction();
				}
			}
			
			n += 1;
			return a;
		}
	}
	
	private static class PostDynamicsAction extends Action
	{
		private boolean done = false;
		private int old_wind = -1;
		private Path old_path = Path.Start;
		
		private final int new_wind;
		
		public PostDynamicsAction( final int new_wind )
		{
			this.new_wind = new_wind;
		}
		
		@Override
		public void undoAction( final State s )
		{
			assert( done );
			s.wind = old_wind;
			s.path = old_path;
			done = false;
		}

		@Override
		public void doAction( final State s )
		{
			assert( !done );
			old_wind = s.wind;
			old_path = s.path;
			s.wind = new_wind;
			if( s.slip >= s.F ) {
				s.path = Path.Dead;
			}
			done = true;
		}

		@Override
		public boolean isDone()
		{ return done; }

		@Override
		public Action create()
		{ return new PostDynamicsAction( new_wind ); }
	}
	
	// -----------------------------------------------------------------------
	
	public static class Simulator implements UndoSimulator<State, Action>
	{
		private final State s;
		
		private final RandomGenerator rng;
		private final Deque<Action> action_history = new ArrayDeque<Action>();
		private final Deque<Action> postdynamics_history = new ArrayDeque<Action>();
		
		public Simulator( final State s, final RandomGenerator rng )
		{
			this.s = s;
			this.rng = rng;
		}
		
		@Override
		public State state()
		{ return s; }

		@Override
		public void takeAction( final JointAction<Action> a )
		{
			final Action ai = a.get( 0 );
			ai.doAction( s );
			action_history.push( ai );
			
			final PostDynamicsAction post = new PostDynamicsAction( rng.nextInt( s.W ) );
			post.doAction( s );
			postdynamics_history.push( post );
		}

		@Override
		public long depth()
		{ return action_history.size() + postdynamics_history.size(); }

		@Override
		public long t()
		{ return action_history.size(); }

		@Override
		public int nagents()
		{ return 1; }

		@Override
		public int[] turn()
		{ return new int[] { 0 }; }

		@Override
		public double[] reward()
		{
//			if( s.isTerminal() ) {
//				if( s.path == Path.Dead ) {
//					return new double[] { -100 };
//				}
//				else {
//					return new double[] { 0 };
//				}
//			}
			
			if( s.path == Path.Dead ) {
				return new double[] { -30 };
			}
			
			final Action a = action_history.peek();
			if( a instanceof CautiousAction ) {
				return new double[] { -2 };
			}
			else if( a instanceof SteadyAction ) {
				return new double[] { -2.5 };
			}
			else {
				return new double[] { -1 };
			}
		}

		@Override
		public boolean isTerminalState()
		{ return s.isTerminal(); }

		@Override
		public long horizon()
		{ return Long.MAX_VALUE; }

		@Override
		public String detailString()
		{ return "CliffWorldSimulator"; }

		@Override
		public void untakeLastAction()
		{
			final Action post = postdynamics_history.pop();
			post.undoAction( s );
			
			final Action a = action_history.pop();
			a.undoAction( s );
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static class PrimitiveRepresentation extends FactoredRepresentation<State>
	{
		private final double[] phi;
		
		public PrimitiveRepresentation( final State s )
		{
			phi = new double[4];
			int idx = 0;
			phi[idx++] = s.path.ordinal();
			phi[idx++] = s.location;
			phi[idx++] = s.wind;
			phi[idx++] = s.slip;
		}
		
		private PrimitiveRepresentation( final double[] phi )
		{ this.phi = phi; }
		
		@Override
		public double[] phi()
		{ return phi; }

		@Override
		public FactoredRepresentation<State> copy()
		{ return new PrimitiveRepresentation( phi ); }

		@Override
		public boolean equals( final Object obj )
		{
			if( !(obj instanceof PrimitiveRepresentation) ) {
				return false;
			}
			final PrimitiveRepresentation that = (PrimitiveRepresentation) obj;
			return Arrays.equals( phi, that.phi );
		}

		@Override
		public int hashCode()
		{ return Arrays.hashCode( phi ); }
		
		@Override
		public String toString()
		{ return Arrays.toString( phi ); }
	}
	
	public static class PrimitiveRepresenter implements FactoredRepresenter<State, FactoredRepresentation<State>>
	{
		private static ArrayList<Attribute> attributes;
		static {
			attributes = new ArrayList<Attribute>();
			attributes.add( new Attribute( "path" ) );
			attributes.add( new Attribute( "location" ) );
			attributes.add( new Attribute( "wind" ) );
			attributes.add( new Attribute( "slipping" ) );
		}
		
		@Override
		public FactoredRepresenter<State, FactoredRepresentation<State>> create()
		{ return new PrimitiveRepresenter(); }

		@Override
		public PrimitiveRepresentation encode( final State s )
		{ return new PrimitiveRepresentation( s ); }

		@Override
		public ArrayList<Attribute> attributes()
		{
			return attributes;
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] argv ) throws NumberFormatException, IOException
	{
		final int L = 7;
		final int W = 4;
		final int F = 5;
		final State s = new State( L, W, F );
		final RandomGenerator rng = new MersenneTwister( 42 );
		final Simulator sim = new Simulator( s, rng );
		
		final Actions actions = new Actions( rng );
		
		while( !sim.state().isTerminal() ) {
			System.out.println( s );
			actions.setState( sim.state(), sim.t(), sim.turn() );
			final ArrayList<Action> action_list = Fn.takeAll( actions );
			for( int i = 0; i < action_list.size(); ++i ) {
				System.out.println( i + ": " + action_list.get( i ) );
			}
			System.out.print( ">>> " );
			final BufferedReader cin = new BufferedReader( new InputStreamReader( System.in ) );
			final int choice = Integer.parseInt( cin.readLine() );
			final Action a = action_list.get( choice );
			sim.takeAction( new JointAction<Action>( a ) );
			System.out.println( "Reward: " + Arrays.toString( sim.reward() ) );
		}
	}
}
