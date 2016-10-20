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

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.Player;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerInstance;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerParameters;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerState;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerVisualization;
import edu.oregonstate.eecs.mcplan.domains.voyager.policies.VoyagerPolicyFactory;
import edu.oregonstate.eecs.mcplan.experiments.Environment;
import edu.oregonstate.eecs.mcplan.experiments.ExecutionTimer;
import edu.oregonstate.eecs.mcplan.experiments.Experiment;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveRunner;

/**
 * @author jhostetler
 *
 */
public class VoyagerPolicyComparison<A> extends Experiment<VoyagerParameters, VoyagerInstance>
{
	public static final String log_filename = "log.csv";
	
	private Environment env_ = null;
	private VoyagerParameters params_ = null;
	private VoyagerInstance world_ = null;
	
	private Policy<VoyagerState, UndoableAction<VoyagerState>> pi_ = null;
	private Policy<VoyagerState, UndoableAction<VoyagerState>> phi_ = null;
	
	private final VoyagerPolicyFactory pi_factory_;
	private final VoyagerPolicyFactory phi_factory_;
	
	public EndScoreRecorder end_state = null;
	public ExecutionTimer<VoyagerState, UndoableAction<VoyagerState>> timer = null;
	
	private VoyagerVisualization vis_ = null;
	
	public VoyagerPolicyComparison( final VoyagerPolicyFactory pi_factory,
								    final VoyagerPolicyFactory phi_factory )
	{
		pi_factory_ = pi_factory;
		phi_factory_ = phi_factory;
	}
	
	@Override
	public String getFileSystemName()
	{
		return pi_.toString() + "_vs_" + phi_.toString();
	}
	
	@Override
	public void setup( final Environment env, final VoyagerParameters params, final VoyagerInstance world )
	{
		env_ = env;
		params_ = params;
		world_ = world;
		
		final Environment pi_env = new Environment.Builder()
			.root_directory( new File( env_.root_directory, "a0" ) )
			.finish();
		pi_env.root_directory.mkdir();
		pi_ = pi_factory_.create( pi_env, params, world, Player.Min );
		final Environment phi_env = new Environment.Builder()
			.root_directory( new File( env_.root_directory, "a1" ) )
			.finish();
		phi_env.root_directory.mkdir();
		phi_ = phi_factory_.create( phi_env, params, world, Player.Max );
		
		end_state = new EndScoreRecorder();
		timer = new ExecutionTimer<VoyagerState, UndoableAction<VoyagerState>>( params_.Nplayers );
		
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
			rout.println( "winner," + end_state.winner );
			for( int i = 0; i < end_state.population_difference.length; ++i ) {
				rout.println( "score" + i + "," + end_state.population_difference[i] );
			}
			rout.close();
		}
		catch( final FileNotFoundException ex ) {
			throw new RuntimeException( ex );
		}
		finally {
			if( vis_ != null ) {
				vis_.setVisible( false );
				vis_.dispose();
				vis_ = null;
			}
		}
	}

	@Override
	public void run()
	{
		final ArrayList<Policy<VoyagerState, UndoableAction<VoyagerState>>>
			agents = new ArrayList<Policy<VoyagerState, UndoableAction<VoyagerState>>>();
		agents.add( pi_ );
		agents.add( phi_ );
		
		final SimultaneousMoveRunner<VoyagerState, UndoableAction<VoyagerState>> runner
			= new SimultaneousMoveRunner<VoyagerState, UndoableAction<VoyagerState>>(
				world_.simulator(), agents,	params_.policy_horizon );
		runner.addListener( timer ); // Add this one first
		runner.addListener( new VoyagerGameLogger( env_.root_directory, params_.Nplanets ) );
		runner.addListener( end_state );
		
		if( params_.use_monitor ) {
			final int wait = 0;
			vis_ = new VoyagerVisualization( params_, new Dimension( 720, 720 ), wait );
			vis_.attach( runner );
		}
		
		runner.run();
	}
}
