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
package edu.oregonstate.eecs.mcplan.domains.tamarisk;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.util.Fn;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.jgraph.JGraph;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedWeightedGraph;

/**
 * @author jhostetler
 *
 */
public class TamariskParameters
{
	/*
	 * class GerminationDispersionParameterClass:
           def __init__(self, germinationSuccNat, germinationSuccTam):
               self.germinationSuccTam = germinationSuccTam
               self.germinationSuccNat = germinationSuccNat
     */
	// This is found in 'InvasiveEnvironment.py', line 96.
	public final double[] germination_success = new double[] { 1.0, 1.0 };
	
	/*
	 * def __init__(self, nbrReaches, habitatSize, prodRate, deathRate, exogenousOnOffIndicator, reachArrivalRates,
             reachArrivalProbs, upStreamRate, downStreamRate, competitionFactor, graph):
        """
            :param competitionFactor, competition parameter
            :param deathRate (array of length 2 of float), first column shows Native, and the second column shows Tamarisk
            :param habitatSize (int)
            :param exogenousOnOffIndicator (On=2, Off=1), indicates if there is exogenous arrival
            :param prodRate (array of length 2 of float) production rate
            :param reachArrivalProbs (matrix of size (nbrReaches,2)), first column shows Native, and the second column shows Tamarisk
            :param reachArrivalRates (matrix of size (nbrReaches,2)), first column shows Native, and the second column shows Tamarisk
            :param upStreamRate (float)
            :param downStreamRate (float)
            :param graph (networkx graph), a graph representing the river network
            Note that the position of the reaches in the state and action is based on the graph.edges() output
        """
        self.nbrReaches = nbrReaches
        self.habitatSize = habitatSize
        self.prodRate = prodRate
        self.deathRate = deathRate
        self.exogenousArrivalIndicator = exogenousOnOffIndicator
        self.reachArrivalRates = reachArrivalRates
        self.reachArrivalProbs = reachArrivalProbs
        self.downStreamRate = downStreamRate
        self.upStreamRate = upStreamRate
        self.competitionFactor = competitionFactor
        self.graph = graph
	 */
	public final int Nreaches;
	public final int Nhabitats;
	public final double[] production_rate = new double[] { 200, 200 };
	public final double[] death_rate = new double[] { 0.2, 0.2 };
	public final boolean exongenous_arrivals = true;
	public final int[][] reach_arrival_rate;
	public final double[][] reach_arrival_prob;
	public final double upstream_rate = 0.1;
	public final double downstream_rate = 0.5;
	/**
	 * The probability that a Native will out-compete a Tamarisk when
	 * propagules of both species arrive at the same habitat.
	 */
	public final double competition_factor = 0.5;
	
	/*
	 * def __init__(self, costPerTree, eradicationCost, restorationCost, eradicationRate, restorationRate, costPerReach,
                 emptyCost, varEradicationCost, varInvasiveRestorationCost, varEmptyRestorationCost, budget):
        """
        :param budget (float)
        :param costPerReach (float), cost per invaded reach
        :param costPerTree (float), cost per invaded tree
        :param emptyCost (float), cost for empty slot
        :param eradicationCost (float), fixed eradication cost
        :param eradicationRate (float), eradication success rate
        :param restorationCost (float), fixed restoration cost
        :param restorationRate (float), restoration success rate
        :param varEmptyRestorationCost (float), variable restoration cost for empty slot
        :param varEradicationCost (float), variable eradication cost for empty slot
        :param varInvasiveRestorationCost (float), variable restoration cost for empty slot
        """
        self.costPerTree = costPerTree
        self.eradicationCost = eradicationCost
        self.restorationCost = restorationCost
        self.eradicationRate = eradicationRate
        self.restorationRate = restorationRate
        self.costPerReach = costPerReach
        self.emptyCost = emptyCost
        self.varEradicationCost = varEradicationCost
        self.varInvasiveRestorationCost = varInvasiveRestorationCost
        self.varEmptyRestorationCost = varEmptyRestorationCost
        self.budget = budget
	 */
	public final double budget = 100;
	public final double cost_per_reach = 10.0;
	public final double cost_per_tree = 0.1;
	public final double cost_empty = 0.05;
	public final double eradicate_cost = 0.5;
	public final double eradicate_rate = 0.85;
	public final double restore_cost = 0.9;
	public final double restore_rate = 0.65;
	public final double eradicate_cost_per_habitat = 0.4;
	public final double restore_cost_per_empty = 0.4;
	public final double restore_cost_per_invasive = 0.8;
	
	// Extra stuff that was not in the RL competition version
	
