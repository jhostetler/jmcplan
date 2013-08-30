/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.io.PrintStream;

import edu.oregonstate.eecs.mcplan.experiments.Parameters;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.ListUtil;


/**
 * @author jhostetler
 *
 */
public class VoyagerParameters extends Parameters
{
	public final int Nplayers = 2;
	
	public final int min_planet_capacity = 4;
	public final int planet_capacity_steps = 4;
	public final int max_neutral_population = 0;
	
	public final int horizon = 512;
	public final int primitive_epoch = 1;
	public final int primitive_horizon = horizon / primitive_epoch;
	
	public final boolean use_monitor = true;
	
	// -----------------------------------------------------------------------
	// Customizable
	
	public static final class Builder
	{
		private long master_seed_ = 43;
		private int Nplanets_ = 10;
		private int Nsites_ = 10;
		private int max_population_ = 100;
		private double ship_speed_ = 1.0;
		private int policy_epoch_ = 1;
		private int max_depth_ = Integer.MAX_VALUE;
		private final int[] max_time_ = new int[Player.competitors];
		private double discount_ = 0.9;
		private int leaf_lookahead_ = Integer.MAX_VALUE;
		private double leaf_lookahead_percent_ = 0.0;
		
		public Builder master_seed( final long seed ) { master_seed_ = seed; return this; }
		public Builder Nplanets( final int n ) { Nplanets_ = n; return this; }
		public Builder Nsites( final int n ) { Nsites_ = n; return this; }
		public Builder max_population( final int n ) { max_population_ = n; return this; }
		public Builder ship_speed( final double s ) { ship_speed_ = s; return this; }
		public Builder policy_epoch( final int i ) { policy_epoch_ = i; return this; }
		public Builder max_depth( final int d ) { max_depth_ = d; return this; }
		public Builder max_time( final int t, final Player p ) { max_time_[p.ordinal()] = t; return this; }
		public Builder max_time( final int[] ts ) { Fn.memcpy( max_time_, ts, ts.length ); return this; }
		public Builder discount( final double d ) { discount_ = d; return this; }
		public Builder leaf_lookahead( final int i ) { leaf_lookahead_ = i; return this; }
		public Builder leaf_lookahead_percent( final double p ) { leaf_lookahead_percent_ = p; return this; }
		
		public VoyagerParameters finish()
		{
			return new VoyagerParameters( master_seed_, Nplanets_, Nsites_, max_population_,
										  ship_speed_, policy_epoch_, max_depth_, max_time_,
										  discount_, leaf_lookahead_, leaf_lookahead_percent_ );
		}
	}
	
	public final long master_seed;
	public final int Nplanets;
	public final int Nsites;
	public final int max_population;
	public final double ship_speed;
	public final int Rmax;
	public final int policy_epoch;
	public final int policy_horizon;
	public final int max_depth;
	public final int[] max_time; // milliseconds
	public final double discount;
	public final int leaf_lookahead;
	public final double leaf_lookahead_percent;
	
	public final int max_eta;
	
	public VoyagerParameters( final long master_seed, final int Nplanets, final int Nsites,
							  final int max_population, final double ship_speed, final int policy_epoch,
							  final int max_depth, final int[] max_time, final double discount,
							  final int leaf_lookahead, final double leaf_lookahead_percent )
	{
		this.master_seed = master_seed;
		this.Nplanets = Nplanets;
		this.Nsites = Nsites;
		this.max_population = max_population;
		this.ship_speed = ship_speed;
		Rmax = Nplanets * 2 * 1000; // This is pretty conservative; could probably be *1000
		this.policy_epoch = policy_epoch;
		this.max_time = max_time;
		this.discount = discount;
		this.leaf_lookahead = leaf_lookahead;
		this.leaf_lookahead_percent = leaf_lookahead_percent;
		policy_horizon = primitive_horizon / policy_epoch;
		this.max_depth = Math.min( max_depth, policy_horizon );
		
		max_eta = (int) Math.ceil( Math.sqrt( 2 * 4*Nsites*Nsites ) / ship_speed );
	}
	
	@Override
	public void writeCsv( final PrintStream out )
	{
		out.print( "Nplayers," ); out.println( Nplayers );
		out.print( "Nplanets," ); out.println( Nplanets );
		out.print( "Nsites," ); out.println( Nsites );
		out.print( "max_population," ); out.println( max_population );
		out.print( "Rmax," ); out.println( Rmax );
		out.print( "master_seed," ); out.println( master_seed );
		out.print( "horizon," ); out.println( horizon );
		out.print( "primitive_epoch," ); out.println( primitive_epoch );
		out.print( "primitive_horizon," ); out.println( primitive_horizon );
		out.print( "policy_epoch," ); out.println( policy_epoch );
		out.print( "policy_horizon," ); out.println( policy_horizon );
		out.print( "max_depth," ); out.println( max_depth );
		out.print( "max_time," ); out.println( max_time );
		out.print( "discount," ); out.println( discount );
		out.print( "leaf_lookahead," ); out.println( leaf_lookahead );
		out.print( "leaf_lookahead_percent," ); out.println( leaf_lookahead_percent );
	}
	
	@Override
	public String toString()
	{
		return ListUtil.join( max_time, "," );
	}

//	@Override
	public int policy_horizon()
	{
		return policy_horizon;
	}

//	@Override
	public int max_time( final Player p )
	{
		return max_time[p.ordinal()];
	}
}
