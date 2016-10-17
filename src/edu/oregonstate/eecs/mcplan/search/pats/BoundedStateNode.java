/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.pats;

import java.util.ArrayList;

import com.google.common.collect.Iterables;

import edu.oregonstate.eecs.mcplan.State;

/**
 * @author jhostetler
 *
 */
public class BoundedStateNode<S extends State, A> implements AutoCloseable
{
	public final S s;
	public final double r;
	private double U;
	private double L;
	public final int depth;
	
	private final ArrayList<BoundedActionNode<S, A>> successors = new ArrayList<>();
	
	//FIXME: Memory debugging
//	public final char[] deadweight = new char[1000];
	
	public BoundedStateNode( final BoundedActionNode<S, A> predecessor,
							 final S s, final double r, final BoundedValueModel<S, A> bounds )
	{
		this.s = s;
		this.r = r;
		this.U = bounds.U( s );
		this.L = bounds.L( s );
		this.depth = predecessor.depth - 1;
	}
	
	public boolean isLeaf()
	{
		return depth == 0 || s.isTerminal();
	}
	
	public Iterable<BoundedActionNode<S, A>> successors()
	{
		return Iterables.unmodifiableIterable( successors );
	}
	
	public int Nsuccessors()
	{
		return successors.size();
	}
	
	@Override
	public void close()
	{
		s.close();
		
		for( final BoundedActionNode<S, A> an : successors ) {
			an.close();
		}
		successors.clear();
		successors.trimToSize();
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "[@" ).append( Integer.toHexString( hashCode() ) )
		  .append( ": " ).append( s )
		  .append( "; r: " ).append( r )
		  .append( "; U: " ).append( U )
		  .append( "; L: " ).append( L )
		  .append( "]" );
		return sb.toString();
	}
	
	public S s()
	{
		return s;
	}
	
	public double U()
	{
		return U;
	}
	
	public double L()
	{
		return L;
	}
	
	public void backup()
	{
		assert( Nsuccessors() > 0 );
		double max_u = -Double.MAX_VALUE;
		double max_l = -Double.MAX_VALUE;
		for( final BoundedActionNode<S, A> an : successors() ) {
			final double u = an.U();
			if( u > max_u ) {
				max_u = u;
			}
			
			final double l = an.L();
			if( l > max_l ) {
				max_l = l;
			}
		}
		U = r + max_u;
		L = r + max_l;
	}
	
	/**
	 * Returns all actions that achieve the maximum value of U(s, a).
	 * @return
	 */
	public ArrayList<BoundedActionNode<S, A>> greatestUpperBound()
	{
		final ArrayList<BoundedActionNode<S, A>> best = new ArrayList<>();
		double Ustar = -Double.MAX_VALUE;
		for( final BoundedActionNode<S, A> an : successors() ) {
			final double U = an.U();
			if( U > Ustar ) {
				Ustar = U;
				best.clear();
				best.add( an );
			}
			else if( U >= Ustar ) {
				best.add( an );
			}
		}
		return best;
	}
	
	/**
	 * Returns all actions that achieve the maximum value of L(s, a).
	 * @return
	 */
	public ArrayList<BoundedActionNode<S, A>> greatestLowerBound()
	{
		final ArrayList<BoundedActionNode<S, A>> best = new ArrayList<>();
		double Lstar = -Double.MAX_VALUE;
		for( final BoundedActionNode<S, A> an : successors() ) {
			final double L = an.L();
			if( L > Lstar ) {
				Lstar = L;
				best.clear();
				best.add( an );
			}
			else if( L >= Lstar ) {
				best.add( an );
			}
		}
		return best;
	}
}
