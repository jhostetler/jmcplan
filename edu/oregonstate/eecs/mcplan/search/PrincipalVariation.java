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
//		states.addAll( that.states );
//		actions.addAll( that.actions );
//		d = that.d;
//		score = that.score;
		
		throw new UnsupportedOperationException();
	}
	
	public void setState( final int idx, final S state )
	{
		states.set( idx, state.toString() );
	}
	
	public ArrayList<String> getStates()
	{
		return states;
	}
	
//	public void assign( final S s, final A a, final PrincipalVariation<S, A> future, final double score )
//	{
//		states.add( s );
//		states.addAll( future.states );
//		actions.add( a );
//		actions.addAll( future.actions );
//		this.score = score;
//	}
//
//	public void clear()
//	{
//		states.clear();
//		actions.clear();
//		d = 0;
//		score = 0.0;
//	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "[" ).append( score ).append( "] " );
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
