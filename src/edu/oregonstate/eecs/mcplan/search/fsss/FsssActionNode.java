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
public class FsssActionNode<S extends State, A>
{
	private final FsssModel<S, A> model;
	private final S s;
	private final A a;
	
	private double U;
	private double L;
	
	private final ArrayList<FsssStateNode<S, A>> successors = new ArrayList<FsssStateNode<S, A>>();
	
	public FsssActionNode( final FsssModel<S, A> model, final S s, final A a )
	{
		this.model = model;
		this.s = s;
		this.a = a;
		this.U = model.Vmax();
		this.L = model.Vmin();
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
	
	public FsssStateNode<S, A> sstar()
	{
		FsssStateNode<S, A> sstar = null;
		double bstar = 0;
		for( final FsssStateNode<S, A> s : successors() ) {
			final double b = s.U() - s.L();
			assert( b >= 0 );
			if( b > bstar ) {
				bstar = b;
				sstar = s;
			}
		}
		assert( sstar != null );
		return sstar;
	}
	
	public void backup()
	{
		double u = 0;
		double l = 0;
		for( final FsssStateNode<S, A> sn : successors() ) {
			u += sn.U();
			l += sn.L();
		}
		U = model.discount() * u / model.width();
		L = model.discount() * l / model.width();
	}
	
	public Iterable<FsssStateNode<S, A>> successors()
	{
		return successors;
	}
	
	public void expand()
	{
		for( int i = 0; i < model.width(); ++i ) {
			final FsssStateNode<S, A> snprime = model.sampleTransition( s, a );
			successors.add( snprime );
		}
	}
	
	public void leaf()
	{
		L = model.reward( s, a );
		U = L;
	}
}
