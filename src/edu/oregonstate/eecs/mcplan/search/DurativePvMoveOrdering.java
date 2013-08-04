package edu.oregonstate.eecs.mcplan.search;

import java.util.Iterator;

import edu.oregonstate.eecs.mcplan.DurativeAction;
import edu.oregonstate.eecs.mcplan.UndoableAction;

public class DurativePvMoveOrdering<S, A extends UndoableAction<S, A>>
	extends DurativeNegamaxVisitor<S, A>
{
	private final PrincipalVariation<S, DurativeAction<S, A>> pv_;
	private final int[] epochs_;

	public DurativePvMoveOrdering( final NegamaxVisitor<S, A> base,
								   final PrincipalVariation<S, DurativeAction<S, A>> pv,
								   final int[] epochs )
	{
		super( base );
		pv_ = pv;
		epochs_ = epochs;
	}

	@Override
	public Iterator<DurativeAction<S, A>> orderActions(
		final S s, final Iterator<DurativeAction<S, A>> itr )
	{
		int T = 0;
		for( int i = 0; i < getDepth(); ++i ) {
			T += epochs_[i];
		}
		int t = 0;
		for( int i = turn(); i < pv_.cmove; i += 2 ) {
			final DurativeAction<S, A> a = pv_.actions.get( i );
			t += a.T_;
			if( t > T ) {
//				System.out.println( "[DurativePvMoveOrdering] PV action: " + a.toString() );
				return new DistinguishedElementIterator<DurativeAction<S, A>>(
					itr, new DurativeAction<S, A>( a.policy_, epochs_[getDepth()] ) );
			}
		}
//		System.out.println( "[DurativePvMoveOrdering] ! No PV action" );
		return itr;
	}
}
