/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.pats;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author jhostetler
 *
 */
public class SampleStateNode<S, A>
{
	public final S s;
	private final Map<A, SampleActionNode<S, A>> successors = new LinkedHashMap<>();
	
	private final int multiplicity = 0;
	
	private double U;
	private double L;
	public final double r;
	public final int depth;
	
	public SampleStateNode( final SampleActionNode<S, A> predecessor, final S s, final double r )
	{
		this.s = s;
		this.r = r;
		this.depth = predecessor.depth - 1;
	}
	
	public void addSuccessor( final SampleActionNode<S, A> an )
	{
		final SampleActionNode<S, A> prev = successors.put( an.a, an );
		assert( prev == null );
	}
	
	public SampleActionNode<S, A> successor( final A a )
	{ return actions.get( a ); }
}
