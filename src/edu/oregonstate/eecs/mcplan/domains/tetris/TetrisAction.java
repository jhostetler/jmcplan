/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.tetris;

import edu.oregonstate.eecs.mcplan.Action;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class TetrisAction implements Action<TetrisState>, VirtualConstructor<TetrisAction>
{
	public final int position;
	public final int rotation;
	
	public TetrisAction( final int position, final int rotation )
	{
		this.position = position;
		this.rotation = rotation;
	}
	
	@Override
	public int hashCode()
	{
		return 17 * (13 + position * (23 + rotation));
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		final TetrisAction that = (TetrisAction) obj;
		return position == that.position && rotation == that.rotation;
	}
	
	@Override
	public String toString()
	{
		return "TetrisAction[" + position + ", " + rotation + "]";
	}
	
	@Override
	public TetrisAction create()
	{
		return new TetrisAction( position, rotation );
	}

	@Override
	public void doAction( final TetrisState s )
	{
		final Tetromino tetro = s.getCurrentTetromino();
		tetro.setRotation( rotation );
		tetro.setPosition( position, s.params.Nrows - 1 - tetro.getBoundingBox().top );
		s.createTetromino( tetro );
		s.t += 1;
	}

}
