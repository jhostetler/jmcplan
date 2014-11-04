/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2.subtask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.oregonstate.eecs.mcplan.ActionSpace;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.Hand;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.KeepAction;
import edu.oregonstate.eecs.mcplan.domains.yahtzee2.YahtzeeAction;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.Generator;
import edu.oregonstate.eecs.mcplan.util.ValueType;

/**
 * @author jhostetler
 *
 */
public class YahtzeeSubtaskActionSpace extends ActionSpace<YahtzeeDiceState, YahtzeeAction>
{
	public static final Map<ValueType<int[]>, Integer> index_map;
	public static final ArrayList<int[]> action_map;
	
	static {
		index_map = new HashMap<ValueType<int[]>, Integer>();
		action_map = new ArrayList<int[]>();
		// +1 to add a "don't keep" category
		final Fn.MultinomialTermGenerator g = new Fn.MultinomialTermGenerator( Hand.Ndice, Hand.Nfaces + 1 );
		int idx = 0;
		while( g.hasNext() ) {
			final int[] t = g.next();
			final int[] h = Arrays.copyOf( t, Hand.Nfaces );
			assert( Fn.sum( t ) == 5 );
			assert( Fn.sum( h ) <= 5 );
			assert( !index_map.containsKey( h ) );
			index_map.put( ValueType.of( h ), idx++ );
			action_map.add( Fn.copy( h ) );
		}
		
		for( final Map.Entry<ValueType<int[]>, Integer> e : index_map.entrySet() ) {
			System.out.println( e.getKey() + " => " + e.getValue() );
		}
	}
	
	private YahtzeeDiceState s = null;
	
	@Override
	public void setState( final YahtzeeDiceState s )
	{
		this.s = s;
	}

	@Override
	public int cardinality()
	{
//		int r = 1;
//		for( int i = 0; i < Hand.Nfaces; ++i ) {
//			r *= (s.hand.dice[i] + 1);
//		}
//		return r;
		
		return index_map.size();
	}

	@Override
	public boolean isFinite()
	{
		return true;
	}

	@Override
	public boolean isCountable()
	{
		return true;
	}

	@Override
	public int index( final YahtzeeAction a )
	{
		assert( a != null );
		try {
			return index_map.get( ValueType.of( ((KeepAction) a).keepers ) );
		}
		catch( final NullPointerException ex ) {
			System.out.println( a );
			throw ex;
		}
	}

	@Override
	public Generator<YahtzeeAction> generator()
	{
		assert( s != null );
		return new G( s );
	}
	
	private static final class G extends Generator<YahtzeeAction>
	{
		private final Fn.MultisetPowerSetGenerator power_set;
		
		public G( final YahtzeeDiceState s )
		{
			if( s.isTerminal() ) {
				power_set = null;
			}
			else {
				power_set = new Fn.MultisetPowerSetGenerator( Fn.copy( s.hand.dice ) );
			}
		}
	
		@Override
		public boolean hasNext()
		{
			if( power_set == null ) {
				return false;
			}
			
			return power_set.hasNext();
		}
	
		@Override
		public YahtzeeAction next()
		{
			final int[] keepers = power_set.next();
			assert( keepers != null );
			return new KeepAction( keepers );
		}
	}
	
	public static void main( final String[] argv )
	{
		final YahtzeeDiceState s = new YahtzeeDiceState( new Hand( new int[] { 0, 2, 2, 0, 0, 1 } ), 1 );
		final YahtzeeSubtaskActionSpace A = new YahtzeeSubtaskActionSpace();
		
		A.setState( s );
		final Generator<YahtzeeAction> g = A.generator();
		int c = 0;
		while( g.hasNext() ) {
			System.out.println( (c++) + " : " + g.next() );
		}
	}

}
