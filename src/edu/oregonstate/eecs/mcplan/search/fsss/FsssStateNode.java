/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.State;


/**
 * @author jhostetler
 *
 */
public class FsssStateNode<S extends State, A>
{
	private final FsssModel<S, A> model;
	private final S s;
	private int n = 0;
	
	private double U;
	private double L;
	
	private final ArrayList<FsssActionNode<S, A>> successors = new ArrayList<FsssActionNode<S, A>>();
	
	public FsssStateNode( final FsssModel<S, A> model, final S s )
	{
		this.model = model;
		this.s = s;
		this.U = model.Vmax();
		this.L = model.Vmin();
	}
	
	public S s()
	{
		return s;
	}
	
	public int n()
	{
		return n;
	}
	
	public void visit()
	{
		n += 1;
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
		U = max_u;
		L = max_l;
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
	
	public Iterable<FsssActionNode<S, A>> successors()
	{
		return successors;
	}
	
	public void expand( final Iterable<A> actions )
	{
		for( final A a : actions ) {
//			System.out.println( "StateNode.expand(): Action " + a );
			final FsssActionNode<S, A> an = new FsssActionNode<S, A>( model, s, a );
			successors.add( an );
			an.expand();
		}
	}
	
	public void leaf( final Iterable<A> actions )
	{
		for( final A a : actions ) {
			final FsssActionNode<S, A> an = new FsssActionNode<S, A>( model, s, a );
			successors.add( an );
			an.leaf();
		}
	}
}
