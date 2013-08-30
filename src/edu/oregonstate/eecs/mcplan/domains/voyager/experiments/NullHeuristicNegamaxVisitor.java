/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager.experiments;

import java.io.PrintStream;

import edu.oregonstate.eecs.mcplan.domains.voyager.Player;
import edu.oregonstate.eecs.mcplan.domains.voyager.Voyager;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerState;
import edu.oregonstate.eecs.mcplan.search.LoggingNegamaxVisitor;

/**
 * @author jhostetler
 *
 */
public class NullHeuristicNegamaxVisitor<A> extends LoggingNegamaxVisitor<VoyagerState, A>
{
	private final Player player_;
	
	public NullHeuristicNegamaxVisitor( final PrintStream out, final Player player )
	{
		super( out );
		player_ = player;
	}

	@Override
	public double goal( final VoyagerState s )
	{
		final Player winner = Voyager.winner( s );
		if( winner == null ) {
			return 0;
		}
		else if( winner == player_ ) {
			return 1;
		}
		else {
			return -1;
		}
	}
	
	@Override
	public boolean isGoal( final VoyagerState s )
	{
		return Voyager.winner( s ) != null;
	}
	
	@Override
	public double heuristic( final VoyagerState s )
	{
		return goal( s );
	}
}
