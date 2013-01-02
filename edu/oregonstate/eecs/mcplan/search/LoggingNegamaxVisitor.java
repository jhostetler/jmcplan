/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.io.PrintStream;

/**
 * @author jhostetler
 *
 */
public class LoggingNegamaxVisitor<Vertex, Edge> extends NegamaxVisitorBase<Vertex, Edge>
{
	private int depth_ = 0;
	private int node_count_ = 0;
	private final PrintStream out_;
	
	/**
	 * 
	 */
	public LoggingNegamaxVisitor( final PrintStream out )
	{
		out_ = out;
	}

	@Override
	public void discoverVertex( final Vertex v )
	{
//		for( int d = 0; d < depth_; ++d ) {
//			out_.print( "-" );
//		}
//		out_.println( v.toString() );
		++depth_;
		++node_count_;
		if( node_count_ % 10000 == 0 ) {
			out_.println( "Node count: " + node_count_ );
		}
	}

	@Override
	public void treeEdge( final Edge e, final Vertex dest )
	{
//		out_.println( "treeEdge( " + src.toString() + " ++ " + e.toString() + " +> " + dest.toString() + " )" );
	}

	@Override
	public void prunedEdge( final Edge e, final Vertex dest )
	{
//		out_.println( "prunedEdge( -- " + e.toString() + " -> " + dest.toString() + " )" );
	}
	
	@Override
	public void principalVariation( final PrincipalVariation<Vertex, Edge> pv )
	{
		out_.println( "principalVariation()" );
		out_.print( "[" + pv.score + "] " );
		out_.print( "(" + pv.states.size() + ", " + pv.actions.size() + ") " );
		out_.println( pv );
	}

	@Override
	public void finishVertex( final Vertex v )
	{
		--depth_;
	}

	@Override
	public void depthLimit( final Vertex v )
	{
//		out_.println( "depthLimit( " + v.toString() + " )" );
	}
}
