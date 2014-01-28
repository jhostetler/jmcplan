/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import edu.oregonstate.eecs.mcplan.Representation;

/**
 * @author jhostetler
 *
 */
public class AStarAbstraction extends Representation<BlackjackState>
{
	private final BlackjackAction astar_;
	
	private final int dv_;
	private final int pv_;
	
	private final BlackjackStateToken t_;
	
	public AStarAbstraction( final BlackjackAction astar, final int dv, final int pv, final BlackjackStateToken t )
	{
		astar_ = astar;
		dv_ = dv;
		pv_ = pv;
		t_ = t;
	}
	
	@Override
	public Representation<BlackjackState> copy()
	{
		return new AStarAbstraction( astar_.create(), dv_, pv_, t_ );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof AStarAbstraction) ) {
			return false;
		}
		final AStarAbstraction that = (AStarAbstraction) obj;
		if( astar_ == null ) {
			return that.astar_ == null;
		}
		else {
			return astar_.equals( that.astar_ );
		}
	}

	@Override
	public int hashCode()
	{
		if( astar_ == null ) {
			return 3;
		}
		else {
			return 5 * astar_.hashCode();
		}
	}
	
	@Override
	public String toString()
	{
		final String s = (astar_ == null ? "null" : astar_.toString());
		return "AStarAbstraction[" + s + "] d:" + dv_ + ", p:" + pv_ + " " + t_;
	}

}
