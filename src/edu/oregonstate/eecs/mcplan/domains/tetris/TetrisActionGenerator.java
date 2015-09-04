/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.tetris;

import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public class TetrisActionGenerator extends Generator<TetrisAction>
{
	private int t = 0;
	private int r = 0;
	
	@Override
	public boolean hasNext()
	{
		return t < TetrisState.Ncolumns && r < 4;
	}

	@Override
	public TetrisAction next()
	{
		final TetrisAction a = new TetrisAction( t, r );
		r += 1;
		if( r >= 4 ) {
			t += 1;
			r = 0;
		}
		return a;
	}
}
