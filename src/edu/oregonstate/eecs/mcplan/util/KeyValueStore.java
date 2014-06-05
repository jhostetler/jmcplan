/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

/**
 * @author jhostetler
 *
 */
public interface KeyValueStore
{
	public String get( final String key );
	
	public int getInt( final String key );
	
	public double getDouble( final String key );
	
	public Iterable<String> keys();
}
