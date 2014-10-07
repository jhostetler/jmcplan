/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Generator;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * @author jhostetler
 *
 */
public abstract class MutableActionNode<S, A extends VirtualConstructor<A>>
	extends ActionNode<S, A>
{
	protected static final class StateTuple<S>
	{
		public final Representation<S> x;
		public final int[] turn;
		
		public StateTuple( final Representation<S> x, final int[] turn )
		{
			this.x = x;
			this.turn = turn;
		}
		
		@Override
		public int hashCode()
		{
			return 181 * (x.hashCode() + 191 * Arrays.hashCode( turn ));
		}
		
		@Override
		public boolean equals( final Object obj )
		{
			if( obj == null || !(obj instanceof StateTuple<?>) ) {
				return false;
			}
			final StateTuple<?> that = (StateTuple<?>) obj;
			return x.equals( that.x ) && Arrays.equals( turn, that.turn );
		}
	}
	
	protected final Representer<S, ? extends Representation<S>> repr_;
	
	protected int n_ = 0;
	protected final MeanVarianceAccumulator[] qv_;
	protected final MeanVarianceAccumulator[] rv_;
	protected final Map<StateTuple<S>, MutableStateNode<S, A>> s_
		= new HashMap<StateTuple<S>, MutableStateNode<S, A>>();
	
	public MutableActionNode( final JointAction<A> a, final int nagents,
							  final Representer<S, ? extends Representation<S>> repr )
	{
		super( a, nagents );
		repr_ = repr;
		qv_ = new MeanVarianceAccumulator[nagents];
		rv_ = new MeanVarianceAccumulator[nagents];
		for( int i = 0; i < nagents; ++i ) {
			qv_[i] = new MeanVarianceAccumulator();
			rv_[i] = new MeanVarianceAccumulator();
		}
	}
	
	@Override
	public String toString()
	{
		return "ActionNode[" + a() + "]";
	}
	
	public abstract MutableActionNode<S, A> create();
	
	/**
	 * @param token
	 * @param nagents
	 * @param turn
	 * @param action_gen Will be copied if it is needed.
	 * @return
	 */
	public abstract MutableStateNode<S, A> successor(
			final S s, final int nagents, final int[] turn, final ActionGenerator<S, JointAction<A>> action_gen );
	
	public Representer<S, ? extends Representation<S>> repr()
	{
		return repr_;
	}
	
	protected void attachSuccessor( final Representation<S> x, final int[] turn, final MutableStateNode<S, A> node )
	{
		s_.put( new StateTuple<S>( x, turn ), node );
	}
	
	public void visit()
	{ n_ += 1; }
	
//	@Override
	public MutableStateNode<S, A> getStateNode( final Representation<S> x, final int[] turn )
	{
		return s_.get( new StateTuple<S>( x, turn ) );
	}
	
	@Override
	public Generator<MutableStateNode<S, A>> successors()
	{
		return Generator.fromIterator( s_.values().iterator() );
	}
	
	@Override
	public int n()
	{ return n_; }
	
	@Override
	public double[] r()
	{
		final double[] r = new double[rv_.length];
		for( int i = 0; i < r.length; ++i ) {
			r[i] = rv_[i].mean();
		}
		return r;
	}
	
	@Override
	public double r( final int i )
	{
		return rv_[i].mean();
	}
	
	@Override
	public double[] rvar()
	{
		final double[] r = new double[rv_.length];
		for( int i = 0; i < r.length; ++i ) {
			r[i] = rv_[i].variance();
		}
		return r;
	}
	
	@Override
	public double rvar( final int i )
	{
		return rv_[i].variance();
	}
	
	public void updateR( final double[] r )
	{
//		if( r.length != rv_.length ) {
//			System.out.println( Arrays.toString( r ) );
//			System.out.println( Arrays.toString( rv_ ) );
//		}
		
		assert( r.length == rv_.length );
		for( int i = 0; i < r.length; ++i ) {
			rv_[i].add( r[i] );
		}
	}
	
	@Override
	public double[] q()
	{
		final double[] q = new double[qv_.length];
		for( int i = 0; i < q.length; ++i ) {
			q[i] = qv_[i].mean();
		}
		return q;
	}
	
	@Override
	public double q( final int i )
	{
		return qv_[i].mean();
	}
	
	@Override
	public double[] qvar()
	{
		final double[] q = new double[qv_.length];
		for( int i = 0; i < q.length; ++i ) {
			q[i] = qv_[i].variance();
		}
		return q;
	}
	
	@Override
	public double qvar( final int i )
	{
		return qv_[i].variance();
	}
	
	public void updateQ( final double[] q )
	{
		assert( q.length == qv_.length );
		for( int i = 0; i < q.length; ++i ) {
			qv_[i].add( q[i] );
		}
	}

}
