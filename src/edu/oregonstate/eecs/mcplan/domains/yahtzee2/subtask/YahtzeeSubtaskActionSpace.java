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
package edu.oregonstate.eecs.mcplan.domains.yahtzee2.subtask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.oregonstate.eecs.mcplan.ActionSet;
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
	
	private final YahtzeeDiceState s = null;

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
	
	@Override
	public ActionSet<YahtzeeDiceState, YahtzeeAction> getActionSet(
			final YahtzeeDiceState s )
	{
		return ActionSet.constant( Fn.in(new G( s )) );
	}
	
	public static void main( final String[] argv )
	{
		final YahtzeeDiceState s = new YahtzeeDiceState( new Hand( new int[] { 0, 2, 2, 0, 0, 1 } ), 1 );
		final YahtzeeSubtaskActionSpace A = new YahtzeeSubtaskActionSpace();
		
		int c = 0;
		for( final YahtzeeAction a : A.getActionSet( s ) ) {
			System.out.println( (c++) + " : " + a );
		}
	}

}
