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
package edu.oregonstate.eecs.mcplan.domains.taxi;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class MoveAction extends TaxiAction
{
	public final int dx;
	public final int dy;
	
	private int old_x_ = 0;
	private int old_y_ = 0;
	private final boolean old_pickup_success_ = false;
	private boolean old_illegal_ = false;
	private boolean legal_ = false;
	private boolean done_ = false;
	
	public MoveAction( final int dx, final int dy )
	{
		// Assertion reflects an assumption relied on by doAction()
		assert( Math.abs( dx ) + Math.abs( dy ) <= 1 );
		this.dx = dx;
		this.dy = dy;
	}

	@Override
	public void undoAction( final TaxiState s )
	{
		assert( done_ );
		if( legal_ ) {
			s.taxi[0] = old_x_;
			s.taxi[1] = old_y_;
		}
		s.pickup_success = old_pickup_success_;
		s.illegal_pickup_dropoff = old_illegal_;
		legal_ = false;
		done_ = false;
	}

	@Override
	public void doAction( final RandomGenerator rng, final TaxiState s )
	{
		assert( !done_ );
		old_illegal_ = s.illegal_pickup_dropoff;
		old_x_ = s.taxi[0];
		old_y_ = s.taxi[1];
		
		final int[] new_pos;
		if( s.slip == 0.0 ) {
			new_pos = new int[] { s.taxi[0] + dx, s.taxi[1] + dy };
		}
		else {
			final double r = rng.nextDouble();
			
			// Slip actions are no-ops
			if( r < s.slip ) {
				new_pos = new int[] { s.taxi[0], s.taxi[1] };
			}
			else {
				new_pos = new int[] { s.taxi[0] + dx, s.taxi[1] + dy };
			}
			
//			// Slip actions move orthogonally to the intended direction
//			if( r < s.slip ) {
//				if( dx != 0 ) {
//					new_pos = new int[] { s.taxi[0], s.taxi[1] - 1 };
//				}
//				else {
//					new_pos = new int[] { s.taxi[0] - 1, s.taxi[1] };
//				}
//			}
//			else if( r < 2*s.slip ) {
//				if( dx != 0 ) {
//					new_pos = new int[] { s.taxi[0], s.taxi[1] + 1 };
//				}
//				else {
//					new_pos = new int[] { s.taxi[0] + 1, s.taxi[1] };
//				}
//			}
//			else {
//				new_pos = new int[] { s.taxi[0] + dx, s.taxi[1] + dy };
//			}
		}
		
		if( s.isLegalMove( s.taxi, new_pos ) ) {
			Fn.memcpy( s.taxi, new_pos, new_pos.length );
			legal_ = true;
		}
		s.pickup_success = false;
		s.illegal_pickup_dropoff = false;
		done_ = true;
	}

	@Override
	public boolean isDone()
	{
		return done_;
	}

	@Override
	public TaxiAction create()
	{
		return new MoveAction( dx, dy );
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof MoveAction) ) {
			return false;
		}
		final MoveAction that = (MoveAction) obj;
		return dx == that.dx && dy == that.dy;
	}
	
	@Override
	public int hashCode()
	{
		return 17 + 19 * (dx + 23 * dy);
	}
	
	@Override
	public String toString()
	{
		return "MoveAction(" + dx + "; " + dy + ")";
	}
}
