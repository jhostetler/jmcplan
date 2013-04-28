package edu.oregonstate.eecs.mcplan.domains.voyager;

public enum EntityType
{
	Worker( 20, 1 ),
	Soldier( 20, 3 );
	
	// -----------------------------------------------------------------------
	
	public static final double[][] matchups = new double[values().length][values().length];
	static {
		int i = 0;
		for( final EntityType a : values() ) {
			int j = 0;
			for( final EntityType d : values() ) {
				matchups[i][j++] = matchup( a, d );
			}
			++i;
		}
	};
	
	public static double matchup( final EntityType a, final EntityType d )
	{
		return ((double) a.strength()) / (a.strength() + d.strength());
	}
	
	// -----------------------------------------------------------------------
	
	private final int cost_;
	private final int strength_;
	
	private EntityType( final int cost, final int strength )
	{
		cost_ = cost;
		strength_ = strength;
	}
	
	public int cost()
	{
		return cost_;
	}
	
	public int strength()
	{
		return strength_;
	}
}
