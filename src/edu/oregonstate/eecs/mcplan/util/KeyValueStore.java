/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

/**
 * @author jhostetler
 *
 */
public abstract class KeyValueStore
{
	public abstract Iterable<String> keys();
	public abstract String get( final String key );
	
	public String defaultValue( final String key )
	{
		throw new UnsupportedOperationException();
	}
	
	// -----------------------------------------------------------------------
	
	public final boolean getBoolean( final String key )
	{
		final String value = get( key );
		if( value == null ) {
			return parseBoolean( defaultValue( key ) );
		}
		else {
			return parseBoolean( value );
		}
	}
	
	public final double getDouble( final String key )
	{
		final String value = get( key );
		if( value == null ) {
			return parseDouble( defaultValue( key ) );
		}
		else {
			return parseDouble( value );
		}
	}
	
	public final int getInt( final String key )
	{
		final String value = get( key );
		if( value == null ) {
			return parseInt( defaultValue( key ) );
		}
		else {
			return parseInt( value );
		}
	}
	
	// -----------------------------------------------------------------------
	
	private final boolean parseBoolean( final String s )
	{
		if( "true".equalsIgnoreCase( s ) ) {
			return true;
		}
		else if( "false".equalsIgnoreCase( s ) ) {
			return false;
		}
		else {
			throw new IllegalArgumentException( s );
		}
	}
	
	private final double parseDouble( final String s )
	{
		return Double.parseDouble( s );
	}
	
	private final int parseInt( final String s )
	{
		return Integer.parseInt( s );
	}
}
