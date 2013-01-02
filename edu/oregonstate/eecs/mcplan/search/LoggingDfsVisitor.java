/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.io.PrintStream;

/**
 * @author jhostetler
 *
 */
public class LoggingDfsVisitor<Vertex> implements DepthFirstVisitor<Vertex>
{
	private int depth_ = 0;
	private final PrintStream out_;
	
	/**
	 * 
	 */
	public LoggingDfsVisitor( final PrintStream out )
	{
		out_ = out;
	}

	@Override
	public void initializeVertex( final Vertex v ) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startVertex(Vertex v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void discoverVertex( final Vertex v )
	{
		for( int d = 0; d < depth_; ++d ) {
			out_.print( "-" );
		}
		out_.println( v.toString() );
		++depth_;
	}

	@Override
	public void examineEdge(Vertex src, Vertex dest) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void treeEdge(Vertex src, Vertex dest) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void backEdge(Vertex src, Vertex dest) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void forwardOrCrossEdge(Vertex src, Vertex dest) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finishVertex( final Vertex v )
	{
		--depth_;
	}

	@Override
	public void depthLimit( final Vertex v ){
		// TODO Auto-generated method stub
		
	}

}
