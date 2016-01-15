/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

/**
 * @author jhostetler
 *
 */
public enum CosmicError
{
	None			(0),
	NotConverged	(1<<1);
	
	public final int code;
	
	CosmicError( final int code )
	{
		this.code = code;
	}
}
