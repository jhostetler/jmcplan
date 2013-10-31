package edu.oregonstate.eecs.mcplan.domains.voyager;

public enum Unit
{
	Worker( 20, 1, 1 ),
	Soldier( 20, 8, 8 );
	
	// -----------------------------------------------------------------------
	
	public static final double[][] matchups = new double[values().length][values().length];
	static {
		int i = 0;
		for( final Unit a : values() ) {
			int j = 0;
			for( final Unit d : values() ) {
				matchups[i][j++] = matchup( a, d );
			}
			++i;
		}
	};
	
	public static final double[][] attack_matchups = new double[values().length][values().length];
	static {
		int i = 0;
		for( final Unit a : values() ) {
			int j = 0;
			for( final Unit d : values() ) {
				attack_matchups[i][j++] = attack_matchup( a, d );
			}
			++i;
		}
	};
	
	public static double matchup( final Unit a, final Unit d )
	{
		return ((double) a.attack()) / (a.attack() + d.defense());
	}
	
	public static double attack_matchup( final Unit a, final Unit b )
	{
		return ((double) a.attack()) / (a.attack() + b.attack());
	}
	
	public static Unit defaultProduction()
	{
		return Worker;
	}
	
	// -----------------------------------------------------------------------
	
	private final int cost_;
	private final int attack_;
	private final int defense_;
	
	private Unit( final int cost, final int attack, final int defense )
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
