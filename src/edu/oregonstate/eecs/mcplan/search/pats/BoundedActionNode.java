/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.pats;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.State;

/**
 * @author jhostetler
 *
 */
public class BoundedActionNode<S extends State, A> implements AutoCloseable
{
	private final A a;
	private final double r;
	private final BoundedValueModel<S, A> model;
	private double U;
	private double L;
	public final int depth;
	
	private final ArrayList<BoundedStateNode<S, A>> successors = new ArrayList<>();
	
	public BoundedActionNode( final BoundedStateNode<S, A> predecessor,
							  final A a, final double r, final BoundedValueModel<S, A> model )
	{
		this.a = a;
		this.r = r;
		this.model = model;
		this.U = model.U( predecessor.s, a );
		this.L = model.L( predecessor.s, a );
		this.depth = predecessor.depth;
	}
	
	/**
	 * Successors are returned in insertion order.
	 * @return
	 */
	public Iterable<BoundedStateNode<S, A>> successors()
	{
		return successors;
	}
	
	public int Nsuccessors()
	{
		return successors.size();
	}
	
	@Override
	public void close()
	{
		successors.clear();
		successors.trimToSize();
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "[" ).append( a )
		  .append( "; r: " ).append( r )
		  .append( "; U: " ).append( U )
		  .append( "; L: " ).append( L )
		  .append( "]" );
		return sb.toString();
	}
	
	public A a()
	{
		return a;
	}
	
	public double U()
	{
		return U;
	}
	
	public double L()
	{
		return L;
	}
	
	public ArrayList<BoundedStateNode<S, A>> sstar()
	{
		final ArrayList<BoundedStateNode<S, A>> sstar = new ArrayList<>();
		double bstar = 0;
		for( final BoundedStateNode<S, A> succ : successors() ) {
			final double b = succ.U() - succ.L();
			assert( b >= 0 );
			if( b > bstar ) {
				bstar = b;
				sstar.clear();
				sstar.add( succ );
			}
			else if( b == bstar ) {
				sstar.add( succ );
			}
		}
		assert( !sstar.isEmpty() );
		return sstar;
	}
	
	public void backup()
	{
		assert( Nsuccessors() > 0 );
		
		double u = 0;
		double l = 0;
		for( final BoundedStateNode<S, A> sn : successors() ) {
			u += sn.U();
			l += sn.L();
		}
		U = r + model.discount() * u / Nsuccessors();
		L = r + model.discount() * l / Nsuccessors();
	}
}
