/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.util.HashMap;
import java.util.Map;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public abstract class MutableStateNode<S, X extends Representation<S>, A extends VirtualConstructor<A>>
	extends StateNode<X, A>
{
	private final Map<JointAction<A>, MutableActionNode<S, X, A>> a_
		= new HashMap<JointAction<A>, MutableActionNode<S, X, A>>();
	private int n_ = 0;

	public MutableStateNode( final X token, final int nagents, final int[] turn )
	{
		super( token, nagents, turn );
		// TODO Auto-generated constructor stub
	}
	
	public void setVhat( final double[] vhat )
	{
		Fn.memcpy( vhat_, vhat, nagents );
	}
	
	public void visit()
	{ n_ += 1; }
	
	@Override
	public int n()
	{ return n_; }
	
	public void attachSuccessor( final JointAction<A> a, final MutableActionNode<S, X, A> node )
	{
		a_.put( a, node );
	}
	
	@Override
	public Generator<MutableActionNode<S, X, A>> successors()
	{
		return Generator.fromIterator( a_.values().iterator() );
	}
	
	@Override
	public MutableActionNode<S, X, A> getActionNode( final JointAction<A> a )
	{
		final MutableActionNode<S, X, A> an = a_.get( a );
		return an;
	}

}
