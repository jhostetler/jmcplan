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
