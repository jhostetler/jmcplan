/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.increment;

import java.util.Arrays;

/**
 * @author jhostetler
 *
 */
public class IncrementState
{
	public final int[] counters;
	
	public IncrementState( final int Ncounters )
	{
		counters = new int[Ncounters];
		Arrays.fill( counters, 0 );
	}
	
	public IncrementState( final IncrementState that )
	{
		this.counters = Arrays.copyOf( that.counters, that.counters.length );
	}
	
	public IncrementState copy()
	{
		return new IncrementState( this );
	}
}
