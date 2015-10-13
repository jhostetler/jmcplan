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
	
	public final int T;
	
	/**
	 * Can be changed safely.
	 */
	public final int Nrows;
	
	public final byte[][] scratch;
	
	public TetrisParameters( final int T, final int Nrows )
	{
		this.T = T;
		this.Nrows = Nrows;
		
		this.scratch = new byte[Nrows][Ncolumns];
	}
}
