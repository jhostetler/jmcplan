/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

import java.util.AbstractList;
import java.util.ArrayList;

/**
 * @author jhostetler
 *
 */
public class Trajectory<S, A> extends AbstractList<Transition<S, A>>
{
	private final S s0;
	private final ArrayList<Transition<S, A>> ts;
	
	public Trajectory( final S s0, final ArrayList<Transition<S, A>> ts )
	{
		this.s0 = s0;
		this.ts = ts;
	}
	
	@Override
	public Transition<S, A> get( final int i )
	{
		return ts.get( i );
	}

	@Override
	public int size()
	{
		return ts.size();
	}
	
	public final double sumReward()
	{
		double r = 0;
		for( final Transition<S, A> t : ts ) {
			r += t.r;
		}
		return r;
	}
}
