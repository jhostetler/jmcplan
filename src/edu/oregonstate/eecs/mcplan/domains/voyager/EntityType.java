package edu.oregonstate.eecs.mcplan.domains.voyager;

public enum EntityType
{
	Worker( 20, 1, 1 ),
	Soldier( 20, 8, 8 );
	
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
	
	public static final double[][] attack_matchups = new double[values().length][values().length];
	static {
		int i = 0;
		for( final EntityType a : values() ) {
			int j = 0;
			for( final EntityType d : values() ) {
				attack_matchups[i][j++] = attack_matchup( a, d );
			}
			++i;
		}
	};
	
	public static double matchup( final EntityType a, final EntityType d )
	{
		return ((double) a.attack()) / (a.attack() + d.defense());
	}
	
	public static double attack_matchup( final EntityType a, final EntityType b )
	{
		return ((double) a.attack()) / (a.attack() + b.attack());
	}
	
	public static EntityType defaultProduction()
	{
		return Worker;
	}
	
	// -----------------------------------------------------------------------
	
	private final int cost_;
	private final int attack_;
	private final int defense_;
	
	private EntityType( final int cost, final int attack, final int defense )
	{
		cost_ = cost;
		attack_ = attack;
		defense_ = defense;
	}
	
	public int cost()
	{
		return cost_;
	}
	
	public int attack()
	{
		return attack_;
	}
	
	public int defense()
	{
		return defense_;
	}
}
