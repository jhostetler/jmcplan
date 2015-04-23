/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Fn;


/**
 * @author jhostetler
 *
 */
public class FsssStateNode<S extends State, A extends VirtualConstructor<A>>
{
	private final FsssActionNode<S, A> predecessor;
	private final FsssModel<S, A> model;
	private final S s;
	private final FactoredRepresentation<S> x;
	private int nvisits = 0;
	
	private double U;
	private double L;
	public final double r;
	public final int depth;
	
	private final ArrayList<FsssActionNode<S, A>> successors = new ArrayList<FsssActionNode<S, A>>();
	
	public FsssStateNode( final FsssActionNode<S, A> predecessor, final FsssModel<S, A> model, final S s )
	{
		this.predecessor = predecessor;
		this.model = model;
		this.s = s;
		this.x = model.base_repr().encode( s );
		this.r = model.reward( s );
		this.U = model.Vmax( s );
		this.L = model.Vmin( s );
		this.depth = predecessor.depth - 1;
	}
	
	public FsssStateNode( final int depth, final FsssModel<S, A> model, final S s )
	{
		this.predecessor = null;
		this.model = model;
		this.s = s;
		this.x = model.base_repr().encode( s );
		this.r = model.reward( s );
		this.U = model.Vmax( s );
		this.L = model.Vmin( s );
		this.depth = depth;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "[@" ).append( Integer.toHexString( hashCode() ) )
		  .append( ": " ).append( s )
		  .append( "; nvisits: " ).append( nvisits )
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
	
	public FactoredRepresentation<S> x()
	{
		return x;
	}
	
//	public int n()
//	{
//		return n;
//	}
	
	public int nvisits()
	{
		return nvisits;
	}
	
	public void visit()
	{
		nvisits += 1;
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
		assert( nsuccessors() > 0 );
		double max_u = -Double.MAX_VALUE;
		double max_l = -Double.MAX_VALUE;
		for( final FsssActionNode<S, A> an : successors() ) {
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
	
	public FsssActionNode<S, A> astar()
	{
		FsssActionNode<S, A> astar = null;
		double ustar = -Double.MAX_VALUE;
		for( final FsssActionNode<S, A> an : successors() ) {
			final double u = an.U();
			if( u > ustar ) {
				ustar = u;
				astar = an;
			}
		}
		assert( astar != null );
		return astar;
	}
	
	/**
	 * Successors are returned in insertion order.
	 * @return
	 */
	public Iterable<FsssActionNode<S, A>> successors()
	{
		return successors;
	}
	
	public int nsuccessors()
	{
		return successors.size();
	}
	
	public void expand( final Iterable<A> actions, final int width )
	{
		createActionNodes( actions );
		for( final FsssActionNode<S, A> an : successors() ) {
			for( int i = 0; i < width; ++i ) {
				an.sample();
			}
		}
	}
	
	public FsssActionNode<S, A> createActionNode( final A a )
	{
		// TODO: Debugging code
		if( !Fn.takeAll( model.actions( s() ) ).contains( a ) ) {
			System.out.println( "!\t In GSN " + this + ": illegal action " + a );
			assert( false );
		}
		
		final FsssActionNode<S, A> an = new FsssActionNode<S, A>( this, model, s, a );
		successors.add( an );
		return an;
	}
	
	public void createActionNodes( final Iterable<A> actions )
	{
		for( final A a : actions ) {
//			System.out.println( "StateNode.expand(): Action " + a );
			final FsssActionNode<S, A> an = new FsssActionNode<S, A>( this, model, s, a );
			successors.add( an );
		}
	}
	
	public void sample()
	{
		for( final FsssActionNode<S, A> an : successors() ) {
			an.sample();
		}
	}
	
	public void leaf()
	{
		final double V = r + model.heuristic( s );
		U = L = V;
	}
	
	public boolean isTerminal()
	{
		return depth == 1 || s.isTerminal();
	}
}
