/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.tetris;


/**
 * @author jhostetler
 *
 */
public class TetrisGameOver extends Exception
{
	public final int conflict_x;
	public final int conflict_y;
	
	public TetrisGameOver( final int conflict_x, final int conflict_y )
	{
		this.conflict_x = conflict_x;
		this.conflict_y = conflict_y;
	}
}
