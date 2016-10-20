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
package edu.oregonstate.eecs.mcplan.domains.sailing;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class SailingAction extends UndoableAction<SailingState> implements VirtualConstructor<SailingAction>
{
	public static final double obstacle_penalty = -1;
	
	public final int direction;
	
	private boolean done = false;
	private int old_x = 0;
	private int old_y = 0;
	private final int old_w = 0;
	
	public SailingAction( final int direction )
	{
		this.direction = direction;
	}
	
	@Override
	public int hashCode()
	{
		return 7 + (5 * direction);
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		final SailingAction that = (SailingAction) obj;
		return direction == that.direction;
	}
	
	@Override
	public String toString()
	{
		return "SailingAction[" + direction + "]";
	}
	
	private int relativeWind( final int w )
	{
		final int rel = Fn.mod( direction - w, SailingState.Nwind_directions );
		return (rel > SailingState.Nwind_directions / 2 ? SailingState.Nwind_directions - rel : rel);
		
//		final int ww = (w > SailingState.Nwind_directions / 2 ? w - SailingState.Nwind_directions : w);
//		final int dd = (direction > SailingState.Nwind_directions / 2 ? direction - SailingState.Nwind_directions : direction);
//		final int rel = Math.abs( ww - dd );
//		return (rel > SailingState.Nwind_directions / 2 ? rel - SailingState.Nwind_directions : rel);
	}
	
	public double reward( final SailingState s )
	{
		final int relative_wind = relativeWind( s.w ); //Math.abs( s.w - direction );
//		System.out.println( "relative_wind = " + relative_wind );
		if( relative_wind == 0 ) {
			// Pointing into the wind. The boat doesn't move and we give a
			// fixed reward of -1.
			return -1;
		}
		else if( s.isNeighborObstacle( direction ) ) {
			return obstacle_penalty;
		}
		else {
			// Distance sailed
			final double d = (direction % 2 == 0 ? 1.0 : Math.sqrt( 2 ));
			// Performance numbers are eyeballed from Figure 13 in the paper
			// referenced in the class docs, for a "Laser standard" in 10 kts wind.
			final double v;
			switch( relative_wind ) {
			case 1:
				v = (4.0 * s.v) / SailingState.reference_v; break;
			case 2:
				v = (7.5 * s.v) / SailingState.reference_v; break;
			case 3:
				v = (5.5 * s.v) / SailingState.reference_v; break;
			case 4:
				v = (4.0 * s.v) / SailingState.reference_v; break;
			default:
				// There are 8 valid directions. Sailing into wind was
				// already handled.
				throw new IllegalArgumentException( "relative_wind = " + relative_wind );
			}
			// We need v >= 1 so that heading into the wind is the worst action
			assert( v >= 1 );
			assert( v <= s.max_speed() );
			// Cost is equal to negative time required to cover distance.
			return -d / v;
		}
	}

	@Override
	public void doAction( final RandomGenerator rng, final SailingState s )
	{
		final int relative_wind = relativeWind( s.w ); //Math.abs( s.w - direction );
		if( relative_wind != 0 && !s.isNeighborObstacle( direction ) ) {
			// Boat doesn't move if sailing into the wind
			old_x = s.x;
			old_y = s.y;
			switch( direction ) {
			case 0:
				s.x += 1; break;
			case 1:
				s.x += 1; s.y += 1; break;
			case 2:
				s.y += 1; break;
			case 3:
				s.x -= 1; s.y += 1; break;
			case 4:
				s.x -= 1; break;
			case 5:
				s.x -= 1; s.y -= 1; break;
			case 6:
				s.y -= 1; break;
			case 7:
				s.x += 1; s.y -= 1; break;
			default:
				throw new IllegalArgumentException( "direction = " + direction );
			}
		}
		
		done = true;
	}

	@Override
	public SailingAction create()
	{
		return new SailingAction( direction );
	}

	@Override
	public boolean isDone()
	{
		return done;
	}

	@Override
	public void undoAction( final SailingState s )
	{
		s.x = old_x;
		s.y = old_y;
		s.w = old_w;
		done = false;
	}

}
