/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.fuelworld;

import java.util.ArrayList;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.State;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * FuelWorld is a modification of the TireWorld problem from IPC-4,
 * probilistic track, in 2004. The TireWorld domain is described here:
 * https://www.cs.cmu.edu/afs/cs/project/jair/pub/volume24/younes05a-html/node18.html
 * 
 * In FuelWorld, we replace tires with fuel. The car begins with F=100 units of
 * fuel. Each transition consumes 1 + DiscreteUniform{0,23} fuel. If the
 * transition consumes more fuel than the car has, the car stays where it is
 * and ends up with 0 fuel. The 'move' and 'pump-gas' action each cost -1.
 * 
 * @author jhostetler
 *
 */
public class FuelWorldState implements State
{
	/**
	 * This creates a problem with similar topology to the IPC-4 TireWorld
	 * domain. The only difference is that the fuel stations in the three
	 * loops on the "conservative" path (top path) are moved one space
	 * further along. This is because if they were left in the same positions,
	 * under the probability model we've adopted, reaching the first one
	 * would be almost certain.
	 * @param rng
	 * @return
	 */
	public static FuelWorldState createDefault( final RandomGenerator rng )
	{
		int counter = 0;
		final ArrayList<TIntList> adjacency = new ArrayList<TIntList>();
		final int start = counter++;
		adjacency.add( new TIntArrayList() );
		final int goal = counter++;
		adjacency.add( new TIntArrayList() );
		
		final TIntList fuel_depots = new TIntArrayList();
		
		// Bottom path
		final int nuisance_begin = counter + 5;
		final int nuisance_end = counter + 7;
		adjacency.get( start ).add( counter++ );
		adjacency.add( new TIntArrayList() );
		for( int i = 0; i < 7; ++i ) {
			adjacency.get( counter - 1 ).add( counter++ );
			adjacency.add( new TIntArrayList() );
		}
		adjacency.get( counter - 1 ).add( goal );
		
		// Nuisance loop on bottom path
		adjacency.get( nuisance_begin ).add( counter++ );
		adjacency.add( new TIntArrayList() );
		for( int i = 0; i < 3; ++i ) {
			adjacency.get( counter - 1 ).add( counter++ );
			adjacency.add( new TIntArrayList() );
		}
		fuel_depots.add( counter );
		adjacency.get( counter - 1 ).add( counter++ );
		adjacency.add( new TIntArrayList() );
		adjacency.get( counter - 1 ).add( nuisance_end );
		
		// Top main-line path
		adjacency.get( start ).add( counter++ );
		adjacency.add( new TIntArrayList() );
		for( int i = 0; i < 9; ++i ) {
			if( i == 8 ) {
				fuel_depots.add( counter - 1 );
			}
			adjacency.get( counter - 1 ).add( counter++ );
			adjacency.add( new TIntArrayList() );
		}
		adjacency.get( counter - 1 ).add( goal );
		
		// Fuel depot loops
		int loop_begin = adjacency.get( adjacency.get( start ).get( 1 ) ).get( 0 );
		for( int loop = 0; loop < 3; ++loop ) {
			adjacency.get( loop_begin ).add( counter++ );
			adjacency.add( new TIntArrayList() );
			adjacency.get( counter - 1 ).add( counter++ );
			adjacency.add( new TIntArrayList() );
			fuel_depots.add( counter - 1 );
			adjacency.get( counter - 1 ).add( loop_begin + 1 );
			loop_begin += 2;
		}
		
//		for( int i = 0; i < adjacency.size(); ++i ) {
//			final TIntList list = adjacency.get( i );
//			System.out.println( "V" + i + ": " + list );
//		}
//		System.out.println( "Fuel: " + fuel_depots );
		
		return new FuelWorldState( rng, adjacency, goal, new TIntHashSet( fuel_depots ) );
	}
	
