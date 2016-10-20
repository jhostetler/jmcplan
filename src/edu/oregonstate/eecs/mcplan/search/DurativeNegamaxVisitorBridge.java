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

public class DurativeNegamaxVisitorBridge<S, A extends UndoableAction<S, A>>
	implements NegamaxVisitor<S, A>
{
	private final NegamaxVisitor<S, A> primitive_visitor_;
	private final NegamaxVisitor<S, DurativeAction<S, A>> durative_visitor_;
	
	private class SpecialDurativeVisitor implements NegamaxVisitor<S, DurativeAction<S, A>>
	{
		// These forward to durative_visitor_
		
		@Override
		public void initializeVertex( final S v )
		{
			durative_visitor_.initializeVertex( v );
		}

		@Override
		public void startVertex( final S v )
		{
			durative_visitor_.startVertex( v );
		}

		@Override
		public boolean discoverVertex( final S v )
		{
			return durative_visitor_.discoverVertex( v );
		}

		@Override
		public void examineEdge( final DurativeAction<S, A> e, final S dest )
		{
			durative_visitor_.examineEdge( e, dest );
		}

		@Override
		public void treeEdge( final DurativeAction<S, A> e, final S dest )
		{
			durative_visitor_.treeEdge( e, dest );
		}

		@Override
		public void prunedEdge( final DurativeAction<S, A> e, final S dest )
		{
			durative_visitor_.prunedEdge( e, dest );
		}

		@Override
		public void principalVariation(
				final PrincipalVariation<S, DurativeAction<S, A>> pv )
		{
			durative_visitor_.principalVariation( pv );
		}

		@Override
		public void finishVertex( final S v )
		{
			durative_visitor_.finishVertex( v );
		}

		@Override
		public void depthLimit( final S v )
		{
			durative_visitor_.depthLimit( v );
		}
		
		@Override
		public Iterator<DurativeAction<S, A>> orderActions(
			final S v, final Iterator<DurativeAction<S, A>> itr )
		{
			return durative_visitor_.orderActions( v, itr );
		}
		
		// These forward to primitive_visitor_

		@Override
		public double goal( final S v )
		{
			return primitive_visitor_.goal( v );
		}

		@Override
		public boolean isGoal( final S v )
		{
			return primitive_visitor_.isGoal( v );
		}

		@Override
		public double heuristic( final S v )
		{
			return primitive_visitor_.heuristic( v );
		}
	}
	
	public DurativeNegamaxVisitorBridge( final NegamaxVisitor<S, A> primitive_visitor,
										 final NegamaxVisitor<S, DurativeAction<S, A>> durative_visitor,
										 final int[] schedule )
	{
		primitive_visitor_ = primitive_visitor;
		durative_visitor_ = durative_visitor;
	}

	@Override
	public void initializeVertex( final S v )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startVertex( final S v )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean discoverVertex( final S v )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void examineEdge( final A e, final S dest )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void treeEdge( final A e, final S dest )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void prunedEdge( final A e, final S dest )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void principalVariation( final PrincipalVariation<S, A> pv )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finishVertex( final S v )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void depthLimit( final S v )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public double goal( final S v )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isGoal( final S v )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double heuristic( final S v )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Iterator<A> orderActions( final S v, final Iterator<A> itr )
	{
		// TODO Auto-generated method stub
		return null;
	}
}
