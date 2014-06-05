/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.tamarisk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import edu.oregonstate.eecs.mcplan.util.Csv;

/**
 * @author jhostetler
 *
 */
public class TamariskDataset
{

	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main( final String[] args ) throws FileNotFoundException
	{
		int idx = 0;
		final int Ninstances = Integer.parseInt( args[idx++] );
		System.out.println( "Ninstances = " + Ninstances );
		final int Nreaches = Integer.parseInt( args[idx++] );
		System.out.println( "Nreaches = " + Nreaches );
		final int Nhabitats = Integer.parseInt( args[idx++] );
		System.out.println( "Nhabitats = " + Nhabitats );
		final int seed = Integer.parseInt( args[idx++] );
		System.out.println( "seed = " + seed );
		final String out_filename = args[idx++];
		System.out.println( "out_filename = " + out_filename );
		
		final RandomGenerator rng = new MersenneTwister( seed );
		
		final Csv.Writer writer = new Csv.Writer( new PrintStream( new File( out_filename) ) );
		writer.cell( "Nreaches" ).cell( "Nhabitats" );
		for( int r = 0; r < Nreaches; ++r ) {
			for( int h = 0; h < Nhabitats; ++h ) {
				writer.cell( "r" + r + "h" + h );
			}
		}
		writer.newline();
		
		for( int i = 0; i < Ninstances; ++i ) {
			final TamariskParameters params = new TamariskParameters( rng, Nreaches, Nhabitats );
			final DirectedGraph<Integer, DefaultEdge> g = params.createBalancedGraph( 2 );
			final TamariskState s = new TamariskState( rng, params, g );
			
			writer.cell( Nreaches ).cell( Nhabitats );
			for( int r = 0; r < Nreaches; ++r ) {
				for( int h = 0; h < Nhabitats; ++h ) {
					writer.cell( s.habitats[r][h] );
				}
			}
			writer.newline();
		}
	}

}
