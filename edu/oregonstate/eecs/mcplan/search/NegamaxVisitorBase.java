/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.util.Iterator;


/**
 * @author jhostetler
 *
 */
public class NegamaxVisitorBase<Vertex, Edge> implements NegamaxVisitor<Vertex, Edge>
{
	@Override
	public void initializeVertex( final Vertex v )
	{ }

	@Override
	public void startVertex( final Vertex v )
	{ }

	@Override
	public boolean discoverVertex( final Vertex v )
	{
		return false;
	}

	@Override
	public void examineEdge( final Edge e, final Vertex dest )
	{ }

	@Override
	public void treeEdge( final Edge e, final Vertex dest )
	{ }

	@Override
	public void prunedEdge( final Edge e, final Vertex dest )
	{ }
	
	@Override
	public void principalVariation( final PrincipalVariation<Vertex, Edge> pv )
	{ }

	@Override
	public void finishVertex( final Vertex v )
	{ }

	@Override
	public void depthLimit( final Vertex v )
	{ }

	@Override
	public double goal( final Vertex v )
	{
		return 0.0;
	}

	@Override
	public boolean isGoal( final Vertex v )
	{
		return false;
	}

	@Override
	public double heuristic(final Vertex v)
	{
		return 0.0;
	}
	
	@Override
	public Iterator<Edge> orderActions( final Vertex v, final Iterator<Edge> itr )
	{
		return itr;
	}
}
