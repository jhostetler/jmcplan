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
		assert( max_a != null );
		return max_a;
	}
}
