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
