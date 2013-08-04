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
