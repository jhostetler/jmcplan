/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.planetwars;


/**
 * @author jhostetler
 *
 */
public enum PwPlayer
{
	Min, Max, Neutral;
	
	// -----------------------------------------------------------------------
	
	public static final int Ncompetitors;
	public static final PwPlayer[] competitors;
	public static final int Nplayers;
	
	static {
		Ncompetitors = values().length - 1;
		competitors = new PwPlayer[Ncompetitors];
		for( int i = 0; i < Ncompetitors; ++i ) {
			final PwPlayer p = values()[i];
			assert( p != Neutral );
			competitors[i] = p;
		}
		Nplayers = values().length;
	}
	
	public final int id;
	
	private final String repr_;
	
	private PwPlayer()
	{
		this.id = this.ordinal();
		repr_ = "Player" + id;
	}
	
	public PwPlayer enemy()
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
