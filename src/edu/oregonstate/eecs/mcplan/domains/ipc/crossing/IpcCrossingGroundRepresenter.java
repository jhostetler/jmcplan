/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.ipc.crossing;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.ArrayFactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public final class IpcCrossingGroundRepresenter
	implements FactoredRepresenter<IpcCrossingState, FactoredRepresentation<IpcCrossingState>>
{
	private final ArrayList<Attribute> attributes;
	
	public IpcCrossingGroundRepresenter( final IpcCrossingParameters params )
	{
		attributes = new ArrayList<Attribute>();
		attributes.add( new Attribute( "x" ) );
		attributes.add( new Attribute( "y" ) );
		// Top and bottom rows never have obstacles
		for( int y = 1; y < params.height - 1; ++y ) {
			for( int x = 0; x < params.width; ++x ) {
				attributes.add( new Attribute( "x" + x + "y" + y ) );
			}
		}
	}
	
	private IpcCrossingGroundRepresenter( final IpcCrossingGroundRepresenter that )
	{
		this.attributes = that.attributes;
	}

	@Override
	public FactoredRepresenter<IpcCrossingState, FactoredRepresentation<IpcCrossingState>> create()
	{
		return new IpcCrossingGroundRepresenter( this );
	}

	@Override
	public FactoredRepresentation<IpcCrossingState> encode( final IpcCrossingState s )
	{
		final double[] phi = new double[attributes.size()];
		int idx = 0;
		phi[idx++] = s.x;
		phi[idx++] = s.y;
		// Top and bottom rows never have obstacles
		for( int y = 1; y < s.params.height - 1; ++y ) {
			for( int x = 0; x < s.params.width; ++x ) {
				phi[idx++] = (s.grid[y][x] ? 1 : 0);
			}
		}
		assert( idx == phi.length );
		return new ArrayFactoredRepresentation<IpcCrossingState>( phi );
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes;
	}
}
