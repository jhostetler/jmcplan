/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * A Policy that uses a GameTreeSearch to choose an action.
 * 
 * TODO: Maybe the TimeLimitMctsVisitor shouldn't be baked in to this class.
 * 
 * @author jhostetler
 */
public abstract class SearchPolicy<S, A extends VirtualConstructor<A>>
	extends Policy<S, JointAction<A>>
{
	private S s_ = null;
	private long t_ = 0L;
	
	protected abstract GameTree<S, A> initializeSearch( final S s, final long t );
	protected abstract JointAction<A> selectAction( final GameTree<S, A> tree );
	
	@Override
	public void setState( final S s, final long t )
	{
		s_ = s;
		t_ = t;
	}

	@Override
	public JointAction<A> getAction()
	{
		final GameTree<S, A> search = initializeSearch( s_, t_ );
		search.run();
		return selectAction( search );
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{ }

	@Override
	public String getName()
	{
		return "SearchPolicy";
	}
}
