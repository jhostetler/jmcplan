package edu.oregonstate.eecs.mcplan.search;

import java.util.Iterator;

import edu.oregonstate.eecs.mcplan.DurativeUndoableAction;
import edu.oregonstate.eecs.mcplan.UndoableAction;

public class DurativePvMoveOrdering<S, A extends UndoableAction<S, A>>
	extends DurativeNegamaxVisitor<S, A>
{
	private final PrincipalVariation<S, DurativeUndoableAction<S, A>> pv_;
	private final int[] epochs_;

	public DurativePvMoveOrdering( final NegamaxVisitor<S, A> base,
								   final PrincipalVariation<S, DurativeUndoableAction<S, A>> pv,
								   final int[] epochs )
	{
		super( base );
		pv_ = pv;
		epochs_ = epochs;
	}

	@Override
	public Iterator<DurativeUndoableAction<S, A>> orderActions(
		final S s, final Iterator<DurativeUndoableAction<S, A>> itr )
	{
		long t = 0;
		for( int i = 0; i < pv_.cmove; ++i ) {
			final DurativeUndoableAction<S, A> a = pv_.actions.get( i );
			t += a.T_;
			if( t > time() ) {
//				System.out.println( "[DurativePvMoveOrdering] PV action: " + a.toString() );
				return new DistinguishedElementIterator<DurativeUndoableAction<S, A>>(
					itr, new DurativeUndoableAction<S, A>( a.policy_, epochs_[getDepth()] ) );
			}
		}
//		System.out.println( "[DurativePvMoveOrdering] ! No PV action" );
		return itr;
	}
}
