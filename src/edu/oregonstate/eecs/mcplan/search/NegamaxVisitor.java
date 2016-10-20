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

/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.util.Iterator;


/**
 * @author jhostetler
 *
 */
public interface NegamaxVisitor<Vertex, Edge>
{
	public abstract void initializeVertex( final Vertex v );
	public abstract void startVertex( final Vertex v );
	public abstract boolean discoverVertex( final Vertex v );
	public abstract void examineEdge( final Edge e, final Vertex dest );
	public abstract void treeEdge( final Edge e, final Vertex dest );
	public abstract void prunedEdge( final Edge e, final Vertex dest );
	public abstract void principalVariation( final PrincipalVariation<Vertex, Edge> pv );
	public abstract void finishVertex( final Vertex v );
	public abstract void depthLimit( final Vertex v );
	public abstract double goal( final Vertex v );
	public abstract boolean isGoal( final Vertex v );
	public abstract double heuristic( final Vertex v );
	public abstract Iterator<Edge> orderActions( final Vertex v, final Iterator<Edge> itr );
}
