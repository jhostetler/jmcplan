/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.util.ArrayList;
import java.util.ListIterator;

import edu.oregonstate.eecs.mcplan.util.ListUtil;

/**
 * @author jhostetler
 *
 */
public class PrincipalVariation<S, A>
{
//	public final LinkedList<S> states = new LinkedList<S>();
//	public final LinkedList<A> actions = new LinkedList<A>();
//	public int d = 0;
//	public double score = 0.0;
	
	public S s0;
	public final ArrayList<String> states = new ArrayList<String>();
	public final ArrayList<A> actions = new ArrayList<A>();
	public double alpha = -Double.MAX_VALUE;
	public double beta = Double.MAX_VALUE;
	public double score = 0.0;
	public int cmove = 0;
	public final int max_depth;
	
	public PrincipalVariation( final int max_depth )
	{
		this.max_depth = max_depth;
		ListUtil.populateList( states, null, this.max_depth + 1 );
		ListUtil.populateList( actions, null, this.max_depth );
	}
	
	public PrincipalVariation( final PrincipalVariation<S, A> that )
	{
		s0 = that.s0;
		states.addAll( that.states );
		actions.addAll( that.actions );
		alpha = that.alpha;
		beta = that.beta;
		score = that.score;
		cmove = that.cmove;
		max_depth = that.max_depth;
	}
	
	public boolean isNarrowerThan( final PrincipalVariation<S, A> that )
	{
		if( alpha > beta || that.alpha > that.beta ) {
			System.out.println( "!!! alpha = " + alpha + ", beta = " + beta );
			System.out.println( "!!! that.alpha = " + that.alpha + ", that.beta = " + that.beta );
			throw new AssertionError();
		}
		return (alpha > that.alpha && beta <= that.beta)
			|| (alpha >= that.alpha && beta < that.beta);
	}
	
	public void setState( final int idx, final String state_token )
	{
		states.set( idx, state_token );
	}
	
	public ArrayList<String> getStates()
	{
		return states;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "[" ).append( score ).append( "] " );
		sb.append( "[" ).append( alpha ).append( ", " ).append( beta ).append( "] " );
		final ListIterator<String> sitr = states.listIterator();
		if( sitr.hasNext() ) {
			sb.append( sitr.next() );
		}
		else {
			return "";
		}
		final ListIterator<A> aitr = actions.listIterator();
		while( sitr.hasNext() ) {
			sb.append( " -- " ).append( aitr.next() ).append( " -> " ).append( sitr.next() );
		}
		return sb.toString();
	}
}
