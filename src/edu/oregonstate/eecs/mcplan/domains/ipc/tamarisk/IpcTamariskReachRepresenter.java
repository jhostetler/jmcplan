/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.ipc.tamarisk;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.ArrayFactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * Represents counts of Tamarisk and Native for each reach.
 */
public final class IpcTamariskReachRepresenter
	implements FactoredRepresenter<IpcTamariskState, FactoredRepresentation<IpcTamariskState>>
{
	private final IpcTamariskParameters params;
	private final ArrayList<Attribute> attributes = new ArrayList<Attribute>();

	public IpcTamariskReachRepresenter( final IpcTamariskParameters params )
	{
		this.params = params;
		for( int i = 0; i < params.Nreaches; ++i ) {
			final String r = "r" + i;
			attributes.add( new Attribute( r + "t" ) );
			attributes.add( new Attribute( r + "n" ) );
		}
	}
	
	@Override
	public FactoredRepresenter<IpcTamariskState, FactoredRepresentation<IpcTamariskState>> create()
	{
		return new IpcTamariskReachRepresenter( params );
	}

	@Override
	public FactoredRepresentation<IpcTamariskState> encode( final IpcTamariskState s )
	{
		final double[] x = new double[attributes.size()];
		int idx = 0;
		for( int i = 0; i < params.Nreaches; ++i ) {
			final byte[] r = s.reaches[i];
			int t = 0;
			int n = 0;
			for( int j = 0; j < r.length; ++j ) {
				if( (r[j] & IpcTamariskState.Tamarisk) != 0 ) {
					t += 1;
				}
				if( (r[j] & IpcTamariskState.Native) != 0 ) {
					n += 1;
				}
			}
			x[idx++] = t;
			x[idx++] = n;
		}
		assert( idx == x.length );
		return new ArrayFactoredRepresentation<IpcTamariskState>( x );
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes;
	}
}
