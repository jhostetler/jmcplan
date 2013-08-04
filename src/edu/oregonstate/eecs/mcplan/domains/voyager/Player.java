/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

/**
 * @author jhostetler
 *
 */
public enum Player
{
	Min, Max, Neutral;
	
	// -----------------------------------------------------------------------
	
	public static final int competitors;
	
	static {
		int n = 0;
		for( final Player p : values() ) {
			if( p != Neutral ) {
				++n;
			}
		}
		competitors = n;
	}
	
	public final int id;
	
	private final String repr_;
	
	private Player()
	{
		this.id = this.ordinal();
		repr_ = "Player" + id;
	}
	
	public Player enemy()
	{
		switch( this ) {
			case Min: return Max;
			case Max: return Min;
			case Neutral: return null;
			default: throw new AssertionError();
		}
	}
	
	@Override
	public String toString()
	{
		return repr_;
	}
}
