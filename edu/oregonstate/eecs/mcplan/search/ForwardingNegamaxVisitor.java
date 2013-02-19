/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.util.Iterator;

/**
 * @author jhostetler
 *
 */
public class ForwardingNegamaxVisitor<Vertex, Edge> implements NegamaxVisitor<Vertex, Edge>
{
	protected NegamaxVisitor<Vertex, Edge> inner_;
	
	public ForwardingNegamaxVisitor( final NegamaxVisitor<Vertex, Edge> inner )
	{
		inner_ = inner;
	}
	
	@Override
	public void initializeVertex( final Vertex v )
	{ inner_.initializeVertex( v ); }

	@Override
	public void startVertex( final Vertex v )
	{ inner_.startVertex( v ); }

	@Override
	public boolean discoverVertex( final Vertex v )
	{ return inner_.discoverVertex( v ); }

	@Override
	public void examineEdge( final Edge e, final Vertex dest )
	{ inner_.examineEdge( e, dest ); }

	@Override
	public void treeEdge( final Edge e, final Vertex dest )
	{ inner_.treeEdge( e, dest ); }

	@Override
	public void prunedEdge( final Edge e, final Vertex dest )
	{ inner_.prunedEdge( e, dest ); }

	@Override
	public void principalVariation( final PrincipalVariation<Vertex, Edge> pv )
	{ inner_.principalVariation( pv ); }

	@Override
	public void finishVertex( final Vertex v )
	{ inner_.finishVertex( v ); }

	@Override
	public void depthLimit( final Vertex v )
	{ inner_.depthLimit( v ); }

	@Override
	public double goal( final Vertex v )
	{ return inner_.goal( v ); }

	@Override
	public boolean isGoal( final Vertex v )
	{ return inner_.isGoal( v ); }

	@Override
	public double heuristic( final Vertex v )
	{ return inner_.heuristic( v ); }
	
	@Override
	public Iterator<Edge> orderActions( Vertex v, final Iterator<Edge> itr )
	{ return inner_.orderActions( null, itr ); }
}
