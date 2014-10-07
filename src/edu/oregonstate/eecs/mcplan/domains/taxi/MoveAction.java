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
	public void doAction( final TaxiState s, final RandomGenerator rng )
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
			// Slip actions move orthogonally to the intended direction
			final double r = rng.nextDouble();
			if( r < s.slip ) {
				if( dx != 0 ) {
					new_pos = new int[] { s.taxi[0], s.taxi[1] - 1 };
				}
				else {
					new_pos = new int[] { s.taxi[0] - 1, s.taxi[1] };
				}
			}
			else if( r < 2*s.slip ) {
				if( dx != 0 ) {
					new_pos = new int[] { s.taxi[0], s.taxi[1] + 1 };
				}
				else {
					new_pos = new int[] { s.taxi[0] + 1, s.taxi[1] };
				}
			}
			else {
				new_pos = new int[] { s.taxi[0] + dx, s.taxi[1] + dy };
			}
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
