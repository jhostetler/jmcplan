/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.tetris;

/**
 * @author jhostetler
 *
 */
public final class TetrisParameters
{
	/**
	 * Must be =10 in current implementation. If you want to change this, you
	 * need to alter the bounds checking in TetrominoTypes.
	 */
	public final int Ncolumns = 10;
	
	/**
	 * Can be changed safely.
	 */
	public final int Nrows;
	
	public TetrisParameters( final int Nrows )
	{
		this.Nrows = Nrows;
	}
}
