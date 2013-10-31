/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

/**
 * @author jhostetler
 *
 */
public class PlanetId
{
	public static final int Main_Min = 0;
	public static final int Main_Max = 1;
	public static final int Natural_2nd_Min = 2;
	public static final int Natural_2nd_Max = 3;
	public static final int Natural_3rd_Min = 4;
	public static final int Natural_3rd_Max = 5;
	public static final int Wing_Min = 6;
	public static final int Wing_Max = 7;
	public static final int Center = 8;
	public static final int N = 9;
	
	public static final int Main( final Player player )
	{ return Main_Min + player.ordinal(); }
	
	public static final int Natural_2nd( final Player player )
	{ return Natural_2nd_Min + player.ordinal(); }
	
	public static final int Natural_3rd( final Player player )
	{ return Natural_3rd_Min + player.ordinal(); }
	
	public static final int Wing( final Player player )
	{ return Wing_Min + player.ordinal(); }
}
