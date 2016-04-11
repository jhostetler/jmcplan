/**
 * 
 */
package edu.oregonstate.eecs.mcplan.sim;

/**
 * @author jhostetler
 *
 */
public class SumRewardsVisitor<S, A> extends StateActionGraphVisitor<S, A>
{
	private double r = 0;
	
	public double getSum()
	{
		return r;
	}
	
	@Override
	public void visitStateNode( final StateNode<S, A> sn )
	{
		r += sn.r;
	}
	
	@Override
	public void visitActionNode( final ActionNode<S, A> an )
	{
		r += an.r;
	}
}
