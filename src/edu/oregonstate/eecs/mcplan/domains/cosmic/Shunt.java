/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import java.lang.ref.WeakReference;

import org.apache.commons.math3.complex.Complex;

import com.mathworks.toolbox.javabuilder.MWNumericArray;

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
public final class Shunt
{
	private final int id;
	private final CosmicParameters params;
	private final WeakReference<MWNumericArray> mshunt;
	
	public Shunt( final int id, final CosmicParameters params, final MWNumericArray mshunt )
	{
		this.id = id;
		this.params = params;
		this.mshunt = new WeakReference<>( mshunt );
		
		assert( id == mshunt.getInt( new int[] { id, params.sh_col_names.get( "id" ) } ) );
	}
	
	@Override
	public String toString()
	{
		return "Shunt[" + id + "; factor: " + factor() + "; P: " + P()
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
	
	public int id()
	{
//		return mshunt.get().getInt( new int[] { id, params.sh_col_names.get( "id" ) } );
		return id;
	}
	
	public int bus()
	{
		return mshunt.get().getInt( new int[] { id, params.sh_col_names.get( "bus" ) } );
	}
	
	public double P()
	{
		return mshunt.get().getDouble( new int[] { id, params.sh_col_names.get( "P" ) } );
	}
	
	public double Q()
	{
		return mshunt.get().getDouble( new int[] { id, params.sh_col_names.get( "Q" ) } );
	}
	
	public double factor()
	{
		return status();
	}
	
	public double status()
	{
		return mshunt.get().getDouble( new int[] { id, params.sh_col_names.get( "status" ) } );
	}
	
	public int type()
	{
		return mshunt.get().getInt( new int[] { id, params.sh_col_names.get( "type" ) } );
	}
	
	public double value()
	{
		return mshunt.get().getDouble( new int[] { id, params.sh_col_names.get( "value" ) } );
	}
	
	public int near_gen()
	{
		return mshunt.get().getInt( new int[] { id, params.sh_col_names.get( "near_gen" ) } );
	}
	
	public double current_P()
	{
		return mshunt.get().getDouble( new int[] { id, params.sh_col_names.get( "current_P" ) } );
	}
	
	public double current_Q()
	{
		return mshunt.get().getDouble( new int[] { id, params.sh_col_names.get( "current_Q" ) } );
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
