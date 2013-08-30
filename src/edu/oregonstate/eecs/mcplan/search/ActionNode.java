/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Generator;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;
import edu.oregonstate.eecs.mcplan.util.Tuple.Tuple2;

/**
 * @author jhostetler
 *
 */
public class ActionNode<S, A extends VirtualConstructor<A>>
	implements GameTreeNode<S, A>
{
	public final A a;
	public final int nagents;
	private int n_ = 0;
	private final MeanVarianceAccumulator[] qv_;
	private final MeanVarianceAccumulator[] rv_;
	private final Map<Tuple2<S, Integer>, StateNode<S, A>> s_
		= new HashMap<Tuple2<S, Integer>, StateNode<S, A>>();
	
	public ActionNode( final A a, final int nagents )
	{
		assert( nagents == 2 ); // TODO:
		this.a = a;
		this.nagents = nagents;
		qv_ = new MeanVarianceAccumulator[nagents];
		rv_ = new MeanVarianceAccumulator[nagents];
		for( int i = 0; i < nagents; ++i ) {
			qv_[i] = new MeanVarianceAccumulator();
			rv_[i] = new MeanVarianceAccumulator();
		}
	}
	
	public StateNode<S, A> getStateNode( final S token, final int turn )
	{
		return s_.get( Tuple2.of( token, turn ) );
	}
	
	public StateNode<S, A> stateNode( final S token, final int turn )
	{
		StateNode<S, A> si = getStateNode( token, turn );
		if( si == null ) {
			si = new StateNode<S, A>( token, nagents, turn );
			s_.put( Tuple2.of( token, turn ), si );
		}
		return si;
	}
	
	@Override
	public Generator<StateNode<S, A>> successors()
	{
		return Generator.fromIterator( s_.values().iterator() );
	}
	
	public int n()
	{ return n_; }
	
	public void visit()
	{ n_ += 1; }
	
	public void updateR( final double[] r )
	{
		if( r.length != rv_.length ) {
			System.out.println( Arrays.toString( r ) );
			System.out.println( Arrays.toString( rv_ ) );
			assert( r.length == rv_.length );
		}
		
		for( int i = 0; i < r.length; ++i ) {
			rv_[i].add( r[i] );
		}
	}
	
	public double[] r()
	{
		final double[] r = new double[rv_.length];
		for( int i = 0; i < r.length; ++i ) {
			r[i] = rv_[i].mean();
		}
		return r;
	}
	
	public double r( final int i )
	{
		return rv_[i].mean();
	}
	
	public double[] rvar()
	{
		final double[] r = new double[rv_.length];
		for( int i = 0; i < r.length; ++i ) {
			r[i] = rv_[i].variance();
		}
		return r;
	}
	
	public double rvar( final int i )
	{
		return rv_[i].variance();
	}
	
	public void updateQ( final double[] q )
	{
		assert( q.length == qv_.length );
		for( int i = 0; i < q.length; ++i ) {
			qv_[i].add( q[i] );
		}
	}
	
	public double[] q()
	{
		final double[] q = new double[qv_.length];
		for( int i = 0; i < q.length; ++i ) {
			q[i] = qv_[i].mean();
		}
		return q;
	}
	
	public double q( final int i )
	{
		return qv_[i].mean();
	}
	
	public double[] qvar()
	{
		final double[] q = new double[qv_.length];
		for( int i = 0; i < q.length; ++i ) {
			q[i] = qv_[i].variance();
		}
		return q;
	}
	
	public double qvar( final int i )
	{
		return qv_[i].variance();
	}
	
	@Override
	public String toString()
	{ return a.toString(); }

	@Override
	public void accept( final GameTreeVisitor<S, A> visitor )
	{ visitor.visit( this ); }
}
