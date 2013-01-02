/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

/**
 * @author jhostetler
 *
 */
public interface DepthFirstVisitor<Vertex>
{
	public abstract void initializeVertex( final Vertex v );
	public abstract void startVertex( final Vertex v );
	public abstract void discoverVertex( final Vertex v );
	public abstract void examineEdge( final Vertex src, final Vertex dest );
	public abstract void treeEdge( final Vertex src, final Vertex dest );
	public abstract void backEdge( final Vertex src, final Vertex dest );
	public abstract void forwardOrCrossEdge( final Vertex src, final Vertex dest );
	public abstract void finishVertex( final Vertex v );
	public abstract void depthLimit( final Vertex v );
}