	/**
	 * Maximum number of steps. Good algorithms should rarely reach this
	 * number with the default parameter settings. It is enforced mainly
	 * to prevent poor algorithms from taking a long time.
	 */
	public final int T = 50;
	
	public TamariskParameters( final RandomGenerator rng, final int Nreaches, final int Nhabitats )
	{
		this.Nreaches = Nreaches;
		this.Nhabitats = Nhabitats;
		
		reach_arrival_rate = new int[Nreaches][Species.N];
		reach_arrival_prob = new double[Nreaches][Species.N];
		if( exongenous_arrivals ) {
			for( int i = 0; i < Nreaches; ++i ) {
				for( int j = 0; j < Species.N; ++j ) {
//					reach_arrival_rate[i][j] = 100 + rng.nextInt( 1000 - 100 );
//					reach_arrival_prob[i][j] = rng.nextDouble();
					reach_arrival_rate[i][j] = 100;
					reach_arrival_prob[i][j] = 0.5; //rng.nextDouble();
				}
			}
		}
	}
	
	/**
	 * From 'InvasiveUtilities.createRandomGraph'.
	 * @param seed
	 * @param balanced
	 * @return
	 */
	public ListenableDirectedWeightedGraph<Integer, DefaultEdge> createRandomGraph(
			final int seed, final boolean balanced )
	{
		final RandomGenerator rng = new MersenneTwister( seed );
		final ListenableDirectedWeightedGraph<Integer, DefaultEdge> graph
			= new ListenableDirectedWeightedGraph<Integer, DefaultEdge>( DefaultEdge.class );
		for( int i = 0; i < Nreaches + 1; ++i ) {
			graph.addVertex( i );
		}
		final TIntArrayList parents = new TIntArrayList( Fn.range( 0, Nreaches ) );
		final TIntArrayList nodes = new TIntArrayList();
		final int[] visited = Fn.repeat( 0, Nreaches );
		final int choice = rng.nextInt( parents.size() );
		final int root = parents.get( choice );
		nodes.add( root );
		parents.removeAt( choice );
		graph.addEdge( root, Nreaches );
		while( !parents.isEmpty() ) {
			final int node_idx;
			if( balanced ) {
				node_idx = 0;
			}
			else {
				node_idx = rng.nextInt( nodes.size() );
			}
			final int node = nodes.get( node_idx );
			final int parent_idx = rng.nextInt( parents.size() );
			final int parent = parents.get( parent_idx );
			graph.addEdge( parent, node );
			parents.removeAt( parent_idx );
			nodes.add( parent );
			visited[node] += 1;
			if( visited[node] >= 2 ) {
				nodes.removeAt( node_idx );
			}
		}
		return graph;
	}
	
