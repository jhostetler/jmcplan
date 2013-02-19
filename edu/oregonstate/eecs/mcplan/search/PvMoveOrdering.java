/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.agents.galcon.ActionGenerator;
import edu.oregonstate.eecs.mcplan.agents.galcon.UndoableAction;

/**
 * Implements the principal variation move ordering heuristic.
 * 
 * The actions in the provided PrincipalVariation will be tried before any
 * other actions in the appropriate states. Otherwise, action selection
 * is deferred to the "inner" ActionGenerator instance.
 */
public class PvMoveOrdering<S, A extends UndoableAction<S, A>> implements ActionGenerator<S, A>
{
	private final ActionGenerator<S, A> inner_;
	private final PrincipalVariation<S, A> pv_;
	private final boolean[] used_;
	private static final int PvIdxInvalid = -1;
	private int pv_idx_ = PvIdxInvalid;
	
	public PvMoveOrdering( final ActionGenerator<S, A> inner, final PrincipalVariation<S, A> pv )
	{
		inner_ = inner;
		pv_ = pv;
		used_ = new boolean[pv_.actions.size()];
	}
	
	@Override
	public boolean hasNext()
	{
		// FIXME: This gives incorrect results if the pv action is the only
		// action, since inner_ doesn't get advanced when the pv action is
		// executed.
		return inner_.hasNext();
	}

	@Override
	public A next()
	{
		if( pv_idx_ == PvIdxInvalid ) {
			// No PV action
			return inner_.next();
		}
		else if( !used_[pv_idx_] ) {
			used_[pv_idx_] = true;
			final A a = pv_.actions.get( pv_idx_ ).create();
//			System.out.println( "[PvMoveOrdering] In state " + pv_.states.get( pv_idx_ )
//								+ "; using PV move " + a.toString() );
			return a;
		}
		else {
			A a = null;
			do {
				a = inner_.next();
			} while( a.equals( pv_.actions.get( pv_idx_ ) ) );
			return a.create();
		}
	}

	@Override
	public ActionGenerator<S, A> create()
	{
		return new PvMoveOrdering<S, A>( inner_.create(), pv_ );
	}

	@Override
	public void remove()
	{
		inner_.remove();
	}

	@Override
	public void setState( final S s, final long t )
	{
		pv_idx_ = PvIdxInvalid;
		// size() - 1 because the end state has no associated action.
//		for( int i = 0; i < pv_.states.size() - 1; ++i ) {
		for( int i = 0; i < pv_.cmove; ++i ) {
			// FIXME: This relies on the (depth, turn) string encoding of
			// the state. The is a hackish way of doing it, which is necessary
			// only because FastGalconState is also the Simulator
			// for the galcon domain. If you designed it properly, by splitting
			// up the state and the simulator, you could make copies of states
			// and it would make sense to compare them to one another!
			//
			// Update: Actually, since these are simultaneous move games,
			// "turn" is a property of the simulator, not the state. But,
			// again, this is another reason why the simulator and the state
			// should be separate classes.
			if( s.toString().equals( pv_.states.get( i ) ) ) {
				pv_idx_ = i;
				break;
			}
		}
		assert( pv_idx_ < pv_.cmove );
		Arrays.fill( used_, false );
		inner_.setState( s, t );
	}

	@Override
	public int size()
	{
		return inner_.size();
	}

}
