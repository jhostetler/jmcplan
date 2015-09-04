/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.tetris;

import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.abstraction.IndexRepresentation;

/**
 * @author jhostetler
 *
 */
public class TetrisActionSetRepresenter implements Representer<TetrisState, Representation<TetrisState>>
{
	@Override
	public Representer<TetrisState, Representation<TetrisState>> create()
	{
		return new TetrisActionSetRepresenter();
	}

	@Override
	public Representation<TetrisState> encode( final TetrisState s )
	{
		if( s.isTerminal() ) {
			return new IndexRepresentation<TetrisState>( 0 );
		}
		else {
			return new IndexRepresentation<TetrisState>( 1 );
		}
	}
}
