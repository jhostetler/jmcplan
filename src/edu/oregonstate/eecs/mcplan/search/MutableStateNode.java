/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

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
public abstract class MutableStateNode<S, A extends VirtualConstructor<A>>
	extends StateNode<S, A>
{
	protected final Map<JointAction<A>, MutableActionNode<S, A>> a_
		= new HashMap<JointAction<A>, MutableActionNode<S, A>>();
	protected int n_ = 0;
	protected final MeanVarianceAccumulator[] rv_;
	
	public final ActionGenerator<S, JointAction<A>> action_gen_;

	public MutableStateNode( /*final Representation<S> x,*/ final int nagents, final int[] turn,
							 final ActionGenerator<S, JointAction<A>> action_gen )
	{
		super( /*x,*/ nagents, turn );
		rv_ = new MeanVarianceAccumulator[nagents];
		for( int i = 0; i < nagents; ++i ) {
			rv_[i] = new MeanVarianceAccumulator();
		}
		action_gen_ = action_gen;
	}
	
	public abstract MutableActionNode<S, A> successor(
			final JointAction<A> a, final int nagents, final Representer<S, ? extends Representation<S>> repr );
	
	public void visit()
	{ n_ += 1; }
	
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
		assert( r.length == rv_.length );
		for( int i = 0; i < r.length; ++i ) {
			rv_[i].add( r[i] );
		}
	}
	
	public void attachSuccessor( final JointAction<A> a, final MutableActionNode<S, A> node )
	{
		a_.put( a, node );
	}
	
	public Map<JointAction<A>, MutableActionNode<S, A>> successor_map()
	{
		return a_;
	}
	
	@Override
	public Generator<MutableActionNode<S, A>> successors()
	{
		return Generator.fromIterator( a_.values().iterator() );
	}
	
	@Override
	public MutableActionNode<S, A> getActionNode( final JointAction<A> a )
	{
		final MutableActionNode<S, A> an = a_.get( a );
		return an;
	}
	
//	@Override
//	public String toString()
//	{
//		return "StateNode[" + x + "]";
//	}

}
