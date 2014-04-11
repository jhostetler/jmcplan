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
	
	public static final int Ncompetitors;
	public static final Player[] competitors;
	
	static {
		Ncompetitors = values().length - 1;
		competitors = new Player[Ncompetitors];
		for( int i = 0; i < Ncompetitors; ++i ) {
			final Player p = values()[i];
			assert( p != Neutral );
			competitors[i] = p;
		}
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
