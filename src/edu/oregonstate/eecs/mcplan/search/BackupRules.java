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

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public class BackupRules
{
	public static <S, A extends VirtualConstructor<A>>
	double[] MaxMinQ( final StateNode<S, A> sn )
	{
		return MaxMinAction( sn ).q();
	}
	
	public static <S, A extends VirtualConstructor<A>>
	ActionNode<S, A> MaxMinAction( final StateNode<S, A> sn )
	{
		double[] max_q = new double[] { -Double.MAX_VALUE, -Double.MAX_VALUE };
		ActionNode<S, A> max_a = null;
		final Generator<Generator<ActionNode<S, A>>> parts
			= JointAction.partition( 0, sn.successors() );
		for( final Generator<ActionNode<S, A>> max : Fn.in( parts ) ) {
//			System.out.println( "Outer loop" );
			double[] min_q = new double[] { Double.MAX_VALUE, Double.MAX_VALUE };
			ActionNode<S, A> min_a = null;
			for( final ActionNode<S, A> min : Fn.in( max ) ) {
//				System.out.println( "Action " + min.a + ": " + Arrays.toString( min.q() ) );
				assert( min.nagents == 2 );
				if( min.q( 1 ) < min_q[1] ) {
					min_q = Arrays.copyOf( min.q(), 2 );
					min_a = min;
				}
			}
			if( min_q[0] > max_q[0] ) {
				max_q = min_q;
				max_a = min_a;
			}
		}
		// TODO: Can return null for leaf nodes. Better way to handle them?
//		assert( max_a != null );
		return max_a;
	}
	
	public static <S, A extends VirtualConstructor<A>>
	double[] MeanQ( final StateNode<S, A> sn )
	{
		final Generator<? extends ActionNode<S, A>> aitr = sn.successors();
		final double[] v;
		int n = 0;
		if( aitr.hasNext() ) {
			final ActionNode<S, A> an = aitr.next();
			final double[] q = an.q();
			v = Arrays.copyOf( q, q.length );
			++n;
		}
		else {
			return null;
		}
		while( aitr.hasNext() ) {
			final ActionNode<S, A> an = aitr.next();
			final double[] q = an.q();
			final int ni = an.n();
			for( int i = 0; i < v.length; ++i ) {
				v[i] += ni * q[i];
			}
			n += ni;
		}
		return Fn.scalar_multiply_inplace( v, 1.0 / n );
	}
	
	public static <S, A extends VirtualConstructor<A>>
	double[] MaxQ( final StateNode<S, A> sn )
	{
		return MaxAction( sn ).q();
	}
	
	public static <S, A extends VirtualConstructor<A>>
	ActionNode<S, A> MaxAction( final StateNode<S, A> sn )
	{
		assert( sn.nagents == 1 );
		final Generator<? extends ActionNode<S, A>> aitr = sn.successors();
		ActionNode<S, A> max_a = null;
		double max_q = -Double.MAX_VALUE;
		while( aitr.hasNext() ) {
			final ActionNode<S, A> an = aitr.next();
			// FIXME: This is a hack
//			if( an.a() == null ) {
//				continue;
//			}
			final double[] q = an.q();
			if( q[0] > max_q ) {
				max_q = q[0];
				max_a = an;
			}
		}
		return max_a;
	}
	
	public static <S, A extends VirtualConstructor<A>>
	double[] MarginalMaxQ( final StateNode<S, A> sn, final int player )
	{
		final Generator<? extends ActionNode<S, A>> aitr = sn.successors();
		final double[] v;
		if( aitr.hasNext() ) {
			final ActionNode<S, A> an = aitr.next();
			final double[] q = an.q();
			v = Arrays.copyOf( q, q.length );
		}
		else {
			return null;
		}
		while( aitr.hasNext() ) {
			final ActionNode<S, A> an = aitr.next();
			final double[] q = an.q();
			if( q[player] > v[player] ) {
				Fn.memcpy( v, q, q.length );
			}
		}
		return v;
	}
}
