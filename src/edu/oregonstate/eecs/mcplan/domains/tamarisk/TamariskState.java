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

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.util.Fn;

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
	
	public int t = 0;
	
	public TamariskState( final RandomGenerator rng, final TamariskParameters params,
						  final DirectedGraph<Integer, DefaultEdge> graph )
	{
		this.rng = rng;
		this.params = params;
		habitats = new Species[params.Nreaches][params.Nhabitats];
		this.graph = graph; //params.createRandomGraph( rng.nextInt(), true );
		
		// Lower-variance initialization
		final boolean[] invaded = new boolean[params.Nreaches];
		final int Ninvaded = 1 + params.Nreaches / 2;
		for( int i = 0; i < Ninvaded; ++i ) {
			invaded[i] = true;
		}
		Fn.shuffle( rng, invaded );
		final boolean[] tamarisk = new boolean[params.Nhabitats];
		final int Ntamarisk = 1 + params.Nhabitats / 2; //rng.nextInt( params.Nhabitats );
		for( int j = 0; j < Ntamarisk; ++j ) {
			tamarisk[j] = true;
		}
		for( int i = 0; i < params.Nreaches; ++i ) {
			if( invaded[i] ) {
				Fn.shuffle( rng, tamarisk );
				for( int j = 0; j < params.Nhabitats; ++j ) {
					if( tamarisk[j] ) {
						habitats[i][j] = Species.Tamarisk;
					}
					else {
						habitats[i][j] = rng.nextBoolean() ? Species.Native : Species.None;
					}
				}
			}
			else {
				for( int j = 0; j < params.Nhabitats; ++j ) {
					habitats[i][j] = rng.nextBoolean() ? Species.Native : Species.None;
				}
			}
		}
		
		// Seed habitats with species chosen uniformly at random
//		for( int i = 0; i < params.Nreaches; ++i ) {
//			for( int j = 0; j < params.Nhabitats; ++j ) {
//				final int si = rng.nextInt( Species.values().length );
//				habitats[i][j] = Species.values()[si];
//			}
//		}
	}

	@Override
	public boolean isTerminal()
	{
		if( t >= params.T ) {
//			System.out.println( "max T" );
			return true;
		}
		
		for( final Species[] r : habitats ) {
			for( final Species h : r ) {
				if( h == Species.Tamarisk ) {
					return false;
				}
			}
		}
		
		return true;
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

	@Override
	public void close()
	{ }
}
