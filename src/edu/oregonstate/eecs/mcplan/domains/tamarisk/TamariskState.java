/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.tamarisk;

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import edu.oregonstate.eecs.mcplan.State;

/**
 * @author jhostetler
 *
 */
public class TamariskState implements State
{
	public final RandomGenerator rng;
	public final TamariskParameters params;
	public final Species[][] habitats;
	public final DirectedGraph<Integer, DefaultEdge> graph;
	
	public TamariskState( final RandomGenerator rng, final TamariskParameters params )
	{
		this.rng = rng;
		this.params = params;
		habitats = new Species[params.Nreaches][params.Nhabitats];
		graph = params.createRandomGraph( rng.nextInt(), true );
		
		// TODO: How to seed plants initially?
		for( int i = 0; i < params.Nreaches; ++i ) {
			for( int j = 0; j < params.Nhabitats; ++j ) {
				final int si = rng.nextInt( Species.values().length );
				habitats[i][j] = Species.values()[si];
			}
		}
	}

	@Override
	public boolean isTerminal()
	{
		return false;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		for( final Species[] reach : habitats ) {
			sb.append( Arrays.toString( reach ) ).append( "\n" );
		}
		return sb.toString();
	}
}
