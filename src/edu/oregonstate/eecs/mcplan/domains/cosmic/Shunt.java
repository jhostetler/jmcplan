/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import org.apache.commons.math3.complex.Complex;

import com.mathworks.toolbox.javabuilder.MWStructArray;

/**
 * This is a wrapper class to give names to the "shunt" fields.
 * <p>
 * Refer to 'psconstants.m' for further documentation.
 * <p>
 * It holds the underlying Matlab array via WeakReference, so it will not keep
 * it alive. Do not store references to Shunt instances.
 * 
 * TODO: Strictly speaking we wouldn't leak Matlab memory anyway since it all
 * gets disposed when the corresponding CosmicState is disposed. So the
 * WeakReference is potentially unnecessary overhead.
 */
public final class Shunt extends CosmicFacade
{
	public Shunt( final int id, final CosmicParameters params, final MWStructArray ps )
	{
		super( "shunt", id, params.sh_col_names, ps );
	}
	
	@Override
	public String toString()
	{
		return "Shunt[" + id() + "; factor: " + factor() + "; P: " + P()
				+ "; current_P: " + current_P() + "; type: " + type() + "]";
	}
	
	public boolean hasLoad()
	{
		return near_gen() != 0;
	}
	
	public double load_freq( final CosmicState s )
	{
		final int near_gen_id = near_gen();
		
		if( near_gen_id != 0 ) {
			return s.x.omega_pu( s.generator( near_gen_id ) );
		}
		else {
			throw new UnsupportedOperationException( "near_gen == 0" );
		}
	}
	
	public int bus()
	{
		return getInt( "bus" );
	}
	
	public double P()
	{
		return getDouble( "P" );
	}
	
	public double Q()
	{
		return getDouble( "Q" );
	}
	
	public double factor()
	{
		return status();
	}
	
	public double status()
	{
		return getDouble( "status" );
	}
	
	public int type()
	{
		return getInt( "type" );
	}
	
	public double value()
	{
		return getDouble( "value" );
	}
	
	public int near_gen()
	{
		return getInt( "near_gen" );
	}
	
	public double current_P()
	{
		return getDouble( "current_P" );
	}
	
	public double current_Q()
	{
		return getDouble( "current_Q" );
	}
	
	/**
	 * Apparent power at 1pu voltage.
	 * @return
	 */
	public Complex _S()
	{
		return new Complex( P(), Q() );
	}
	
	/**
	 * Current apparent power.
	 * @return
	 */
	public Complex _current_S()
	{
		return new Complex( current_P(), current_Q() );
	}
}
