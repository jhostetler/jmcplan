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

package edu.oregonstate.eecs.mcplan.domains.voyager.experiments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;

import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.Unit;
import edu.oregonstate.eecs.mcplan.domains.voyager.Planet;
import edu.oregonstate.eecs.mcplan.domains.voyager.Player;
import edu.oregonstate.eecs.mcplan.domains.voyager.Spaceship;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerState;
import edu.oregonstate.eecs.mcplan.sim.EpisodeListener;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.ListUtil;

public class VoyagerGameLogger implements EpisodeListener<VoyagerState, UndoableAction<VoyagerState>>
{
	public final int Nplayers = 2;
	public final int Nplanets;
	private int t_ = 0;
	private final PrintStream game_log_;
//	private final PrintStream data_;
	private final ArrayList<UndoableAction<VoyagerState>> actions_ = new ArrayList<UndoableAction<VoyagerState>>();
	
	public VoyagerGameLogger( final File dir, final int Nplanets )
	{
		try {
			game_log_ = new PrintStream( new File( dir, "game-log.csv" ) );
//			data_ = new PrintStream( new File( dir, "data.csv" ) );
			ListUtil.populateList( actions_, null, Nplayers );
			this.Nplanets = Nplanets;
		}
		catch( final FileNotFoundException ex ) {
			throw new RuntimeException( ex );
		}
		
	}
	
	@Override
	public <P extends Policy<VoyagerState, UndoableAction<VoyagerState>>>
	void startState( final VoyagerState s, final ArrayList<P> policies )
	{
		// Game log
		game_log_.print( "t" );
		for( final Unit type : Unit.values() ) {
			game_log_.print( "," );
			game_log_.print( type );
		}
		for( int i = 0; i < Player.Ncompetitors; ++i ) {
			game_log_.print( ",a" );
			game_log_.print( i );
		}
		for( int i = 0; i < 2*Nplanets; ++i ) {
			game_log_.print( ",planet" + i );
		}
		game_log_.print( ",spaceships" );
		game_log_.println();
		onActionsTaken( s ); // Print initial state
	}
	
	@Override
	public void preGetAction( final int player )
	{ }

	@Override
	public void postGetAction( final int player, final UndoableAction<VoyagerState> action )
	{
		actions_.set( player, action );
	}

	@Override
	public void onActionsTaken( final VoyagerState sprime )
	{
		// Game state
		game_log_.print( t_ );
		
		final int[] pop = new int[Unit.values().length];
		for( final Planet p : sprime.planets ) {
			if( p.owner() == Player.Min ) {
				Fn.vminus_inplace( pop, p.population() );
			}
			else if( p.owner() == Player.Max ) {
				Fn.vplus_inplace( pop, p.population() );
			}
		}
		for( final int p : pop ) {
			game_log_.print( "," );
			game_log_.print( p );
		}
		
		for( final UndoableAction<VoyagerState> a : actions_ ) {
			game_log_.print( "," );
			if( a != null ) {
				game_log_.print( a.toString() );
			}
			else {
				game_log_.print( "null" );
			}
		}
		
		// Planets
		for( final Planet p : sprime.planets ) {
			game_log_.print( "," );
			p.writeEntry( game_log_ );
		}
		
		game_log_.print( "," );
		game_log_.print( encodeSpaceships( sprime ) );
		game_log_.println();
		
		// State update
		Collections.fill( actions_, null );
		t_ += 1;
		
		// Data
		
		// TODO: Debugging
//		System.out.println( "FV: " + Arrays.toString( sprime.featureVector() ) );
	}
	
	@Override
	public void endState( final VoyagerState s )
	{ }
	
	private String encodeSpaceships( final VoyagerState state )
	{
		final StringBuilder sb = new StringBuilder();
		
		boolean comma = false;
		for( final Spaceship ship : state.spaceships ) {
			if( comma ) {
				sb.append( ";" );
			}
			comma = true;
			sb.append( ship.toString() );
		}
		
		return sb.toString();
	}
}
