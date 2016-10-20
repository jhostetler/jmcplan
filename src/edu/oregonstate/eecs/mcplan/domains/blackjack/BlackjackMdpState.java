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
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 * @author jhostetler
 *
 */
public class BlackjackMdpState
{
	public final int dealer_value;
	public final int dealer_high_aces;
	public final int player_value;
	public final int player_high_aces;
	public final boolean player_passed;
	
	private final int hash_code_;
	
	public BlackjackMdpState( final int dealer_value, final int dealer_high_aces,
							  final int player_value, final int player_high_aces, final boolean player_passed )
	{
		assert( dealer_value >= 11 * dealer_high_aces );
		assert( player_value >= 11 * player_high_aces );
		this.dealer_value = dealer_value;
		this.dealer_high_aces = dealer_high_aces;
		this.player_value = player_value;
		this.player_high_aces = player_high_aces;
		this.player_passed = player_passed;
		
		final HashCodeBuilder hb = new HashCodeBuilder( 37, 41 );
		hb.append( dealer_value ).append( dealer_high_aces )
		  .append( player_value ).append( player_high_aces ).append( player_passed );
		hash_code_ = hb.toHashCode();
	}
	
	public static final BlackjackMdpState TheAbsorbingState = new BlackjackMdpState( 0, 0, 0, 0, true );
	
	@Override
	public int hashCode()
	{
		return hash_code_;
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null ) {
			return false;
		}
		final BlackjackMdpState that = (BlackjackMdpState) obj;
		final boolean eq = dealer_value == that.dealer_value
			   && dealer_high_aces == that.dealer_high_aces
			   && player_value == that.player_value
			   && player_high_aces == that.player_high_aces
			   && player_passed == that.player_passed;
//		assert( (id == that.id) == eq );
		return eq;
	}
	
	@Override
	public String toString()
	{
		return "d:" + dealer_value + " (" + dealer_high_aces + "), p:" + player_value
			   + " (" + player_high_aces + ")" + (player_passed ? " passed" : "");
	}
	
//	private BlackjackMdpState( final BlackjackMdpState that )
//	{
//		dealer_value = that.dealer_value;
//		dealer_busted = that.dealer_busted;
//		player_value = that.player_value;
//		player_aces = that.player_aces;
//		player_passed = that.player_passed;
//		player_busted = that.player_busted;
//		hash_code_ = that.hash_code_;
//		repr_ = that.repr_;
//	}
//
//	@Override
//	public boolean equals( final Object obj )
//	{
//		if( obj == null || !(obj instanceof HandValueAbstraction) ) {
//			return false;
//		}
//		final HandValueAbstraction that = (HandValueAbstraction) obj;
//		if( dealer_value != that.dealer_value && dealer_value <= 21 && that.dealer_value <= 21 ) {
//			return false;
//		}
//		for( int i = 0; i < player_values.length; ++i ) {
//			if( player_values[i] > 21 ) {
//				if( that.player_values[i] <= 21 ) {
//					return false;
//				}
//			}
//			else if( that.player_values[i] > 21 ) {
//				if( player_values[i] <= 21 ) {
//					return false;
//				}
//			}
//			else {
//				if( player_values[i] != that.player_values[i]
//					|| player_aces[i] != that.player_aces[i] ) {
//					return false;
//				}
//			}
//
//		}
//		return true;
//	}
//
//	@Override
//	public int hashCode()
//	{
//		return hash_code_;
//	}
//
//	@Override
//	public String toString()
//	{
//		return repr_;
//	}
}
