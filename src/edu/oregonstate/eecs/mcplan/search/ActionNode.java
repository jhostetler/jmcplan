/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public abstract class ActionNode<S, A extends VirtualConstructor<A>>
	extends GameTreeNode<S, A>
{
	private final JointAction<A> a_;
	public final int nagents;
	
	public ActionNode( final A... a )
	{
		this( new JointAction<A>( a ) );
	}
	
	public ActionNode( final JointAction<A> a )
	{
		a_ = a;
		nagents = a_.size();
	}
	
	// FIXME: This is a temporary fix
	protected ActionNode( final JointAction<A> a, final int nagents )
	{
		a_ = a;
		this.nagents = nagents;
	}
	
	public final A a( final int i )
	{ return a_.get( i ); }
	
	public final JointAction<A> a()
	{ return a_; }
	
//	public abstract StateNode<S, A> getStateNode( final Representation<S> x, final int[] turn );
	
	@Override
	public abstract Generator<? extends StateNode<S, A>> successors();
	
	public abstract int n();
	
	public abstract double[] r();
	
	public abstract double r( final int i );
	
	public abstract double[] rvar();
	
	public abstract double rvar( final int i );
	
	// FIXME: In the literature, it's always:
	// Q(s, a) = R(s, a) + E[V(s')|a]
	// But currently, q() returns only the E[V(s')|a] part! It should have
	// r() added to it, and you should come up with different method names
	// if you want to be able to return the components separately!
	public abstract double[] q();
	
	public abstract double q( final int i );
	
	public abstract double[] qvar();
	
	public abstract double qvar( final int i );
	
	@Override
	public String toString()
	{ return (a_ != null ? a_.toString() : "null"); }

	@Override
	public void accept( final GameTreeVisitor<S, A> visitor )
	{ visitor.visit( this ); }
}
