/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

/**
 * 
 */
public class ShedLoadAction extends CosmicAction
{
	public final int[] shunts;
	public final double[] amounts;
	
	public ShedLoadAction( final int[] shunts, final double[] amounts )
	{
		assert( shunts.length == amounts.length );
		this.shunts = shunts;
		this.amounts = amounts;
	}
	
	@Override
	public CosmicAction create()
	{
		return new ShedLoadAction( shunts, amounts );
	}

	@Override
	public MWNumericArray toMatlab( final CosmicParameters params, final double t )
	{
		final MWNumericArray a = MWNumericArray.newInstance(
			new int[] { shunts.length, params.ev_cols }, MWClassID.DOUBLE, MWComplexity.REAL );
		for( int i = 0; i < shunts.length; ++i ) {
			a.set( new int[] { i+1, params.ev_time }, t );
			a.set( new int[] { i+1, params.ev_type }, params.ev_shed_load );
			a.set( new int[] { i+1, params.ev_shunt_loc }, shunts[i] );
			a.set( new int[] { i+1, params.ev_change_by }, CosmicParameters.ev_change_by_percent );
			a.set( new int[] { i+1, params.ev_quantity }, amounts[i] );
		}
//		System.out.println( "ShedLoadAction -> " + a );
		return a;
	}
	
	@Override
	public void applyNonCosmicChanges( final CosmicState sprime )
	{ }

	@Override
	public int hashCode()
	{
		return getClass().hashCode() + 3*(Arrays.hashCode( shunts ) + 5*(Arrays.hashCode( shunts )));
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof ShedLoadAction) ) {
			return false;
		}
		final ShedLoadAction that = (ShedLoadAction) obj;
		return Arrays.equals( shunts, that.shunts ) && Arrays.equals( amounts, that.amounts );
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "ShedLoad(" ).append( "[" ).append( StringUtils.join( shunts, ';' ) ).append( "]" )
		  .append( "; [" ).append( StringUtils.join( amounts, ';' ) ).append( "]" ).append( ")" );
		return sb.toString();
	}

}
