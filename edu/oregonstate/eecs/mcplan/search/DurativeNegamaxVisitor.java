/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import edu.oregonstate.eecs.mcplan.agents.galcon.DurativeUndoableAction;
import edu.oregonstate.eecs.mcplan.agents.galcon.UndoableAction;

/**
 * FIXME: There's really no way to implement this correctly, since we need
 * access to the primitive actions or we won't call methods on the base_
 * visitor often enough.
 * 
 * It might work better if the NegamaxVisitor drove the DurativeNegamaxVisitor.
 * 
 * A better possibility might be to split Visitor into the "visit" part
 * and the "search control" part. The search control functions still work
 * fine in the durative setting; its the "visit" functions that don't. That
 * way, the base visitor could listen to the base simulator, and this
 * adapter would only affect the search control functions.
 */
public class DurativeNegamaxVisitor<S, A extends UndoableAction<S, A>>
	implements NegamaxVisitor<S, DurativeUndoableAction<S, A>>, DepthRecorder
{
	private final NegamaxVisitor<S, A> base_;
	private int depth_ = -1;
	private long time_ = 0;
	private final Deque<Long> time_stack_ = new ArrayDeque<Long>();
	
	public DurativeNegamaxVisitor( final NegamaxVisitor<S, A> base )
	{
		base_ = base;
		time_stack_.push( 0L );
	}
	
	/**
	 * Returns the accumulated time since starting the search. Time advances
	 * on TreeEdge and retreats on FinishVertex.
	 * 
	 * Note that this does not necessarily correspond to game clock time,
	 * because it might be a simultaneous move game. time() returns the number
	 * of ticks *if the agents had moved sequentially*.
	 * 
	 * @return
	 */
	protected long time()
	{
		return time_;
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
	public final boolean discoverVertex( final S v )
	{
		depth_ += 1;
		return base_.discoverVertex( v ); // FIXME: Not correct
	}

	@Override
	public void examineEdge( final DurativeUndoableAction<S, A> e, final S dest )
	{
		// FIXME: Not (practically) implementable
		final long t = e.T_;
		time_ += t;
		time_stack_.push( t );
		assert( time_stack_.size() == depth_ + 2 );
	}

	@Override
	public final void treeEdge( final DurativeUndoableAction<S, A> e, final S dest )
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
	public final void finishVertex( final S v )
	{
		base_.finishVertex( v ); // FIXME: Not correct
		depth_ -= 1;
		final long t = time_stack_.pop();
		time_ -= t;
	}

	@Override
	public void depthLimit( final S v )
	{
		base_.depthLimit( v ); // FIXME: Not correct
	}

	@Override
	public double goal( final S v )
	{
		return base_.goal( v ); // FIXME: Not correct
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
	public Iterator<DurativeUndoableAction<S, A>> orderActions(
		final S s, final Iterator<DurativeUndoableAction<S, A>> itr )
	{
		return itr;
	}

	@Override
	public int getDepth()
	{
		return depth_ / 2; // TODO: Generalize to any number of players.
	}

}
