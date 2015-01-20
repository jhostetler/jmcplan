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
	public final double r;
	
	private double U;
	private double L;
	
	private final ArrayList<FsssStateNode<S, A>> successors = new ArrayList<FsssStateNode<S, A>>();
	
	public FsssActionNode( final FsssModel<S, A> model, final S s, final A a )
	{
		this.model = model;
		this.s = s;
		this.a = a;
		this.r = model.reward( s, a );
		this.U = model.Vmax();
		this.L = model.Vmin();
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
		assert( nsuccessors() > 0 );
		
		double u = 0;
		double l = 0;
		for( final FsssStateNode<S, A> sn : successors() ) {
			u += sn.U();
			l += sn.L();
		}
		U = r + model.discount() * u / nsuccessors();
		L = r + model.discount() * l / nsuccessors();
	}
	
	/**
	 * Successors are returned in insertion order.
	 * @return
	 */
	public Iterable<FsssStateNode<S, A>> successors()
	{
		return successors;
	}
	
	public int nsuccessors()
	{
		return successors.size();
	}
	
	/**
	 * Samples a state transition, adds the result state to 'successors',
	 * and returns the result state.
	 * @return
	 */
	public FsssStateNode<S, A> sample()
	{
//		System.out.println( "Sampling " + a + " in " + s + "; nsuccessors = " + nsuccessors() );
		final S sprime = model.sampleTransition( s, a );
		final FsssStateNode<S, A> snprime = new FsssStateNode<S, A>( model, sprime );
		successors.add( snprime );
		return snprime;
	}
	
	public void leaf()
	{
		L = model.reward( s, a );
		U = L;
	}
}
