/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;


/**
 * @author jhostetler
 *
 */
public class NegamaxVisitorBase<Vertex, Edge> implements NegamaxVisitor<Vertex, Edge>
{

	@Override
	public void initializeVertex(final Vertex v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startVertex(final Vertex v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean discoverVertex(final Vertex v) {
		return false;
	}

	@Override
	public void examineEdge(final Edge e, final Vertex dest) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void treeEdge(final Edge e, final Vertex dest) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void prunedEdge(final Edge e, final Vertex dest) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void principalVariation(final PrincipalVariation<Vertex, Edge> pv) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finishVertex(final Vertex v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void depthLimit(final Vertex v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void goal(final Vertex v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isGoal(final Vertex v) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double heuristic(final Vertex v) {
		// TODO Auto-generated method stub
		return 0;
	}

}
