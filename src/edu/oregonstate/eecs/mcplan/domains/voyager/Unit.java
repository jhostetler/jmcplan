package edu.oregonstate.eecs.mcplan.domains.voyager;

public enum Unit
{
	Worker( 20, 1, 6 ),
	Soldier( 20, 2, 6 );
	// Add types here. Don't forget to update 'max_hp' below!
	
	// TODO: Must keep this synchronized! No way to derive it due to Java
	// rules about static members in enums.
	public static final int max_hp = 6;
	
	// -----------------------------------------------------------------------
	
	public static Unit defaultProduction()
	{
		return Worker;
	}
	
	// -----------------------------------------------------------------------
	
	private final int cost_;
	private final int attack_;
	private final int hp_;
	
	private Unit( final int cost, final int attack, final int hp )
	{
		cost_ = cost;
		attack_ = attack;
		hp_ = hp;
	}
	
	public int cost()
	{
		return cost_;
	}
	
	public int attack( final Unit u )
	{
		// NOTE: No differential matchups currently
		return attack_;
	}
	
	public int hp()
	{
		return hp_;
	}
}
