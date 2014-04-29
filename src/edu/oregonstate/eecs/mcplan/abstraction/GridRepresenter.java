/**
 * 
 */
package edu.oregonstate.eecs.mcplan.abstraction;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.ArrayFactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public class GridRepresenter<S> implements FactoredRepresenter<S, FactoredRepresentation<S>>
{
	private final FactoredRepresenter<S, ? extends FactoredRepresentation<S>> repr_;
	
	public final double[] grid;
	
	public GridRepresenter( final FactoredRepresenter<S, ? extends FactoredRepresentation<S>> repr,
							final double[] grid )
	{
		repr_ = repr;
		this.grid = grid;
	}
	
	@Override
	public FactoredRepresenter<S, FactoredRepresentation<S>> create()
	{
		return new GridRepresenter<S>( repr_.create(), grid );
	}

	@Override
	public FactoredRepresentation<S> encode( final S s )
	{
		final FactoredRepresentation<S> x = repr_.encode( s );
		final double[] xphi = x.phi();
		final double[] phi = new double[xphi.length];
		assert( grid.length == phi.length );
		for( int i = 0; i < phi.length; ++i ) {
			phi[i] = Math.floor( xphi[i] / grid[i] );
		}
		return new ArrayFactoredRepresentation<S>( phi );
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return repr_.attributes();
	}

}
