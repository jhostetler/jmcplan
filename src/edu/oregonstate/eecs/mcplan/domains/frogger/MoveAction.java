/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.frogger;

/**
 * @author jhostetler
 *
 */
public class MoveAction extends FroggerAction
{
	public final int dx;
	public final int dy;
	
	private int old_x_ = 0;
	private int old_y_ = 0;
	private boolean old_goal_ = false;
	private boolean old_squashed_ = false;
	
	private boolean done_ = false;
	
	public MoveAction( final int dx, final int dy )
	{
		this.dx = dx;
		this.dy = dy;
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
		return 5 + 7 * (dx + 11 * dy);
	}
	
	@Override
	public String toString()
	{
		return "MoveAction(" + dx + "; " + dy + ")";
	}
	
	@Override
	public void undoAction( final FroggerState s )
	{
		assert( done_ );
		s.frog_x = old_x_;
		s.frog_y = old_y_;
		s.goal = old_goal_;
		s.squashed = old_squashed_;
		done_ = false;
	}

	@Override
	public void doAction( final FroggerState s )
	{
		assert( !done_ );
		old_x_ = s.frog_x;
		old_y_ = s.frog_y;
		old_goal_ = s.goal;
		old_squashed_ = s.squashed;
		
		s.squashed = false;
		
		s.frog_x += dx;
		s.frog_y += dy;
		
		final Tile t = s.grid[s.frog_y][s.frog_x];
		if( t == Tile.Car ) {
			s.squashed = true;
		}
		else if( t == Tile.Goal ) {
			s.goal = true;
		}
		
		done_ = true;
	}

	@Override
	public boolean isDone()
	{
		return done_;
	}

	@Override
	public FroggerAction create()
	{
		return new MoveAction( dx, dy );
	}
}
