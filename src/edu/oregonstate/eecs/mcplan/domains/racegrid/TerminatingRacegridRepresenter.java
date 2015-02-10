/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racegrid;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.ArrayFactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public class TerminatingRacegridRepresenter
	implements FactoredRepresenter<RacegridState, FactoredRepresentation<RacegridState>>
{
	private static final ArrayList<Attribute> attributes_;
	
	static {
		attributes_ = new ArrayList<Attribute>();
		attributes_.add( new Attribute( "x" ) );
		attributes_.add( new Attribute( "y" ) );
		attributes_.add( new Attribute( "dx" ) );
		attributes_.add( new Attribute( "dy" ) );
		attributes_.add( new Attribute( "crashed" ) );
		attributes_.add( new Attribute( "goal" ) );
	}

	
	@Override
	public TerminatingRacegridRepresenter create()
	{
		return new TerminatingRacegridRepresenter();
	}

	@Override
	public FactoredRepresentation<RacegridState> encode( final RacegridState s )
	{
		final double[] phi = new double[attributes_.size()];
		int idx = 0;
		phi[idx++] = s.x;
		phi[idx++] = s.y;
		phi[idx++] = s.dx;
		phi[idx++] = s.dy;
		phi[idx++] = s.crashed ? 1 : 0;
		phi[idx++] = s.goal ? 1 : 0;
		return new ArrayFactoredRepresentation<RacegridState>( phi );
	}
	
	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes_;
	}
	
	@Override
	public String toString()
	{
		return "TerminatingRacegridRepresenter";
	}
}