	/**
	 * This is the same as 'createDefault()', but it adds edges from each
	 * main-line top node to a corresponding bottom-track node. This means
	 * that the agent always has an action choice, and must continue making
	 * the right choice to succeed.
	 * @param rng
	 * @return
	 */
	public static FuelWorldState createDefaultWithChoices( final RandomGenerator rng )
	{
		int counter = 0;
		final ArrayList<TIntList> adjacency = new ArrayList<TIntList>();
		final int start = counter++;
		adjacency.add( new TIntArrayList() );
		final int goal = counter++;
		adjacency.add( new TIntArrayList() );
		
		final TIntList fuel_depots = new TIntArrayList();
		
		// Bottom path
		final int nuisance_begin = counter + 5;
		final int nuisance_end = counter + 7;
		adjacency.get( start ).add( counter++ );
		adjacency.add( new TIntArrayList() );
		final int first_bottom = counter - 1;
		for( int i = 0; i < 7; ++i ) {
			adjacency.get( counter - 1 ).add( counter++ );
			adjacency.add( new TIntArrayList() );
		}
		adjacency.get( counter - 1 ).add( goal );
		
		// Nuisance loop on bottom path
		adjacency.get( nuisance_begin ).add( counter++ );
		adjacency.add( new TIntArrayList() );
		for( int i = 0; i < 3; ++i ) {
			adjacency.get( counter - 1 ).add( counter++ );
			adjacency.add( new TIntArrayList() );
		}
		fuel_depots.add( counter );
		adjacency.get( counter - 1 ).add( counter++ );
		adjacency.add( new TIntArrayList() );
		adjacency.get( counter - 1 ).add( nuisance_end );
		
		// Top main-line path
		adjacency.get( start ).add( counter++ );
		adjacency.add( new TIntArrayList() );
		final int first_top = counter - 1;
		for( int i = 0; i < 9; ++i ) {
			if( i == 8 ) {
				fuel_depots.add( counter - 1 );
			}
			adjacency.get( counter - 1 ).add( counter++ );
			adjacency.add( new TIntArrayList() );
		}
		adjacency.get( counter - 1 ).add( goal );
		
		// Fuel depot loops
		int loop_begin = adjacency.get( adjacency.get( start ).get( 1 ) ).get( 0 );
		for( int loop = 0; loop < 3; ++loop ) {
			adjacency.get( loop_begin ).add( counter++ );
			adjacency.add( new TIntArrayList() );
			adjacency.get( counter - 1 ).add( counter++ );
			adjacency.add( new TIntArrayList() );
			fuel_depots.add( counter - 1 );
			adjacency.get( counter - 1 ).add( loop_begin + 1 );
			loop_begin += 2;
		}
		
		// Paths from top -> bottom
		for( int i = 0; i < 8; ++i ) {
			adjacency.get( first_top + i ).add( first_bottom + i );
		}
		
//		for( int i = 0; i < adjacency.size(); ++i ) {
//			final TIntList list = adjacency.get( i );
//			System.out.println( "V" + i + ": " + list );
//		}
//		System.out.println( "Fuel: " + fuel_depots );
		
		return new FuelWorldState( rng, adjacency, goal, new TIntHashSet( fuel_depots ) );
	}
	
	// -----------------------------------------------------------------------
	
	public static final int fuel_capacity = 90;
	
	public int location = 0;
	public int fuel = fuel_capacity;
	
	public final int fuel_consumption = 24;
	
	public final RandomGenerator rng;
	public final ArrayList<TIntList> adjacency;
	public final int goal;
	public final TIntSet fuel_depots;
	
	public boolean out_of_fuel = false;
	
	public int t = 0;
	public final int T = 100;
	
	public FuelWorldState( final RandomGenerator rng, final ArrayList<TIntList> adjacency,
						   final int goal, final TIntSet fuel_depots )
	{
		this.rng = rng;
		this.adjacency = adjacency;
		this.goal = goal;
		this.fuel_depots = fuel_depots;
	}

	@Override
	public boolean isTerminal()
	{
		return location == goal || fuel == 0;
	}
	
	@Override
	public int hashCode()
	{
		return new PrimitiveFuelWorldRepresentation( this ).hashCode();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof FuelWorldState) ) {
			return false;
		}
		final FuelWorldState that = (FuelWorldState) obj;
		return new PrimitiveFuelWorldRepresentation( this ).equals( new PrimitiveFuelWorldRepresentation( that ) );
	}
	
	@Override
	public String toString()
	{
		return "FuelWorldState[location = " + location + "; fuel = " + fuel + "]";
	}

	@Override
	public void close()
	{ }
}
