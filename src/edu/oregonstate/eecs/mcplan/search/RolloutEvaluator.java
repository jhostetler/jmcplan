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

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.sim.Simulator;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * Evaluates a joint policy by simulating it repeatedly.
 * 
 * @author jhostetler
 */
public final class RolloutEvaluator<S extends State, A extends VirtualConstructor<A>>
	implements EvaluationFunction<S, A>
{
	public static <S extends State, A extends VirtualConstructor<A>>
	RolloutEvaluator<S, A> create( final Policy<S, JointAction<A>> policy, final double discount,
								   final int width, final int depth, final EvaluationFunction<S, A> default_value )
   {
		return new RolloutEvaluator<S, A>( policy, discount, width, depth, default_value );
   }
	
	public final Policy<S, JointAction<A>> policy;
	public final double discount;
	public final int width;
	public final int depth;
	public final EvaluationFunction<S, A> default_value;
	
	public RolloutEvaluator( final Policy<S, JointAction<A>> policy, final double discount,
							 final int width, final int depth, final EvaluationFunction<S, A> default_value )
	{
		this.policy = policy;
		this.discount = discount;
		// FIXME: Needed because of interface change to EvaluationFunction
		// making it take a (non-resetable) Simulator.
		assert( width == 1 );
		this.width = width;
		
		this.depth = depth;
		
		this.default_value = default_value;
	}
	
	@Override
	public double[] evaluate( final Simulator<S, A> sim )
	{
//		System.out.println( "evaluate()" );
		final int nagents = sim.nagents();
		final double[] qbar = Fn.repeat( 0.0, nagents );
		for( int w = 0; w < width; ++w ) {
			final double[] q = sim.reward(); // Fn.repeat( 0.0, nagents );
			double running_discount = 1.0;
			int count = 0;
			while( true ) {
				if( sim.isTerminalState() ) {
					break;
				}
				else if( count == depth ) {
					Fn.vplus_ax_inplace( q, running_discount, default_value.evaluate( sim ) );
					break;
				}
				
				policy.setState( sim.state(), sim.t() );
				final JointAction<A> a = policy.getAction();
				sim.takeAction( a );
				final S sprime = sim.state();
				final double[] r = sim.reward();
				policy.actionResult( sprime, r );
				
				Fn.scalar_multiply_inplace( r, running_discount );
				Fn.vplus_inplace( q, r );
				count += 1;
				
				running_discount *= discount;
			}

			Fn.vplus_inplace( qbar, q );
		}
		
		Fn.scalar_multiply_inplace( qbar, 1.0 / width );
		return qbar;
	}
	
}
