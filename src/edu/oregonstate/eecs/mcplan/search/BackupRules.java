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
	double[] MaxMinQ( final StateNode<S, JointAction<A>> sn )
	{
		return MaxMinAction( sn ).q();
	}
	
	public static <S, A extends VirtualConstructor<A>>
	ActionNode<S, JointAction<A>> MaxMinAction( final StateNode<S, JointAction<A>> sn )
	{
		double[] max_q = new double[] { -Double.MAX_VALUE, -Double.MAX_VALUE };
		ActionNode<S, JointAction<A>> max_a = null;
		final Generator<Generator<ActionNode<S, JointAction<A>>>> parts
			= JointAction.partition( 0, sn.successors() );
		for( final Generator<ActionNode<S, JointAction<A>>> max : Fn.in( parts ) ) {
//			System.out.println( "Outer loop" );
			double[] min_q = new double[] { Double.MAX_VALUE, Double.MAX_VALUE };
			ActionNode<S, JointAction<A>> min_a = null;
			for( final ActionNode<S, JointAction<A>> min : Fn.in( max ) ) {
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
		final Generator<ActionNode<S, A>> aitr = sn.successors();
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
			Fn.vplus_inplace( v, an.q() );
			++n;
		}
		return Fn.scalar_multiply_inplace( v, 1.0 / n );
	}
	
	public static <S, A extends VirtualConstructor<A>>
	double[] MaxQ( final StateNode<S, A> sn, final int player )
	{
		final Generator<ActionNode<S, A>> aitr = sn.successors();
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
