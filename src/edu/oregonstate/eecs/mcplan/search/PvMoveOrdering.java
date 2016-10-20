/* LICENSE
Copyright (c) 2013-2016, Jesse Hostetler (jessehostetler@gmail.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.UndoableAction;

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
	public void setState( final S s, final long t, final int turn )
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
		inner_.setState( s, t, turn );
	}

	@Override
	public int size()
	{
		return inner_.size();
	}

}
