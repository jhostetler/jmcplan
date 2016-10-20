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
package edu.oregonstate.eecs.mcplan.experiments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.MersenneTwister;

import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.Player;
import edu.oregonstate.eecs.mcplan.sim.EpisodeListener;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveRunner;

/**
 * @author jhostetler
 *
 */
public class PolicyComparison<S, A extends UndoableAction<S, A>,
							  P extends Parameters, I extends Instance<I, S, A>> extends Experiment<P, I>
{
	public static final String log_filename = "log.csv";
	
	private Environment env_ = null;
	private P params_ = null;
	private I world_ = null;
	
	private AnytimePolicy<S, A> pi_ = null;
	private AnytimePolicy<S, A> phi_ = null;
	
	private final PolicyFactory<S, A, P, I> pi_factory_;
	private final PolicyFactory<S, A, P, I> phi_factory_;
	private final List<EpisodeListener<S, A>> extra_listeners_;
	
	public EndScoreRecorder<A> end_state = null;
	public ExecutionTimer<S, A> timer = null;
	
	public PolicyComparison( final PolicyFactory<S, A, P, I> pi_factory,
							 final PolicyFactory<S, A, P, I> phi_factory,
							 final List<EpisodeListener<S, A>> extra_listeners )
	{
		pi_factory_ = pi_factory;
		phi_factory_ = phi_factory;
		extra_listeners_ = extra_listeners;
	}
	
	public PolicyComparison( final PolicyFactory<S, A, P, I> pi_factory,
							 final PolicyFactory<S, A, P, I> phi_factory )
	{
		this( pi_factory, phi_factory, new ArrayList<EpisodeListener<S, A>>() );
	}
	
	@Override
	public String getFileSystemName()
	{
		return pi_.toString() + "_vs_" + phi_.toString();
	}
	
	@Override
	public void setup( final Environment env, final P params, final I world )
	{
		env_ = env;
		params_ = params;
		world_ = world;
		
		final Environment pi_env = new Environment.Builder()
			.root_directory( new File( env_.root_directory, "a0" ) )
			.rng( new MersenneTwister( env_.rng.nextLong() ) )
			.finish();
		pi_env.root_directory.mkdir();
		pi_ = pi_factory_.create( pi_env, params, world );
		final Environment phi_env = new Environment.Builder()
			.root_directory( new File( env_.root_directory, "a1" ) )
			.rng( new MersenneTwister( env_.rng.nextLong() ) )
			.finish();
		phi_env.root_directory.mkdir();
		phi_ = phi_factory_.create( phi_env, params, world );
		
		end_state = new EndScoreRecorder<A>();
		timer = new ExecutionTimer<S, A>( Player.values().length );
		
		try {
			final PrintStream pout = new PrintStream( new File( env.root_directory, "parameters.csv" ) );
			params_.writeCsv( pout );
			pout.close();
			
			final PrintStream iout = new PrintStream( new File( env.root_directory, "instance.csv" ) );
			world_.writeCsv( iout );
			iout.close();
		}
		catch( final FileNotFoundException ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	@Override
	public void finish()
	{
		try {
			final PrintStream rout = new PrintStream( new File( env_.root_directory, "result.csv" ) );
			rout.println( "key,value" );
			rout.println( "winner," + end_state.win );
			rout.println( "score," + end_state.score );
			rout.close();
		}
		catch( final FileNotFoundException ex ) {
			throw new RuntimeException( ex );
		}
	}

	@Override
	public void run()
	{
		final ArrayList<AnytimePolicy<S, A>> agents = new ArrayList<AnytimePolicy<S, A>>();
		agents.add( pi_ );
		agents.add( phi_ );
		
		final SimultaneousMoveRunner<S,	A> runner = new SimultaneousMoveRunner<S, A>(
			world_.simulator(), agents, params_.policy_horizon(), params_.max_time() );
		runner.addListener( timer ); // Add this one first
		for( final EpisodeListener<S, A> listener : extra_listeners_ ) {
			runner.addListener( listener );
		}
		
		runner.run();
	}
}
