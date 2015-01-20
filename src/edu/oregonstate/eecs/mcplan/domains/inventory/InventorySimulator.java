/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.inventory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.sim.UndoSimulator;
import edu.oregonstate.eecs.mcplan.util.Fn;
import gnu.trove.stack.TDoubleStack;
import gnu.trove.stack.array.TDoubleArrayStack;

/**
 * @author jhostetler
 *
 */
public class InventorySimulator implements UndoSimulator<InventoryState, InventoryAction>
{
	private static final class DynamicsAction extends InventoryAction
	{
		private double old_r = 0;
		private boolean done = false;
		
//		private int[] supplied = null;
//		private int[] arrivals = null;
		private int[] old_inventory = null;
		private int[] old_orders = null;
		private int[] old_demand = null;
		
		@Override
		public DynamicsAction create()
		{
			return new DynamicsAction();
		}
		
		@Override
		public double reward()
		{
			return 0;
		}
		
		@Override
		public void undoAction( final InventoryState s )
		{
			assert( done );
			s.r = old_r;
			Fn.memcpy( s.inventory, old_inventory );
			Fn.memcpy( s.orders, old_orders );
			Fn.memcpy( s.demand, old_demand );
			
			old_r = 0;
			old_inventory = old_orders = old_demand = null;
		}

		@Override
		public void doAction( final InventoryState s )
		{
			assert( !done );
			
//			supplied = new int[s.problem.Nproducts];
//			arrivals = new int[s.problem.Nproducts];
			
			old_inventory = Fn.copy( s.inventory );
			old_orders = Fn.copy( s.orders );
			old_demand = Fn.copy( s.demand );
			old_r = s.r;
			
			applyDynamics( s );
			
			done = true;
		}

		@Override
		public boolean isDone()
		{
			return done;
		}
	}
	
	public static void applyDynamics( final InventoryState s )
	{
		s.r = 0;
		for( int i = 0; i < s.problem.Nproducts; ++i ) {
			// Pending orders arrive
			final BinomialDistribution f = new BinomialDistribution(
				s.orders[i], s.problem.delivery_probability );
			final int arrivals = f.sample();
//			System.out.println( "\tarrivals[" + i + "] = " + arrivals );
			s.orders[i] -= arrivals;
			s.inventory[i] += arrivals;
			
			// Demand is satisfied
			final int supplied = Math.min( s.demand[i], s.inventory[i] );
			s.inventory[i] -= supplied;
			s.r += s.problem.price[i] * supplied;
			
			// Excess inventory is wasted
			s.inventory[i] = Math.min( s.inventory[i], s.problem.max_inventory );
			
			// Warehouse cost
			s.r -= s.inventory[i] * s.problem.warehouse_cost;
			
			// Demand changes
			s.demand[i] = s.rng.nextInt( s.problem.max_demand + 1 );
		}
	}
	
	private final InventoryState s;
	
	private final Deque<InventoryAction> history = new ArrayDeque<InventoryAction>();
	private final TDoubleStack rewards = new TDoubleArrayStack();
	private final int Nevents = 2;
	
	public InventorySimulator( final InventoryState s )
	{
		this.s = s;
		rewards.push( 0 );
	}
	
	@Override
	public InventoryState state()
	{
		return s;
	}

	@Override
	public void takeAction( final JointAction<InventoryAction> a )
	{
		final InventoryAction ai = a.get( 0 );
		ai.doAction( s );
		history.push( ai );
		
		final DynamicsAction d = new DynamicsAction();
		d.doAction( s );
		history.push( d );
		
		rewards.push( ai.reward() + d.reward() );
	}

	@Override
	public long depth()
	{
		return history.size();
	}

	@Override
	public long t()
	{
		return history.size() / Nevents;
	}

	@Override
	public int nagents()
	{
		return 1;
	}

	@Override
	public int[] turn()
	{
		return new int[] { 0 };
	}

	@Override
	public double[] reward()
	{
		return new double[] { rewards.peek() };
	}

	@Override
	public boolean isTerminalState()
	{
		return s.isTerminal();
	}

	@Override
	public long horizon()
	{
		return Long.MAX_VALUE;
	}

	@Override
	public String detailString()
	{
		return "InventorySimulator";
	}

	@Override
	public void untakeLastAction()
	{
		for( int i = 0; i < Nevents; ++i ) {
			history.pop().undoAction( s );
		}
		rewards.pop();
	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] argv ) throws IOException
	{
		final RandomGenerator rng = new MersenneTwister( 42 );
		final int Nproducts = 2;
		final int max_inventory = 10;
		final double warehouse_cost = 1;
		final int max_order = 2;
		final double delivery_probability = 0.5;
		final int max_demand = 5;
		final int[] price = new int[] { 1, 2 };
		
		final InventoryProblem problem = new InventoryProblem(
			Nproducts, price, max_inventory, warehouse_cost, max_order, delivery_probability, max_demand );
		
		while( true ) {
			
			final InventoryState s = new InventoryState( rng, problem );
			final InventorySimulator sim = new InventorySimulator( s );

			final BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) );
			while( !s.isTerminal() ) {
				System.out.println( sim.state() );

				final String cmd = reader.readLine();
				final String[] tokens = cmd.split( "," );
				
				final InventoryAction a;
				if( "n".equals( tokens[0] ) ) {
					a = new InventoryNothingAction();
				}
				else {
					a = new InventoryOrderAction(
						Integer.parseInt( tokens[0] ), Integer.parseInt( tokens[1] ) );
				}
				
				sim.takeAction( new JointAction<InventoryAction>( a ) );
				
				System.out.println( "r = " + Arrays.toString( sim.reward() ) );
			}

//			System.out.print( "Hand: " );
//			System.out.print( sim.state().player_hand );
//			System.out.print( " (" );
//			final ArrayList<int[]> values = sim.state().player_hand.values();
//			for( int i = 0; i < values.size(); ++i ) {
//				if( i > 0 ) {
//					System.out.print( ", " );
//				}
//				System.out.print( Arrays.toString( values.get( i ) ) );
//			}
//			System.out.println( ")" );
//
//			System.out.print( "Reward: " );
//			System.out.println( Arrays.toString( sim.reward() ) );
//			System.out.print( "Dealer hand: " );
//			System.out.print( sim.state().dealerHand().toString() );
//			System.out.print( " (" );
//			System.out.print( SpBjHand.handValue( sim.state().dealerHand() )[0] );
//			System.out.println( ")" );
//			System.out.println( "----------------------------------------" );
		}
	}

}
