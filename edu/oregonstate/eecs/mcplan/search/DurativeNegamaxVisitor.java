/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.agents.galcon.DurativeUndoableAction;
import edu.oregonstate.eecs.mcplan.agents.galcon.UndoableAction;

/**
 * FIXME: There's really no way to implement this correctly, since we need
 * access to the primitive actions or we won't call methods on the base_
 * visitor often enough.
 * 
 * It might work better if the NegamaxVisitor drove the DurativeNegamaxVisitor.
 */
public class DurativeNegamaxVisitor<S, A extends UndoableAction<S, A>>
	implements NegamaxVisitor<S, DurativeUndoableAction<S, A>>, DepthRecorder
{
	private final NegamaxVisitor<S, A> base_;
	private int depth_ = -1;
	
	public DurativeNegamaxVisitor( final NegamaxVisitor<S, A> base )
	{
		base_ = base;
	}
	
	@Override
	public void initializeVertex( final S v )
	{
		base_.initializeVertex( v ); // FIXME: Not correct
	}

	@Override
	public void startVertex( final S v )
	{
		base_.startVertex( v );
	}

	@Override
	public boolean discoverVertex( final S v )
	{
		depth_ += 1;
		return base_.discoverVertex( v ); // FIXME: Not correct
	}

	@Override
	public void examineEdge( final DurativeUndoableAction<S, A> e, final S dest )
	{
		// FIXME: Not (practically) implementable
	}

	@Override
	public void treeEdge( final DurativeUndoableAction<S, A> e, final S dest )
	{
		// FIXME: Not (practically) implementable
	}

	@Override
	public void prunedEdge( final DurativeUndoableAction<S, A> e, final S dest )
	{
		// FIXME: Not (practically) implementable
	}

	@Override
	public void principalVariation(
			final PrincipalVariation<S, DurativeUndoableAction<S, A>> pv )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finishVertex( final S v )
	{
		base_.finishVertex( v ); // FIXME: Not correct
		depth_ -= 1;
	}

	@Override
	public void depthLimit( final S v )
	{
		base_.depthLimit( v ); // FIXME: Not correct
	}

	@Override
	public void goal( final S v )
	{
		base_.goal( v ); // FIXME: Not correct
	}

	@Override
	public boolean isGoal( final S v )
	{
		return base_.isGoal( v );
	}

	@Override
	public double heuristic( final S v )
	{
		return base_.heuristic( v );
	}

	@Override
	public int getDepth()
	{
		return depth_ / 2; // TODO: Generalize to any number of players.
	}

}
