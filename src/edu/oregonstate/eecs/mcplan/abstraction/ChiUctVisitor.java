/**
 * 
 */
package edu.oregonstate.eecs.mcplan.abstraction;

import weka.classifiers.Classifier;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.search.MctsVisitor;
import edu.oregonstate.eecs.mcplan.search.MutableActionNode;
import edu.oregonstate.eecs.mcplan.search.MutableStateNode;

/**
 * @author jhostetler
 *
 */
public class ChiUctVisitor<S, A extends VirtualConstructor<A>>
	implements MctsVisitor<S, FactoredRepresentation<S>, A>
{
	private final Classifier classifier_;
	
	public ChiUctVisitor( final Classifier classifier )
	{
		classifier_ = classifier;
	}
	
	@Override
	public void visitNode( final MutableStateNode<S, FactoredRepresentation<S>, A> sn )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void leaveNode( final MutableStateNode<S, FactoredRepresentation<S>, A> sn )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitNode( final MutableActionNode<S, FactoredRepresentation<S>, A> an )
	{
		if( an.)
	}

	@Override
	public void leaveNode( final MutableActionNode<S, FactoredRepresentation<S>, A> an )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startEpisode( final S s, final int nagents, final int[] turn )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean startRollout( final S s, final int[] turn )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void startTree( final S s, final int[] turn )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void treeAction( final JointAction<A> a, final S sprime, final int[] next_turn )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void treeDepthLimit( final S s, final int[] turn )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startDefault( final S s, final int[] turn )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void defaultAction( final JointAction<A> a, final S sprime, final int[] next_turn )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void defaultDepthLimit( final S s, final int[] turn )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void depthLimit( final S s, final int[] turn )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void checkpoint()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean halt()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
