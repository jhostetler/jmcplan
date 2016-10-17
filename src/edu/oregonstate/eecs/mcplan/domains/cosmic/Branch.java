/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import com.mathworks.toolbox.javabuilder.MWStructArray;


/**
 * @author jhostetler
 *
 */
public final class Branch extends CosmicFacade
{
	public Branch( final int id, final CosmicParameters params, final MWStructArray ps )
	{
		super( "branch", id, params.br_col_names, ps );
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "Branch[" ).append( id() )
		  .append( "; status: " ).append( status() )
		  .append( "; Pf: " ).append( Pf() )
		  .append( "; Qf: " ).append( Qf() )
		  .append( "; Pt: " ).append( Pt() )
		  .append( "; Qt: " ).append( Qt() )
		  .append( "]" );
		return sb.toString();
	}
	
	public int from()
	{
		return getInt( "from" );
	}
	
	public int to()
	{
		return getInt( "to" );
	}
	
	public int status()
	{
		return getInt( "status" );
	}
	
	public double Pf()
	{
		return getDouble( "Pf" );
	}
	
	public double Qf()
	{
		return getDouble( "Qf" );
	}
	
	public double Pt()
	{
		return getDouble( "Pt" );
	}
	
	public double Qt()
	{
		return getDouble( "Qt" );
	}
}
