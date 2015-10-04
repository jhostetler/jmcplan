/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.mathworks.toolbox.javabuilder.MWStructArray;

import edu.oregonstate.eecs.mcplan.State;

/**
 * Represents a Cosmic state.
 * <p>
 * CosmicState owns all Matlab objects passed to its constructor.
 */
public class CosmicState implements State, AutoCloseable
{
	public final MWStructArray ps;
	public final int t;
	public final MWNumericArray x;
	public final MWNumericArray y;
	public final MWNumericArray event;
	
	public CosmicState( final MWStructArray ps, final int t,
						final MWNumericArray x, final MWNumericArray y, final MWNumericArray event )
	{
		this.ps = ps;
		this.t = t;
		this.x = x;
		this.y = y;
		this.event = event;
	}
	
	@Override
	public void close()
	{
		ps.dispose();
		x.dispose();
		y.dispose();
		event.dispose();
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "ps:\n" ).append( ps ).append( "\n" );
		sb.append( "t: " ).append( t ).append( "\n" );
		sb.append( "x:\n" ).append( x ).append( "\n" );
		sb.append( "y:\n" ).append( y ).append( "\n" );
		sb.append( "event:\n" ).append( event ).append( "\n" );
		return sb.toString();
	}
	
	@Override
	public boolean isTerminal()
	{
		// TODO Auto-generated method stub
		return false;
	}
}