	/**
	 * Creates a tree that is "balanced", in the sense that the longest path
	 * is at most 1 longer than the shortest path. Node indices increase as we
	 * head upstream. The furthest-downstream reach is always single, so
	 * branching begins above node 1.
	 * 
	 * @param branching Number of branches per node.
	 * @return
	 */
	public ListenableDirectedWeightedGraph<Integer, DefaultEdge> createBalancedGraph( final int branching )
	{
		final ListenableDirectedWeightedGraph<Integer, DefaultEdge> graph
			= new ListenableDirectedWeightedGraph<Integer, DefaultEdge>( DefaultEdge.class );
		for( int i = 0; i < Nreaches + 1; ++i ) {
			graph.addVertex( i );
		}
		
		final TIntArrayList pred = new TIntArrayList();
		graph.addEdge( 1, 0 );
		int v = 1;
		pred.add( v );
		while( !pred.isEmpty() ) {
			final int p = pred.removeAt( 0 );
			for( int b = 0; b < branching; ++b ) {
				v += 1;
				if( v >= Nreaches + 1 ) {
					break;
				}
				else {
					graph.addEdge( v, p );
					pred.add( v );
				}
			}
		}
		return graph;
	}
	
	
	/**
	 * From 'InvasiveUtilities.calculatePath'
	 * @param directed
	 * @return
	 */
	public <V, E> double[][] calculateDispersionKernel( final DirectedGraph<V, E> directed )
	{
		final double[][] K = new double[Nreaches][Nreaches];
		final double C = ((1 - 2*upstream_rate)*(1 - downstream_rate))
						 / (1.0 - upstream_rate*downstream_rate);
		final AsUndirectedGraph<V, E> undirected = new AsUndirectedGraph<V, E>( directed );
		final FloydWarshallShortestPaths<V, E> paths = new FloydWarshallShortestPaths<V, E>( undirected );
		final Set<E> edge_set = directed.edgeSet();
		final Iterator<E> itr_i = edge_set.iterator();
		for( int i = 0; i < Nreaches; ++i ) {
			assert( itr_i.hasNext() );
			final E src_edge = itr_i.next();
			final Iterator<E> itr_j = edge_set.iterator();
			for( int j = 0; j < Nreaches; ++j ) {
				// Begin by setting K to the normalization constant
				K[i][j] = C;
				
				assert( itr_j.hasNext() );
				final E dest_edge = itr_j.next();
				if( i == j ) {
					continue;
				}
				
				// Get all 4 shortest paths between vertices to which either
				// of the two current Reaches are incident.
				final ArrayList<GraphPath<V, E>> p = new ArrayList<GraphPath<V, E>>();
				p.add( paths.getShortestPath( undirected.getEdgeSource( src_edge ),
											  undirected.getEdgeSource( dest_edge ) ) );
				p.add( paths.getShortestPath( undirected.getEdgeSource( src_edge ),
											  undirected.getEdgeTarget( dest_edge ) ) );
				p.add( paths.getShortestPath( undirected.getEdgeTarget( src_edge ),
											  undirected.getEdgeSource( dest_edge ) ) );
				p.add( paths.getShortestPath( undirected.getEdgeTarget( src_edge ),
											  undirected.getEdgeTarget( dest_edge ) ) );
				
				// Find the longest shortest path in the *undirected* graph.
				// This is necessary to avoid issues such as trivial paths
				// between adjacent Reaches, since the Reaches are *edges*
				// in the graph.
				double longest_weight = -Double.MAX_VALUE;
				GraphPath<V, E> longest_path = null;
				for( final GraphPath<V, E> candidate : p ) {
					if( candidate != null && candidate.getWeight() > longest_weight ) {
						longest_weight = candidate.getWeight();
						longest_path = candidate;
					}
				}
				assert( longest_path != null );
				
				// Calculate kernel weight
				final List<V> vertex_list = Graphs.getPathVertexList( longest_path );
				final Iterator<V> vitr = vertex_list.iterator();
				V src = vitr.next();
				while( vitr.hasNext() ) {
					final V dest = vitr.next();
					// Try to retrieve the edge from the *directed* graph.
					// If the result is null, the edge is an *upstream* link.
					final E e = directed.getEdge( src, dest );
					if( e != null ) {
						K[i][j] *= downstream_rate;
					}
					else {
						K[i][j] *= upstream_rate;
					}
					
					src = dest;
				}
			}
		}
		
		return K;
	}
	
	public static void main( final String[] argv )
	{
//		for( int i = 0; i < 1000; ++i ) {
//			final int Nreaches = 4;
//			final int Nhabitats = 4;
//			final RandomGenerator rng = new MersenneTwister( 42 );
//			final ListenableDirectedWeightedGraph<Integer, DefaultEdge> g
//				= new TamariskParameters( rng, Nreaches, Nhabitats ).createRandomGraph( 42 + i, true );
//		}
		
		final int Nreaches = 10;
		final int Nhabitats = 4;
		final RandomGenerator rng = new MersenneTwister( 42 );
		final TamariskParameters params = new TamariskParameters( rng, Nreaches, Nhabitats );
		
//		final ListenableDirectedWeightedGraph<Integer, DefaultEdge> g = params.createRandomGraph( 42, true );
		final ListenableDirectedWeightedGraph<Integer, DefaultEdge> g = params.createBalancedGraph( 3 );
		final double[][] K = params.calculateDispersionKernel( g );
		for( int i = 0; i < K.length; ++i ) {
			System.out.println( Arrays.toString( K[i] ) );
		}
		
		final JGraphModelAdapter adapter = new JGraphModelAdapter( g );
		final JGraph jgraph = new JGraph( adapter );
//		final JGraphLayoutAlgorithm layout = new SugiyamaLayoutAlgorithm();

		final JFrame frame = new JFrame();
		frame.setSize( 480, 360 );
		frame.getContentPane().add( new JGraph( new JGraphModelAdapter( g ) ) );
		frame.setVisible( true );
		
		final TamariskState state = new TamariskState( rng, params, g );
		final TamariskSimulator sim = new TamariskSimulator( state );
		
		System.out.println( state );
		System.out.println();
		
		final int T = 30;
		final ArrayDeque<String> hist = new ArrayDeque<String>();
		for( int t = 0; t < T; ++t ) {
			sim.takeAction( new JointAction<TamariskAction>( new NothingAction() ) );
			System.out.println( state );
			System.out.println();
			hist.push( state.toString() );
		}
		
		for( int t = 0; t < T; ++t ) {
			final String forward = hist.pop();
			final String backward = sim.state().toString();
			assert( forward.equals( backward ) );
			sim.untakeLastAction();
		}
	}
}
