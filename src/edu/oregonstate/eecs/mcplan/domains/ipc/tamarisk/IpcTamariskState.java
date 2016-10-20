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
package edu.oregonstate.eecs.mcplan.domains.ipc.tamarisk;

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * This is a direct translation of the Tamarisk domain from IPC 2014.
 */
public final class IpcTamariskState implements State
{
	public static final byte None 		= 0;
	public static final byte Native 	= 0b01;
	public static final byte Tamarisk 	= 0b10;
	public static final byte Both		= Native | Tamarisk;
	public static final int Nspecies = 2;
	
	public final IpcTamariskParameters params;
	
	public final byte[][] reaches;
	
	public int t = 0;
	
	public IpcTamariskState( final IpcTamariskParameters params )
	{
		this.params = params;
		reaches = new byte[params.Nreaches][];
		for( int i = 0; i < params.Nreaches; ++i ) {
			reaches[i] = new byte[params.cells_per_reach];
		}
	}
	
	public IpcTamariskState( final IpcTamariskState that )
	{
		this.params = that.params;
		this.reaches = Fn.copy( that.reaches );
	}
	
	@Override
	public void close()
	{ }
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "t: " ).append( t );
		for( final byte[] r : reaches ) {
			sb.append( ", " ).append( Arrays.toString( r ) ); //.append( "\n" );
		}
		return sb.toString();
	}
	
	public IpcTamariskState step( final RandomGenerator rng, final IpcTamariskAction a )
	{
		final IpcTamariskState sprime = new IpcTamariskState( params );
		sprime.t = t + 1;
		
		for( int i = 0; i < reaches.length; ++i ) {
			final byte[] r = reaches[i];
			final byte[] rprime = sprime.reaches[i];
			for( int j = 0; j < r.length; ++j ) {
				final boolean has_tamarisk	= (r[j] & Tamarisk) != 0;
				final boolean has_native	= (r[j] & Native) != 0;
				
				// tamarisk-at'
				if( r[j] == Both ) { // Case 1
					// NOTE: The RDDL spec for this case has a redundant
					// statement ("~forall...")
					rprime[j] |= rng.nextDouble() < params.competition_win_rate_tamarisk
								 ? Tamarisk : None;
				}
				else if( a.reach == i && a.type == IpcTamariskActionSet.Eradicate ) {
					if( has_tamarisk ) { // Case 3
						// NOTE: The RDDL spec for this case has a redundant
						// statement ("~forall...")
						rprime[j] |= rng.nextDouble() < (1.0 - params.eradication_rate)
									 ? Tamarisk : None;
					}
					// Case 2: if !has_tamarisk, (rprime[j] & Tamarisk) is already 0
				}
				else if( has_tamarisk ) { // Case 4
					rprime[j] |= rng.nextDouble() < (1.0 - params.death_rate_tamarisk)
								 ? Tamarisk : None;
				}
				else if( r[j] == None ) { // Case 5
					final double p = params.exogenous_prod_rate_tamarisk
									 + (1.0 - params.exogenous_prod_rate_tamarisk) * infectionRate( i );
					rprime[j] |= rng.nextDouble() < p
								 ? Tamarisk : None;
				}
				else {
					assert( r[j] == Native );
					rprime[j] |= (r[j] & Tamarisk);
				}
				
				// native-at'
				if( r[j] == Both ) { // Case 1
					rprime[j] |= rng.nextDouble() < params.competition_win_rate_native
								 ? Native : None;
				}
				else if( !has_tamarisk && a.reach == i && a.type == IpcTamariskActionSet.Restore ) {
					if( has_native ) { // Case 2
						rprime[j] |= Native;
					}
					else { // Case 3
						rprime[j] |= rng.nextDouble() < params.restoration_rate
									 ? Native : None;
					}
				}
				else if( has_native ) { // Case 4
					rprime[j] |= rng.nextDouble() < (1.0 - params.death_rate_native)
								 ? Native : None;
				}
				else if( r[j] == None ) { // Case 5
					rprime[j] |= rng.nextDouble() < params.exogenous_prod_rate_native
								 ? Native : None;
				}
				else { // Case 6
					assert( r[j] == Tamarisk );
					rprime[j] |= (r[j] & Native);
				}
			}
		}
		
		return sprime;
	}
	
	private double infectionRate( final int ri )
	{
		// p will be the probability that *none* of the spread events happen
		double p = 1.0;
		final byte[] r = reaches[ri];
		// NOTE: The RDDL code uses *downstream* spread rate for within-reach
		// spread, even though the RDDL comments say that it uses *upstream*.
		for( int j = 0; j < r.length; ++j ) {
			if( (r[j] & Tamarisk) != 0 ) {
				p *= (1.0 - params.downstream_spread_rate);
			}
		}
		for( final int u : params.upstream[ri] ) {
			final byte[] ur = reaches[u];
			for( int j = 0; j < ur.length; ++j ) {
				if( (ur[j] & Tamarisk) != 0 ) {
					p *= (1.0 - params.downstream_spread_rate);
				}
			}
		}
		for( final int d : params.downstream[ri] ) {
			final byte[] dr = reaches[d];
			for( int j = 0; j < dr.length; ++j ) {
				if( (dr[j] & Tamarisk) != 0 ) {
					p *= (1.0 - params.upstream_spread_rate);
				}
			}
		}
		// 1 - p is the probability that *some* spread event happens
		return 1.0 - p;
	}
	
	@Override
	public boolean isTerminal()
	{
		return t >= params.T;
	}

}
