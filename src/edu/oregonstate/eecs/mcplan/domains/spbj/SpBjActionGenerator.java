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
package edu.oregonstate.eecs.mcplan.domains.spbj;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.ActionGenerator;

/**
 * @author jhostetler
 *
 */
public class SpBjActionGenerator extends ActionGenerator<SpBjState, SpBjAction>
{
	private final ArrayList<ArrayList<SpBjActionCategory>> cat = new ArrayList<ArrayList<SpBjActionCategory>>();
	private int[] index = null;
	private int size = 0;
	private int ai = 0;
	private boolean has_next = false;
	
	@Override
	public SpBjActionGenerator create()
	{
		return new SpBjActionGenerator();
	}
	
	/**
	 * Returns an integer bitset representation of the legal actions in 's'.
	 * This is cheaper than actually generating the action set, if all you
	 * care about is which actions are legal.
	 * @param s
	 * @return
	 */
	public static int bitmask( final SpBjState s )
	{
		int mask = 0;
		for( int i = 0; i < s.player_hand.Nhands; ++i ) {
			int mask_i = 0;
			if( !s.player_hand.passed[i] ) {
				mask_i |= 1<<0;
				if( s.player_hand.canDouble( i ) ) {
					mask_i |= 1<<1;
				}
				if( s.player_hand.canSplit( i ) ) {
					mask_i |= 1<<2;
				}
			}
			
			mask |= (mask_i << (i*4));
		}
		return mask;
	}

	@Override
	public void setState( final SpBjState s, final long t )
	{
		cat.clear();
		size = 1;
		ai = 0;
		int Nsplits = 0;
		for( int i = 0; i < s.player_hand.Nhands; ++i ) {
			final ArrayList<SpBjActionCategory> cat_i = new ArrayList<SpBjActionCategory>();
			cat_i.add( SpBjActionCategory.Pass );
			if( !s.player_hand.passed[i] ) {
				cat_i.add( SpBjActionCategory.Hit );
				// Note: Split must not be the last action added, since the
				// way we calculate has_next in next() depends on there always
				// being another action after any multiple-split action that
				// we have to skip.
				if( s.player_hand.canSplit( i ) ) {
					cat_i.add( SpBjActionCategory.Split );
					Nsplits += 1;
				}
				if( s.player_hand.canDouble( i ) ) {
					cat_i.add( SpBjActionCategory.Double );
				}
			}
			
			cat.add( cat_i );
			size *= cat_i.size();
		}
		
//		if( s.player_hand.Nhands + Nsplits > SpBjHand.max_hands ) {
//			// We can't do all of the combinations of splits
//			final int max_splits = SpBjHand.max_hands - s.player_hand.Nhands;
//			int Ncombos = 0;
//			for( int k = max_splits + 1; k <= Nsplits; ++k ) {
//				Ncombos += (int) ArithmeticUtils.binomialCoefficient( Nsplits, k );
//			}
//			size -= Ncombos;
//		}
		
		index = new int[cat.size()];
		if( size > 0 ) {
			has_next = true;
		}
	}

	@Override
	public int size()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasNext()
	{
//		return ai < size;
		return has_next;
	}

	@Override
	public SpBjAction next()
	{
		final SpBjActionCategory[] ac = new SpBjActionCategory[cat.size()];
		int Nsplits = 0;
		for( int i = 0; i < ac.length; ++i ) {
			ac[i] = cat.get( i ).get( index[i] );
			if( ac[i] == SpBjActionCategory.Split ) {
				Nsplits += 1;
			}
		}
		
		for( int i = 0; i < index.length; ++i ) {
			index[i] += 1;
			if( index[i] == cat.get( i ).size() ) {
				index[i] = 0;
			}
			else {
				break;
			}
		}
		
		boolean all_zero = true;
		for( final int i : index ) {
			if( i > 0 ) {
				all_zero = false;
				break;
			}
		}
		has_next = !all_zero;
		
		if( ac.length + Nsplits > SpBjHand.max_hands ) {
//			assert( hasNext() );
			return next();
		}
		else {
			ai += 1;
//			assert( hasNext() || Arrays.equals( index, new int[index.length] ) );
			return new SpBjAction( ac );
		}
	}

}
