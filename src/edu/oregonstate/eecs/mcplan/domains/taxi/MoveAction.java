/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.taxi;

import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class MoveAction extends TaxiAction
{
	public final int dx;
	public final int dy;
	
	private boolean old_illegal_ = false;
	private boolean legal_ = false;
	private boolean done_ = false;
	
	public MoveAction( final int dx, final int dy )
	{
		this.dx = dx;
		this.dy = dy;
	}

	@Override
	public void undoAction( final TaxiState s )
	{
		assert( done_ );
		if( legal_ ) {
			s.taxi[0] -= dx;
			s.taxi[1] -= dy;
		}
		s.illegal_pickup_dropoff = old_illegal_;
		legal_ = false;
		done_ = false;
	}

	@Override
	public void doAction( final TaxiState s )
	{
		assert( !done_ );
		old_illegal_ = s.illegal_pickup_dropoff;
		final int[] new_pos = new int[] { s.taxi[0] + dx, s.taxi[1] + dy };
		if( s.isLegalMove( s.taxi, new_pos ) ) {
			Fn.memcpy( s.taxi, new_pos, new_pos.length );
			legal_ = true;
		}
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
