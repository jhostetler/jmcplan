/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;


/**
 * @author jhostetler
 *
 */
public interface NegamaxVisitor<Vertex, Edge>
{
	public abstract void initializeVertex( final Vertex v );
	public abstract void startVertex( final Vertex v );
	public abstract void discoverVertex( final Vertex v );
	public abstract void examineEdge( final Edge e, final Vertex dest );
	public abstract void treeEdge( final Edge e, final Vertex dest );
	public abstract void prunedEdge( final Edge e, final Vertex dest );
	public abstract void principalVariation( final PrincipalVariation<Vertex, Edge> pv );
	public abstract void finishVertex( final Vertex v );
	public abstract void depthLimit( final Vertex v );
	public abstract void goal( final Vertex v );
	public abstract boolean isGoal( final Vertex v );
	public abstract double heuristic( final Vertex v );
}
